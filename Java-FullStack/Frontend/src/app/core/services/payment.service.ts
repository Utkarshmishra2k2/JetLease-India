import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Booking, Payment } from '../models/models';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private base = `${environment.apiUrl}/payments`;

  constructor(private http: HttpClient) {}

  my(): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.base}/my`);
  }

  payable(bookingId: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.base}/booking/${bookingId}`);
  }

  pay(bookingId: string, transactionId: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/booking/${bookingId}/pay`, { transactionId });
  }
}