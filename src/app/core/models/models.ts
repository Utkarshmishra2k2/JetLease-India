export interface AuthResponse {
  token: string;
  email: string;
  fullName: string;
  role: 'customer' | 'admin';
}

export interface User {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  dob: string;
  emergencyContact: string;
  country: string;
  role: string;
  status: string;
  membership: string;
  loyaltyPoints: number;
  createdAt: string;
}

export interface Aircraft {
  id: string;
  reg: string;
  model: string;
  manufacturer: string;
  category: string;
  capacity: number;
  speed: number;
  rangeKm: number;
  hourlyRate: number;
  status: string;
  typeRating: string;
}

export interface Route {
  code: string;
  city: string;
  airport: string;
  lat: number;
  lon: number;
}

export interface Passenger {
  id?: number;
  bookingId?: string;
  name: string;
  dob: string;
  gender: string;
  aadhaar: string;
  verificationStatus: string;
  noAadhaar: boolean;
  altDocumentId: string;
}

export interface SelfFlyDetails {
  licenseNumber: string;
  licenseClass: string;
  flyingHours: number;
  dgcaDeclaration: boolean;
  verified: boolean;
  certificateFileName: string;
}

export interface Booking {
  id: string;
  userEmail: string;
  type: string;
  tripType: string;
  origin: string;
  destination: string;
  date: string;
  time: string;
  returnDate: string;
  returnTime: string;
  pax: number;
  aircraftId: string;
  aircraftModel: string;
  selfFly: boolean;
  licenseNumber: string;
  licenseClass: string;
  flyingHours: number;
  dgcaDeclaration: boolean;
  licenseVerified: boolean;
  hours: number;
  aircraftCost: number;
  pilotCost: number;
  crewCost: number;
  airportCharges: number;
  fuelSurcharge: number;
  gst: number;
  total: number;
  status: string;
  assignedPilotId: string;
  assignedCrewIds: string;
  createdAt: string;
}

export interface Payment {
  id: string;
  bookingId: string;
  userEmail: string;
  amount: number;
  transactionId: string;
  status: string;
  submittedAt: string;
  cancellationFee: number;
  refundAmount: number;
}

export interface Lease {
  id: string;
  bookingId: string;
  userEmail: string;
  status: string;
  signedBy: string;
  signedDate: string;
  approvalDate: string;
  createdAt: string;
}

export interface Notification {
  id: string;
  userEmail: string;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}

export interface Pilot {
  id: string;
  name: string;
  licenseNumber: string;
  remainingHours: number;
  available: boolean;
}

export interface Crew {
  id: string;
  name: string;
  role: string;
  remainingHours: number;
  available: boolean;
}

export interface AuditLog {
  id: string;
  actor: string;
  category: string;
  action: string;
  details: string;
  timestamp: string;
}

export interface ContactMessage {
  id: string;
  name: string;
  phone: string;
  email: string;
  message: string;
  status: string;
  createdAt: string;
}

export interface ReportIssue {
  id: string;
  bookingId: string;
  userEmail: string;
  subject: string;
  details: string;
  status: string;
  createdAt: string;
}

export interface Faq {
  id: number;
  question: string;
  answer: string;
}

export interface Testimonial {
  id: number;
  name: string;
  role: string;
  quote: string;
  rating: number;
}

export interface Recommendation {
  id: string;
  model: string;
  capacity: number;
  range: number;
  estCost: number;
}

export interface RecommendationResult {
  best: Recommendation | null;
  alternatives: Recommendation[];
}

export interface VerifyResult {
  verified: boolean;
  message: string;
  holderName?: string;
  dob?: string;
  gender?: string;
  licenseClass?: string;
  hoursOnRecord?: number;
}

export interface CostBreakdown {
  hours: number;
  aircraftCost: number;
  pilotCost: number;
  crewCost: number;
  airportCharges: number;
  fuelSurcharge: number;
  gst: number;
  total: number;
}

export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
  fieldErrors?: { [key: string]: string };
}