/* ============================================================
   JETLEASE INDIA — Authentication
   ============================================================ */

document.addEventListener('jl:ready', () => {
  mountNav();
  mountFooter();
  if(document.getElementById('detailsForm')) initRegisterFlow();
  if(document.getElementById('loginForm')) initLoginFlow();
  if(document.getElementById('adminLoginForm')) initAdminLoginFlow();
});

/* ---------------- REGISTER ---------------- */
function initRegisterFlow(){
  // Future dates are disabled (and rendered faded/unclickable by the browser's native
  // date picker) for date of birth — a person cannot be born in the future.
  document.getElementById('dob').max = todayISO();

  const pwEl = document.getElementById('password');
  pwEl.addEventListener('input', () => {
    const s = passwordStrength(pwEl.value);
    const bar = document.getElementById('strengthBar');
    bar.style.width = s.pct + '%';
    bar.style.background = s.score <= 1 ? 'var(--red)' : s.score <= 3 ? 'var(--amber)' : 'var(--green)';
    document.getElementById('strengthLabel').textContent = 'Strength: ' + s.label;
  });

  let pendingUser = null;

  document.getElementById('detailsForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const fullName = document.getElementById('fullName').value.trim();
    const email = document.getElementById('email').value.trim().toLowerCase();
    const phone = document.getElementById('phone').value.trim();
    const dob = document.getElementById('dob').value;
    const emergencyContact = document.getElementById('emergencyContact').value.trim();
    const password = pwEl.value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    let valid = true;
    const nameErr = VALIDATORS.name(fullName);
    setFieldError('f-fullName', !nameErr, nameErr); if(nameErr) valid = false;
    const emailErr = VALIDATORS.email(email);
    setFieldError('f-email', !emailErr, emailErr); if(emailErr) valid = false;
    const phoneErr = VALIDATORS.phone10(phone);
    setFieldError('f-phone', !phoneErr, phoneErr); if(phoneErr) valid = false;
    const dobOk = dob && isAdult(dob);
    setFieldError('f-dob', dobOk, 'You must be 18 or older.'); if(!dobOk) valid = false;
    const emgErr = VALIDATORS.phone10(emergencyContact);
    setFieldError('f-emergency', !emgErr, emgErr); if(emgErr) valid = false;
    const strong = passwordStrength(password).score >= 3;
    setFieldError('f-password', strong, 'Password is too weak — add uppercase, numbers &amp; symbols.'); if(!strong) valid = false;
    const matchOk = password === confirmPassword && confirmPassword.length > 0;
    setFieldError('f-confirm', matchOk, 'Passwords do not match.'); if(!matchOk) valid = false;

    if(DB.users.all().some(u => u.email === email)){
      setFieldError('f-email', false, 'An account with this email already exists.');
      valid = false;
    }

    if(!valid){ toast('Please fix the highlighted fields.', 'error'); return; }

    pendingUser = { fullName, email, phone, dob, emergencyContact, password };
    document.getElementById('detailsForm').style.display = 'none';
    document.getElementById('otpForm').style.display = 'block';
    document.getElementById('stepLabel').textContent = '2';
    document.getElementById('ps2').classList.add('done');
    document.getElementById('formTitle').textContent = 'Verify your contact details';
    toast('Mock OTPs sent to email & phone. Use 123456.', 'success');
  });

  document.getElementById('resendEmail').addEventListener('click', () => toast('Email OTP resent.', 'success'));
  document.getElementById('resendPhone').addEventListener('click', () => toast('Phone OTP resent.', 'success'));

  document.getElementById('otpForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const emailOtp = document.getElementById('emailOtp').value;
    const phoneOtp = document.getElementById('phoneOtp').value;
    const emailOk = emailOtp === '123456';
    const phoneOk = phoneOtp === '123456';
    document.getElementById('emailOtpErr').style.display = emailOk ? 'none' : 'block';
    document.getElementById('phoneOtpErr').style.display = phoneOk ? 'none' : 'block';
    if(!emailOk || !phoneOk){ toast('Incorrect OTP. Use 123456 for this demo.', 'error'); return; }

    const users = DB.users.all();
    const newUser = {
      id: uid('CUS'),
      ...pendingUser,
      role: 'customer',
      status: 'active',
      membership: 'none',
      loyaltyPoints: 0,
      createdAt: nowISO()
    };
    users.push(newUser);
    DB.users.save(users);
    addAudit(newUser.email, 'Login', 'Account Registered', 'Self-registered via public form');
    addNotification(newUser.email, 'Welcome to JetLease', 'Your account has been created. Explore the fleet and book your first flight.', 'success');
    setSession(newUser.email, 'customer');

    document.getElementById('otpForm').style.display = 'none';
    document.getElementById('doneStep').style.display = 'block';
    document.getElementById('stepLabel').textContent = '3';
    document.getElementById('ps3').classList.add('done');
    document.getElementById('formTitle').textContent = 'You\'re all set';
  });
}

/* ---------------- LOGIN (Customer) ---------------- */
function initLoginFlow(){
  const tabs = document.querySelectorAll('#loginTabs .search-tab');
  let mode = 'email';
  tabs.forEach(t => t.addEventListener('click', () => {
    tabs.forEach(x => x.classList.remove('active'));
    t.classList.add('active');
    mode = t.dataset.mode;
    document.getElementById('emailLoginFields').style.display = mode === 'email' ? 'block' : 'none';
    document.getElementById('phoneLoginFields').style.display = mode === 'phone' ? 'block' : 'none';
    document.getElementById('otpLoginFields').style.display = mode === 'otp' ? 'block' : 'none';
  }));

  document.getElementById('sendLoginOtp').addEventListener('click', () => {
    const id = document.getElementById('otpIdentifier').value.trim();
    if(!id){ toast('Enter your registered email or phone first.', 'error'); return; }
    toast('OTP sent (demo code: 123456).', 'success');
  });

  document.getElementById('loginForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const users = DB.users.all();
    let user = null;

    if(mode === 'email'){
      const email = document.getElementById('loginEmail').value.trim().toLowerCase();
      const pw = document.getElementById('loginPassword').value;
      user = users.find(u => u.email === email && u.password === pw && u.role === 'customer');
    } else if(mode === 'phone'){
      const phone = document.getElementById('loginPhone').value.trim();
      const pw = document.getElementById('loginPhonePassword').value;
      user = users.find(u => u.phone === phone && u.password === pw && u.role === 'customer');
    } else {
      const id = document.getElementById('otpIdentifier').value.trim().toLowerCase();
      const otp = document.getElementById('loginOtp').value.trim();
      if(otp !== '123456'){ toast('Incorrect OTP. Use 123456 for this demo.', 'error'); return; }
      user = users.find(u => (u.email === id || u.phone === id) && u.role === 'customer');
    }

    if(!user){ toast('Invalid credentials. Try demo@jetlease.in / Demo@123', 'error'); return; }
    if(user.status === 'suspended'){ toast('This account has been suspended. Contact support.', 'error'); return; }

    const remember = document.getElementById('rememberMe')?.checked;
    setSession(user.email, 'customer');
    addAudit(user.email, 'Login', 'Customer Login', remember ? 'Remember me enabled' : '');
    toast('Welcome back, ' + user.fullName.split(' ')[0] + '.', 'success');
    setTimeout(() => window.location.href = 'dashboard.html', 500);
  });
}

/* ---------------- ADMIN LOGIN ---------------- */
function initAdminLoginFlow(){
  document.getElementById('adminLoginForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const email = document.getElementById('adminEmail').value.trim().toLowerCase();
    const pw = document.getElementById('adminPassword').value;
    const user = DB.users.all().find(u => u.email === email && u.password === pw && u.role === 'admin');
    if(!user){ toast('Invalid admin credentials.', 'error'); return; }
    setSession(user.email, 'admin');
    addAudit(user.email, 'Login', 'Admin Login', '');
    toast('Welcome back, Admin.', 'success');
    setTimeout(() => window.location.href = 'admin.html', 500);
  });
}

/* ---------------- FORGOT PASSWORD (mock) ---------------- */
function requestPasswordReset(){
  const email = document.getElementById('forgotEmail').value.trim().toLowerCase();
  const user = DB.users.all().find(u => u.email === email);
  if(!user){ toast('No account found with that email.', 'error'); return; }
  toast('Password reset link sent (demo). Check your inbox.', 'success');
  document.getElementById('forgotModal').style.display = 'none';
}
