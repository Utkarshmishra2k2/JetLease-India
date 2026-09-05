import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Lease } from '../models/models';

@Injectable({ providedIn: 'root' })
export class LeaseService {
  private base = `${environment.apiUrl}/leases`;

  constructor(private http: HttpClient) {}

  my(): Observable<Lease[]> {
    return this.http.get<Lease[]>(`${this.base}/my`);
  }

  get(id: string): Observable<{ lease: Lease; contractText: string }> {
    return this.http.get<{ lease: Lease; contractText: string }>(`${this.base}/${id}`);
  }

  sign(id: string, legalName: string): Observable<Lease> {
    return this.http.post<Lease>(`${this.base}/${id}/sign`, { legalName });
  }

  exportUrl(id: string): string {
    return `${this.base}/${id}/export`;
  }
}