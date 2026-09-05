/* ============================================================
   JETLEASE INDIA — Payment
   ============================================================ */

const BANK_DETAILS = {
  bankName: 'HDFC Bank, Bandra Kurla Complex Branch',
  accountHolder: 'JetLease Aviation Private Limited',
  accountNumber: '50200012345678',
  ifsc: 'HDFC0000123'
};

document.addEventListener('jl:ready', () => {
  const user = requireAuth('customer');
  if(!user) return;
  mountNav();
  mountFooter();

  const qs = new URLSearchParams(window.location.search);
  const bookingId = qs.get('booking');
  const bookings = DB.bookings.all();
  const booking = bookings.find(b => b.id === bookingId && b.userEmail === user.email);
  const body = document.getElementById('paymentBody');

  if(!booking){
    body.innerHTML = `<div class="card empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No booking found. <a href="booking.html" style="color:var(--accent);">Start a new booking</a>.</div>`;
    return;
  }

  // Look at the MOST RECENT payment attempt for this booking. If it was rejected, the
  // customer should be able to submit a fresh transaction ID rather than being stuck on
  // a dead-end status screen — this is the fix for the payment/booking sync issue.
  const attempts = DB.payments.all().filter(p => p.bookingId === booking.id).sort((a,b) => new Date(a.submittedAt) - new Date(b.submittedAt));
  const existingPayment = attempts[attempts.length - 1];

  if(existingPayment && existingPayment.status !== 'REJECTED'){
    renderPaymentStatus(booking, existingPayment);
  } else {
    renderPaymentForm(booking, !!existingPayment);
  }
});

function renderPaymentForm(booking, isResubmission){
  document.getElementById('paymentBody').innerHTML = `
    <div class="card" style="margin-bottom:18px;">
      <div class="panel-head"><h4 style="margin:0;">Booking ${booking.id}</h4><span class="badge-status ${statusClass(booking.status)}">${booking.status}</span></div>
      <p style="margin:0;">${booking.aircraftModel} · ${booking.origin} → ${booking.destination} · ${booking.date}</p>
      <div class="cost-line total" style="margin-top:10px;"><span>Amount Payable</span><span>${convert(booking.cost.total)}</span></div>
    </div>
    ${isResubmission ? `<div class="card" style="margin-bottom:18px;border-color:var(--amber);"><p style="margin:0;color:var(--amber);">Your previous transaction ID was rejected. Please double-check the details below and submit a valid transaction ID.</p></div>` : ''}

    <div class="card" style="margin-bottom:18px;">
      <h4>Bank Transfer Details</h4>
      <div class="cost-line"><span>Bank Name</span><span>${BANK_DETAILS.bankName}</span></div>
      <div class="cost-line"><span>Account Holder</span><span>${BANK_DETAILS.accountHolder}</span></div>
      <div class="cost-line"><span>Account Number</span><span style="font-family:var(--mono);">${BANK_DETAILS.accountNumber}</span></div>
      <div class="cost-line"><span>IFSC Code</span><span style="font-family:var(--mono);">${BANK_DETAILS.ifsc}</span></div>
      <p class="hint" style="margin-top:12px;">Transfer the exact amount shown above, then submit your transaction ID below for verification.</p>
    </div>

    <form class="card" id="paymentForm">
      <div class="field"><label>Transaction ID / UTR Number</label><input type="text" id="transactionId" required placeholder="e.g. UTR2026071600123"></div>
      <button class="btn btn-primary btn-block" style="margin-top:16px;" type="submit">Submit for Verification</button>
    </form>`;

  document.getElementById('paymentForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const txnId = document.getElementById('transactionId').value.trim();
    if(txnId.length < 5){ toast('Enter a valid transaction ID.', 'error'); return; }

    const payment = {
      id: uid('PAY'), bookingId: booking.id, userEmail: booking.userEmail,
      amount: booking.cost.total, currency: booking.currency, ...BANK_DETAILS,
      transactionId: txnId, status: 'PENDING_VERIFICATION', submittedAt: nowISO()
    };
    const payments = DB.payments.all(); payments.push(payment); DB.payments.save(payments);
    recordLedgerEntry(payment); // simulate the bank's own independent record of the cleared transfer

    const bookings = DB.bookings.all();
    const idx = bookings.findIndex(b => b.id === booking.id);
    bookings[idx].status = 'Pending Verification';
    DB.bookings.save(bookings);

    addAudit(booking.userEmail, 'Payment', 'Payment Submitted', `${payment.id} · ${txnId}`);
    addNotification(booking.userEmail, 'Payment Submitted', `Transaction ${txnId} submitted for verification.`, 'info');
    addNotification('admin', 'Payment Awaiting Verification', `${booking.userEmail} submitted payment for ${booking.id}.`, 'warning');

    toast('Payment submitted — awaiting verification.', 'success');
    renderPaymentStatus({ ...booking, status:'Pending Verification' }, payment);
  });
}

function renderPaymentStatus(booking, payment){
  document.getElementById('paymentBody').innerHTML = `
    <div class="strip">
      <div class="strip-top">
        <div><div class="strip-code">${payment.id}</div><h3 style="margin:4px 0 0;">${booking.aircraftModel}</h3></div>
        <div style="text-align:right;"><span class="badge-status ${statusClass(payment.status)}">${payment.status.replace('_',' ')}</span></div>
      </div>
      <div class="strip-body">
        <div class="cost-line"><span>Amount</span><span>${convert(payment.amount)}</span></div>
        <div class="cost-line"><span>Transaction ID</span><span style="font-family:var(--mono);">${payment.transactionId}</span></div>
        <div class="cost-line"><span>Submitted</span><span>${fmtDate(payment.submittedAt)}</span></div>
      </div>
      <div class="notch-r"></div>
    </div>
    <p class="hint" style="margin-top:16px;">
      ${payment.status === 'PENDING_VERIFICATION' ? 'Our finance desk verifies bank transfers within 2 business hours. You\'ll be notified once confirmed.' :
        payment.status === 'VERIFIED' ? 'Payment verified. Your lease agreement (if applicable) is being prepared.' :
        payment.status === 'RETURNED' ? 'This payment has been returned because the lease agreement for this booking was rejected. Contact support if you have questions about your refund.' :
        'This payment was rejected. Please contact support or resubmit with a valid transaction ID.'}
    </p>
    <a href="dashboard.html" class="btn btn-primary btn-block" style="margin-top:16px;">Go to Dashboard</a>`;
}
