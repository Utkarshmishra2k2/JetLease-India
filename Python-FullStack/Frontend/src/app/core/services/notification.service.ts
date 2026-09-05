import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private base = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  my(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.base}/my`);
  }

  markRead(): Observable<any> {
    return this.http.post(`${this.base}/mark-read`, {});
  }
}