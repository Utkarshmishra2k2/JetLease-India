/* ============================================================
   JETLEASE INDIA — Customer Dashboard
   ============================================================ */

let dUser = null;
let dRoutes = null;

document.addEventListener('jl:ready', async () => {
  dUser = requireAuth('customer');
  if(!dUser) return;
  mountNav('dashboard');
  mountFooter();
  dRoutes = (await tryFetchJSON('data/routes.json')) || SEED_ROUTES;

  document.getElementById('dashGreeting').textContent = 'Welcome back, ' + dUser.fullName.split(' ')[0];
  document.getElementById('profileMenuEmail').textContent = dUser.email;
  renderKPIs();

  document.querySelectorAll('#dashTabs button').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('#dashTabs button').forEach(x => x.classList.remove('active'));
      btn.classList.add('active');
      renderPanel(btn.dataset.tab);
    });
  });

  document.addEventListener('click', (e) => {
    const menu = document.getElementById('profileMenu');
    const btn = document.getElementById('profileBtn');
    if(menu.style.display === 'flex' && !menu.contains(e.target) && e.target !== btn && !btn.contains(e.target)){
      menu.style.display = 'none';
    }
  });

  const hash = window.location.hash.replace('#','');
  const startTab = hash && document.querySelector(`[data-tab="${hash}"]`) ? hash : 'overview';
  document.querySelectorAll('#dashTabs button').forEach(x => x.classList.toggle('active', x.dataset.tab === startTab));
  renderPanel(startTab);
});

function toggleProfileMenu(){
  const menu = document.getElementById('profileMenu');
  menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
}

/* ---------------- Shared modal (used by forgot password + report an issue) ---------------- */
function openDashModal(html){ document.getElementById('dashModalBody').innerHTML = html; document.getElementById('dashModal').style.display = 'flex'; }
function closeDashModal(){ document.getElementById('dashModal').style.display = 'none'; }

function myBookings(){ return DB.bookings.all().filter(b => b.userEmail === dUser.email); }
function myPayments(){ return DB.payments.all().filter(p => p.userEmail === dUser.email); }
function myLeases(){ return DB.leases.all().filter(l => l.userEmail === dUser.email); }
function myNotifications(){ return DB.notifications.all().filter(n => n.userEmail === dUser.email); }
function myReports(){ return DB.reports.all().filter(r => r.userEmail === dUser.email); }

function renderKPIs(){
  const bookings = myBookings();
  const upcoming = bookings.filter(b => isUpcomingBooking(b.status)).length;
  const payments = myPayments();
  const leases = myLeases();
  const activeLeases = leases.filter(l => ['Signed','Approved'].includes(l.status)).length;

  const kpis = [
    ['Total Bookings', bookings.length],
    ['Upcoming Flights', upcoming],
    ['Payments Made', payments.length],
    ['Active Agreements', activeLeases],
  ];
  document.getElementById('kpiGrid').innerHTML = kpis.map(([l,v]) => `<div class="kpi-card"><span>${l}</span><b>${v}</b></div>`).join('');
}

function renderPanel(tab){
  const el = document.getElementById('dashPanels');
  if(tab === 'overview') return renderOverview(el);
  if(tab === 'profile') return renderProfile(el);
  if(tab === 'bookings') return renderBookings(el);
  if(tab === 'payments') return renderPayments(el);
  if(tab === 'notifications') return renderNotifications(el);
  if(tab === 'leases') return renderLeasesPanel(el);
  if(tab === 'reports') return renderReportsPanel(el);
}

/* ==================================================================
   OVERVIEW — Featured Services, Popular Routes, Aircraft Gallery,
   Recent Bookings, Upcoming Flights, Quick Actions, Statistics
   ================================================================== */
function renderOverview(el){
  const bookings = myBookings().sort((a,b) => new Date(b.createdAt) - new Date(a.createdAt));
  const recent = bookings.slice(0,3);
  const upcoming = bookings.filter(b => isUpcomingBooking(b.status)).slice(0,3);
  const fleet = DB.aircraft.all().slice(0,4);
  const routes = (dRoutes?.domestic || []).slice(0,6);
  const totalSpent = myPayments().filter(p=>p.status==='VERIFIED').reduce((s,p)=>s+p.amount,0);
  const completedFlights = bookings.filter(b=>b.status==='Completed').length;

  el.innerHTML = `
    <div class="panel">
      <div class="panel-head" style="justify-content:center;"><h4 style="margin:0;">Quick Actions</h4></div>
      <div style="display:flex;gap:12px;flex-wrap:wrap;justify-content:center;">
        <a href="booking.html" class="btn btn-primary btn-sm">Book a Flight</a>
        <button class="btn btn-ghost btn-sm" onclick="switchDashTab('bookings')">View My Bookings</button>
        <button class="btn btn-ghost btn-sm" onclick="switchDashTab('payments')">View Payments</button>
        <button class="btn btn-ghost btn-sm" onclick="switchDashTab('reports')">Report an Issue</button>
      </div>
    </div>

    <div class="stats-grid" style="margin-bottom:22px;">
      <div class="stat-cell"><b>${bookings.length}</b><span>Total Bookings</span></div>
      <div class="stat-cell"><b>${completedFlights}</b><span>Flights Completed</span></div>
      <div class="stat-cell"><b>${fmtINR(totalSpent)}</b><span>Total Paid (Verified)</span></div>
      <div class="stat-cell"><b>${dUser.country||'—'}</b><span>Home Country</span></div>
    </div>

    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Featured Services</h4></div>
      <div class="fleet-grid" style="grid-template-columns:repeat(3,1fr);">
        <div class="aircraft-card"><div class="aircraft-media"><img src="assets/images/placeholder-icon.svg" alt="" style="width:40px;height:40px;opacity:.6;"></div>
          <div class="aircraft-body"><h3 style="font-size:16px;">Charter Booking</h3><p style="margin:6px 0 0;font-size:13px;">Domestic jet &amp; helicopter charters with live fleet availability.</p>
          <a href="booking.html" class="btn btn-ghost btn-sm btn-block" style="margin-top:12px;">Book Now</a></div></div>
        <div class="aircraft-card"><div class="aircraft-media"><img src="assets/images/placeholder-icon.svg" alt="" style="width:40px;height:40px;opacity:.6;"></div>
          <div class="aircraft-body"><h3 style="font-size:16px;">Self-Fly Program</h3><p style="margin:6px 0 0;font-size:13px;">Fly it yourself with a verified license and 100+ logged hours.</p>
          <a href="booking.html" class="btn btn-ghost btn-sm btn-block" style="margin-top:12px;">Learn More</a></div></div>
        <div class="aircraft-card"><div class="aircraft-media"><img src="assets/images/placeholder-icon.svg" alt="" style="width:40px;height:40px;opacity:.6;"></div>
          <div class="aircraft-body"><h3 style="font-size:16px;">Lease Desk</h3><p style="margin:6px 0 0;font-size:13px;">Review, digitally sign, and download your charter lease agreements.</p>
          <button class="btn btn-ghost btn-sm btn-block" style="margin-top:12px;" onclick="switchDashTab('leases')">Open Lease Desk</button></div></div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Popular Routes</h4></div>
      <div class="tag-row">
        ${routes.map(r => `<span class="tag" style="cursor:pointer;" onclick="window.location.href='booking.html?origin=BOM&destination=${r.code}'">Mumbai → ${r.city}</span>`).join('')}
      </div>
    </div>

    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Aircraft Gallery</h4></div>
      <div class="fleet-grid" style="grid-template-columns:repeat(4,1fr);">
        ${fleet.map(a => `
          <div class="aircraft-card">
            <div class="aircraft-media"><img src="${a.image||'assets/images/placeholder-aircraft.svg'}" alt="${a.model}" style="width:56px;height:56px;opacity:.75;"></div>
            <div class="aircraft-body"><h3 style="font-size:14px;margin-bottom:0;">${a.model}</h3><p style="margin:4px 0 0;font-size:12px;">${a.category}</p></div>
          </div>`).join('')}
      </div>
    </div>

    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Recent Bookings</h4></div>
      ${recent.length ? recent.map(b => `
        <div style="padding:12px 0;border-bottom:1px solid var(--border);display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;">
          <div><b style="font-size:14px;">${b.aircraftModel}</b><div class="hint">${b.origin} → ${b.destination} · ${b.date}</div></div>
          <span class="badge-status ${statusClass(b.status)}">${b.status}</span>
        </div>`).join('') : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No bookings yet.</div>`}
    </div>

    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Upcoming Flights</h4></div>
      ${upcoming.length ? upcoming.map(b => `
        <div style="padding:12px 0;border-bottom:1px solid var(--border);display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;">
          <div><b style="font-size:14px;">${b.origin} → ${b.destination}</b><div class="hint">${b.date} ${b.time||''}</div></div>
          <span class="badge-status ${statusClass(b.status)}">${b.status}</span>
        </div>`).join('') : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No upcoming flights.</div>`}
    </div>`;
}

function switchDashTab(tab){
  document.querySelectorAll('#dashTabs button').forEach(x => x.classList.toggle('active', x.dataset.tab === tab));
  renderPanel(tab);
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

/* ==================================================================
   PROFILE — readonly email, DOB 18+, phone-change OTP, forgot password
   ================================================================== */
let pendingPhoneValue = null; // holds a phone edit awaiting OTP confirmation

function renderProfile(el){
  el.innerHTML = `
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">My Profile</h4></div>
      <form id="profileForm" class="form-grid">
        <div class="field" id="f-pFullName"><label>Full Name</label><input type="text" id="pFullName" value="${dUser.fullName}"><span class="error-text">Only letters are allowed.</span></div>
        <div class="field"><label>Email <span class="hint" style="display:inline;">(cannot be changed)</span></label><input type="email" value="${dUser.email}" disabled readonly></div>
        <div class="field" id="f-pPhone">
          <label>Phone</label>
          <input type="tel" id="pPhone" value="${dUser.phone}">
          <div class="hint" id="phoneOtpHint" style="display:none;">Phone number changed — you'll need to verify it with an OTP before saving.</div>
        </div>
        <div class="field" id="f-pDob"><label>Date of Birth</label><input type="date" id="pDob" value="${dUser.dob}" max="${todayISO()}"><span class="error-text">You must be 18 years or older.</span></div>
        <div class="field"><label>Country</label><input type="text" id="pCountry" value="${dUser.country||''}"></div>
        <div class="field" id="f-pEmergency"><label>Emergency Contact</label><input type="tel" id="pEmergency" value="${dUser.emergencyContact||''}"><span class="error-text">Enter a valid 10-digit phone number.</span></div>
        <div class="full" style="display:flex;gap:12px;margin-top:6px;flex-wrap:wrap;">
          <button type="submit" class="btn btn-primary">Save Changes</button>
          <button type="button" class="btn btn-ghost" onclick="openForgotPasswordModal()">Forgot Password</button>
        </div>
      </form>
    </div>`;

  document.getElementById('pPhone').addEventListener('input', () => {
    const changed = document.getElementById('pPhone').value.trim() !== dUser.phone;
    document.getElementById('phoneOtpHint').style.display = changed ? 'block' : 'none';
  });

  document.getElementById('profileForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const fullName = document.getElementById('pFullName').value;
    const phone = document.getElementById('pPhone').value;
    const dob = document.getElementById('pDob').value;
    const emergency = document.getElementById('pEmergency').value;

    let ok = true;
    const nameErr = VALIDATORS.name(fullName);
    setFieldError('f-pFullName', !nameErr, nameErr); if(nameErr) ok = false;
    const phoneErr = VALIDATORS.phone10(phone);
    setFieldError('f-pPhone', !phoneErr, phoneErr); if(phoneErr) ok = false;
    const emgErr = VALIDATORS.phone10(emergency);
    setFieldError('f-pEmergency', !emgErr, emgErr); if(emgErr) ok = false;
    const dobOk = dob && isAdult(dob);
    setFieldError('f-pDob', dobOk, 'You must be 18 years or older.'); if(!dobOk) ok = false;
    if(!ok){ toast('Please fix the highlighted fields.', 'error'); return; }

    const phoneChanged = phone !== dUser.phone;
    if(phoneChanged){
      pendingPhoneValue = phone;
      openPhoneOtpModal({ fullName, dob, emergency });
      return;
    }
    saveProfile({ fullName, phone, dob, emergency });
  });
}

function saveProfile({ fullName, phone, dob, emergency }){
  const users = DB.users.all();
  const idx = users.findIndex(u => u.email === dUser.email);
  users[idx].fullName = fullName;
  users[idx].phone = phone;
  users[idx].dob = dob;
  users[idx].country = document.getElementById('pCountry')?.value ?? users[idx].country;
  users[idx].emergencyContact = emergency;
  DB.users.save(users);
  dUser = users[idx];
  addAudit(dUser.email, 'Admin', 'Profile Updated', '');
  toast('Profile updated.', 'success');
  document.getElementById('dashGreeting').textContent = 'Welcome back, ' + dUser.fullName.split(' ')[0];
}

// Phone number changes require OTP verification before saving (Requirement 15).
function openPhoneOtpModal(pendingFields){
  openDashModal(`
    <h3>Verify Your New Phone Number</h3>
    <p>We've sent a mock OTP to <b>${pendingPhoneValue}</b>. Enter <b>123456</b> to confirm (demo mode).</p>
    <div class="field" style="margin-bottom:14px;"><label>OTP Code</label><input type="text" id="phoneOtpInput" maxlength="6" placeholder="123456"></div>
    <div style="display:flex;gap:10px;">
      <button class="btn btn-ghost btn-block" onclick="closeDashModal()">Cancel</button>
      <button class="btn btn-primary btn-block" onclick='confirmPhoneOtp(${JSON.stringify(pendingFields)})'>Verify &amp; Save</button>
    </div>`);
}
function confirmPhoneOtp(pendingFields){
  const code = document.getElementById('phoneOtpInput').value;
  if(!verifyMockOTP(code)){ toast('Incorrect OTP. Use 123456 for this demo.', 'error'); return; }
  saveProfile({ ...pendingFields, phone: pendingPhoneValue });
  addAudit(dUser.email, 'Admin', 'Phone Number Verified & Updated', pendingPhoneValue);
  pendingPhoneValue = null;
  closeDashModal();
  renderProfile(document.getElementById('dashPanels'));
}

/* ---------------- Forgot Password (Requirement 7) ---------------- */
function openForgotPasswordModal(){
  openDashModal(`
    <h3>Reset Your Password</h3>
    <p>Send a mock OTP to your registered email to reset your password.</p>
    <button class="btn btn-primary btn-block" onclick="sendForgotPasswordOtp()">Send OTP</button>`);
}
function sendForgotPasswordOtp(){
  toast('Mock OTP sent to ' + dUser.email + ' (use 123456).', 'success');
  openDashModal(`
    <h3>Enter OTP</h3>
    <p>Enter the 6-digit code sent to <b>${dUser.email}</b>.</p>
    <div class="field" style="margin-bottom:14px;"><label>OTP Code</label><input type="text" id="fpOtp" maxlength="6" placeholder="123456"></div>
    <div style="display:flex;gap:10px;">
      <button class="btn btn-ghost btn-block" onclick="closeDashModal()">Cancel</button>
      <button class="btn btn-primary btn-block" onclick="verifyForgotPasswordOtp()">Verify</button>
    </div>`);
}
function verifyForgotPasswordOtp(){
  const code = document.getElementById('fpOtp').value;
  if(!verifyMockOTP(code)){ toast('Incorrect OTP. Use 123456 for this demo.', 'error'); return; }
  openDashModal(`
    <h3>Set a New Password</h3>
    <div class="field" style="margin-bottom:14px;">
      <label>New Password</label>
      <div class="otp-row"><input type="password" id="fpNewPassword"><button type="button" class="btn btn-ghost btn-sm" onclick="togglePasswordVisibility('fpNewPassword', this)">Show</button></div>
      <div class="strength-meter"><span id="fpStrengthBar"></span></div>
    </div>
    <div class="field" style="margin-bottom:14px;"><label>Confirm New Password</label><input type="password" id="fpConfirmPassword"></div>
    <div style="display:flex;gap:10px;">
      <button class="btn btn-ghost btn-block" onclick="closeDashModal()">Cancel</button>
      <button class="btn btn-primary btn-block" onclick="saveNewPassword()">Save Password</button>
    </div>`);
  document.getElementById('fpNewPassword').addEventListener('input', (e) => {
    const s = passwordStrength(e.target.value);
    const bar = document.getElementById('fpStrengthBar');
    bar.style.width = s.pct + '%';
    bar.style.background = s.score <= 1 ? 'var(--red)' : s.score <= 3 ? 'var(--amber)' : 'var(--green)';
  });
}
function saveNewPassword(){
  const pw = document.getElementById('fpNewPassword').value;
  const confirm = document.getElementById('fpConfirmPassword').value;
  if(passwordStrength(pw).score < 3){ toast('Choose a stronger password.', 'error'); return; }
  if(pw !== confirm){ toast('Passwords do not match.', 'error'); return; }
  const users = DB.users.all();
  const idx = users.findIndex(u => u.email === dUser.email);
  users[idx].password = pw;
  DB.users.save(users);
  addAudit(dUser.email, 'Login', 'Password Reset via Dashboard', '');
  toast('Password updated successfully.', 'success');
  closeDashModal();
}

/* ==================================================================
   BOOKINGS — includes self-service cancellation (Requirement 16)
   ================================================================== */
function renderBookings(el){
  const bookings = myBookings().sort((a,b) => new Date(b.createdAt) - new Date(a.createdAt));
  el.innerHTML = `<div class="panel"><div class="panel-head"><h4 style="margin:0;">Booking History</h4></div>
    ${bookings.length ? `<div class="table-wrap"><table>
      <thead><tr><th>Booking</th><th>Aircraft</th><th>Route</th><th>Date</th><th>Total</th><th>Status</th><th></th></tr></thead>
      <tbody>${bookings.map(b => `
        <tr>
          <td style="font-family:var(--mono);">${b.id}</td>
          <td>${b.aircraftModel}</td>
          <td>${b.origin} → ${b.destination}</td>
          <td>${b.date}</td>
          <td>${convert(b.cost.total)}</td>
          <td><span class="badge-status ${statusClass(b.status)}">${b.status}</span></td>
          <td style="white-space:nowrap;">
            ${isPayableBooking(b.status) ? `<a href="payment.html?booking=${b.id}" class="btn btn-primary btn-sm">${b.status === 'Payment Rejected' ? 'Retry Payment' : 'Pay'}</a>` : ''}
            ${isCancellableBooking(b.status) ? `<button class="btn btn-danger btn-sm" onclick="cancelBooking('${b.id}')">Cancel</button>` : ''}
          </td>
        </tr>`).join('')}</tbody>
    </table></div>` : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No bookings yet. <a href="booking.html" style="color:var(--accent);">Book your first flight</a>.</div>`}
  </div>`;
}

// Requirement 16: customers can cancel after payment clears but before the lease is
// signed. A 20% cancellation charge applies; 80% of the paid amount is refunded.
function cancelBooking(bookingId){
  const bookings = DB.bookings.all();
  const idx = bookings.findIndex(b => b.id === bookingId);
  const booking = bookings[idx];
  if(!booking || !isCancellableBooking(booking.status)){
    toast('This booking can no longer be cancelled online.', 'error');
    return;
  }
  const payments = DB.payments.all();
  const pidx = payments.findIndex(p => p.bookingId === bookingId && p.status === 'VERIFIED');
  const total = pidx > -1 ? payments[pidx].amount : booking.cost.total;
  const fee = Math.round(total * 0.20);
  const refund = total - fee;

  openDashModal(`
    <h3>Cancel Booking ${bookingId}?</h3>
    <p>This booking is eligible for cancellation. Our refund policy applies:</p>
    <div class="cost-line"><span>Amount Paid</span><span>${fmtINR(total)}</span></div>
    <div class="cost-line"><span>Cancellation Charge (20%)</span><span>-${fmtINR(fee)}</span></div>
    <div class="cost-line total"><span>Refund Amount (80%)</span><span>${fmtINR(refund)}</span></div>
    <div style="display:flex;gap:10px;margin-top:18px;">
      <button class="btn btn-ghost btn-block" onclick="closeDashModal()">Keep Booking</button>
      <button class="btn btn-danger btn-block" onclick="confirmCancelBooking('${bookingId}')">Confirm Cancellation</button>
    </div>`);
}
function confirmCancelBooking(bookingId){
  const bookings = DB.bookings.all();
  const idx = bookings.findIndex(b => b.id === bookingId);
  const booking = bookings[idx];
  if(!isCancellableBooking(booking.status)){ toast('This booking can no longer be cancelled.', 'error'); closeDashModal(); return; }

  const payments = DB.payments.all();
  const pidx = payments.findIndex(p => p.bookingId === bookingId && p.status === 'VERIFIED');
  let refund = 0, fee = 0;
  if(pidx > -1){
    const total = payments[pidx].amount;
    fee = Math.round(total * 0.20);
    refund = total - fee;
    payments[pidx].status = 'RETURNED';
    payments[pidx].cancellationFee = fee;
    payments[pidx].refundAmount = refund;
    DB.payments.save(payments);
  }

  releaseBookingResources(booking); // frees aircraft + refunds any committed crew/pilot hours
  bookings[idx].status = 'Cancelled';
  bookings[idx].assignedPilotId = null;
  bookings[idx].assignedCrewIds = [];
  DB.bookings.save(bookings);

  addAudit(dUser.email, 'Booking', 'Booking Cancelled by Customer', `${bookingId} · refund ${fmtINR(refund)} · fee ${fmtINR(fee)}`);
  addNotification(dUser.email, 'Booking Cancelled', `Booking ${bookingId} cancelled. ${fmtINR(refund)} will be refunded (${fmtINR(fee)} cancellation charge retained).`, 'info');
  addNotification('admin', 'Booking Cancelled by Customer', `${dUser.fullName} cancelled booking ${bookingId}.`, 'warning');

  closeDashModal();
  toast('Booking cancelled — refund is being processed.', 'success');
  renderBookings(document.getElementById('dashPanels'));
  renderKPIs();
}

/* ---------------- PAYMENTS ---------------- */
function renderPayments(el){
  const payments = myPayments().sort((a,b) => new Date(b.submittedAt) - new Date(a.submittedAt));
  el.innerHTML = `<div class="panel"><div class="panel-head"><h4 style="margin:0;">Payments</h4></div>
    ${payments.length ? `<div class="table-wrap"><table>
      <thead><tr><th>Payment</th><th>Booking</th><th>Amount</th><th>Transaction ID</th><th>Submitted</th><th>Status</th></tr></thead>
      <tbody>${payments.map(p => `
        <tr><td style="font-family:var(--mono);">${p.id}</td><td>${p.bookingId}</td><td>${convert(p.amount)}</td>
        <td style="font-family:var(--mono);">${p.transactionId}</td><td>${fmtDate(p.submittedAt)}</td>
        <td><span class="badge-status ${statusClass(p.status)}">${p.status.replace('_',' ')}</span></td></tr>`).join('')}</tbody>
    </table></div>` : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No payments submitted yet.</div>`}
  </div>`;
}

/* ---------------- NOTIFICATIONS ---------------- */
function renderNotifications(el){
  const notes = myNotifications();
  el.innerHTML = `<div class="panel"><div class="panel-head"><h4 style="margin:0;">Notifications</h4>${notes.length ? `<button class="btn btn-ghost btn-sm" onclick="markAllRead()">Mark all read</button>`:''}</div>
    ${notes.length ? notes.map(n => `
      <div style="padding:14px 0;border-bottom:1px solid var(--border);opacity:${n.read?0.55:1};">
        <div style="display:flex;justify-content:space-between;"><b style="font-size:14px;">${n.title}</b><span class="hint">${fmtDate(n.createdAt)}</span></div>
        <p style="margin:4px 0 0;font-size:13.5px;">${n.message}</p>
      </div>`).join('') : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">You're all caught up.</div>`}
  </div>`;
}
function markAllRead(){
  const all = DB.notifications.all();
  all.forEach(n => { if(n.userEmail === dUser.email) n.read = true; });
  DB.notifications.save(all);
  renderNotifications(document.getElementById('dashPanels'));
  toast('Notifications marked as read.', 'success');
}

/* ---------------- LEASES ---------------- */
function renderLeasesPanel(el){
  const leases = myLeases();
  const bookings = DB.bookings.all();
  el.innerHTML = `<div class="panel"><div class="panel-head"><h4 style="margin:0;">Lease Agreements</h4><a href="lease.html" class="btn btn-ghost btn-sm">Open Lease Desk</a></div>
    ${leases.length ? leases.map(l => {
      const b = bookings.find(bk => bk.id === l.bookingId);
      return `<div style="padding:14px 0;border-bottom:1px solid var(--border);display:flex;justify-content:space-between;align-items:center;">
        <div><b style="font-size:14px;">${l.id}</b><div class="hint">${b ? b.aircraftModel : ''}</div></div>
        <span class="badge-status ${statusClass(l.status)}">${l.status}</span>
      </div>`;
    }).join('') : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No lease agreements yet.</div>`}
  </div>`;
}

/* ==================================================================
   REPORT AN ISSUE — only for dispatched/completed flights (Requirement 17)
   ================================================================== */
function renderReportsPanel(el){
  const bookings = myBookings().filter(b => ['Dispatched','Completed'].includes(b.status));
  const reports = myReports().sort((a,b) => new Date(b.createdAt) - new Date(a.createdAt));

  el.innerHTML = `
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Report an Issue</h4></div>
      ${bookings.length ? `
      <p class="hint">Reporting is available once a flight has dispatched. Our operations team reviews every report.</p>
      <form id="reportForm" class="form-grid">
        <div class="field full"><label>Booking</label>
          <select id="reportBooking">${bookings.map(b => `<option value="${b.id}">${b.id} — ${b.aircraftModel} (${b.origin}→${b.destination})</option>`).join('')}</select>
        </div>
        <div class="field full" id="f-reportSubject"><label>Subject</label><input type="text" id="reportSubject"><span class="error-text">Subject is required.</span></div>
        <div class="field full" id="f-reportDetails">
          <label>Details</label>
          <textarea id="reportDetails" rows="4" style="width:100%;padding:11px 12px;border-radius:8px;border:1px solid var(--border);background:var(--surface-2);color:var(--text);"></textarea>
          <span class="error-text">Please provide at least 10 characters of detail.</span>
        </div>
        <button type="submit" class="btn btn-primary full" style="margin-top:6px;">Submit Report</button>
      </form>` : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">Reporting unlocks once one of your flights has dispatched.</div>`}
    </div>
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">My Reports</h4></div>
      ${reports.length ? reports.map(r => `
        <div style="padding:12px 0;border-bottom:1px solid var(--border);">
          <div style="display:flex;justify-content:space-between;"><b style="font-size:14px;">${r.subject}</b><span class="badge-status ${statusClass(r.status)}">${r.status}</span></div>
          <p style="margin:4px 0 0;font-size:13px;">${r.details}</p>
          <span class="hint">${r.bookingId} · ${fmtDate(r.createdAt)}</span>
        </div>`).join('') : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No reports submitted yet.</div>`}
    </div>`;

  const form = document.getElementById('reportForm');
  if(form){
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const bookingId = document.getElementById('reportBooking').value;
      const subject = document.getElementById('reportSubject').value;
      const details = document.getElementById('reportDetails').value;

      let ok = true;
      const subjErr = subject.trim().length >= 3 ? '' : 'Subject is required.';
      setFieldError('f-reportSubject', !subjErr, subjErr); if(subjErr) ok = false;
      const detErr = VALIDATORS.message(details);
      setFieldError('f-reportDetails', !detErr, detErr); if(detErr) ok = false;
      if(!ok){ toast('Please fix the highlighted fields.', 'error'); return; }

      const reports = DB.reports.all();
      reports.unshift({
        id: uid('RPT'), bookingId, userEmail: dUser.email, subject, details,
        status: 'Open', createdAt: nowISO()
      });
      DB.reports.save(reports);
      addAudit(dUser.email, 'Admin', 'Issue Reported', `${bookingId} · ${subject}`);
      addNotification('admin', 'New Flight Report', `${dUser.fullName} reported an issue on ${bookingId}: "${subject}"`, 'warning');
      toast('Report submitted — our team will follow up.', 'success');
      renderReportsPanel(document.getElementById('dashPanels'));
    });
  }
}
