import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/services/admin.service';
import { ToastService } from '../../core/services/toast.service';
import { Aircraft, Booking, Payment, Lease, User, Pilot, Crew, ContactMessage, ReportIssue, AuditLog } from '../../core/models/models';
import { fmtINR, fmtDate, statusClass } from '../../core/util/validators';

type Tab = 'overview' | 'aircraft' | 'bookings' | 'payments' | 'leases' | 'customers' | 'crew' | 'routes' | 'inbox' | 'exports' | 'audit';

const ACTIVE_BOOKING_STATUSES = ['Lease Pending', 'Lease Signed', 'Approved'];

/** Exact port of admin.html + admin.js — the 11-tab operations console. */
@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
})
export class AdminComponent implements OnInit {
  private admin = inject(AdminService);
  private toast = inject(ToastService);

  tab: Tab = 'overview';
  fmtINR = fmtINR;
  fmtDate = fmtDate;
  statusClass = statusClass;

  aircraft: Aircraft[] = [];
  bookings: Booking[] = [];
  payments: Payment[] = [];
  leases: Lease[] = [];
  customers: User[] = [];
  pilots: Pilot[] = [];
  crew: Crew[] = [];
  routes: any[] = [];
  routeFilter = '';
  messages: ContactMessage[] = [];
  reports: ReportIssue[] = [];
  auditLogs: AuditLog[] = [];
  auditCategory = 'All';
  auditCategories = ['All', 'Login', 'Booking', 'Payment', 'Lease', 'Admin'];

  // Modals
  aircraftModalOpen = false;
  editingAircraftId: string | null = null;
  af = { model: '', manufacturer: '', reg: '', category: 'Light Jet', capacity: 6, speed: 600, rangeKm: 3000, hourlyRate: 150000, typeRating: '', status: 'Available' };

  bookingModalOpen = false;
  viewedBooking: Booking | null = null;

  crewModalOpen = false;
  crewModalBooking: Booking | null = null;
  selectedPilotId: string | null = null;
  selectedCrewIds: string[] = [];

  customerHistoryOpen = false;
  historyEmail = '';
  historyBookings: Booking[] = [];
  historyPayments: Payment[] = [];

  ngOnInit(): void {
    this.loadKpiSources();
    this.switchTab('overview');
  }

  loadKpiSources() {
    this.admin.aircraft().subscribe((a) => (this.aircraft = a));
    this.admin.bookings().subscribe((b) => (this.bookings = b.sort((x, y) => +new Date(y.createdAt) - +new Date(x.createdAt))));
    this.admin.payments().subscribe((p) => (this.payments = p));
    this.admin.leases().subscribe((l) => (this.leases = l));
    this.admin.customers().subscribe((c) => (this.customers = c));
    this.admin.pilots().subscribe((p) => (this.pilots = p));
    this.admin.crew().subscribe((c) => (this.crew = c));
  }

  switchTab(t: Tab) {
    this.tab = t;
    if (t === 'routes' && !this.routes.length) this.admin.routes().subscribe((r) => (this.routes = r));
    if (t === 'inbox') {
      this.admin.messages().subscribe((m) => (this.messages = m));
      this.admin.reports().subscribe((r) => (this.reports = r));
    }
    if (t === 'audit') this.loadAudit();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /* ---------------- KPIs ---------------- */
  get kpis() {
    const revenue = this.payments.reduce((s, p) => {
      if (p.status === 'VERIFIED') return s + p.amount;
      if (p.status === 'RETURNED' && p.cancellationFee) return s + p.cancellationFee;
      return s;
    }, 0);
    const pendingPayments = this.payments.filter((p) => p.status === 'PENDING_VERIFICATION').length;
    const activeLeases = this.leases.filter((l) => ['Signed', 'Approved'].includes(l.status)).length;
    return [
      ['Total Bookings', this.bookings.length],
      ['Revenue', fmtINR(revenue)],
      ['Pending Payments', pendingPayments],
      ['Aircraft Count', this.aircraft.length],
      ['Customer Count', this.customers.length],
      ['Pilot Count', this.pilots.length],
      ['Crew Count', this.crew.length],
      ['Active Leases', activeLeases],
    ];
  }

  /* ---------------- OVERVIEW ---------------- */
  get approvedCount() {
    return this.bookings.filter((b) => ['Approved', 'Dispatched', 'Completed'].includes(b.status)).length;
  }
  get cancelledCount() {
    return this.bookings.filter((b) => ['Cancelled', 'Rejected'].includes(b.status)).length;
  }
  get popularAircraft(): [string, number][] {
    const byAircraft: Record<string, number> = {};
    this.bookings.forEach((b) => (byAircraft[b.aircraftModel] = (byAircraft[b.aircraftModel] || 0) + 1));
    return Object.entries(byAircraft).sort((a, b) => b[1] - a[1]).slice(0, 5);
  }
  get maxPopular() {
    return this.popularAircraft.length ? this.popularAircraft[0][1] : 1;
  }
  get aircraftStatusCounts(): [string, number][] {
    const counts: Record<string, number> = {};
    this.aircraft.forEach((a) => (counts[a.status] = (counts[a.status] || 0) + 1));
    return Object.entries(counts);
  }
  get revenueVerified() {
    return this.payments.filter((p) => p.status === 'VERIFIED').reduce((s, p) => s + p.amount, 0);
  }
  get revenuePending() {
    return this.payments.filter((p) => p.status === 'PENDING_VERIFICATION').reduce((s, p) => s + p.amount, 0);
  }
  get cancellationFeesRetained() {
    return this.payments.filter((p) => p.status === 'RETURNED' && p.cancellationFee).reduce((s, p) => s + p.cancellationFee, 0);
  }
  get cancellationRefunds() {
    return this.payments.filter((p) => p.status === 'RETURNED' && p.cancellationFee).reduce((s, p) => s + (p.refundAmount || 0), 0);
  }
  get leaseRejectionReturns() {
    return this.payments.filter((p) => p.status === 'RETURNED' && !p.cancellationFee).reduce((s, p) => s + p.amount, 0);
  }
  get totalRevenue() {
    return this.revenueVerified + this.cancellationFeesRetained;
  }
  barPct(value: number, max: number) {
    return max ? Math.round((value / max) * 100) : 0;
  }

  /* ---------------- AIRCRAFT ---------------- */
  openAircraftForm(a?: Aircraft) {
    this.editingAircraftId = a?.id || null;
    this.af = a
      ? { model: a.model, manufacturer: a.manufacturer, reg: a.reg, category: a.category, capacity: a.capacity, speed: a.speed, rangeKm: a.rangeKm, hourlyRate: a.hourlyRate, typeRating: a.typeRating, status: a.status }
      : { model: '', manufacturer: '', reg: '', category: 'Light Jet', capacity: 6, speed: 600, rangeKm: 3000, hourlyRate: 150000, typeRating: '', status: 'Available' };
    this.aircraftModalOpen = true;
  }
  closeAircraftForm() {
    this.aircraftModalOpen = false;
  }
  saveAircraft() {
    if (!this.af.model || !this.af.reg) {
      this.toast.error('Model and registration are required.');
      return;
    }
    if (this.editingAircraftId) {
      // Backend only exposes discrete status/rate updates for existing aircraft, not a
      // full-record PUT, so persist each changed field through its own endpoint.
      this.admin.updateAircraftRate(this.editingAircraftId, this.af.hourlyRate).subscribe();
      this.admin.updateAircraftStatus(this.editingAircraftId, this.af.status).subscribe({
        next: () => {
          this.toast.success('Aircraft saved.');
          this.aircraftModalOpen = false;
          this.refreshAircraft();
        },
      });
    } else {
      this.admin.addAircraft(this.af).subscribe({
        next: () => {
          this.toast.success('Aircraft saved.');
          this.aircraftModalOpen = false;
          this.refreshAircraft();
        },
      });
    }
  }
  refreshAircraft() {
    this.admin.aircraft().subscribe((a) => (this.aircraft = a));
  }
  setAircraftStatus(id: string, status: string) {
    this.admin.updateAircraftStatus(id, status).subscribe({
      next: () => {
        this.toast.success(`Aircraft ${status.toLowerCase()}.`);
        this.refreshAircraft();
      },
    });
  }
  deleteAircraft(id: string) {
    if (!confirm('Delete this aircraft permanently?')) return;
    this.admin.deleteAircraft(id).subscribe({
      next: () => {
        this.toast.success('Aircraft removed.');
        this.refreshAircraft();
      },
    });
  }

  /* ---------------- BOOKINGS ---------------- */
  isActiveBooking(status: string) {
    return ACTIVE_BOOKING_STATUSES.includes(status);
  }
  isEndedBooking(status: string) {
    return ['Completed', 'Cancelled', 'Rejected'].includes(status);
  }
  refreshBookings() {
    this.admin.bookings().subscribe((b) => (this.bookings = b.sort((x, y) => +new Date(y.createdAt) - +new Date(x.createdAt))));
  }
  advanceBooking(id: string, action: 'approve' | 'dispatch' | 'complete' | 'reject') {
    const call =
      action === 'approve' ? this.admin.approveBooking(id) : action === 'dispatch' ? this.admin.dispatchBooking(id) : action === 'complete' ? this.admin.completeBooking(id) : this.admin.rejectBooking(id);
    call.subscribe({
      next: () => {
        this.toast.success(`Booking updated.`);
        this.refreshBookings();
      },
    });
  }
  viewBooking(b: Booking) {
    this.viewedBooking = b;
    this.bookingModalOpen = true;
  }
  closeBookingModal() {
    this.bookingModalOpen = false;
  }
  pilotName(id: string | null) {
    if (!id) return null;
    return this.pilots.find((p) => p.id === id)?.name || id;
  }
  crewNames(ids: string) {
    const list = (ids || '').split(',').map((s) => s.trim()).filter(Boolean);
    if (!list.length) return 'Not yet assigned';
    return list.map((id) => this.crew.find((c) => c.id === id)?.name || id).join(', ');
  }

  /* ---------------- CREW ASSIGNMENT ---------------- */
  openAssignCrewModal(b: Booking) {
    if (!this.isActiveBooking(b.status)) {
      this.toast.error('Crew can only be assigned to active bookings.');
      return;
    }
    this.crewModalBooking = b;
    this.selectedPilotId = b.assignedPilotId || null;
    this.selectedCrewIds = (b.assignedCrewIds || '').split(',').map((s) => s.trim()).filter(Boolean);
    this.crewModalOpen = true;
  }
  closeCrewModal() {
    this.crewModalOpen = false;
  }
  effectivePilotHours(p: Pilot) {
    const b = this.crewModalBooking;
    return p.remainingHours + (b && b.assignedPilotId === p.id ? b.hours : 0);
  }
  effectiveCrewHours(c: Crew) {
    const b = this.crewModalBooking;
    const isCurrent = b ? (b.assignedCrewIds || '').split(',').includes(c.id) : false;
    return c.remainingHours + (isCurrent ? (b?.hours || 0) : 0);
  }
  toggleCrewSelection(id: string, checked: boolean) {
    if (checked) this.selectedCrewIds = [...this.selectedCrewIds, id];
    else this.selectedCrewIds = this.selectedCrewIds.filter((c) => c !== id);
  }
  saveCrewAssignment() {
    const b = this.crewModalBooking;
    if (!b) return;
    if (!b.selfFly && !this.selectedPilotId) {
      
      this.toast.error('Please select a pilot.');
      
      return;
      
      }
    const pilotId = b.selfFly ? '' : this.selectedPilotId || '';
    this.admin.assignCrew(b.id, pilotId, this.selectedCrewIds).subscribe({
      next: () => {
        this.toast.success('Crew assignment saved.');
        this.crewModalOpen = false;
        this.refreshBookings();
        this.admin.pilots().subscribe((p) => (this.pilots = p));
        this.admin.crew().subscribe((c) => (this.crew = c));
      },
      error: (err) => this.toast.error(err?.error?.message || 'Could not save crew assignment.'),
    });
  }

  /* ---------------- PAYMENTS ---------------- */
  refreshPayments() {
    this.admin.payments().subscribe((p) => (this.payments = p));
  }
  verifyPayment(id: string) {
    this.admin.ledgerCheck(id).subscribe((result) => {
      if (!result.verified && !confirm(`Mock bank ledger check: ${result.message}\n\nVerify this payment anyway?`)) return;
      this.admin.verifyPayment(id).subscribe({
        next: () => {
          this.toast.success('Payment verified — lease agreement generated.');
          this.refreshPayments();
          this.refreshBookings();
        },
      });
    });
  }
  rejectPayment(id: string) {
    this.admin.rejectPayment(id).subscribe({
      next: () => {
        this.toast.success('Payment rejected.');
        this.refreshPayments();
        this.refreshBookings();
      },
    });
  }

  /* ---------------- LEASES ---------------- */
  refreshLeases() {
    this.admin.leases().subscribe((l) => (this.leases = l));
  }
  approveLease(id: string) {
    this.admin.approveLease(id).subscribe({
      next: () => {
        this.toast.success('Lease approved.');
        this.refreshLeases();
        this.refreshBookings();
      },
    });
  }
  rejectLease(id: string) {
    this.admin.rejectLease(id).subscribe({
      next: () => {
        this.toast.success('Lease rejected — payment marked as returned.');
        this.refreshLeases();
        this.refreshBookings();
        this.refreshPayments();
      },
    });
  }

  /* ---------------- CUSTOMERS ---------------- */
  toggleCustomerStatus(email: string, currentStatus: string) {
    this.admin.toggleCustomerStatus(email).subscribe({
      next: () => {
        this.toast.success(currentStatus === 'active' ? 'Customer suspended.' : 'Customer activated.');
        this.admin.customers().subscribe((c) => (this.customers = c));
      },
    });
  }
  viewCustomerHistory(email: string) {
    this.historyEmail = email;
    this.admin.customerBookings(email).subscribe((b) => (this.historyBookings = b));
    this.historyPayments = this.payments.filter((p) => p.userEmail === email);
    this.customerHistoryOpen = true;
  }
  closeCustomerHistory() {
    this.customerHistoryOpen = false;
  }

  /* ---------------- CREW & PILOT AVAILABILITY ---------------- */
  togglePilot(id: string) {
    this.admin.togglePilot(id).subscribe(() => this.admin.pilots().subscribe((p) => (this.pilots = p)));
  }
  toggleCrewAvailability(id: string) {
    this.admin.toggleCrew(id).subscribe(() => this.admin.crew().subscribe((c) => (this.crew = c)));
  }

  /* ---------------- ROUTES ---------------- */
  get filteredRoutes() {
    const f = this.routeFilter.toLowerCase();
    if (!f) return this.routes;
    return this.routes.filter((r) => r.city?.toLowerCase().includes(f) || r.code?.toLowerCase().includes(f));
  }
  bookingCountForRoute(code: string) {
    return this.bookings.filter((b) => b.origin === code || b.destination === code).length;
  }

  /* ---------------- INBOX ---------------- */
  markMessageRead(id: string) {
    this.admin.markMessageRead(id).subscribe(() => this.admin.messages().subscribe((m) => (this.messages = m)));
  }
  resolveReport(id: string) {
    this.admin.resolveReport(id).subscribe({
      next: () => {
        this.toast.success('Report marked resolved.');
        this.admin.reports().subscribe((r) => (this.reports = r));
      },
    });
  }

  /* ---------------- EXPORTS ---------------- */
  exportCSV(kind: 'bookings' | 'customers' | 'payments') {
    const url = this.admin.exportUrl(kind);
    this.admin.downloadExport(url).subscribe({
      next: (csv) => {
        if (!csv.trim()) {
          this.toast.error('No data to export yet.');
          return;
        }
        const blob = new Blob([csv], { type: 'text/csv' });
        const objectUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = objectUrl;
        a.download = `jetlease-${kind}-report.csv`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(objectUrl);
      },
      error: () => this.toast.error('Export failed.'),
    });
  }

  /* ---------------- AUDIT LOG ---------------- */
  loadAudit() {
    this.admin.auditLog(this.auditCategory === 'All' ? undefined : this.auditCategory).subscribe((logs) => (this.auditLogs = logs));
  }
  setAuditCategory(cat: string) {
    this.auditCategory = cat;
    this.loadAudit();
  }
}