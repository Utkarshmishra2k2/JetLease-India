/* ============================================================
   JETLEASE INDIA — Admin Console
   ============================================================ */

let aUser = null;

document.addEventListener('jl:ready', () => {
  aUser = requireAuth('admin');
  if(!aUser) return;
  mountNav();
  mountFooter();
  document.getElementById('adminGreeting').textContent = 'Operations Console';
  renderAdminKPIs();

  document.querySelectorAll('#adminSide a').forEach(a => {
    a.addEventListener('click', (e) => {
      e.preventDefault();
      document.querySelectorAll('#adminSide a').forEach(x => x.classList.remove('active'));
      a.classList.add('active');
      renderAdminPanel(a.dataset.tab);
    });
  });
  renderAdminPanel('overview');
});

function closeModal(){ document.getElementById('adminModal').style.display = 'none'; }
function openModal(html){ document.getElementById('adminModalBody').innerHTML = html; document.getElementById('adminModal').style.display = 'flex'; }

function renderAdminKPIs(){
  const bookings = DB.bookings.all();
  const payments = DB.payments.all();
  const aircraft = DB.aircraft.all();
  const customers = DB.users.all().filter(u => u.role === 'customer');
  const pilots = DB.pilots.all();
  const crew = DB.crew.all();
  const leases = DB.leases.all();

  const revenue = payments.filter(p => p.status === 'VERIFIED').reduce((s,p) => s + p.amount, 0);
  const pendingPayments = payments.filter(p => p.status === 'PENDING_VERIFICATION').length;
  const activeLeases = leases.filter(l => ['Signed','Approved'].includes(l.status)).length;

  const kpis = [
    ['Total Bookings', bookings.length], ['Revenue', fmtINR(revenue)],
    ['Pending Payments', pendingPayments], ['Aircraft Count', aircraft.length],
    ['Customer Count', customers.length], ['Pilot Count', pilots.length],
    ['Crew Count', crew.length], ['Active Leases', activeLeases],
  ];
  document.getElementById('adminKpiGrid').innerHTML = kpis.map(([l,v]) => `<div class="kpi-card"><span>${l}</span><b>${v}</b></div>`).join('');
}

function renderAdminPanel(tab){
  renderAdminKPIs();
  const el = document.getElementById('adminPanels');
  if(tab==='overview') return renderOverview(el);
  if(tab==='aircraft') return renderAircraftMgmt(el);
  if(tab==='bookings') return renderBookingMgmt(el);
  if(tab==='payments') return renderPaymentMgmt(el);
  if(tab==='leases') return renderLeaseMgmt(el);
  if(tab==='customers') return renderCustomerMgmt(el);
  if(tab==='crew') return renderCrewMgmt(el);
  if(tab==='routes') return renderRoutesMgmt(el);
  if(tab==='inbox') return renderInbox(el);
  if(tab==='exports') return renderExports(el);
  if(tab==='audit') return renderAuditLog(el);
}

/* ================= OVERVIEW / ANALYTICS ================= */
function bar(label, value, max){
  const pct = max ? Math.round((value/max)*100) : 0;
  return `<div style="margin-bottom:12px;">
    <div style="display:flex;justify-content:space-between;font-size:12.5px;margin-bottom:5px;"><span>${label}</span><span style="color:var(--text-dim);">${value}</span></div>
    <div style="height:8px;background:var(--surface-2);border-radius:5px;overflow:hidden;"><div style="width:${pct}%;height:100%;background:var(--accent);"></div></div>
  </div>`;
}

function renderOverview(el){
  const bookings = DB.bookings.all();
  const payments = DB.payments.all();
  const aircraft = DB.aircraft.all();

  const approved = bookings.filter(b => ['Approved','Dispatched','Completed'].includes(b.status)).length;
  const cancelled = bookings.filter(b => ['Cancelled','Rejected'].includes(b.status)).length;

  const byAircraft = {};
  bookings.forEach(b => { byAircraft[b.aircraftModel] = (byAircraft[b.aircraftModel]||0) + 1; });
  const popular = Object.entries(byAircraft).sort((a,b) => b[1]-a[1]).slice(0,5);
  const maxPop = popular.length ? popular[0][1] : 1;

  const statusCounts = {};
  aircraft.forEach(a => statusCounts[a.status] = (statusCounts[a.status]||0)+1);

  const revenueVerified = payments.filter(p=>p.status==='VERIFIED').reduce((s,p)=>s+p.amount,0);
  const revenuePending = payments.filter(p=>p.status==='PENDING_VERIFICATION').reduce((s,p)=>s+p.amount,0);
  const revenueReturned = payments.filter(p=>p.status==='RETURNED').reduce((s,p)=>s+p.amount,0);

  el.innerHTML = `
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Booking Analytics</h4></div>
      ${bar('Total Bookings', bookings.length, Math.max(bookings.length,1))}
      ${bar('Approved / Dispatched / Completed', approved, Math.max(bookings.length,1))}
      ${bar('Cancelled / Rejected', cancelled, Math.max(bookings.length,1))}
    </div>
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Fleet Utilization — Most Popular Aircraft</h4></div>
      ${popular.length ? popular.map(([m,c]) => bar(m, c, maxPop)).join('') : '<p class="hint">No bookings yet.</p>'}
    </div>
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Aircraft Status Breakdown</h4></div>
      <div class="tag-row">${Object.entries(statusCounts).map(([s,c]) => `<span class="tag">${s}: ${c}</span>`).join('')}</div>
    </div>
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Revenue Summary</h4></div>
      <div class="cost-line"><span>Verified Revenue</span><span>${fmtINR(revenueVerified)}</span></div>
      <div class="cost-line"><span>Pending Verification</span><span>${fmtINR(revenuePending)}</span></div>
      <div class="cost-line"><span>Returned (Lease Rejected)</span><span>${fmtINR(revenueReturned)}</span></div>
      <div class="cost-line total"><span>Total Payment Volume</span><span>${fmtINR(revenueVerified+revenuePending)}</span></div>
    </div>`;
}

/* ================= AIRCRAFT MANAGEMENT ================= */
function renderAircraftMgmt(el){
  const fleet = DB.aircraft.all();
  el.innerHTML = `<div class="panel">
    <div class="panel-head"><h4 style="margin:0;">Aircraft Management</h4><button class="btn btn-primary btn-sm" onclick="openAircraftForm()">+ Add Aircraft</button></div>
    <div class="table-wrap"><table>
      <thead><tr><th>ID</th><th>Model</th><th>Category</th><th>Reg</th><th>Rate/hr</th><th>Status</th><th></th></tr></thead>
      <tbody>${fleet.map(a => `
        <tr><td style="font-family:var(--mono);">${a.id}</td><td>${a.model}</td><td>${a.category}</td><td>${a.reg}</td><td>${fmtINR(a.hourlyRate)}</td>
        <td><span class="badge-status ${statusClass(a.status)}">${a.status}</span></td>
        <td style="white-space:nowrap;">
          <button class="btn btn-ghost btn-sm" onclick="openAircraftForm('${a.id}')">Edit</button>
          ${a.status==='Grounded' ? `<button class="btn btn-ghost btn-sm" onclick="setAircraftStatus('${a.id}','Available')">Unground</button>`
            : `<button class="btn btn-ghost btn-sm" onclick="setAircraftStatus('${a.id}','Grounded')">Ground</button>`}
          <button class="btn btn-danger btn-sm" onclick="deleteAircraft('${a.id}')">Delete</button>
        </td></tr>`).join('')}</tbody>
    </table></div>
  </div>`;
}

function openAircraftForm(id){
  const fleet = DB.aircraft.all();
  const a = id ? fleet.find(x => x.id===id) : null;
  openModal(`
    <h3>${a ? 'Edit Aircraft' : 'Add Aircraft'}</h3>
    <form id="aircraftForm" class="form-grid">
      <div class="field"><label>Model</label><input id="afModel" value="${a?.model||''}" required></div>
      <div class="field"><label>Manufacturer</label><input id="afManufacturer" value="${a?.manufacturer||''}" required></div>
      <div class="field"><label>Registration No.</label><input id="afReg" value="${a?.reg||''}" required></div>
      <div class="field"><label>Category</label>
        <select id="afCategory">${['Light Jet','Mid Jet','Heavy Jet','Helicopter','Turboprop','Air Ambulance'].map(c=>`<option ${a?.category===c?'selected':''}>${c}</option>`).join('')}</select>
      </div>
      <div class="field"><label>Capacity (seats)</label><input type="number" id="afCapacity" value="${a?.capacity||6}" required></div>
      <div class="field"><label>Speed (km/h)</label><input type="number" id="afSpeed" value="${a?.speed||600}" required></div>
      <div class="field"><label>Range (km)</label><input type="number" id="afRange" value="${a?.range||3000}" required></div>
      <div class="field"><label>Hourly Rate (₹)</label><input type="number" id="afRate" value="${a?.hourlyRate||150000}" required></div>
      <div class="field"><label>Type Rating</label><input id="afTypeRating" value="${a?.typeRating||''}"></div>
      <div class="field"><label>Status</label>
        <select id="afStatus">${['Available','Booked','Dispatched','Maintenance','Grounded','Retired'].map(s=>`<option ${a?.status===s?'selected':''}>${s}</option>`).join('')}</select>
      </div>
    </form>
    <div style="display:flex;gap:10px;margin-top:18px;">
      <button class="btn btn-ghost btn-block" onclick="closeModal()">Cancel</button>
      <button class="btn btn-primary btn-block" onclick="saveAircraft('${id||''}')">Save Aircraft</button>
    </div>`);
}

function saveAircraft(id){
  const fleet = DB.aircraft.all();
  const data = {
    model: document.getElementById('afModel').value, manufacturer: document.getElementById('afManufacturer').value,
    reg: document.getElementById('afReg').value, category: document.getElementById('afCategory').value,
    capacity: Number(document.getElementById('afCapacity').value), speed: Number(document.getElementById('afSpeed').value),
    range: Number(document.getElementById('afRange').value), hourlyRate: Number(document.getElementById('afRate').value),
    typeRating: document.getElementById('afTypeRating').value, status: document.getElementById('afStatus').value,
  };
  if(!data.model || !data.reg){ toast('Model and registration are required.', 'error'); return; }
  if(id){
    const idx = fleet.findIndex(a => a.id===id);
    fleet[idx] = { ...fleet[idx], ...data };
    addAudit(aUser.email, 'Admin', 'Aircraft Updated', id);
  } else {
    fleet.push({ id: uid('AC'), ...data, image: 'assets/images/placeholder-aircraft.svg' });
    addAudit(aUser.email, 'Admin', 'Aircraft Added', data.model);
  }
  DB.aircraft.save(fleet);
  closeModal();
  toast('Aircraft saved.', 'success');
  renderAdminPanel('aircraft');
}

function setAircraftStatus(id, status){
  const fleet = DB.aircraft.all();
  const idx = fleet.findIndex(a => a.id===id);
  fleet[idx].status = status;
  DB.aircraft.save(fleet);
  addAudit(aUser.email, 'Admin', 'Aircraft Status Changed', `${id} → ${status}`);
  toast(`Aircraft ${status.toLowerCase()}.`, 'success');
  renderAdminPanel('aircraft');
}

function deleteAircraft(id){
  if(!confirm('Delete this aircraft permanently?')) return;
  DB.aircraft.save(DB.aircraft.all().filter(a => a.id!==id));
  addAudit(aUser.email, 'Admin', 'Aircraft Deleted', id);
  toast('Aircraft removed.', 'success');
  renderAdminPanel('aircraft');
}

/* ================= BOOKING MANAGEMENT ================= */
function renderBookingMgmt(el){
  const bookings = DB.bookings.all().sort((a,b) => new Date(b.createdAt)-new Date(a.createdAt));
  el.innerHTML = `<div class="panel">
    <div class="panel-head"><h4 style="margin:0;">Booking Management</h4></div>
    <div class="table-wrap"><table>
      <thead><tr><th>Booking</th><th>Customer</th><th>Aircraft</th><th>Route</th><th>Status</th><th></th></tr></thead>
      <tbody>${bookings.map(b => `
        <tr><td style="font-family:var(--mono);">${b.id}</td><td>${b.userEmail}</td><td>${b.aircraftModel}</td><td>${b.origin}→${b.destination}</td>
        <td><span class="badge-status ${statusClass(b.status)}">${b.status}</span></td>
        <td style="white-space:nowrap;">
          <button class="btn btn-ghost btn-sm" onclick="viewBooking('${b.id}')">View</button>
          ${bookingActionButtons(b)}
        </td></tr>`).join('')}</tbody>
    </table></div>
  </div>`;
}

function bookingActionButtons(b){
  let html = '';
  if(isActiveBooking(b.status)) html += `<button class="btn btn-ghost btn-sm" onclick="openAssignCrewModal('${b.id}')">Assign Crew</button>`;
  if(b.status === 'Lease Signed') html += `<button class="btn btn-primary btn-sm" onclick="advanceBooking('${b.id}','Approved')">Approve</button>`;
  if(b.status === 'Approved') html += `<button class="btn btn-primary btn-sm" onclick="advanceBooking('${b.id}','Dispatched')">Dispatch</button>`;
  if(b.status === 'Dispatched') html += `<button class="btn btn-primary btn-sm" onclick="advanceBooking('${b.id}','Completed')">Mark Completed</button>`;
  if(!['Completed','Cancelled','Rejected'].includes(b.status)) html += `<button class="btn btn-danger btn-sm" onclick="advanceBooking('${b.id}','Rejected')">Reject</button>`;
  return html;
}

function advanceBooking(id, status){
  const bookings = DB.bookings.all();
  const idx = bookings.findIndex(b => b.id===id);
  bookings[idx].status = status;

  if(status === 'Rejected' || status === 'Cancelled'){
    releaseBookingResources(bookings[idx]); // frees the aircraft + refunds any pilot/crew hours
    bookings[idx].assignedPilotId = null;
    bookings[idx].assignedCrewIds = [];
  }
  if(status === 'Completed'){
    const users = DB.users.all();
    const ui = users.findIndex(u => u.email === bookings[idx].userEmail);
    if(ui>-1){ users[ui].loyaltyPoints += Math.round(bookings[idx].cost.total/10000); DB.users.save(users); }
  }
  DB.bookings.save(bookings);

  addAudit(aUser.email, 'Booking', `Booking ${status}`, id);
  addNotification(bookings[idx].userEmail, `Booking ${status}`, `Your booking ${id} is now ${status}.`, status==='Rejected'?'error':'success');
  toast(`Booking marked ${status}.`, 'success');
  renderAdminPanel('bookings');
}

function viewBooking(id){
  const b = DB.bookings.all().find(x => x.id===id);
  const pilots = DB.pilots.all(); const crew = DB.crew.all();
  const pilotName = b.assignedPilotId ? (pilots.find(p=>p.id===b.assignedPilotId)?.name || b.assignedPilotId) : (b.selfFly ? 'Self-Fly (no pilot required)' : 'Not yet assigned');
  const crewNames = (b.assignedCrewIds||[]).map(cid => crew.find(c=>c.id===cid)?.name || cid).join(', ') || 'Not yet assigned';
  openModal(`
    <h3>Booking ${b.id}</h3>
    <div class="cost-line"><span>Customer</span><span>${b.userEmail}</span></div>
    <div class="cost-line"><span>Aircraft</span><span>${b.aircraftModel}</span></div>
    <div class="cost-line"><span>Route</span><span>${b.origin} → ${b.destination}</span></div>
    <div class="cost-line"><span>Date</span><span>${b.date} ${b.time||''}${b.returnDate?' / '+b.returnDate+' '+(b.returnTime||''):''}</span></div>
    <div class="cost-line"><span>Type</span><span>${b.type} ${b.selfFly?'(Self-Fly)':''}</span></div>
    <div class="cost-line"><span>Passengers</span><span>${b.passengers.length}</span></div>
    <div class="cost-line"><span>Pilot</span><span>${pilotName}</span></div>
    <div class="cost-line"><span>Crew</span><span>${crewNames}</span></div>
    <div class="cost-line total"><span>Total</span><span>${convert(b.cost.total)}</span></div>
    <h4 style="margin-top:16px;">Passengers</h4>
    ${b.passengers.map(p => `<div class="cost-line"><span>${p.name}</span><span class="badge-status ${statusClass(p.verificationStatus)}">${p.verificationStatus}</span></div>`).join('')}
    <button class="btn btn-ghost btn-block" style="margin-top:16px;" onclick="closeModal()">Close</button>`);
}

/* ================= CREW / PILOT ASSIGNMENT (per booking) ================= */

function openAssignCrewModal(bookingId){
  const booking = DB.bookings.all().find(b => b.id === bookingId);
  if(!booking || !isActiveBooking(booking.status)){ toast('Crew can only be assigned to active bookings.', 'error'); return; }
  const hours = booking.cost.hours;
  const pilots = DB.pilots.all();
  const crew = DB.crew.all();

  const pilotOptions = booking.selfFly ? '' : pilots.map(p => {
    const isCurrent = booking.assignedPilotId === p.id;
    // Effective hours = what they'd have available if this booking's current
    // commitment (if any) were given back — matches what saveCrewAssignment validates.
    const effectiveHours = p.remainingHours + (isCurrent ? hours : 0);
    const enough = effectiveHours >= hours;
    const disabled = !p.available || (!enough && !isCurrent);
    return `<label style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid var(--border);${disabled?'opacity:.45;':''}">
      <span><input type="radio" name="assignPilot" value="${p.id}" ${isCurrent?'checked':''} ${disabled?'disabled':''}> ${p.name} — ${effectiveHours}h available</span>
      <span class="hint">${p.available ? (enough || isCurrent ? '' : 'Insufficient hours') : 'Unavailable'}</span>
    </label>`;
  }).join('');

  const crewOptions = crew.map(c => {
    const isCurrent = (booking.assignedCrewIds||[]).includes(c.id);
    const effectiveHours = c.remainingHours + (isCurrent ? hours : 0);
    const enough = effectiveHours >= hours;
    const disabled = !c.available || (!enough && !isCurrent);
    return `<label style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid var(--border);${disabled?'opacity:.45;':''}">
      <span><input type="checkbox" name="assignCrew" value="${c.id}" ${isCurrent?'checked':''} ${disabled?'disabled':''}> ${c.name} — ${c.role} (${effectiveHours}h available)</span>
      <span class="hint">${c.available ? (enough || isCurrent ? '' : 'Insufficient hours') : 'Unavailable'}</span>
    </label>`;
  }).join('');

  openModal(`
    <h3>Assign Crew — ${booking.id}</h3>
    <p class="hint">This flight requires an estimated ${hours} block hour(s). Only active bookings can have crew assigned; staff below their remaining-hours threshold are disabled.</p>
    ${booking.selfFly ? `<p style="margin-top:10px;color:var(--amber);">This is a Self-Fly booking — no company pilot is required.</p>` : `
    <h4 style="margin-top:16px;">Pilot in Command</h4>
    <div>${pilotOptions || '<p class="hint">No pilots on file.</p>'}</div>`}
    <h4 style="margin-top:16px;">Cabin Crew</h4>
    <div>${crewOptions || '<p class="hint">No crew on file.</p>'}</div>
    <div style="display:flex;gap:10px;margin-top:18px;">
      <button class="btn btn-ghost btn-block" onclick="closeModal()">Cancel</button>
      <button class="btn btn-primary btn-block" onclick="saveCrewAssignment('${booking.id}')">Save Assignment</button>
    </div>`);
}

function saveCrewAssignment(bookingId){
  const bookings = DB.bookings.all();
  const bidx = bookings.findIndex(b => b.id === bookingId);
  const booking = bookings[bidx];
  if(!isActiveBooking(booking.status)){ toast('This booking is no longer active.', 'error'); closeModal(); return; }

  const pilotRadio = document.querySelector('input[name="assignPilot"]:checked');
  const newPilotId = booking.selfFly ? null : (pilotRadio ? pilotRadio.value : null);
  const newCrewIds = Array.from(document.querySelectorAll('input[name="assignCrew"]:checked')).map(el => el.value);
  const hours = booking.cost.hours;

  // Work out what remainingHours WOULD be after refunding this booking's previous
  // assignment (if any) — entirely in memory, nothing written yet. This lets us validate
  // the whole new assignment up front instead of deducting-then-discovering-a-problem,
  // which previously left staff hours permanently corrupted on a failed save.
  let pilots = DB.pilots.all().map(p => ({ ...p }));
  let crew = DB.crew.all().map(c => ({ ...c }));
  if(booking.assignedPilotId){
    const pi = pilots.findIndex(p => p.id === booking.assignedPilotId);
    if(pi > -1) pilots[pi].remainingHours += hours;
  }
  (booking.assignedCrewIds||[]).forEach(cid => {
    const ci = crew.findIndex(c => c.id === cid);
    if(ci > -1) crew[ci].remainingHours += hours;
  });

  // Validate the ENTIRE new selection against those post-refund numbers before committing
  // anything — if any one part is invalid, nothing is saved and no hours are touched.
  if(newPilotId){
    const pi = pilots.findIndex(p => p.id === newPilotId);
    if(pi > -1 && pilots[pi].remainingHours < hours){
      toast(`${pilots[pi].name} does not have enough remaining hours.`, 'error');
      return;
    }
  }
  for(const cid of newCrewIds){
    const ci = crew.findIndex(c => c.id === cid);
    if(ci > -1 && crew[ci].remainingHours < hours){
      toast(`${crew[ci].name} does not have enough remaining hours.`, 'error');
      return;
    }
  }

  // Everything checks out — now actually deduct and commit, all at once.
  if(newPilotId){
    const pi = pilots.findIndex(p => p.id === newPilotId);
    if(pi > -1) pilots[pi].remainingHours -= hours;
  }
  newCrewIds.forEach(cid => {
    const ci = crew.findIndex(c => c.id === cid);
    if(ci > -1) crew[ci].remainingHours -= hours;
  });
  DB.pilots.save(pilots);
  DB.crew.save(crew);

  booking.assignedPilotId = newPilotId;
  booking.assignedCrewIds = newCrewIds;
  DB.bookings.save(bookings);

  addAudit(aUser.email, 'Admin', 'Crew Assigned', `${bookingId} · pilot:${newPilotId||'none (self-fly)'} · crew:${newCrewIds.join(',')||'none'}`);
  addNotification(booking.userEmail, 'Flight Crew Assigned', `Crew has been assigned to your booking ${bookingId}.`, 'info');
  toast('Crew assignment saved.', 'success');
  closeModal();
  renderAdminPanel('bookings');
}

/* ================= PAYMENT MANAGEMENT ================= */
function renderPaymentMgmt(el){
  const payments = DB.payments.all().sort((a,b) => new Date(b.submittedAt)-new Date(a.submittedAt));
  el.innerHTML = `<div class="panel">
    <div class="panel-head"><h4 style="margin:0;">Payment Management</h4></div>
    <div class="table-wrap"><table>
      <thead><tr><th>Payment</th><th>Booking</th><th>Customer</th><th>Amount</th><th>Txn ID</th><th>Status</th><th></th></tr></thead>
      <tbody>${payments.map(p => `
        <tr><td style="font-family:var(--mono);">${p.id}</td><td>${p.bookingId}</td><td>${p.userEmail}</td><td>${convert(p.amount)}</td>
        <td style="font-family:var(--mono);">${p.transactionId}</td>
        <td><span class="badge-status ${statusClass(p.status)}">${p.status.replace('_',' ')}</span></td>
        <td style="white-space:nowrap;">
          ${p.status==='PENDING_VERIFICATION' ? `
            <button class="btn btn-primary btn-sm" onclick="verifyPayment('${p.id}')">Verify</button>
            <button class="btn btn-danger btn-sm" onclick="rejectPayment('${p.id}')">Reject</button>` : ''}
        </td></tr>`).join('')}</tbody>
    </table></div>
  </div>`;
}

async function verifyPayment(id){
  const payments = DB.payments.all();
  const idx = payments.findIndex(p => p.id===id);
  const payment = payments[idx];

  toast('Checking bank ledger…', 'success');
  const result = await mockVerifyPaymentAPI(payment);
  if(!result.verified){
    const proceedAnyway = confirm(`Mock bank ledger check: ${result.message}\n\nVerify this payment anyway?`);
    if(!proceedAnyway) return;
  }

  payments[idx].status = 'VERIFIED';
  DB.payments.save(payments);

  const bookings = DB.bookings.all();
  const bidx = bookings.findIndex(b => b.id===payments[idx].bookingId);
  bookings[bidx].status = 'Lease Pending';
  DB.bookings.save(bookings);

  ensureLeaseForBooking(bookings[bidx]);

  addAudit(aUser.email, 'Payment', 'Payment Verified', `${id} · bank ledger: ${result.verified ? 'matched' : 'manual override'}`);
  addNotification(payments[idx].userEmail, 'Payment Verified', `Payment ${id} verified. Your lease agreement is ready.`, 'success');
  toast('Payment verified — lease agreement generated.', 'success');
  renderAdminPanel('payments');
}

function rejectPayment(id){
  const payments = DB.payments.all();
  const idx = payments.findIndex(p => p.id===id);
  payments[idx].status = 'REJECTED';
  DB.payments.save(payments);

  const bookings = DB.bookings.all();
  const bidx = bookings.findIndex(b => b.id===payments[idx].bookingId);
  bookings[bidx].status = 'Payment Rejected';
  DB.bookings.save(bookings);

  addAudit(aUser.email, 'Payment', 'Payment Rejected', id);
  addNotification(payments[idx].userEmail, 'Payment Rejected', `Payment ${id} was rejected. Please resubmit a valid transaction ID.`, 'error');
  toast('Payment rejected.', 'success');
  renderAdminPanel('payments');
}

/* ================= LEASE MANAGEMENT ================= */
function renderLeaseMgmt(el){
  const leases = DB.leases.all().sort((a,b) => new Date(b.createdAt)-new Date(a.createdAt));
  el.innerHTML = `<div class="panel">
    <div class="panel-head"><h4 style="margin:0;">Lease Management</h4></div>
    <div class="table-wrap"><table>
      <thead><tr><th>Lease</th><th>Booking</th><th>Customer</th><th>Signed By</th><th>Status</th><th></th></tr></thead>
      <tbody>${leases.map(l => `
        <tr><td style="font-family:var(--mono);">${l.id}</td><td>${l.bookingId}</td><td>${l.userEmail}</td><td>${l.signedBy||'—'}</td>
        <td><span class="badge-status ${statusClass(l.status)}">${l.status}</span></td>
        <td style="white-space:nowrap;">
          ${l.status==='Signed' ? `
            <button class="btn btn-primary btn-sm" onclick="approveLease('${l.id}')">Approve</button>
            <button class="btn btn-danger btn-sm" onclick="rejectLease('${l.id}')">Reject</button>` : ''}
        </td></tr>`).join('')}</tbody>
    </table></div>
  </div>`;
}

function approveLease(id){
  const leases = DB.leases.all();
  const idx = leases.findIndex(l => l.id===id);
  leases[idx].status = 'Approved';
  leases[idx].approvalDate = nowISO();
  DB.leases.save(leases);

  const bookings = DB.bookings.all();
  const bidx = bookings.findIndex(b => b.id===leases[idx].bookingId);
  bookings[bidx].status = 'Approved';
  DB.bookings.save(bookings);

  addAudit(aUser.email, 'Lease', 'Lease Approved', id);
  addNotification(leases[idx].userEmail, 'Lease Approved', `Lease ${id} has been approved. Your booking is confirmed for dispatch.`, 'success');
  toast('Lease approved.', 'success');
  renderAdminPanel('leases');
}
function rejectLease(id){
  const leases = DB.leases.all();
  const idx = leases.findIndex(l => l.id===id);
  leases[idx].status = 'Rejected';
  DB.leases.save(leases);
  const bookings = DB.bookings.all();
  const bidx = bookings.findIndex(b => b.id===leases[idx].bookingId);
  bookings[bidx].status = 'Rejected';
  DB.bookings.save(bookings);

  // The customer already paid and was verified — since the lease (and therefore the
  // booking) is now rejected, that payment is refunded: mark it RETURNED so it no
  // longer counts toward admin revenue, and reflect that in Payment Management.
  const payments = DB.payments.all();
  const pidx = payments.findIndex(p => p.bookingId === leases[idx].bookingId && p.status === 'VERIFIED');
  if(pidx > -1){
    payments[pidx].status = 'RETURNED';
    DB.payments.save(payments);
    addNotification(leases[idx].userEmail, 'Payment Returned', `Payment ${payments[pidx].id} has been returned following lease rejection for booking ${leases[idx].bookingId}.`, 'info');
  }
  releaseBookingResources(bookings[bidx]); // frees the aircraft + refunds any staff hours already committed
  bookings[bidx].assignedPilotId = null;
  bookings[bidx].assignedCrewIds = [];
  DB.bookings.save(bookings);

  addAudit(aUser.email, 'Lease', 'Lease Rejected', id + (pidx > -1 ? ` · payment ${payments[pidx].id} returned` : ''));
  addNotification(leases[idx].userEmail, 'Lease Rejected', `Lease ${id} was rejected. Contact support for details.`, 'error');
  toast('Lease rejected — payment marked as returned.', 'success');
  renderAdminPanel('leases');
}

/* ================= CUSTOMER MANAGEMENT ================= */
function renderCustomerMgmt(el){
  const customers = DB.users.all().filter(u => u.role === 'customer');
  el.innerHTML = `<div class="panel">
    <div class="panel-head"><h4 style="margin:0;">Customer Management</h4></div>
    <div class="table-wrap"><table>
      <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Membership</th><th>Status</th><th></th></tr></thead>
      <tbody>${customers.map(c => `
        <tr><td>${c.fullName}</td><td>${c.email}</td><td>${c.phone}</td><td>${c.membership}</td>
        <td><span class="badge-status ${statusClass(c.status)}">${c.status}</span></td>
        <td style="white-space:nowrap;">
          <button class="btn btn-ghost btn-sm" onclick="viewCustomerHistory('${c.email}')">History</button>
          ${c.status==='active' ? `<button class="btn btn-danger btn-sm" onclick="setCustomerStatus('${c.email}','suspended')">Suspend</button>`
            : `<button class="btn btn-primary btn-sm" onclick="setCustomerStatus('${c.email}','active')">Activate</button>`}
        </td></tr>`).join('')}</tbody>
    </table></div>
  </div>`;
}

function setCustomerStatus(email, status){
  const users = DB.users.all();
  const idx = users.findIndex(u => u.email===email);
  users[idx].status = status;
  DB.users.save(users);
  addAudit(aUser.email, 'Admin', `Customer ${status}`, email);
  addNotification(email, `Account ${status}`, `Your account has been ${status} by JetLease operations.`, status==='suspended'?'error':'success');
  toast(`Customer ${status}.`, 'success');
  renderAdminPanel('customers');
}

function viewCustomerHistory(email){
  const bookings = DB.bookings.all().filter(b => b.userEmail===email);
  const payments = DB.payments.all().filter(p => p.userEmail===email);
  openModal(`
    <h3>History — ${email}</h3>
    <h4>Bookings (${bookings.length})</h4>
    ${bookings.map(b => `<div class="cost-line"><span>${b.id} — ${b.aircraftModel}</span><span class="badge-status ${statusClass(b.status)}">${b.status}</span></div>`).join('') || '<p class="hint">None</p>'}
    <h4 style="margin-top:14px;">Payments (${payments.length})</h4>
    ${payments.map(p => `<div class="cost-line"><span>${p.id}</span><span class="badge-status ${statusClass(p.status)}">${p.status.replace('_',' ')}</span></div>`).join('') || '<p class="hint">None</p>'}
    <button class="btn btn-ghost btn-block" style="margin-top:16px;" onclick="closeModal()">Close</button>`);
}

/* ================= CREW & PILOT MANAGEMENT ================= */
function renderCrewMgmt(el){
  const pilots = DB.pilots.all();
  const crew = DB.crew.all();
  el.innerHTML = `
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Pilot Management</h4></div>
      <div class="table-wrap"><table>
        <thead><tr><th>Name</th><th>License No.</th><th>Flying Hours</th><th>Remaining Hrs</th><th>Type Ratings</th><th>Available</th><th></th></tr></thead>
        <tbody>${pilots.map(p => `
          <tr><td>${p.name}</td><td style="font-family:var(--mono);">${p.licenseNumber}</td><td>${p.flyingHours.toLocaleString()}</td>
          <td>${p.remainingHours}h</td>
          <td>${p.typeRatings.join(', ')}</td>
          <td><span class="badge-status ${p.available?'st-active':'st-suspended'}">${p.available?'Available':'Unavailable'}</span></td>
          <td><button class="btn btn-ghost btn-sm" onclick="togglePilot('${p.id}')">Toggle</button></td></tr>`).join('')}</tbody>
      </table></div>
    </div>
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Crew Management</h4></div>
      <div class="table-wrap"><table>
        <thead><tr><th>Name</th><th>Role</th><th>Duty Hours</th><th>Remaining Hrs</th><th>Available</th><th></th></tr></thead>
        <tbody>${crew.map(c => `
          <tr><td>${c.name}</td><td>${c.role}</td><td>${c.dutyHours}</td>
          <td>${c.remainingHours}h</td>
          <td><span class="badge-status ${c.available?'st-active':'st-suspended'}">${c.available?'Available':'Unavailable'}</span></td>
          <td><button class="btn btn-ghost btn-sm" onclick="toggleCrew('${c.id}')">Toggle</button></td></tr>`).join('')}</tbody>
      </table></div>
    </div>`;
}
function togglePilot(id){
  const pilots = DB.pilots.all();
  const idx = pilots.findIndex(p => p.id===id);
  pilots[idx].available = !pilots[idx].available;
  DB.pilots.save(pilots);
  addAudit(aUser.email, 'Admin', 'Pilot Availability Changed', id);
  renderAdminPanel('crew');
}
function toggleCrew(id){
  const crew = DB.crew.all();
  const idx = crew.findIndex(c => c.id===id);
  crew[idx].available = !crew[idx].available;
  DB.crew.save(crew);
  addAudit(aUser.email, 'Admin', 'Crew Availability Changed', id);
  renderAdminPanel('crew');
}

/* ================= ROUTES ================= */
let routesCache = null;
async function renderRoutesMgmt(el){
  routesCache = routesCache || (await tryFetchJSON('data/routes.json')) || SEED_ROUTES;
  const bookings = DB.bookings.all();
  const countFor = (code) => bookings.filter(b => b.origin===code || b.destination===code).length;
  el.innerHTML = `
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Route Explorer</h4></div>
      <input type="text" id="routeFilter" placeholder="Filter by city or code..." style="width:100%;padding:11px 12px;border-radius:8px;border:1px solid var(--border);background:var(--surface-2);color:var(--text);margin-bottom:16px;">
      <h4>Domestic Routes</h4>
      <div class="tag-row" id="domesticRoutes"></div>
    </div>`;
  function renderRoutes(filter){
    const f = (filter||'').toLowerCase();
    const dom = routesCache.domestic.filter(r => !f || r.city.toLowerCase().includes(f) || r.code.toLowerCase().includes(f));
    document.getElementById('domesticRoutes').innerHTML = dom.map(r => `<span class="tag">${r.city} (${r.code}) · ${countFor(r.code)} bookings</span>`).join('') || '<span class="hint">No match</span>';
  }
  renderRoutes('');
  document.getElementById('routeFilter').addEventListener('input', (e) => renderRoutes(e.target.value));
}

/* ================= INBOX (Contact Messages + Flight Reports) ================= */
function renderInbox(el){
  const messages = DB.contactMessages.all();
  const reports = DB.reports.all();
  el.innerHTML = `
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Contact Messages</h4></div>
      ${messages.length ? `<div class="table-wrap"><table>
        <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Message</th><th>Received</th><th></th></tr></thead>
        <tbody>${messages.map(m => `
          <tr><td>${m.name}</td><td>${m.email}</td><td>${m.phone}</td><td style="max-width:280px;">${m.message}</td><td>${fmtDate(m.createdAt)}</td>
          <td>${m.status==='New' ? `<button class="btn btn-ghost btn-sm" onclick="markMessageRead('${m.id}')">Mark Read</button>` : `<span class="badge-status st-active">Read</span>`}</td></tr>`).join('')}</tbody>
      </table></div>` : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No contact messages yet.</div>`}
    </div>
    <div class="panel">
      <div class="panel-head"><h4 style="margin:0;">Flight Issue Reports</h4></div>
      ${reports.length ? `<div class="table-wrap"><table>
        <thead><tr><th>Report</th><th>Booking</th><th>Customer</th><th>Subject</th><th>Details</th><th>Status</th><th></th></tr></thead>
        <tbody>${reports.map(r => `
          <tr><td style="font-family:var(--mono);">${r.id}</td><td>${r.bookingId}</td><td>${r.userEmail}</td><td>${r.subject}</td><td style="max-width:260px;">${r.details}</td>
          <td><span class="badge-status ${statusClass(r.status)}">${r.status}</span></td>
          <td>${r.status==='Open' ? `<button class="btn btn-primary btn-sm" onclick="resolveReport('${r.id}')">Mark Resolved</button>` : ''}</td></tr>`).join('')}</tbody>
      </table></div>` : `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No flight issue reports yet.</div>`}
    </div>`;
}
function markMessageRead(id){
  const messages = DB.contactMessages.all();
  const idx = messages.findIndex(m => m.id===id);
  messages[idx].status = 'Read';
  DB.contactMessages.save(messages);
  renderInbox(document.getElementById('adminPanels'));
}
function resolveReport(id){
  const reports = DB.reports.all();
  const idx = reports.findIndex(r => r.id===id);
  reports[idx].status = 'Resolved';
  DB.reports.save(reports);
  addAudit(aUser.email, 'Admin', 'Flight Report Resolved', id);
  addNotification(reports[idx].userEmail, 'Report Resolved', `Your report "${reports[idx].subject}" has been resolved.`, 'success');
  toast('Report marked resolved.', 'success');
  renderInbox(document.getElementById('adminPanels'));
}

/* ================= EXPORTS ================= */
function renderExports(el){
  el.innerHTML = `<div class="panel">
    <div class="panel-head"><h4 style="margin:0;">Reports &amp; Exports</h4></div>
    <p>Export platform data as CSV for offline analysis or finance reconciliation.</p>
    <div style="display:flex;gap:12px;flex-wrap:wrap;margin-top:12px;">
      <button class="btn btn-ghost" onclick="exportCSV('bookings')">Export Booking Report (CSV)</button>
      <button class="btn btn-ghost" onclick="exportCSV('customers')">Export Customer Report (CSV)</button>
      <button class="btn btn-ghost" onclick="exportCSV('payments')">Export Payment Report (CSV)</button>
    </div>
  </div>`;
}
function toCSV(rows){
  if(!rows.length) return '';
  const headers = Object.keys(rows[0]);
  const escape = (v) => `"${String(v ?? '').replace(/"/g,'""')}"`;
  return [headers.join(','), ...rows.map(r => headers.map(h => escape(r[h])).join(','))].join('\n');
}
function exportCSV(type){
  let rows = [];
  if(type==='bookings') rows = DB.bookings.all().map(b => ({ id:b.id, customer:b.userEmail, aircraft:b.aircraftModel, origin:b.origin, destination:b.destination, date:b.date, status:b.status, total:b.cost.total }));
  if(type==='customers') rows = DB.users.all().filter(u=>u.role==='customer').map(c => ({ name:c.fullName, email:c.email, phone:c.phone, membership:c.membership, status:c.status, loyaltyPoints:c.loyaltyPoints }));
  if(type==='payments') rows = DB.payments.all().map(p => ({ id:p.id, booking:p.bookingId, customer:p.userEmail, amount:p.amount, transactionId:p.transactionId, status:p.status }));
  if(!rows.length){ toast('No data to export yet.', 'error'); return; }
  const csv = toCSV(rows);
  const blob = new Blob([csv], { type:'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a'); a.href = url; a.download = `jetlease-${type}-report.csv`;
  document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
  addAudit(aUser.email, 'Admin', 'Report Exported', type);
}

/* ================= AUDIT LOG ================= */
function renderAuditLog(el){
  const logs = DB.audit.all();
  el.innerHTML = `<div class="panel">
    <div class="panel-head"><h4 style="margin:0;">Audit Log</h4></div>
    <div class="pill-nav" id="auditFilters">
      ${['All','Login','Booking','Payment','Lease','Admin'].map((c,i)=>`<button class="${i===0?'active':''}" data-cat="${c}">${c}</button>`).join('')}
    </div>
    <div class="table-wrap"><table>
      <thead><tr><th>Time</th><th>Actor</th><th>Category</th><th>Action</th><th>Details</th></tr></thead>
      <tbody id="auditRows"></tbody>
    </table></div>
  </div>`;
  function renderRows(cat){
    const filtered = cat==='All' ? logs : logs.filter(l => l.category===cat);
    document.getElementById('auditRows').innerHTML = filtered.slice(0,150).map(l => `
      <tr><td>${fmtDate(l.timestamp)}</td><td>${l.actor}</td><td><span class="tag">${l.category}</span></td><td>${l.action}</td><td class="hint">${l.details}</td></tr>`).join('')
      || `<tr><td colspan="5" class="hint">No entries.</td></tr>`;
  }
  renderRows('All');
  document.getElementById('auditFilters').addEventListener('click', (e) => {
    if(e.target.tagName!=='BUTTON') return;
    document.querySelectorAll('#auditFilters button').forEach(b=>b.classList.remove('active'));
    e.target.classList.add('active');
    renderRows(e.target.dataset.cat);
  });
}
