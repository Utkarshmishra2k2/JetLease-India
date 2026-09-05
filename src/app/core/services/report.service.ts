import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReportIssue } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private base = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  file(payload: { bookingId: string; subject: string; details: string }): Observable<ReportIssue> {
    return this.http.post<ReportIssue>(this.base, payload);
  }
}