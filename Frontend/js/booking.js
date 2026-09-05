/* ============================================================
   JETLEASE INDIA — Booking Flow
   ============================================================ */

let bkState = {
  type: 'Domestic Charter', tripType: 'One Way', origin: '', destination: '',
  date: '', time: '', returnDate: '', returnTime: '', pax: 2,
  aircraftId: null, passengers: [], selfFly: false, selfFlyDetails: null,
  licenseVerified: false, cost: null
};
let bkRoutes = null;
let bkUser = null;

document.addEventListener('jl:ready', async () => {
  bkUser = requireAuth('customer');
  if(!bkUser) return;
  mountNav();
  mountFooter();

  bkRoutes = (await tryFetchJSON('data/routes.json')) || SEED_ROUTES;
  const qs = new URLSearchParams(window.location.search);
  if(qs.get('type') && qs.get('type') !== 'International Charter') bkState.type = qs.get('type');
  if(qs.get('trip')) bkState.tripType = qs.get('trip');
  if(qs.get('pax')) bkState.pax = Number(qs.get('pax'));
  if(qs.get('date')) bkState.date = qs.get('date');

  document.getElementById('bkType').value = bkState.type;
  document.getElementById('bkTripType').value = bkState.tripType;
  document.getElementById('bkPax').value = bkState.pax;
  document.getElementById('bkDate').min = todayISO();
  if(bkState.date) document.getElementById('bkDate').value = bkState.date;
  syncReturnDateMin();

  fillRouteOptions();
  if(qs.get('origin')) document.getElementById('bkOrigin').value = qs.get('origin');
  if(qs.get('destination')) document.getElementById('bkDestination').value = qs.get('destination');
  const showReturnInit = document.getElementById('bkTripType').value === 'Round Trip';
  document.getElementById('returnDateField').style.display = showReturnInit ? 'block' : 'none';
  document.getElementById('returnTimeField').style.display = showReturnInit ? 'block' : 'none';
  updateRecommendationDistance();

  document.getElementById('bkType').addEventListener('change', (e) => {
    bkState.type = e.target.value;
    fillRouteOptions();
    renderFleetOptions();
    updateRecommendationDistance();
  });
  document.getElementById('bkTripType').addEventListener('change', (e) => {
    bkState.tripType = e.target.value;
    const showReturn = e.target.value === 'Round Trip';
    document.getElementById('returnDateField').style.display = showReturn ? 'block' : 'none';
    document.getElementById('returnTimeField').style.display = showReturn ? 'block' : 'none';
  });
  document.getElementById('bkDate').addEventListener('change', syncReturnDateMin);
  document.getElementById('bkOrigin').addEventListener('change', updateRecommendationDistance);
  document.getElementById('bkDestination').addEventListener('change', updateRecommendationDistance);

  renderFleetOptions();

  const preselect = qs.get('aircraft');
  if(preselect) selectAircraft(preselect);

  document.getElementById('selfFlyToggle').addEventListener('change', (e) => {
    bkState.selfFly = e.target.checked;
    document.getElementById('selfFlyPanel').style.display = e.target.checked ? 'block' : 'none';
  });
});

// Keeps the Return Date picker's minimum selectable date locked to whatever Departure
// Date is currently chosen, so a return before departure can't even be picked in the UI.
function syncReturnDateMin(){
  const depDate = document.getElementById('bkDate').value;
  const returnInput = document.getElementById('bkReturnDate');
  const floor = depDate || todayISO();
  returnInput.min = floor;
  // If a previously chosen return date is now before the new departure date, clear it
  // so the customer is forced to re-pick a valid one.
  if(returnInput.value && returnInput.value < floor){
    returnInput.value = '';
  }
}

// Recalculates the recommendation panel's Distance (km) field automatically from the
// currently selected origin/destination route — no manual entry.
function updateRecommendationDistance(){
  const origin = document.getElementById('bkOrigin').value;
  const destination = document.getElementById('bkDestination').value;
  const dist = routeDistanceKm(origin, destination, bkRoutes);
  const field = document.getElementById('recDistance');
  const hint = document.getElementById('recDistanceHint');
  if(dist){
    field.value = dist;
    hint.textContent = `Auto-calculated straight-line distance for ${origin} → ${destination}: ${dist.toLocaleString()} km.`;
  } else {
    field.value = 0;
    hint.textContent = 'Select an origin and destination above — distance is calculated automatically from the route.';
  }
}

function fillRouteOptions(){
  const list = bkRoutes.domestic;
  const o = document.getElementById('bkOrigin'), d = document.getElementById('bkDestination');
  o.innerHTML = list.map(r=>`<option value="${r.code}">${r.city} (${r.code})</option>`).join('');
  d.innerHTML = list.map(r=>`<option value="${r.code}">${r.city} (${r.code})</option>`).join('');
  d.selectedIndex = Math.min(1, list.length-1);
}

function categoryForType(type){
  if(type === 'Helicopter Charter') return 'Helicopter';
  return null; // jets: any jet/turboprop category allowed
}

function renderFleetOptions(){
  const grid = document.getElementById('bkFleetGrid');
  const fleet = DB.aircraft.all();
  const cat = categoryForType(bkState.type);
  const filtered = fleet.filter(a => a.status === 'Available' && (cat ? a.category === cat : a.category !== 'Helicopter'));
  grid.innerHTML = filtered.map(a => `
    <div class="aircraft-card" id="ac-${a.id}" style="cursor:pointer;" onclick="selectAircraft('${a.id}')">
      <div class="aircraft-media"><img src="${a.image||'assets/images/placeholder-aircraft.svg'}" alt="${a.model}" style="width:64px;height:64px;opacity:.75;"></div>
      <div class="aircraft-body">
        <h3 style="margin-bottom:2px;">${a.model}</h3>
        <p style="margin:0;font-family:var(--mono);font-size:12px;">${a.reg} · ${a.capacity} seats</p>
        <div class="price-row"><span style="font-size:12px;color:var(--text-dim);">per hour</span><b>${fmtINR(a.hourlyRate)}</b></div>
      </div>
    </div>`).join('') || `<div class="empty-state"><img class="glyph" src="assets/images/placeholder-icon.svg" alt="">No aircraft currently available in this category.</div>`;
}

function selectAircraft(id){
  bkState.aircraftId = id;
  document.querySelectorAll('#bkFleetGrid .aircraft-card').forEach(c => c.style.borderColor = 'var(--border)');
  const el = document.getElementById('ac-' + id);
  if(el) el.style.borderColor = 'var(--accent)';
}

function runRecommendation(){
  const budget = Number(document.getElementById('recBudget').value);
  const distance = Number(document.getElementById('recDistance').value);
  if(!distance){ toast('Select an origin and destination first so the distance can be calculated.', 'error'); return; }
  const category = categoryForType(bkState.type); // null for jets/turboprops, 'Helicopter' for Helicopter Charter — fixes the jet/helicopter mix-up
  const result = recommendAircraft({ passengers: bkState.pax, budgetINR: budget, distanceKm: distance, category });
  const box = document.getElementById('recResult');
  if(!result.best){ box.innerHTML = `<p style="color:var(--red);">No ${category ? category.toLowerCase() : 'jet/turboprop'} aircraft matches this passenger count and range. Try increasing the budget or reducing passengers.</p>`; return; }
  box.innerHTML = `
    <div class="strip">
      <div class="strip-top">
        <div><div class="strip-code">Recommended</div><h3 style="margin:4px 0 0;">${result.best.model}</h3></div>
        <div style="text-align:right;"><div class="strip-code">Est. ${fmtINR(result.best.estCost)}</div></div>
      </div>
      <div class="strip-body">
        <p style="margin:0;">Seats ${result.best.capacity} · Range ${result.best.range.toLocaleString()} km · ${result.best.category}</p>
        <button class="btn btn-primary btn-sm" style="margin-top:12px;" onclick="selectAircraft('${result.best.id}');document.getElementById('bkFleetGrid').scrollIntoView({behavior:'smooth'})">Select This Aircraft</button>
      </div>
    </div>
    ${result.alternatives.length ? `<p class="hint" style="margin-top:10px;">Alternatives: ${result.alternatives.map(a=>a.model).join(', ')}</p>` : ''}`;
}

/* ---------------- STEP NAV ---------------- */
function goStep(n){
  if(n === 2 && !bkState.aircraftId){ toast('Select an aircraft first.', 'error'); return; }
  bkState.origin = document.getElementById('bkOrigin').value;
  bkState.destination = document.getElementById('bkDestination').value;
  bkState.date = document.getElementById('bkDate').value;
  bkState.time = document.getElementById('bkTime').value;
  bkState.returnDate = document.getElementById('bkReturnDate').value;
  bkState.returnTime = document.getElementById('bkReturnTime').value;
  bkState.pax = Number(document.getElementById('bkPax').value);

  if(n === 2){
    let ok = true;
    setFieldError('f-bkDate', !!bkState.date); if(!bkState.date) ok = false;
    setFieldError('f-bkTime', !!bkState.time); if(!bkState.time) ok = false;
    if(bkState.tripType === 'Round Trip'){
      const returnValid = !!bkState.returnDate && bkState.returnDate >= bkState.date;
      setFieldError('returnDateField', returnValid); if(!returnValid) ok = false;
    }
    if(!ok){ toast('Select a valid departure date/time and a return date on or after the departure date.', 'error'); return; }
  }

  for(let i=1;i<=4;i++) document.getElementById('step'+i).style.display = (i===n) ? 'block' : 'none';
  for(let i=1;i<=4;i++) document.getElementById('bps'+i).classList.toggle('done', i<=n);

  if(n === 2) renderPassengerForm();
  if(n === 4) renderCostReview();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

/* ---------------- PASSENGERS ---------------- */
function passengerTemplate(){
  return { name:'', dob:'', gender:'Male', aadhaar:'', verificationStatus:'Pending', aadhaarAutoFilled:false };
}

function renderPassengerForm(){
  document.getElementById('passengerTypeNote').textContent =
    'Aadhaar details are required for mock verification, except for children under 5 years and 15 days old.';
  if(bkState.passengers.length === 0){
    for(let i=0;i<bkState.pax;i++) bkState.passengers.push(passengerTemplate());
  }
  document.getElementById('passengerList').innerHTML = '';
  bkState.passengers.forEach((p,i) => renderPassengerRow(p,i));
}

function renderPassengerRow(p, i){
  const wrap = document.createElement('div');
  wrap.className = 'panel';
  wrap.id = 'pax-' + i;
  const { min: dobMin, max: dobMax } = dobRange();
  const exempt = isAadhaarExempt(p.dob);
  const locked = !!p.aadhaarAutoFilled;
  wrap.innerHTML = `
    <div class="panel-head"><h4 style="margin:0;">Passenger ${i+1}</h4>${i>0?`<button type="button" class="btn btn-ghost btn-sm" onclick="removePassengerRow(${i})">Remove</button>`:''}</div>
    ${locked ? `<div class="hint" style="color:var(--green);margin-bottom:10px;">Name, date of birth &amp; gender were auto-filled from the matched Aadhaar record. <button type="button" class="btn btn-ghost btn-sm" onclick="clearAadhaarAutoFill(${i})">Clear &amp; Edit Manually</button></div>` : ''}
    <div class="form-grid">
      <div class="field" id="f-pax-name-${i}">
        <label>Full Name</label>
        <input type="text" value="${p.name}" ${locked?'disabled':''} oninput="onPassengerInput(${i},'name',this.value)">
        <span class="error-text">Only letters are allowed.</span>
      </div>
      <div class="field" id="f-pax-dob-${i}">
        <label>Date of Birth</label>
        <input type="date" value="${p.dob}" min="${dobMin}" max="${dobMax}" ${locked?'disabled':''} oninput="onPassengerInput(${i},'dob',this.value)">
        <span class="error-text">Age must be between 15 days and 100 years — future dates are not allowed.</span>
      </div>
      <div class="field"><label>Gender</label>
        <select ${locked?'disabled':''} onchange="bkState.passengers[${i}].gender=this.value"><option ${p.gender==='Male'?'selected':''}>Male</option><option ${p.gender==='Female'?'selected':''}>Female</option><option ${p.gender==='Other'?'selected':''}>Other</option></select>
      </div>
      <div class="field" id="f-pax-aadhaar-${i}">
        <label>Aadhaar Number ${exempt ? '<span class="hint" style="display:inline;">(optional — under 5y 15d)</span>' : ''}</label>
        <input type="text" maxlength="12" value="${p.aadhaar}" oninput="onPassengerInput(${i},'aadhaar',this.value)" placeholder="${exempt ? 'Not required for this age' : ''}">
        <span class="error-text">Aadhaar number must be exactly 12 digits.</span>
      </div>
    </div>
    <div style="margin-top:12px;display:flex;align-items:center;gap:12px;flex-wrap:wrap;">
      <span class="badge-status ${statusClass(p.verificationStatus)}" id="pax-status-${i}">${p.verificationStatus}</span>
      <button type="button" class="btn btn-ghost btn-sm" id="pax-verify-btn-${i}" onclick="verifyPassenger(${i})">Run Mock Verification</button>
    </div>`;
  const existing = document.getElementById('pax-' + i);
  if(existing){ existing.replaceWith(wrap); }
  else { document.getElementById('passengerList').appendChild(wrap); }
}

// Live per-field validation for passenger rows, reusing the shared VALIDATORS rules.
// Aadhaar is only mandatory when the passenger is NOT exempt (5 years + 15 days or older).
function onPassengerInput(i, field, value){
  bkState.passengers[i][field] = value;
  const p = bkState.passengers[i];
  let err = '';
  if(field === 'name') err = VALIDATORS.name(p.name);
  else if(field === 'dob'){
    err = VALIDATORS.dob(p.dob);
    renderPassengerRow(p, i); // re-render so the Aadhaar-optional hint updates for the new DOB
    return;
  }
  else if(field === 'aadhaar'){
    const exempt = isAadhaarExempt(p.dob);
    if(exempt && !p.aadhaar) err = ''; // no Aadhaar entered and exempt — that's allowed
    else err = VALIDATORS.aadhaar(p.aadhaar);
    setFieldError(`f-pax-aadhaar-${i}`, !err, err);
    // Requirement 5: automatically fetch + auto-fill the moment a valid 12-digit
    // Aadhaar number is entered — no button click required.
    if(!err && p.aadhaar.length === 12){
      verifyPassenger(i);
    }
    return;
  }
  const fieldId = `f-pax-${field}-${i}`;
  setFieldError(fieldId, !err, err);
}

function addPassengerRow(){
  bkState.passengers.push(passengerTemplate());
  bkState.pax = bkState.passengers.length;
  renderPassengerRow(bkState.passengers[bkState.passengers.length-1], bkState.passengers.length-1);
}
function removePassengerRow(i){
  bkState.passengers.splice(i,1);
  bkState.pax = bkState.passengers.length;
  renderPassengerForm();
}

// Releases an auto-filled row back to manual editing.
function clearAadhaarAutoFill(i){
  bkState.passengers[i].aadhaarAutoFilled = false;
  renderPassengerRow(bkState.passengers[i], i);
}

// Mock Aadhaar verification (Requirement 13): looks the entered number up against the
// dummy registry. Per policy this NEVER blocks the booking — it's purely informational,
// shown as Verified / Not Verified. A passenger under the exemption age with no Aadhaar
// entered is marked "Not Required" without any API call. When a record matches
// (Requirement 5), Name/DOB/Gender are auto-filled and locked from further manual entry.
async function verifyPassenger(i){
  const p = bkState.passengers[i];
  const exempt = isAadhaarExempt(p.dob);
  const statusEl = document.getElementById('pax-status-'+i);

  if(exempt && !p.aadhaar){
    p.verificationStatus = 'Not Required';
    p.aadhaarAutoFilled = false;
    if(statusEl){ statusEl.textContent = p.verificationStatus; statusEl.className = 'badge-status ' + statusClass(p.verificationStatus); }
    toast(`Passenger ${i+1} is under the Aadhaar exemption age — no verification required.`, 'success');
    return;
  }
  if(!p.aadhaar){
    setFieldError(`f-pax-aadhaar-${i}`, false, 'Aadhaar number is required.');
    toast('Enter an Aadhaar number to verify.', 'error');
    return;
  }

  if(statusEl){ statusEl.textContent = 'Verifying'; statusEl.className = 'badge-status ' + statusClass('Verifying'); }
  const result = await mockVerifyAadhaarAPI(p.aadhaar);
  p.verificationStatus = result.verified ? 'Verified' : 'Not Verified';

  if(result.verified && result.record){
    p.name = result.record.holderName;
    if(result.record.dob) p.dob = result.record.dob;
    if(result.record.gender) p.gender = result.record.gender;
    p.aadhaarAutoFilled = true;
  } else {
    // Not found (or suspended) — never crash or block the flow; the row stays editable.
    p.aadhaarAutoFilled = false;
  }

  renderPassengerRow(p, i); // reflect fetched details, lock state, and status together
  toast(result.verified ? `Passenger ${i+1}: details auto-filled from the Aadhaar record.` : `Passenger ${i+1}: ${result.message}`, result.verified ? 'success' : 'error');
}

function submitPassengers(){
  let allValid = true;
  bkState.passengers.forEach((p, i) => {
    const nameErr = VALIDATORS.name(p.name);
    setFieldError(`f-pax-name-${i}`, !nameErr, nameErr); if(nameErr) allValid = false;
    const dobErr = VALIDATORS.dob(p.dob);
    setFieldError(`f-pax-dob-${i}`, !dobErr, dobErr); if(dobErr) allValid = false;

    const exempt = isAadhaarExempt(p.dob);
    const aadhaarErr = (exempt && !p.aadhaar) ? '' : VALIDATORS.aadhaar(p.aadhaar);
    setFieldError(`f-pax-aadhaar-${i}`, !aadhaarErr, aadhaarErr); if(aadhaarErr) allValid = false;
  });
  if(!allValid){ toast('Please fix the highlighted passenger fields.', 'error'); return; }
  goStep(3);
}

/* ---------------- CREW / SELF-FLY ---------------- */
// Note: exact crew/pilot assignment for a booking is now handled by admin operations
// (Admin Console → Bookings → Assign Crew) once the booking is active — showing a
// specific "assigned crew" list to the customer at booking time added no value and has
// been removed. The cost engine still accounts for generic pilot/crew cost.

// Mock API call to the DGCA-style pilot license registry (data/pilot-licenses.json).
async function verifyPilotLicense(){
  const licenseNumber = document.getElementById('licenseNumber').value.trim();
  const statusEl = document.getElementById('licenseVerifyStatus');
  if(!licenseNumber){ setFieldError('f-licenseNumber', false, 'License number is required.'); return; }
  statusEl.textContent = 'Verifying with DGCA registry…';
  statusEl.style.color = 'var(--text-dim)';
  const result = await mockVerifyPilotLicenseAPI(licenseNumber);
  bkState.licenseVerified = result.verified;
  statusEl.textContent = result.message;
  statusEl.style.color = result.verified ? 'var(--green)' : 'var(--red)';
  setFieldError('f-licenseNumber', result.verified, result.verified ? '' : 'License number is required.');

  // Requirement 6: when the license matches a record, auto-fill the remaining pilot
  // details (flying hours + license class) so they don't need to be typed manually.
  const hoursInput = document.getElementById('flyingHours');
  const classSelect = document.getElementById('licenseClass');
  const autoFillNote = document.getElementById('selfFlyAutoFillNote');
  if(result.verified && result.record){
    hoursInput.value = result.record.hoursOnRecord;
    classSelect.value = result.record.licenseClass;
    hoursInput.readOnly = true;
    classSelect.disabled = true;
    autoFillNote.style.display = 'block';
    setFieldError('f-flyingHours', true);
    setFieldError('f-licenseClass', true);
  } else {
    // Not found in the registry — show "Not Verified" (already done via statusEl above)
    // and leave the booking flow fully intact; nothing is auto-filled or locked.
    clearSelfFlyAutoFill();
  }
  toast(result.verified ? 'Pilot license verified — details auto-filled.' : 'License not found in registry — please fill details manually.', result.verified ? 'success' : 'error');
}

// Lets the customer override auto-filled Self-Fly details (or is called automatically
// when a license isn't found in the registry, so nothing stays incorrectly locked).
function clearSelfFlyAutoFill(){
  const hoursInput = document.getElementById('flyingHours');
  const classSelect = document.getElementById('licenseClass');
  hoursInput.readOnly = false;
  classSelect.disabled = false;
  document.getElementById('selfFlyAutoFillNote').style.display = 'none';
}

function submitCrewStep(){
  document.getElementById('selfFlyError').style.display = 'none';
  if(bkState.selfFly){
    const details = {
      flyingHours: document.getElementById('flyingHours').value,
      licenseNumber: document.getElementById('licenseNumber').value,
      licenseClass: document.getElementById('licenseClass').value,
      certificateFileName: document.getElementById('certificateFile').files[0]?.name || '',
      dgcaDeclaration: document.getElementById('dgcaDeclaration').checked
    };
    const result = validateSelfFly(details);
    ['flyingHours','licenseNumber','licenseClass','certificate'].forEach(f => {
      document.getElementById('f-'+f)?.classList.remove('invalid');
    });
    if(!result.valid){
      const box = document.getElementById('selfFlyError');
      box.style.display = 'block';
      box.innerHTML = '<b>Self-Fly booking rejected:</b><br>' + result.errors.map(e=>'• '+e).join('<br>');
      if(details.flyingHours < 100) document.getElementById('f-flyingHours').classList.add('invalid');
      if(!details.licenseNumber) document.getElementById('f-licenseNumber').classList.add('invalid');
      if(!details.licenseClass) document.getElementById('f-licenseClass').classList.add('invalid');
      if(!details.certificateFileName) document.getElementById('f-certificate').classList.add('invalid');
      toast('Self-Fly requirements not met — see details above.', 'error');
      return;
    }
    // Note: DGCA license registry verification (verifyPilotLicense) is informational only —
    // a "Not Verified" result does not block the booking, per policy.
    bkState.selfFlyDetails = { ...details, flyingHours: result.flyingHours, licenseVerified: bkState.licenseVerified };
  } else {
    bkState.selfFlyDetails = null;
  }
  goStep(4);
}

/* ---------------- COST REVIEW ---------------- */
function renderCostReview(){
  const aircraft = DB.aircraft.all().find(a => a.id === bkState.aircraftId);
  const cost = calculateCost({ aircraft, bookingType: bkState.type, tripType: bkState.tripType, selfFly: bkState.selfFly });
  bkState.cost = cost;

  document.getElementById('reviewRoute').textContent = `${bkState.origin} → ${bkState.destination}${bkState.tripType==='Round Trip' ? ' → '+bkState.origin : ''}`;
  document.getElementById('reviewAircraft').textContent = aircraft.model;
  document.getElementById('reviewDate').textContent = bkState.date ? `${bkState.date} ${bkState.time||''}`.trim() : '—';
  document.getElementById('reviewType').textContent = bkState.type + (bkState.selfFly ? ' · Self-Fly' : '');

  const lines = [
    ['Aircraft Cost', cost.aircraftCost],
    [bkState.selfFly ? 'Safety Pilot Cost' : 'Pilot Cost', cost.pilotCost],
    ['Crew Cost', cost.crewCost],
    ['Airport Charges', cost.airportCharges],
    ['Fuel Surcharge (8%)', cost.fuelSurcharge],
    ['GST (5%)', cost.gst],
  ];
  document.getElementById('costLines').innerHTML =
    lines.map(([l,v]) => `<div class="cost-line"><span>${l}</span><span>${convert(v)}</span></div>`).join('') +
    `<div class="cost-line total"><span>Total Payable</span><span>${convert(cost.total)}</span></div>`;

  document.getElementById('ticksRow').innerHTML = Array(40).fill('<span></span>').join('');
}

function confirmBooking(){
  const aircraft = DB.aircraft.all().find(a => a.id === bkState.aircraftId);
  // Per policy, verification results (Aadhaar / license) never block a booking — this only
  // checks that basic passenger details were captured, not that verification succeeded.
  const box = document.getElementById('bookingErrorBox');
  box.style.display = 'none';

  const booking = {
    id: uid('BKG'),
    userEmail: bkUser.email,
    type: bkState.type,
    tripType: bkState.tripType,
    origin: bkState.origin,
    destination: bkState.destination,
    date: bkState.date,
    time: bkState.time,
    returnDate: bkState.returnDate,
    returnTime: bkState.returnTime,
    pax: bkState.pax,
    currency: 'INR',
    aircraftId: aircraft.id,
    aircraftModel: aircraft.model,
    passengers: bkState.passengers,
    selfFly: bkState.selfFly,
    selfFlyDetails: bkState.selfFlyDetails,
    cost: bkState.cost,
    status: 'Pending Payment',
    assignedPilotId: null,
    assignedCrewIds: [],
    createdAt: nowISO()
  };
  const bookings = DB.bookings.all();
  bookings.push(booking);
  DB.bookings.save(bookings);

  // mark aircraft booked
  const fleet = DB.aircraft.all();
  const idx = fleet.findIndex(a => a.id === aircraft.id);
  if(idx > -1){ fleet[idx].status = 'Booked'; DB.aircraft.save(fleet); }

  addAudit(bkUser.email, 'Booking', 'Booking Created', `${booking.id} · ${booking.origin}→${booking.destination}`);
  addNotification(bkUser.email, 'Booking Created', `Your booking ${booking.id} is pending payment.`, 'info');
  addNotification('admin', 'New Booking', `${bkUser.fullName} booked ${aircraft.model} (${booking.id}).`, 'info');

  toast('Booking created — proceed to payment.', 'success');
  setTimeout(() => window.location.href = `payment.html?booking=${booking.id}`, 500);
}
