import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private base = `${environment.apiUrl}/profile`;

  constructor(private http: HttpClient) {}

  get(): Observable<User> {
    return this.http.get<User>(this.base);
  }

  update(payload: { fullName: string; dob: string; emergencyContact: string }): Observable<User> {
    return this.http.put<User>(this.base, payload);
  }

  changePhone(payload: { newPhone: string; otp: string }): Observable<User> {
    return this.http.put<User>(`${this.base}/phone`, payload);
  }
}