import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Aircraft, ContactMessage, Faq, Testimonial } from '../models/models';

@Injectable({ providedIn: 'root' })
export class GuestService {
  private base = `${environment.apiUrl}/guest`;

  constructor(private http: HttpClient) {}

  fleet(): Observable<Aircraft[]> {
    return this.http.get<Aircraft[]>(`${this.base}/fleet`);
  }

  faq(): Observable<Faq[]> {
    return this.http.get<Faq[]>(`${this.base}/faq`);
  }

  testimonials(): Observable<Testimonial[]> {
    return this.http.get<Testimonial[]>(`${this.base}/testimonials`);
  }

  contact(payload: { name: string; phone: string; email: string; message: string }): Observable<ContactMessage> {
    return this.http.post<ContactMessage>(`${this.base}/contact`, payload);
  }
}