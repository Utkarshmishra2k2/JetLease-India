import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Aircraft, AuditLog, Booking, ContactMessage, Crew, Lease, Payment, Pilot, ReportIssue, User } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private base = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  overview(): Observable<any> {
    return this.http.get<any>(`${this.base}/overview`);
  }

  auditLog(category?: string): Observable<AuditLog[]> {
    const params: Record<string, string> = {};
    if (category) params['category'] = category;
    return this.http.get<AuditLog[]>(`${this.base}/audit-log`, { params });
  }

  routes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/routes`);
  }

  exportUrl(kind: 'bookings' | 'customers' | 'payments'): string {
    return `${this.base}/exports/${kind}.csv`;
  }
  downloadExport(url: string): Observable<string> {
    return this.http.get(url, { responseType: 'text' });
  }

  // Aircraft
  aircraft(): Observable<Aircraft[]> {
    return this.http.get<Aircraft[]>(`${this.base}/aircraft`);
  }
  addAircraft(payload: any): Observable<Aircraft> {
    return this.http.post<Aircraft>(`${this.base}/aircraft`, payload);
  }
  updateAircraftStatus(id: string, status: string): Observable<Aircraft> {
    return this.http.put<Aircraft>(`${this.base}/aircraft/${id}/status`, { status });
  }
  updateAircraftRate(id: string, hourlyRate: number): Observable<Aircraft> {
    return this.http.put<Aircraft>(`${this.base}/aircraft/${id}/rate`, { hourlyRate });
  }
  deleteAircraft(id: string): Observable<any> {
    return this.http.delete(`${this.base}/aircraft/${id}`);
  }

  // Bookings
  bookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.base}/bookings`);
  }
  booking(id: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.base}/bookings/${id}`);
  }
  assignCrew(id: string, pilotId: string, crewIds: string[]): Observable<Booking> {
    return this.http.post<Booking>(`${this.base}/bookings/${id}/assign-crew`, { pilotId, crewIds });
  }
  approveBooking(id: string): Observable<Booking> {
    return this.http.post<Booking>(`${this.base}/bookings/${id}/approve`, {});
  }
  dispatchBooking(id: string): Observable<Booking> {
    return this.http.post<Booking>(`${this.base}/bookings/${id}/dispatch`, {});
  }
  completeBooking(id: string): Observable<Booking> {
    return this.http.post<Booking>(`${this.base}/bookings/${id}/complete`, {});
  }
  rejectBooking(id: string): Observable<Booking> {
    return this.http.post<Booking>(`${this.base}/bookings/${id}/reject`, {});
  }

  // Payments
  payments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.base}/payments`);
  }
  ledgerCheck(id: string): Observable<{ verified: boolean; message: string }> {
    return this.http.get<{ verified: boolean; message: string }>(`${this.base}/payments/${id}/ledger-check`);
  }
  verifyPayment(id: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/payments/${id}/verify`, {});
  }
  rejectPayment(id: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/payments/${id}/reject`, {});
  }

  // Leases
  leases(): Observable<Lease[]> {
    return this.http.get<Lease[]>(`${this.base}/leases`);
  }
  approveLease(id: string): Observable<Lease> {
    return this.http.post<Lease>(`${this.base}/leases/${id}/approve`, {});
  }
  rejectLease(id: string): Observable<Lease> {
    return this.http.post<Lease>(`${this.base}/leases/${id}/reject`, {});
  }

  // Customers
  customers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/customers`);
  }
  customerBookings(email: string): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.base}/customers/${email}/bookings`);
  }
  toggleCustomerStatus(email: string): Observable<any> {
    return this.http.post(`${this.base}/customers/${email}/toggle-status`, {});
  }

  // Crew / Pilots
  pilots(): Observable<Pilot[]> {
    return this.http.get<Pilot[]>(`${this.base}/pilots`);
  }
  crew(): Observable<Crew[]> {
    return this.http.get<Crew[]>(`${this.base}/crew`);
  }
  togglePilot(id: string): Observable<Pilot> {
    return this.http.post<Pilot>(`${this.base}/pilots/${id}/toggle-availability`, {});
  }
  toggleCrew(id: string): Observable<Crew> {
    return this.http.post<Crew>(`${this.base}/crew/${id}/toggle-availability`, {});
  }

  // Inbox
  messages(): Observable<ContactMessage[]> {
    return this.http.get<ContactMessage[]>(`${this.base}/inbox/messages`);
  }
  markMessageRead(id: string): Observable<ContactMessage> {
    return this.http.post<ContactMessage>(`${this.base}/inbox/messages/${id}/mark-read`, {});
  }
  reports(): Observable<ReportIssue[]> {
    return this.http.get<ReportIssue[]>(`${this.base}/inbox/reports`);
  }
  resolveReport(id: string): Observable<ReportIssue> {
    return this.http.post<ReportIssue>(`${this.base}/inbox/reports/${id}/resolve`, {});
  }
}