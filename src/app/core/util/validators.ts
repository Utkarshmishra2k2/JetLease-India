/**
 * Client-side mirror of backend Validators.java / the reference app's VALIDATORS
 * object (js/data.js). Each function returns '' when valid, or the exact same
 * user-facing error message used throughout the reference UI.
 */
export const VALIDATORS = {
  name(v: string | null | undefined): string {
    const val = (v || '').trim();
    if (!val) return 'This field is required.';
    if (!/^[A-Za-z][A-Za-z\s.'-]{1,49}$/.test(val)) return 'Only letters are allowed.';
    return '';
  },
  email(v: string | null | undefined): string {
    const val = (v || '').trim();
    if (!val) return 'Email is required.';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) return 'Enter a valid email address.';
    return '';
  },
  phone10(v: string | null | undefined): string {
    const val = (v || '').trim();
    if (!val) return 'Phone number is required.';
    if (!/^[0-9]{10}$/.test(val)) return 'Enter a valid 10-digit phone number (numbers only).';
    return '';
  },
  aadhaar(v: string | null | undefined): string {
    const val = (v || '').trim();
    if (!val) return 'Aadhaar number is required.';
    if (!/^[0-9]{12}$/.test(val)) return 'Aadhaar number must be exactly 12 digits.';
    return '';
  },
  licenseNumber(v: string | null | undefined): string {
    const val = (v || '').trim();
    if (!val) return 'License number is required.';
    if (!/^[A-Za-z0-9-]{4,20}$/.test(val)) return 'Enter a valid license number (letters, numbers, hyphens only).';
    return '';
  },
  message(v: string | null | undefined): string {
    const val = (v || '').trim();
    if (!val) return 'Message is required.';
    if (val.length < 10) return 'Message must be at least 10 characters.';
    return '';
  },
  requiredDate(v: string | null | undefined): string {
    if (!v) return 'This date is required.';
    return '';
  },
  dob(v: string | null | undefined): string {
    if (!v) return 'Date of birth is required.';
    const d = new Date(v);
    if (isNaN(d.getTime())) return 'Enter a valid date.';
    const { min, max } = dobRange();
    if (v < min || v > max) return 'Age must be between 15 days and 100 years — future dates are not allowed.';
    return '';
  },
};

export function validateSelfFly(details: {
  flyingHours: string | number;
  licenseNumber: string;
  licenseClass: string;
  certificateFileName: string;
  dgcaDeclaration: boolean;
}): { valid: boolean; errors: string[]; flyingHours: number } {
  const errors: string[] = [];
  if (!details.licenseNumber || details.licenseNumber.trim().length < 4) errors.push('A valid pilot license number is required.');
  if (!details.licenseClass) errors.push('License class is required.');
  if (!details.certificateFileName) errors.push('Certificate upload is required.');
  if (!details.dgcaDeclaration) errors.push('DGCA declaration must be accepted.');
  const hrs = Number(details.flyingHours);
  if (isNaN(hrs) || hrs < 100) {
    errors.push('Minimum 100 logged flying hours are required for Self-Fly. Booking rejected below this threshold.');
  }
  return { valid: errors.length === 0, errors, flyingHours: hrs };
}

export function todayISO(): string {
  return new Date().toISOString().split('T')[0];
}

export function isAdult(dobStr: string | null | undefined): boolean {
  if (!dobStr) return false;
  const dob = new Date(dobStr);
  if (isNaN(dob.getTime())) return false;
  const diff = Date.now() - dob.getTime();
  const age = new Date(diff).getUTCFullYear() - 1970;
  return age >= 18;
}

export function isAadhaarExempt(dobStr: string | null | undefined): boolean {
  if (!dobStr) return false;
  const dob = new Date(dobStr);
  if (isNaN(dob.getTime())) return false;
  const threshold = new Date(dob);
  threshold.setFullYear(threshold.getFullYear() + 5);
  threshold.setDate(threshold.getDate() + 15);
  return new Date() < threshold;
}

/** Passenger DOB bounds: at least 15 days old, no more than 100 years old. */
export function dobRange(): { min: string; max: string } {
  const today = new Date();
  const maxDate = new Date(today);
  maxDate.setDate(maxDate.getDate() - 15);
  const minDate = new Date(today);
  minDate.setFullYear(minDate.getFullYear() - 100);
  const fmt = (d: Date) => d.toISOString().split('T')[0];
  return { min: fmt(minDate), max: fmt(maxDate) };
}

export function passwordStrength(pw: string): { score: number; label: string; pct: number } {
  let score = 0;
  if (pw.length >= 8) score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[a-z]/.test(pw)) score++;
  if (/[0-9]/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  const levels = ['Very Weak', 'Weak', 'Fair', 'Good', 'Strong', 'Very Strong'];
  return { score, label: levels[score], pct: (score / 5) * 100 };
}

export function statusClass(status: string): string {
  return 'st-' + String(status).toLowerCase().replace(/\s+/g, '_');
}

export function fmtINR(n: number): string {
  return '₹' + Number(n).toLocaleString('en-IN');
}

export function fmtDate(iso: string): string {
  try {
    return new Date(iso).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  } catch {
    return iso;
  }
}

/** Haversine great-circle distance in km — mirrors backend CostCalculatorService. */
export function haversineKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371;
  const toRad = (d: number) => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
  return Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
}