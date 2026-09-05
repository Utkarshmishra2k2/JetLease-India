/* ============================================================
   JETLEASE INDIA — Shared App Shell
   ============================================================ */

(function initTheme(){
  const saved = localStorage.getItem('jl_theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
})();

function toggleTheme(){
  const cur = document.documentElement.getAttribute('data-theme');
  const next = cur === 'light' ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('jl_theme', next);
}

function toast(message, type){
  let wrap = document.querySelector('.toast-wrap');
  if(!wrap){ wrap = document.createElement('div'); wrap.className='toast-wrap'; document.body.appendChild(wrap); }
  const t = document.createElement('div');
  t.className = 'toast' + (type ? ' ' + type : '');
  t.textContent = message;
  wrap.appendChild(t);
  setTimeout(()=>{ t.style.opacity='0'; t.style.transition='opacity .3s'; setTimeout(()=>t.remove(),300); }, 3400);
}

function fmtINR(n){ return '₹' + Number(n).toLocaleString('en-IN'); }

// Shared form-field error helper: toggles the .invalid/.valid CSS classes already used
// throughout the app, and optionally updates the field's .error-text message.
function setFieldError(fieldId, isValid, msg){
  const el = document.getElementById(fieldId);
  if(!el) return;
  el.classList.toggle('invalid', !isValid);
  el.classList.toggle('valid', isValid);
  if(msg){ const e = el.querySelector('.error-text'); if(e) e.textContent = msg; }
}
function fmtDate(iso){ try{ return new Date(iso).toLocaleString('en-IN', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }); }catch(e){ return iso; } }

// Show/Hide password toggle (Requirement 1) — flips the input's type and the button label.
function togglePasswordVisibility(inputId, btn){
  const el = document.getElementById(inputId);
  if(!el) return;
  const showing = el.type === 'text';
  el.type = showing ? 'password' : 'text';
  if(btn) btn.textContent = showing ? 'Show' : 'Hide';
}

function requireAuth(role){
  const s = getSession();
  if(!s || (role && s.role !== role)){
    window.location.href = role === 'admin' ? 'admin-login.html' : 'login.html';
    return null;
  }
  return currentUser();
}

function logout(){
  addAudit(getSession()?.userEmail || 'unknown', 'Login', 'Logout', 'User logged out');
  clearSession();
  window.location.href = 'index.html';
}

function mountNav(activeSlug){
  const session = getSession();
  const user = session ? currentUser() : null;
  const navEl = document.getElementById('siteNav');
  if(!navEl) return;

  // On the customer Dashboard page, all profile/logout actions live in the Dashboard Top
  // Bar instead — the shared nav shows no Dashboard link and no Logout button there.
  const isDashboardPage = activeSlug === 'dashboard';

  let rightSide;
  if(isDashboardPage){
    rightSide = '';
  } else if(user){
    // Logged in: a profile icon replaces the Log In / Join buttons. Clicking it opens a
    // small dropdown with the user's email and a Log Out option.
    rightSide = `
      <div style="position:relative;">
        <button class="theme-toggle" id="navProfileBtn" onclick="toggleNavProfileMenu()" aria-label="Account menu">
          <img src="assets/images/placeholder-icon.svg" alt="" style="width:18px;height:18px;">
        </button>
        <div id="navProfileMenu" class="card profile-menu" style="display:none;">
          <p class="pm-email">${user.email}</p>
          <button class="btn btn-primary btn-block btn-sm" onclick="logout()">Log Out</button>
        </div>
      </div>`;
  } else {
    rightSide = `<a href="login.html" class="btn btn-ghost btn-sm">Log In</a>
       <a href="register.html" class="btn btn-primary btn-sm">Join JetLease</a>`;
  }

  // Logo destination: dashboard if logged in (role-appropriate), home page otherwise.
  const logoHref = user ? (user.role==='admin' ? 'admin.html' : 'dashboard.html') : 'index.html';

  navEl.innerHTML = `
    <div class="nav-inner">
      <a href="${logoHref}" class="brand"><span class="mark">JL</span><span>JETLEASE<small>PRIVATE AVIATION · INDIA</small></span></a>
      <nav class="nav-links" id="navLinksList"></nav>
      <div class="nav-cta">
        <button class="theme-toggle" id="themeToggleBtn" onclick="toggleTheme()" aria-label="Toggle theme"><img src="assets/images/placeholder-icon.svg" alt="" style="width:18px;height:18px;"></button>
        ${rightSide}
      </div>
    </div>`;
}

function toggleNavProfileMenu(){
  const menu = document.getElementById('navProfileMenu');
  if(!menu) return;
  menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
}
// Close the nav profile dropdown when clicking anywhere outside it.
document.addEventListener('click', (e) => {
  const menu = document.getElementById('navProfileMenu');
  const btn = document.getElementById('navProfileBtn');
  if(!menu || !btn) return;
  if(menu.style.display === 'flex' && !menu.contains(e.target) && !btn.contains(e.target)){
    menu.style.display = 'none';
  }
});

function mountFooter(){
  const el = document.getElementById('siteFooter');
  if(!el) return;
  el.innerHTML = `
    <div class="container">
      <div class="footer-grid">
        <div>
          <a href="index.html" class="brand"><span class="mark">JL</span><span>JETLEASE<small>PRIVATE AVIATION · INDIA</small></span></a>
          <p style="margin-top:14px;max-width:300px;">Premium private jet and helicopter charter and aircraft lease management — built for India's boardrooms and beyond.</p>
        </div>
        <div>
          <h4>Explore</h4>
          <ul>
            <li><a href="index.html#fleet">Fleet</a></li>
            <li><a href="booking.html">Book a Flight</a></li>
            <li><a href="index.html#faq">FAQ</a></li>
          </ul>
        </div>
        <div>
          <h4>Account</h4>
          <ul>
            <li><a href="login.html">Log In</a></li>
            <li><a href="register.html">Register</a></li>
            <li><a href="dashboard.html">Customer Dashboard</a></li>
            <li><a href="admin-login.html">Admin Portal</a></li>
          </ul>
        </div>
        <div>
          <h4>Contact</h4>
          <ul>
            <li>ops@jetlease.in</li>
            <li>+91 22 6789 0000</li>
            <li>BKC, Mumbai, India</li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© ${new Date().getFullYear()} JetLease India. Frontend demo — all payments &amp; verifications are mocked.</span>
        <span>DGCA NSOP Ref. MOCK-0000 · CIN MOCK00000MH0000</span>
      </div>
    </div>`;
}

function altitudeDivider(){
  return `<div class="altitude-divider"><svg viewBox="0 0 1200 34" preserveAspectRatio="none"><polyline points="0,26 150,26 210,8 330,8 380,20 520,20 580,4 760,4 820,18 1000,18 1060,10 1200,10" fill="none" stroke="var(--border)" stroke-width="1.5"/></svg></div>`;
}

document.addEventListener('DOMContentLoaded', function(){
  seedDatabase().then(()=>{
    document.dispatchEvent(new CustomEvent('jl:ready'));
  });
});
