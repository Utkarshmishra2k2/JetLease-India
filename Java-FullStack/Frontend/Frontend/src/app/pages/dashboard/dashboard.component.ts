import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { BookingService } from '../../core/services/booking.service';
import { PaymentService } from '../../core/services/payment.service';
import { LeaseService } from '../../core/services/lease.service';
import { NotificationService } from '../../core/services/notification.service';
import { ProfileService } from '../../core/services/profile.service';
import { ReportService } from '../../core/services/report.service';
import { ToastService } from '../../core/services/toast.service';
import { Booking, Payment, Lease, Notification, User, ReportIssue } from '../../core/models/models';
import { VALIDATORS, isAdult, todayISO, fmtINR, fmtDate, statusClass } from '../../core/util/validators';

type Tab = 'overview' | 'profile' | 'bookings' | 'payments' | 'notifications' | 'leases' | 'reports';

// Backend's authoritative status rules (BookingRulesService.java) — used instead of the
// stale prototype's client-side lists so the Cancel/Pay buttons only ever appear when the
// server will actually accept the action.
const PAYABLE_STATUSES = ['Pending Payment', 'Payment Rejected'];
const CANCELLABLE_STATUSES = ['Pending Payment', 'Pending Verification', 'Lease Pending', 'Lease Signed', 'Approved'];
const ENDED_STATUSES = ['Completed', 'Cancelled', 'Rejected', 'Payment Rejected'];

/** Exact port of dashboard.html + dashboard.js — the 7-tab customer dashboard. */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private bookingService = inject(BookingService);
  private paymentService = inject(PaymentService);
  private leaseService = inject(LeaseService);
  private notificationService = inject(NotificationService);
  private profileService = inject(ProfileService);
  private reportService = inject(ReportService);
  private toast = inject(ToastService);
  private router = inject(Router);

  tab: Tab = 'overview';
  menuOpen = false;

  bookings: Booking[] = [];
  payments: Payment[] = [];
  leases: Lease[] = [];
  notifications: Notification[] = [];
  reports: ReportIssue[] = [];
  profile: User | null = null;

  fmtINR = fmtINR;
  fmtDate = fmtDate;
  statusClass = statusClass;
  today = todayISO();

  // Profile form state
  pFullName = '';
  pPhone = '';
  pDob = '';
  pEmergency = '';
  phoneChanged = false;
  errors: Record<string, string> = {};

  // Phone OTP modal
  phoneOtpOpen = false;
  phoneOtp = '';
  pendingPhoneValue = '';

  // Cancel modal
  cancelModalBooking: Booking | null = null;
  cancelPreview = { total: 0, fee: 0, refund: 0 };

  // Report form
  reportBookingId = '';
  reportSubject = '';
  reportDetails = '';

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll() {
    this.bookingService.my().subscribe((b) => (this.bookings = b.sort((x, y) => +new Date(y.createdAt) - +new Date(x.createdAt))));
    this.paymentService.my().subscribe((p) => (this.payments = p));
    this.leaseService.my().subscribe((l) => (this.leases = l));
    this.notificationService.my().subscribe((n) => (this.notifications = n));
    this.profileService.get().subscribe((u) => {
      this.profile = u;
      this.pFullName = u.fullName;
      this.pPhone = u.phone;
      this.pDob = u.dob;
      this.pEmergency = u.emergencyContact;
    });
  }

  get firstName(): string {
    return (this.auth.session()?.fullName || '').split(' ')[0];
  }

  switchTab(t: Tab) {
    this.tab = t;
    if (t === 'reports') this.reportService; // no-op, reports list already loaded lazily below
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  toggleMenu(e: Event) {
    e.stopPropagation();
    this.menuOpen = !this.menuOpen;
  }
  closeMenu() {
    this.menuOpen = false;
  }
  logout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  /* ---------------- KPIs ---------------- */
  get upcomingCount(): number {
    return this.bookings.filter((b) => !ENDED_STATUSES.includes(b.status)).length;
  }
  get activeLeaseCount(): number {
    return this.leases.filter((l) => ['Signed', 'Approved'].includes(l.status)).length;
  }
  get recentBookings(): Booking[] {
    return this.bookings.slice(0, 3);
  }
  get upcomingBookings(): Booking[] {
    return this.bookings.filter((b) => !ENDED_STATUSES.includes(b.status)).slice(0, 3);
  }
  get totalSpent(): number {
    return this.payments.filter((p) => p.status === 'Verified' || p.status === 'VERIFIED').reduce((s, p) => s + p.amount, 0);
  }
  get completedFlights(): number {
    return this.bookings.filter((b) => b.status === 'Completed').length;
  }

  isPayable(status: string): boolean {
    return PAYABLE_STATUSES.includes(status);
  }
  isCancellable(status: string): boolean {
    return CANCELLABLE_STATUSES.includes(status);
  }
  isUpcoming(status: string): boolean {
    return !ENDED_STATUSES.includes(status);
  }

  /* ---------------- Profile ---------------- */
  fieldClass(key: string): string {
    if (!(key in this.errors)) return '';
    return this.errors[key] ? 'invalid' : 'valid';
  }

  onPhoneInput() {
    this.phoneChanged = this.pPhone.trim() !== this.profile?.phone;
  }

  submitProfile() {
    const errors: Record<string, string> = {};
    errors['pFullName'] = VALIDATORS.name(this.pFullName);
    errors['pPhone'] = VALIDATORS.phone10(this.pPhone);
    errors['pEmergency'] = VALIDATORS.phone10(this.pEmergency);
    errors['pDob'] = this.pDob && isAdult(this.pDob) ? '' : 'You must be 18 years or older.';
    this.errors = errors;
    if (Object.values(errors).some((e) => e)) {
      this.toast.error('Please fix the highlighted fields.');
      return;
    }

    if (this.phoneChanged) {
      this.pendingPhoneValue = this.pPhone.trim();
      this.phoneOtpOpen = true;
      return;
    }
    this.saveProfile();
  }

  saveProfile() {
    this.profileService.update({ fullName: this.pFullName, dob: this.pDob, emergencyContact: this.pEmergency }).subscribe({
      next: (u) => {
        this.profile = u;
        this.toast.success('Profile updated.');
      },
    });
  }

  confirmPhoneOtp() {
    if (this.phoneOtp.trim() !== '123456') {
      this.toast.error('Incorrect OTP. Use 123456 for this demo.');
      return;
    }
    this.profileService.changePhone({ newPhone: this.pendingPhoneValue, otp: this.phoneOtp.trim() }).subscribe({
      next: (u) => {
        this.profile = u;
        this.pPhone = u.phone;
        this.phoneChanged = false;
        this.phoneOtpOpen = false;
        this.phoneOtp = '';
        this.toast.success('Phone number verified and updated.');
        this.saveProfile();
      },
      error: () => this.toast.error('Could not verify phone number.'),
    });
  }
  closePhoneOtp() {
    this.phoneOtpOpen = false;
    this.phoneOtp = '';
  }

  /* ---------------- Bookings ---------------- */
  openCancelModal(booking: Booking) {
    const verifiedPayment = this.payments.find((p) => p.bookingId === booking.id && (p.status === 'Verified' || p.status === 'VERIFIED'));
    const total = verifiedPayment ? verifiedPayment.amount : booking.total;
    const fee = Math.round(total * 0.2);
    this.cancelPreview = { total, fee, refund: total - fee };
    this.cancelModalBooking = booking;
  }
  closeCancelModal() {
    this.cancelModalBooking = null;
  }

  confirmCancelBooking() {
    const booking = this.cancelModalBooking;
    if (!booking) return;
    this.bookingService.cancel(booking.id).subscribe({
      next: () => {
        this.toast.success('Booking cancelled — refund is being processed.');
        this.closeCancelModal();
        this.loadAll();
      },
      error: () => {
        this.toast.error('This booking can no longer be cancelled online.');
        this.closeCancelModal();
      },
    });
  }

  /* ---------------- Notifications ---------------- */
  markAllRead() {
    this.notificationService.markRead().subscribe(() => {
      this.notifications = this.notifications.map((n) => ({ ...n, read: true }));
      this.toast.success('Notifications marked as read.');
    });
  }

  /* ---------------- Reports ---------------- */
  get reportableBookings(): Booking[] {
    return this.bookings.filter((b) => ['Dispatched', 'Completed'].includes(b.status));
  }

  submitReport() {
    const errors: Record<string, string> = {};
    errors['reportSubject'] = this.reportSubject.trim().length >= 3 ? '' : 'Subject is required.';
    errors['reportDetails'] = VALIDATORS.message(this.reportDetails);
    this.errors = { ...this.errors, ...errors };
    if (Object.values(errors).some((e) => e)) {
      this.toast.error('Please fix the highlighted fields.');
      return;
    }
    this.reportService.file({ bookingId: this.reportBookingId, subject: this.reportSubject, details: this.reportDetails }).subscribe({
      next: (r) => {
        this.reports = [r, ...this.reports];
        this.reportSubject = '';
        this.reportDetails = '';
        this.toast.success('Report submitted — our team will follow up.');
      },
    });
  }
}