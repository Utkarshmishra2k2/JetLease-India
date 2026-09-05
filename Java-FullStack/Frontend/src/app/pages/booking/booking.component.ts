import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CatalogService } from '../../core/services/catalog.service';
import { BookingService } from '../../core/services/booking.service';
import { ToastService } from '../../core/services/toast.service';
import { Aircraft, Route as RouteModel, Recommendation } from '../../core/models/models';
import { VALIDATORS, todayISO, isAadhaarExempt, dobRange, fmtINR, statusClass, validateSelfFly } from '../../core/util/validators';
import { calculateCost } from '../../core/util/cost-calculator';

interface PaxRow {
  name: string;
  dob: string;
  gender: string;
  aadhaar: string;
  verificationStatus: string;
  aadhaarAutoFilled: boolean;
  noAadhaar: boolean;
  altDocumentId: string;
}

/** Exact port of booking.html + booking.js — the 4-step charter booking wizard. */
@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking.component.html',
})
export class BookingComponent implements OnInit {
  private catalog = inject(CatalogService);
  private bookingService = inject(BookingService);
  private toast = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  step = 1;
  today = todayISO();

  // Step 1 state
  type: 'Domestic Charter' | 'Helicopter Charter' = 'Domestic Charter';
  tripType: 'One Way' | 'Round Trip' = 'One Way';
  origin = '';
  destination = '';
  date = '';
  time = '';
  returnDate = '';
  returnTime = '';
  returnDateMin = '';
  pax = 2;
  aircraftId: string | null = null;

  routes: RouteModel[] = [];
  fleet: Aircraft[] = [];
  filteredFleet: Aircraft[] = [];

  recBudget = 800000;
  recDistanceKm = 0;
  recDistanceHint = 'Select an origin and destination above — distance is calculated automatically from the route.';
  recResult: { best: Recommendation | null; alternatives: Recommendation[] } | null = null;

  // Step 2 state
  passengers: PaxRow[] = [];
  dobMin = '';
  dobMax = '';

  // Step 3 state
  selfFly = false;
  flyingHours: number | null = null;
  flyingHoursReadonly = false;
  licenseNumber = '';
  licenseVerified = false;
  licenseVerifyStatus = '';
  licenseVerifyOk = false;
  licenseClass = '';
  licenseClassDisabled = false;
  certificateFileName = '';
  autoFilledFromRegistry = false;
  dgcaDeclaration = false;
  selfFlyErrors: string[] = [];

  // Step 4 state
  cost = { hours: 0, aircraftCost: 0, pilotCost: 0, crewCost: 0, airportCharges: 0, fuelSurcharge: 0, gst: 0, total: 0 };
  bookingErrors: string[] = [];
  submitting = false;

  errors: Record<string, string> = {};
  fmtINR = fmtINR;
  statusClass = statusClass;

  ngOnInit(): void {
    const { min, max } = dobRange();
    this.dobMin = min;
    this.dobMax = max;

    this.catalog.routes().subscribe((r) => {
      this.routes = r;
      if (this.routes.length) {
        this.origin = this.routes[0].code;
        this.destination = this.routes[Math.min(1, this.routes.length - 1)].code;
      }
      this.updateRecommendationDistance();
    });

    this.catalog.aircraft().subscribe((f) => {
      this.fleet = f;
      this.renderFleetOptions();
    });

    const qs = this.route.snapshot.queryParamMap;
    if (qs.get('aircraft')) this.aircraftId = qs.get('aircraft');
    this.syncReturnDateMin();
  }

  fieldClass(key: string): string {
    if (!(key in this.errors)) return '';
    return this.errors[key] ? 'invalid' : 'valid';
  }

  onTypeChange() {
    this.renderFleetOptions();
    this.updateRecommendationDistance();
  }

  onTripTypeChange() {
    if (this.tripType !== 'Round Trip') {
      this.returnDate = '';
      this.returnTime = '';
    }
  }

  syncReturnDateMin() {
    const floor = this.date || this.today;
    this.returnDateMin = floor;
    if (this.returnDate && this.returnDate < floor) this.returnDate = '';
  }

  updateRecommendationDistance() {
    if (!this.origin || !this.destination || this.origin === this.destination) {
      this.recDistanceKm = 0;
      this.recDistanceHint = 'Select an origin and destination above — distance is calculated automatically from the route.';
      return;
    }
    this.catalog.distance(this.origin, this.destination).subscribe({
      next: (res) => {
        this.recDistanceKm = res.distanceKm;
        this.recDistanceHint = `Auto-calculated straight-line distance for ${this.origin} \u2192 ${this.destination}: ${res.distanceKm.toLocaleString()} km.`;
      },
      error: () => {
        this.recDistanceKm = 0;
      },
    });
  }

  private categoryForType(): string | null {
    return this.type === 'Helicopter Charter' ? 'Helicopter' : null;
  }

  renderFleetOptions() {
    const cat = this.categoryForType();
    this.filteredFleet = this.fleet.filter(
      (a) => a.status === 'Available' && (cat ? a.category === cat : a.category !== 'Helicopter')
    );
  }

  selectAircraft(id: string) {
    this.aircraftId = id;
  }

  runRecommendation() {
    if (!this.recDistanceKm) {
      this.toast.error('Select an origin and destination first so the distance can be calculated.');
      return;
    }
    const category = this.categoryForType() || undefined;
    this.catalog.recommend({ pax: this.pax, budget: this.recBudget, distanceKm: this.recDistanceKm, category }).subscribe((res) => {
      this.recResult = res;
      if (!res.best) {
        this.toast.error(`No ${category ? category.toLowerCase() : 'jet/turboprop'} aircraft matches this passenger count and range.`);
      }
    });
  }

  pickRecommended(id: string) {
    this.selectAircraft(id);
  }

  goStep(n: number) {
    if (n === 2 && !this.aircraftId) {
      this.toast.error('Select an aircraft first.');
      return;
    }

    if (n === 2) {
      let ok = true;
      const errors: Record<string, string> = {};
      const paxValid = this.pax >= 1 && this.pax <= 14;
      if (!paxValid) ok = false;
      errors['bkPax'] = paxValid ? '' : 'Add at least 1 passenger to continue (maximum 14 per booking).';
      if (!this.date) ok = false;
      errors['bkDate'] = this.date ? '' : 'Select a valid departure date.';
      if (!this.time) ok = false;
      errors['bkTime'] = this.time ? '' : 'Select a departure time.';
      if (this.tripType === 'Round Trip') {
        const returnValid = !!this.returnDate && this.returnDate >= this.date;
        if (!returnValid) ok = false;
        errors['returnDate'] = returnValid ? '' : 'Return date cannot be before the departure date.';
      }
      this.errors = errors;
      if (!ok) {
        if (!paxValid) {
          this.toast.error(this.pax < 1 ? 'Please add at least 1 passenger to continue.' : 'A maximum of 14 passengers is allowed per booking.');
        } else {
          this.toast.error('Select a valid departure date/time and a return date on or after the departure date.');
        }
        return;
      }
    }

    this.step = n;
    if (n === 2) this.renderPassengerForm();
    if (n === 4) this.renderCostReview();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /* ---------------- PASSENGERS ---------------- */
  private passengerTemplate(): PaxRow {
    return { name: '', dob: '', gender: 'Male', aadhaar: '', verificationStatus: 'Pending', aadhaarAutoFilled: false, noAadhaar: false, altDocumentId: '' };
  }

  renderPassengerForm() {
    if (this.passengers.length === 0) {
      for (let i = 0; i < this.pax; i++) this.passengers.push(this.passengerTemplate());
    }
  }

  isExempt(p: PaxRow): boolean {
    return isAadhaarExempt(p.dob);
  }

  addPassengerRow() {
    if (this.passengers.length >= 14) {
      this.toast.error('A maximum of 14 passengers is allowed per booking.');
      return;
    }
    this.passengers.push(this.passengerTemplate());
    this.pax = this.passengers.length;
  }

  removePassengerRow(i: number) {
    this.passengers.splice(i, 1);
    this.pax = this.passengers.length;
  }

  clearAadhaarAutoFill(i: number) {
    this.passengers[i].aadhaarAutoFilled = false;
  }

  onNoAadhaarToggle(i: number, checked: boolean) {
    const p = this.passengers[i];
    p.noAadhaar = checked;
    if (checked) {
      p.aadhaar = '';
      p.aadhaarAutoFilled = false;
      p.verificationStatus = 'Not Applicable';
    } else {
      p.altDocumentId = '';
      p.verificationStatus = 'Pending';
    }
  }

  onAadhaarInput(i: number) {
    const p = this.passengers[i];
    const exempt = isAadhaarExempt(p.dob);
    const err = exempt && !p.aadhaar ? '' : VALIDATORS.aadhaar(p.aadhaar);
    this.errors[`pax-aadhaar-${i}`] = err;
    if (!err && p.aadhaar.length === 12) {
      this.verifyPassenger(i);
    }
  }

  verifyPassenger(i: number) {
    const p = this.passengers[i];
    const exempt = isAadhaarExempt(p.dob);

    if (exempt && !p.aadhaar) {
      p.verificationStatus = 'Not Required';
      p.aadhaarAutoFilled = false;
      this.toast.success(`Passenger ${i + 1} is under the Aadhaar exemption age — no verification required.`);
      return;
    }
    if (!p.aadhaar) {
      this.errors[`pax-aadhaar-${i}`] = 'Aadhaar number is required.';
      this.toast.error('Enter an Aadhaar number to verify.');
      return;
    }

    p.verificationStatus = 'Verifying';
    this.bookingService.verifyAadhaar(p.aadhaar).subscribe((result) => {
      p.verificationStatus = result.verified ? 'Verified' : 'Not Verified';
      if (result.verified) {
        if (result.holderName) p.name = result.holderName;
        if (result.dob) p.dob = result.dob;
        if (result.gender) p.gender = result.gender;
        p.aadhaarAutoFilled = true;
      } else {
        p.aadhaarAutoFilled = false;
      }
      this.toast[result.verified ? 'success' : 'error'](
        result.verified ? `Passenger ${i + 1}: details auto-filled from the Aadhaar record.` : `Passenger ${i + 1}: ${result.message}`
      );
    });
  }

  submitPassengers() {
    let allValid = true;
    const errors: Record<string, string> = {};
    this.passengers.forEach((p, i) => {
      const nameErr = VALIDATORS.name(p.name);
      errors[`pax-name-${i}`] = nameErr;
      if (nameErr) allValid = false;
      const dobErr = VALIDATORS.dob(p.dob);
      errors[`pax-dob-${i}`] = dobErr;
      if (dobErr) allValid = false;

      if (!p.noAadhaar) {
        const exempt = isAadhaarExempt(p.dob);
        const aadhaarErr = exempt && !p.aadhaar ? '' : VALIDATORS.aadhaar(p.aadhaar);
        errors[`pax-aadhaar-${i}`] = aadhaarErr;
        if (aadhaarErr) allValid = false;
      }
    });
    this.errors = errors;
    if (!allValid) {
      this.toast.error('Please fix the highlighted passenger fields.');
      return;
    }
    this.goStep(3);
  }

  /* ---------------- SELF-FLY ---------------- */
  onSelfFlyToggle(checked: boolean) {
    this.selfFly = checked;
  }

  onLicenseNumberInput() {
    this.licenseVerified = false;
    this.licenseVerifyStatus = '';
    this.clearSelfFlyAutoFill();
  }

  verifyPilotLicense() {
    const num = this.licenseNumber.trim();
    if (!num) {
      this.errors['licenseNumber'] = 'License number is required.';
      return;
    }
    this.licenseVerifyStatus = 'Verifying with DGCA registry\u2026';
    this.bookingService.verifyLicense(num).subscribe((result) => {
      this.licenseVerified = result.verified;
      this.licenseVerifyStatus = result.message;
      this.licenseVerifyOk = result.verified;
      this.errors['licenseNumber'] = result.verified ? '' : '';

      if (result.verified) {
        this.flyingHours = result.hoursOnRecord ?? null;
        this.licenseClass = result.licenseClass ?? '';
        this.flyingHoursReadonly = true;
        this.licenseClassDisabled = true;
        this.autoFilledFromRegistry = true;
      } else {
        this.clearSelfFlyAutoFill();
      }
      this.toast[result.verified ? 'success' : 'error'](
        result.verified ? 'Pilot license verified — details auto-filled.' : 'License not found in registry — please fill details manually.'
      );
    });
  }

  clearSelfFlyAutoFill() {
    this.flyingHoursReadonly = false;
    this.licenseClassDisabled = false;
    this.autoFilledFromRegistry = false;
  }

  onCertificateFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.certificateFileName = input.files?.[0]?.name || '';
  }

  submitCrewStep() {
    this.selfFlyErrors = [];
    if (this.selfFly) {
      const details = {
        flyingHours: this.flyingHours ?? 0,
        licenseNumber: this.licenseNumber,
        licenseClass: this.licenseClass,
        certificateFileName: this.certificateFileName,
        dgcaDeclaration: this.dgcaDeclaration,
      };
      const result = validateSelfFly(details);
      if (!result.valid) {
        this.selfFlyErrors = result.errors;
        this.toast.error('Self-Fly requirements not met — see details above.');
        return;
      }
    }
    this.goStep(4);
  }

  /* ---------------- COST REVIEW ---------------- */
  get selectedAircraft(): Aircraft | undefined {
    return this.fleet.find((a) => a.id === this.aircraftId);
  }

  renderCostReview() {
    const aircraft = this.selectedAircraft;
    if (!aircraft) return;
    this.cost = calculateCost(aircraft.hourlyRate, this.type, this.tripType, this.selfFly, this.recDistanceKm, aircraft.speed);
  }

  confirmBooking() {
    const aircraft = this.selectedAircraft;
    if (!aircraft) return;
    this.bookingErrors = [];
    this.submitting = true;

    const payload = {
      type: this.type,
      tripType: this.tripType,
      origin: this.origin,
      destination: this.destination,
      date: this.date,
      time: this.time,
      returnDate: this.returnDate || null,
      returnTime: this.returnTime || null,
      pax: this.pax,
      aircraftId: aircraft.id,
      selfFly: this.selfFly,
      selfFlyDetails: this.selfFly
        ? {
            licenseNumber: this.licenseNumber,
            licenseClass: this.licenseClass,
            flyingHours: this.flyingHours,
            dgcaDeclaration: this.dgcaDeclaration,
            verified: this.licenseVerified,
            certificateFileName: this.certificateFileName,
          }
        : null,
      passengers: this.passengers.map((p) => ({
        name: p.name,
        dob: p.dob,
        gender: p.gender,
        aadhaar: p.noAadhaar ? '' : p.aadhaar,
        verificationStatus: p.noAadhaar ? 'Not Applicable' : p.verificationStatus,
        noAadhaar: p.noAadhaar,
        altDocumentId: p.altDocumentId,
      })),
    };

    this.bookingService.create(payload).subscribe({
      next: (booking) => {
        this.toast.success('Booking created — proceed to payment.');
        setTimeout(() => this.router.navigate(['/payment', booking.id]), 400);
      },
      error: (err) => {
        this.submitting = false;
        this.bookingErrors = [err?.error?.message || 'Something went wrong creating your booking. Please try again.'];
      },
    });
  }
}
