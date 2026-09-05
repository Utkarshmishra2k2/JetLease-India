/* ============================================================
   JETLEASE INDIA — Lease Agreements
   ============================================================ */

let lsUser = null;

document.addEventListener('jl:ready', () => {
  lsUser = requireAuth('customer');
  if(!lsUser) return;
  mountNav();
  mountFooter();
  renderLeaseList();
});

function renderLeaseList(){
  const leases = DB.leases.all().filter(l => l.userEmail === lsUser.email);
  const bookings = DB.bookings.all();
  const el = document.getElementById('leaseList');
  if(!leases.length){
    el.innerHTML = `<div class="card empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No lease agreements yet. They're generated automatically once a booking's payment is verified.</div>`;
    return;
  }
  el.innerHTML = leases.map(l => {
    const b = bookings.find(bk => bk.id === l.bookingId);
    return `
    <div class="strip" style="margin-bottom:18px;">
      <div class="strip-top">
        <div><div class="strip-code">${l.id}</div><h3 style="margin:4px 0 0;">${b ? b.aircraftModel : 'Aircraft'}</h3></div>
        <div style="text-align:right;"><span class="badge-status ${statusClass(l.status)}">${l.status}</span></div>
      </div>
      <div class="strip-body">
        <p style="margin:0;">${b ? `${b.origin} → ${b.destination} · ${b.date}` : ''}</p>
        <div style="display:flex;gap:10px;margin-top:14px;flex-wrap:wrap;">
          <button class="btn btn-ghost btn-sm" onclick="viewLease('${l.id}')">View Agreement</button>
          ${l.status === 'Sent' ? `<button class="btn btn-primary btn-sm" onclick="signLease('${l.id}')">Sign Agreement</button>` : ''}
          <button class="btn btn-ghost btn-sm" onclick="downloadLease('${l.id}')">Download</button>
        </div>
      </div>
      <div class="notch-r"></div>
    </div>`;
  }).join('');
}

function leaseContent(lease, booking){
  return `JETLEASE INDIA — AIRCRAFT CHARTER LEASE AGREEMENT
Agreement ID: ${lease.id}
Booking Reference: ${booking?.id || 'N/A'}
Generated: ${fmtDate(lease.createdAt)}

LESSOR: JetLease Aviation Private Limited, Bandra Kurla Complex, Mumbai, India
LESSEE: ${lsUser.fullName} (${lsUser.email})

AIRCRAFT: ${booking?.aircraftModel || '—'}
ROUTE: ${booking ? booking.origin + ' → ' + booking.destination : '—'}
DEPARTURE: ${booking?.date || '—'}
CHARTER TYPE: ${booking?.type || '—'}${booking?.selfFly ? ' (Self-Fly, safety pilot aboard)' : ''}
TOTAL CHARTER VALUE: ${booking ? convert(booking.cost.total) : '—'}

TERMS:
1. This agreement governs the charter of the above aircraft for the stated route and date(s).
2. The Lessee agrees to comply with all DGCA regulations and airport authority requirements.
3. Full payment verification is a precondition to dispatch.
4. Cancellations are subject to the fare rules communicated at time of booking.
5. This is a digitally signed agreement; the signature below is legally binding within this platform's demo scope.

STATUS: ${lease.status}
SIGNED BY: ${lease.signedBy || 'Not yet signed'}
SIGNED DATE: ${lease.signedDate ? fmtDate(lease.signedDate) : '—'}
APPROVAL DATE: ${lease.approvalDate ? fmtDate(lease.approvalDate) : '—'}
`;
}

function viewLease(id){
  const lease = DB.leases.all().find(l => l.id === id);
  const booking = DB.bookings.all().find(b => b.id === lease.bookingId);
  document.getElementById('leaseModalBody').innerHTML = `
    <h3>Lease Agreement ${lease.id}</h3>
    <pre style="white-space:pre-wrap;font-family:var(--mono);font-size:12.5px;background:var(--surface-2);padding:16px;border-radius:8px;border:1px solid var(--border);">${leaseContent(lease, booking)}</pre>`;
  document.getElementById('leaseModal').style.display = 'flex';
}

function signLease(id){
  const name = prompt('Type your full legal name to digitally sign this agreement:', lsUser.fullName);
  if(!name || name.trim().length < 3){ toast('Signature name required.', 'error'); return; }
  const leases = DB.leases.all();
  const idx = leases.findIndex(l => l.id === id);
  leases[idx].status = 'Signed';
  leases[idx].signedBy = name.trim();
  leases[idx].signedDate = nowISO();
  DB.leases.save(leases);

  const bookings = DB.bookings.all();
  const bidx = bookings.findIndex(b => b.id === leases[idx].bookingId);
  if(bidx > -1 && bookings[bidx].status === 'Lease Pending'){ bookings[bidx].status = 'Lease Signed'; DB.bookings.save(bookings); }

  addAudit(lsUser.email, 'Lease', 'Lease Signed', id);
  addNotification('admin', 'Lease Signed', `${lsUser.fullName} signed lease ${id}. Awaiting approval.`, 'info');
  toast('Agreement signed. Awaiting admin approval.', 'success');
  renderLeaseList();
}

function downloadLease(id){
  const lease = DB.leases.all().find(l => l.id === id);
  const booking = DB.bookings.all().find(b => b.id === lease.bookingId);
  const blob = new Blob([leaseContent(lease, booking)], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = `JetLease-Agreement-${lease.id}.txt`;
  document.body.appendChild(a); a.click(); a.remove();
  URL.revokeObjectURL(url);
  addAudit(lsUser.email, 'Lease', 'Lease Downloaded', id);
}
