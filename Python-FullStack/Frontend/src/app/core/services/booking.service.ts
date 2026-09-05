import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Booking, Passenger, VerifyResult } from '../models/models';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private base = `${environment.apiUrl}/bookings`;

  constructor(private http: HttpClient) {}

  verifyAadhaar(aadhaar: string): Observable<VerifyResult> {
    return this.http.post<VerifyResult>(`${this.base}/verify-aadhaar`, { aadhaar });
  }

  verifyLicense(licenseNumber: string): Observable<VerifyResult> {
    return this.http.post<VerifyResult>(`${this.base}/verify-license`, { licenseNumber });
  }

  create(payload: any): Observable<Booking> {
    return this.http.post<Booking>(this.base, payload);
  }

  my(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.base}/my`);
  }

  get(id: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.base}/${id}`);
  }

  passengers(id: string): Observable<Passenger[]> {
    return this.http.get<Passenger[]>(`${this.base}/${id}/passengers`);
  }

  cancel(id: string): Observable<any> {
    return this.http.post(`${this.base}/${id}/cancel`, {});
  }
}