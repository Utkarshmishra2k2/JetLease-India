/* ============================================================
   JETLEASE INDIA — Data Layer
   All persistence is localStorage. JSON files in /data are the
   canonical seed source; if the app is opened via file:// (fetch
   blocked by CORS) it falls back to the embedded SEED_* copies
   below so the app still runs with zero setup.
   ============================================================ */

const LS = {
  users:'jl_users', session:'jl_session', aircraft:'jl_aircraft',
  bookings:'jl_bookings', payments:'jl_payments', leases:'jl_leases',
  notifications:'jl_notifications',
  crew:'jl_crew', pilots:'jl_pilots', audit:'jl_audit', seeded:'jl_seeded_v4',
  ledger:'jl_bank_ledger', contactMessages:'jl_contact_messages', reports:'jl_flight_reports',
  otp:'jl_pending_otp'
};

function lsGet(key, fallback){
  try{ const v = localStorage.getItem(key); return v ? JSON.parse(v) : (fallback ?? []); }
  catch(e){ return fallback ?? []; }
}
function lsSet(key, val){ localStorage.setItem(key, JSON.stringify(val)); }
function uid(prefix){ return prefix + '-' + Date.now().toString(36).toUpperCase() + Math.floor(Math.random()*900+100); }
function nowISO(){ return new Date().toISOString(); }
function todayISO(){ return new Date().toISOString().split('T')[0]; }

/* ---------------- Embedded seed fallback (mirrors /data/*.json) ---------------- */
const SEED_AIRCRAFT = [
  { id:"AC-101",reg:"VT-JLA",model:"Cessna Citation CJ3+",manufacturer:"Cessna",category:"Light Jet",capacity:7,speed:770,range:3480,hourlyRate:185000,status:"Available",typeRating:"CJ3-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-102",reg:"VT-JLB",model:"Phenom 300E",manufacturer:"Embraer",category:"Light Jet",capacity:8,speed:839,range:3650,hourlyRate:210000,status:"Available",typeRating:"PH300-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-103",reg:"VT-JLC",model:"Citation Latitude",manufacturer:"Cessna",category:"Mid Jet",capacity:9,speed:841,range:5460,hourlyRate:320000,status:"Available",typeRating:"CLAT-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-104",reg:"VT-JLD",model:"Challenger 350",manufacturer:"Bombardier",category:"Mid Jet",capacity:10,speed:870,range:5926,hourlyRate:410000,status:"Booked",typeRating:"CH350-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-105",reg:"VT-JLE",model:"Gulfstream G650ER",manufacturer:"Gulfstream",category:"Heavy Jet",capacity:14,speed:956,range:13890,hourlyRate:890000,status:"Available",typeRating:"G650-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-106",reg:"VT-JLF",model:"Global 7500",manufacturer:"Bombardier",category:"Heavy Jet",capacity:16,speed:1050,range:14260,hourlyRate:950000,status:"Maintenance",typeRating:"GL7500-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-107",reg:"VT-JLG",model:"AgustaWestland AW139",manufacturer:"Leonardo",category:"Helicopter",capacity:12,speed:306,range:1061,hourlyRate:260000,status:"Available",typeRating:"AW139-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-108",reg:"VT-JLH",model:"Bell 429",manufacturer:"Bell",category:"Helicopter",capacity:6,speed:278,range:722,hourlyRate:150000,status:"Available",typeRating:"B429-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-109",reg:"VT-JLI",model:"Pilatus PC-12 NGX",manufacturer:"Pilatus",category:"Turboprop",capacity:9,speed:528,range:3341,hourlyRate:120000,status:"Available",typeRating:"PC12-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-110",reg:"VT-JLJ",model:"King Air 350i",manufacturer:"Beechcraft",category:"Turboprop",capacity:11,speed:578,range:3336,hourlyRate:135000,status:"Grounded",typeRating:"KA350-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-111",reg:"VT-JLK",model:"Learjet 45XR Air Ambulance",manufacturer:"Bombardier",category:"Air Ambulance",capacity:4,speed:862,range:3688,hourlyRate:275000,status:"Available",typeRating:"LJ45-TR",image:"assets/images/placeholder-aircraft.svg" },
  { id:"AC-112",reg:"VT-JLL",model:"Falcon 2000LXS",manufacturer:"Dassault",category:"Heavy Jet",capacity:10,speed:862,range:7546,hourlyRate:520000,status:"Retired",typeRating:"F2000-TR",image:"assets/images/placeholder-aircraft.svg" }
];
const SEED_ROUTES = { domestic:[
    {code:"BOM",city:"Mumbai",lat:19.0896,lng:72.8656},
    {code:"DEL",city:"Delhi",lat:28.5562,lng:77.1000},
    {code:"BLR",city:"Bangalore",lat:13.1986,lng:77.7066},
    {code:"HYD",city:"Hyderabad",lat:17.2403,lng:78.4294},
    {code:"MAA",city:"Chennai",lat:12.9941,lng:80.1709},
    {code:"GOI",city:"Goa",lat:15.3808,lng:73.8314},
    {code:"AMD",city:"Ahmedabad",lat:23.0772,lng:72.6347},
    {code:"PNQ",city:"Pune",lat:18.5822,lng:73.9197}
  ] };

// External DGCA-style pilot license registry — the "existing JSON data structure" the
// mock Self-Fly license verification API checks entered license numbers against.
const SEED_PILOT_LICENSES = [
  { licenseNumber:"DGCA-CPL-70011", holderName:"Karan Shah", licenseClass:"Commercial Pilot License (CPL)", hoursOnRecord:340, status:"Active" },
  { licenseNumber:"DGCA-PPL-51204", holderName:"Ritika Bose", licenseClass:"Private Pilot License (PPL)", hoursOnRecord:150, status:"Active" },
  { licenseNumber:"DGCA-ATPL-2291", holderName:"Rohan Verma", licenseClass:"Airline Transport Pilot License (ATPL)", hoursOnRecord:8200, status:"Active" },
  { licenseNumber:"DGCA-CPL-88231", holderName:"Aditya Rao", licenseClass:"Commercial Pilot License (CPL)", hoursOnRecord:410, status:"Active" },
  { licenseNumber:"DGCA-PPL-11190", holderName:"Sameer Qureshi", licenseClass:"Private Pilot License (PPL)", hoursOnRecord:95, status:"Suspended" }
];

// Dummy Aadhaar registry — the mock verification database for booking passengers.
const SEED_AADHAAR_REGISTRY = [
  { aadhaarNumber:"123456789012", holderName:"Demo Customer", dob:"1990-06-15", gender:"Male", status:"Active" },
  { aadhaarNumber:"234567890123", holderName:"Karan Shah", dob:"1988-03-22", gender:"Male", status:"Active" },
  { aadhaarNumber:"345678901234", holderName:"Priya Nair", dob:"1992-11-08", gender:"Female", status:"Active" },
  { aadhaarNumber:"456789012345", holderName:"Arjun Malhotra", dob:"1985-01-30", gender:"Male", status:"Active" },
  { aadhaarNumber:"567890123456", holderName:"Ritika Bose", dob:"1995-07-19", gender:"Female", status:"Active" },
  { aadhaarNumber:"678901234567", holderName:"Sameer Qureshi", dob:"1979-09-05", gender:"Male", status:"Suspended" }
];

// Countries JetLease currently operates in, with their international dialing code.
// A phone country-code prompt is only shown for these — everywhere else, no code is asked for.
const SEED_COUNTRIES = [
  { name:"India", callingCode:"+91", serviceAvailable:true },
  { name:"United Arab Emirates", callingCode:"+971", serviceAvailable:true },
  { name:"Singapore", callingCode:"+65", serviceAvailable:true },
  { name:"United Kingdom", callingCode:"+44", serviceAvailable:true },
  { name:"United States", callingCode:"+1", serviceAvailable:true },
  { name:"Other", callingCode:"", serviceAvailable:false }
];

// Starting bank ledger — simulates the bank's own transaction records, independent of
// our own payments table, which the admin payment-verification mock API reconciles against.
const SEED_LEDGER = [];
const SEED_TESTIMONIALS = [
  {name:"Arjun Malhotra",role:"MD, Malhotra Textiles",quote:"JetLease turned a same-day Mumbai board crisis into a non-event. Wheels up in ninety minutes.",initials:"AM"},
  {name:"Priya Nair",role:"Founder, Nair Health Group",quote:"The lease desk handled our aircraft agreement end-to-end online — no paperwork chased across three cities.",initials:"PN"},
  {name:"Karan Shah",role:"Self-fly Member",quote:"Verification of my hours and license was fast, and the cost breakdown before I signed was completely transparent.",initials:"KS"}
];
const SEED_FAQ = [
  {q:"How fast can I book a charter?",a:"Domestic charters can be confirmed in as little as 90 minutes once payment and passenger details are verified."},
  {q:"Can I fly the aircraft myself?",a:"Yes, through Self-Fly. You'll need a minimum of 100 logged flying hours, a valid license number and class, and an uploaded certificate. Bookings below 100 hours are automatically rejected."},
  {q:"What documents do passengers need?",a:"Full name, date of birth, gender, and Aadhaar number for mock verification. Aadhaar is not required for children under 5 years and 15 days old."},
  {q:"How is the total charter cost calculated?",a:"Aircraft cost + pilot cost + crew cost + airport charges + fuel surcharge, with GST applied on the subtotal. The full breakdown is shown before you pay."},
  {q:"How do I pay?",a:"Bank transfer to the account shown on the payment page. After transferring, submit your transaction ID — our team verifies it and updates your booking status."},
  {q:"What is a lease agreement and how do I sign it?",a:"For qualifying bookings, a lease agreement is generated automatically. You can view, digitally sign, and download it from your dashboard; admin then approves it to finalize the booking."}
];

const SEED_PILOTS = [
  {id:"PLT-01",name:"Capt. Rohan Verma",licenseNumber:"DGCA-ATPL-2291",flyingHours:8200,remainingHours:60,typeRatings:["G650-TR","GL7500-TR"],certifications:["ATPL","CAT-II"],available:true},
  {id:"PLT-02",name:"Capt. Neha Kulkarni",licenseNumber:"DGCA-ATPL-3387",flyingHours:6100,remainingHours:45,typeRatings:["CH350-TR","CLAT-TR"],certifications:["ATPL"],available:true},
  {id:"PLT-03",name:"Capt. Imran Sheikh",licenseNumber:"DGCA-ATPL-4410",flyingHours:5400,remainingHours:38,typeRatings:["AW139-TR","B429-TR"],certifications:["ATPL","Helicopter Rating"],available:true},
  {id:"PLT-04",name:"Capt. Leela Menon",licenseNumber:"DGCA-ATPL-5522",flyingHours:9100,remainingHours:0,typeRatings:["CJ3-TR","PH300-TR"],certifications:["ATPL"],available:false}
];
const SEED_CREW = [
  {id:"CRW-01",name:"Sanya Kapoor",role:"Cabin Crew",dutyHours:120,remainingHours:50,available:true},
  {id:"CRW-02",name:"Devika Rao",role:"Cabin Crew",dutyHours:95,remainingHours:42,available:true},
  {id:"CRW-03",name:"Farhan Ali",role:"Flight Engineer",dutyHours:140,remainingHours:36,available:true},
  {id:"CRW-04",name:"Meera Iyer",role:"Cabin Crew",dutyHours:60,remainingHours:0,available:false}
];

/* ---------------- Seeding ---------------- */
async function tryFetchJSON(path){
  try{ const r = await fetch(path); if(!r.ok) throw 0; return await r.json(); }catch(e){ return null; }
}

async function seedDatabase(){
  if(localStorage.getItem(LS.seeded)) return;

  const aircraft = (await tryFetchJSON('data/aircraft.json')) || SEED_AIRCRAFT;
  const pilots = (await tryFetchJSON('data/pilots.json')) || SEED_PILOTS;
  const crew = (await tryFetchJSON('data/crew.json')) || SEED_CREW;
  const ledger = (await tryFetchJSON('data/bank-ledger.json')) || SEED_LEDGER;
  lsSet(LS.aircraft, aircraft);
  lsSet(LS.crew, crew);
  lsSet(LS.pilots, pilots);
  lsSet(LS.ledger, ledger);
  lsSet(LS.bookings, []);
  lsSet(LS.payments, []);
  lsSet(LS.leases, []);
  lsSet(LS.notifications, []);
  lsSet(LS.audit, []);
  lsSet(LS.contactMessages, []);
  lsSet(LS.reports, []);

  const adminUser = {
    id:'ADM-0001', fullName:'JetLease Admin', email:'admin@jetlease.in', phone:'9800000000',
    dob:'1985-01-01', country:'India', password:'Admin@123', emergencyContact:'',
    role:'admin', status:'active', membership:'none', loyaltyPoints:0, createdAt: nowISO()
  };
  const demoCustomer = {
    id:'CUS-0001', fullName:'Demo Customer', email:'demo@jetlease.in', phone:'9123456780',
    dob:'1990-06-15', country:'India', password:'Demo@123', emergencyContact:'9988776655',
    role:'customer', status:'active', membership:'gold', loyaltyPoints:1250, createdAt: nowISO()
  };
  lsSet(LS.users, [adminUser, demoCustomer]);
  localStorage.setItem(LS.seeded, '1');
}

/* ---------------- Generic collection helpers ---------------- */
const DB = {
  users:      { all:()=>lsGet(LS.users,[]),      save:(a)=>lsSet(LS.users,a) },
  aircraft:   { all:()=>lsGet(LS.aircraft,[]),   save:(a)=>lsSet(LS.aircraft,a) },
  bookings:   { all:()=>lsGet(LS.bookings,[]),   save:(a)=>lsSet(LS.bookings,a) },
  payments:   { all:()=>lsGet(LS.payments,[]),   save:(a)=>lsSet(LS.payments,a) },
  leases:     { all:()=>lsGet(LS.leases,[]),     save:(a)=>lsSet(LS.leases,a) },
  notifications:{ all:()=>lsGet(LS.notifications,[]), save:(a)=>lsSet(LS.notifications,a) },
  crew:       { all:()=>lsGet(LS.crew,[]),       save:(a)=>lsSet(LS.crew,a) },
  pilots:     { all:()=>lsGet(LS.pilots,[]),     save:(a)=>lsSet(LS.pilots,a) },
  audit:      { all:()=>lsGet(LS.audit,[]),      save:(a)=>lsSet(LS.audit,a) },
  ledger:     { all:()=>lsGet(LS.ledger,[]),     save:(a)=>lsSet(LS.ledger,a) },
  contactMessages: { all:()=>lsGet(LS.contactMessages,[]), save:(a)=>lsSet(LS.contactMessages,a) },
  reports:    { all:()=>lsGet(LS.reports,[]),    save:(a)=>lsSet(LS.reports,a) },
};

function addAudit(actor, category, action, details){
  const rows = DB.audit.all();
  rows.unshift({ id:uid('AUD'), actor, category, action, details: details||'', timestamp: nowISO() });
  DB.audit.save(rows.slice(0,500));
}

function addNotification(userEmail, title, message, type){
  const rows = DB.notifications.all();
  rows.unshift({ id:uid('NTF'), userEmail, title, message, type: type||'info', read:false, createdAt: nowISO() });
  DB.notifications.save(rows);
}

/* ---------------- Session ---------------- */
function getSession(){ return lsGet(LS.session, null); }
function setSession(userEmail, role){ localStorage.setItem(LS.session, JSON.stringify({ userEmail, role, at: nowISO() })); }
function clearSession(){ localStorage.removeItem(LS.session); }
function currentUser(){
  const s = getSession(); if(!s) return null;
  return DB.users.all().find(u=>u.email===s.userEmail) || null;
}

/* ---------------- Business Logic: Costing ---------------- */
// Real-world grounded assumptions:
// - Charter services in India attract 5% GST (non-scheduled air transport).
// - Estimated block hours derived from great-circle-ish distance heuristics per route type.
const RATES = {
  pilotRatePerHour: 45000,
  crewRatePerHour: 12000,
  airportCharges: { domestic: 35000, helicopter: 18000 },
  fuelSurchargePct: 0.08,
  gstPct: 0.05,
  selfFlySafetyPilotFactor: 0.5 // safety pilot still required, at half the normal pilot cost
};

function estimateBlockHours(bookingType, tripType){
  let hrs = bookingType === 'Helicopter Charter' ? 1 : 1.5;
  if(tripType === 'Round Trip') hrs *= 2;
  return hrs;
}

function calculateCost({ aircraft, bookingType, tripType, selfFly }){
  const hours = estimateBlockHours(bookingType, tripType);
  const aircraftCost = Math.round(aircraft.hourlyRate * hours);
  const pilotCost = selfFly
    ? Math.round(RATES.pilotRatePerHour * hours * RATES.selfFlySafetyPilotFactor)
    : Math.round(RATES.pilotRatePerHour * hours);
  const crewCost = Math.round(RATES.crewRatePerHour * hours * 2); // 2 cabin crew baseline
  const key = bookingType === 'Helicopter Charter' ? 'helicopter' : 'domestic';
  const airportCharges = RATES.airportCharges[key];
  const fuelSurcharge = Math.round(aircraftCost * RATES.fuelSurchargePct);
  const subtotal = aircraftCost + pilotCost + crewCost + airportCharges + fuelSurcharge;
  const gst = Math.round(subtotal * RATES.gstPct);
  const total = subtotal + gst;
  return { hours, aircraftCost, pilotCost, crewCost, airportCharges, fuelSurcharge, gst, total, subtotal };
}

/* ---------------- Business Logic: Self-Fly Validation ---------------- */
function validateSelfFly({ flyingHours, licenseNumber, licenseClass, certificateFileName, dgcaDeclaration }){
  const errors = [];
  if(!licenseNumber || licenseNumber.trim().length < 4) errors.push('A valid pilot license number is required.');
  if(!licenseClass) errors.push('License class is required.');
  if(!certificateFileName) errors.push('Certificate upload is required.');
  if(!dgcaDeclaration) errors.push('DGCA declaration must be accepted.');
  const hrs = Number(flyingHours);
  if(isNaN(hrs) || hrs < 100){
    errors.push('Minimum 100 logged flying hours are required for Self-Fly. Booking rejected below this threshold.');
  }
  return { valid: errors.length === 0, errors, flyingHours: hrs };
}

/* ---------------- Currency ---------------- */
// INR is the only currency the platform operates in — kept as a function (rather than
// inlining ₹ everywhere) so every price in the app still renders through one place.
function convert(amountINR){
  return '₹' + Number(amountINR).toLocaleString('en-IN', { maximumFractionDigits: 0 });
}

/* ---------------- Aircraft Recommendation Engine ---------------- */
// `category` narrows results to a single aircraft category (e.g. 'Helicopter' for a
// Helicopter Charter booking); pass null to allow any non-helicopter category (jets/turboprops).
function recommendAircraft({ passengers, budgetINR, distanceKm, category }){
  const fleet = DB.aircraft.all().filter(a => a.status === 'Available');
  const categoryFiltered = fleet.filter(a => category ? a.category === category : a.category !== 'Helicopter');
  const scored = categoryFiltered
    .filter(a => a.capacity >= Number(passengers) && a.range >= Number(distanceKm))
    .map(a => {
      const hours = distanceKm / a.speed;
      const estCost = Math.round(a.hourlyRate * hours * 1.2); // rough all-in multiplier incl. surcharges
      return { ...a, estCost, hours };
    })
    .sort((a,b) => a.estCost - b.estCost);

  const withinBudget = scored.filter(a => a.estCost <= Number(budgetINR));
  const best = withinBudget.length ? withinBudget[withinBudget.length-1] : (scored[0] || null);
  const alternatives = scored.filter(a => a.id !== best?.id).slice(0,3);
  return { best, alternatives, all: scored };
}

/* ---------------- Password strength ---------------- */
function passwordStrength(pw){
  let score = 0;
  if(pw.length >= 8) score++;
  if(/[A-Z]/.test(pw)) score++;
  if(/[a-z]/.test(pw)) score++;
  if(/[0-9]/.test(pw)) score++;
  if(/[^A-Za-z0-9]/.test(pw)) score++;
  const levels = ['Very Weak','Weak','Fair','Good','Strong','Very Strong'];
  return { score, label: levels[score], pct: (score/5)*100 };
}

/* ---------------- Booking status flow ---------------- */
const BOOKING_FLOW = ['Draft','Pending Payment','Pending Verification','Payment Verified','Lease Pending','Lease Signed','Approved','Dispatched','Completed'];
const TERMINAL_NEGATIVE = ['Cancelled','Rejected'];

function statusClass(status){
  return 'st-' + String(status).toLowerCase().replace(/\s+/g,'_');
}

/* ---------------- Lease creation (shared by payment verification flow) ---------------- */
function ensureLeaseForBooking(booking){
  const leases = DB.leases.all();
  if(leases.some(l => l.bookingId === booking.id)) return leases.find(l => l.bookingId === booking.id);
  const lease = {
    id: uid('LSE'),
    bookingId: booking.id,
    userEmail: booking.userEmail,
    status: 'Sent',
    signedBy: null,
    signedDate: null,
    approvalDate: null,
    createdAt: nowISO()
  };
  leases.push(lease);
  DB.leases.save(leases);
  addNotification(booking.userEmail, 'Lease Agreement Ready', `Your lease agreement for booking ${booking.id} is ready to review and sign.`, 'info');
  return lease;
}

/* ---------------- Passenger Date of Birth range ---------------- */
// A passenger must be at least 15 days old (no newborns/future dates) and no more than
// 100 years old. Returns ISO date strings usable directly as <input type="date"> min/max.
function dobRange(){
  const today = new Date();
  const maxDate = new Date(today); maxDate.setDate(maxDate.getDate() - 15);
  const minDate = new Date(today); minDate.setFullYear(minDate.getFullYear() - 100);
  const fmt = (d) => d.toISOString().split('T')[0];
  return { min: fmt(minDate), max: fmt(maxDate) };
}

/* ---------------- Route distance (for aircraft recommendation) ---------------- */
// Haversine great-circle distance between two lat/lng points, in kilometres.
function haversineKm(lat1, lon1, lat2, lon2){
  const R = 6371;
  const toRad = (d) => d * Math.PI / 180;
  const dLat = toRad(lat2 - lat1), dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat/2)**2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon/2)**2;
  return Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
}
// Looks up two route codes (e.g. "BOM", "DXB") in the routes dataset and returns the
// straight-line distance between them in km. Returns null if either city isn't found.
function routeDistanceKm(originCode, destCode, routesData){
  const all = routesData?.domestic || [];
  const o = all.find(r => r.code === originCode);
  const d = all.find(r => r.code === destCode);
  if(!o || !d || o.code === d.code) return null;
  return haversineKm(o.lat, o.lng, d.lat, d.lng);
}

/* ---------------- Age helpers ---------------- */
// Shared by registration (18+), dashboard profile editing (18+), and the Aadhaar
// exemption rule for young children — one source of truth instead of duplicated math.
function isAdult(dobStr){
  if(!dobStr) return false;
  const dob = new Date(dobStr);
  if(isNaN(dob)) return false;
  const diff = Date.now() - dob.getTime();
  const age = new Date(diff).getUTCFullYear() - 1970;
  return age >= 18;
}
// Aadhaar is not mandatory for a passenger under 5 years + 15 days old.
function isAadhaarExempt(dobStr){
  if(!dobStr) return false;
  const dob = new Date(dobStr);
  if(isNaN(dob)) return false;
  const threshold = new Date(dob);
  threshold.setFullYear(threshold.getFullYear() + 5);
  threshold.setDate(threshold.getDate() + 15);
  return new Date() < threshold;
}

/* ---------------- Mock API: Aadhaar Verification ---------------- */
// Checks an entered Aadhaar number against the dummy registry (data/aadhaar-registry.json).
// Per policy, a failed/missing verification never blocks the booking — it's informational.
async function mockVerifyAadhaarAPI(aadhaarNumber){
  const registry = (await tryFetchJSON('data/aadhaar-registry.json')) || SEED_AADHAAR_REGISTRY;
  await new Promise(resolve => setTimeout(resolve, 700)); // simulate network latency
  const record = registry.find(r => r.aadhaarNumber === String(aadhaarNumber||'').trim());
  if(!record) return { verified:false, message:'Aadhaar number not found in the registry.' };
  if(record.status !== 'Active') return { verified:false, message:`Aadhaar found but its status is "${record.status}", not Active.`, record };
  return { verified:true, message:`Aadhaar verified — registered to ${record.holderName}.`, record };
}

/* ---------------- Form field validators ---------------- */
// Shared, reusable validation rules. Each returns '' when the value is valid, or a
// user-friendly error message when it isn't — used across registration, booking
// passenger forms, the contact form, and the dashboard profile form.
const VALIDATORS = {
  name(v){
    const val = (v||'').trim();
    if(!val) return 'This field is required.';
    if(!/^[A-Za-z][A-Za-z\s.'-]{1,49}$/.test(val)) return 'Only letters are allowed.';
    return '';
  },
  email(v){
    const val = (v||'').trim();
    if(!val) return 'Email is required.';
    if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) return 'Enter a valid email address.';
    return '';
  },
  phone10(v){
    const val = (v||'').trim();
    if(!val) return 'Phone number is required.';
    if(!/^[0-9]{10}$/.test(val)) return 'Enter a valid 10-digit phone number (numbers only).';
    return '';
  },
  aadhaar(v){
    const val = (v||'').trim();
    if(!val) return 'Aadhaar number is required.';
    if(!/^[0-9]{12}$/.test(val)) return 'Aadhaar number must be exactly 12 digits.';
    return '';
  },
  licenseNumber(v){
    const val = (v||'').trim();
    if(!val) return 'License number is required.';
    if(!/^[A-Za-z0-9-]{4,20}$/.test(val)) return 'Enter a valid license number (letters, numbers, hyphens only).';
    return '';
  },
  requiredDate(v){
    if(!v) return 'This date is required.';
    return '';
  },
  message(v){
    const val = (v||'').trim();
    if(!val) return 'Message is required.';
    if(val.length < 10) return 'Message must be at least 10 characters.';
    return '';
  },
  dob(v){
    if(!v) return 'Date of birth is required.';
    const { min, max } = dobRange();
    if(v < min) return 'Age cannot be more than 100 years.';
    if(v > max) return 'Passenger must be at least 15 days old — future dates are not allowed.';
    return '';
  }
};

/* ---------------- Booking status: which stages count as "active" ---------------- */
// Used by crew/pilot assignment: admins may only assign staff to bookings still in
// progress, not to ones already finished or dead-ended.
const ACTIVE_BOOKING_STATUSES = ['Pending Payment','Pending Verification','Payment Verified','Lease Pending','Lease Signed','Approved','Dispatched'];
function isActiveBooking(status){ return ACTIVE_BOOKING_STATUSES.includes(status); }

// Statuses in which a customer may still self-cancel: payment has cleared, but the
// lease has not yet been signed (Requirement 16).
const CANCELLABLE_BOOKING_STATUSES = ['Payment Verified','Lease Pending'];
function isCancellableBooking(status){ return CANCELLABLE_BOOKING_STATUSES.includes(status); }

// Statuses in which the customer still needs to submit (or resubmit) a payment.
// 'Payment Rejected' is distinct from the initial 'Pending Payment' so the booking
// history clearly shows a payment attempt failed, rather than looking unchanged.
const PAYABLE_BOOKING_STATUSES = ['Pending Payment','Payment Rejected'];
function isPayableBooking(status){ return PAYABLE_BOOKING_STATUSES.includes(status); }

// A booking still counts as "upcoming" unless it reached a true end state. Used by both
// the dashboard KPI count and the Overview panel's Upcoming Flights list, so the two
// can never disagree (Requirement 7 fix).
const ENDED_BOOKING_STATUSES = ['Completed','Cancelled','Rejected'];
function isUpcomingBooking(status){ return !ENDED_BOOKING_STATUSES.includes(status); }

// Shared cleanup used whenever a booking is rejected, cancelled, or its lease rejected:
// frees the aircraft back to Available, and refunds any pilot/crew hours already
// committed to that booking. Consolidates logic that used to be duplicated between
// booking rejection and lease rejection in the admin console.
function releaseBookingResources(booking){
  const fleet = DB.aircraft.all();
  const fi = fleet.findIndex(a => a.id === booking.aircraftId);
  if(fi > -1 && fleet[fi].status === 'Booked'){ fleet[fi].status = 'Available'; DB.aircraft.save(fleet); }

  if(booking.assignedPilotId){
    const pilots = DB.pilots.all();
    const pi = pilots.findIndex(p => p.id === booking.assignedPilotId);
    if(pi > -1){ pilots[pi].remainingHours += booking.cost.hours; DB.pilots.save(pilots); }
  }
  if(booking.assignedCrewIds && booking.assignedCrewIds.length){
    const crew = DB.crew.all();
    booking.assignedCrewIds.forEach(cid => {
      const ci = crew.findIndex(c => c.id === cid);
      if(ci > -1) crew[ci].remainingHours += booking.cost.hours;
    });
    DB.crew.save(crew);
  }
}

/* ---------------- Mock OTP (shared by registration, login, forgot password, phone updates) ---------------- */
const MOCK_OTP_CODE = '123456';
function verifyMockOTP(code){ return String(code||'').trim() === MOCK_OTP_CODE; }

/* ---------------- Mock API: Pilot License Verification ---------------- */
// Simulates a real DGCA license-lookup API call (network delay + JSON registry lookup)
// for the Self-Fly module. Checks the entered license number against the pilot-licenses
// registry loaded from /data/pilot-licenses.json (falls back to SEED_PILOT_LICENSES).
async function mockVerifyPilotLicenseAPI(licenseNumber){
  const registry = (await tryFetchJSON('data/pilot-licenses.json')) || SEED_PILOT_LICENSES;
  await new Promise(resolve => setTimeout(resolve, 800)); // simulate network latency
  const record = registry.find(r => r.licenseNumber.toLowerCase() === String(licenseNumber||'').trim().toLowerCase());
  if(!record) return { verified:false, message:'License number not found in the DGCA registry.' };
  if(record.status !== 'Active') return { verified:false, message:`License found but its status is "${record.status}", not Active.`, record };
  return { verified:true, message:`License verified — registered to ${record.holderName}, ${record.hoursOnRecord.toLocaleString()} hours on record.`, record };
}

/* ---------------- Mock API: Payment (Bank Ledger) Verification ---------------- */
// When a customer submits a transaction ID, a matching "cleared" record is written to the
// bank ledger (simulating the bank's own independent system). When an admin clicks Verify,
// this function performs the reconciliation check — a genuine two-sided mock API pattern.
function recordLedgerEntry(payment){
  const ledger = DB.ledger.all();
  ledger.push({ transactionId: payment.transactionId, bookingId: payment.bookingId, amount: payment.amount, status:'CLEARED', clearedAt: nowISO() });
  DB.ledger.save(ledger);
}
async function mockVerifyPaymentAPI(payment){
  await new Promise(resolve => setTimeout(resolve, 900)); // simulate network latency
  const ledger = DB.ledger.all();
  const match = ledger.find(r => r.transactionId === payment.transactionId && r.bookingId === payment.bookingId);
  if(!match) return { verified:false, message:'Transaction ID not found in the bank ledger.' };
  if(match.amount !== payment.amount) return { verified:false, message:'Transaction found, but the settled amount does not match the invoice.' };
  return { verified:true, message:`Bank ledger confirms this transaction cleared for ₹${match.amount.toLocaleString('en-IN')} on ${match.clearedAt}.` };
}

/* ---------------- Serviceable countries / phone country codes ---------------- */
// A calling-code prefix is only ever shown for countries JetLease actually operates in.
async function getCountryInfo(countryName){
  const countries = (await tryFetchJSON('data/countries.json')) || SEED_COUNTRIES;
  return countries.find(c => c.name === countryName) || null;
}
