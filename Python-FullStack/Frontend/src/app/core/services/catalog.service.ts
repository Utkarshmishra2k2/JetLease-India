import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Aircraft, Route, RecommendationResult } from '../models/models';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  routes(): Observable<Route[]> {
    return this.http.get<Route[]>(`${this.base}/routes`);
  }

  distance(origin: string, destination: string): Observable<{ distanceKm: number }> {
    return this.http.get<{ distanceKm: number }>(`${this.base}/routes/distance`, { params: { origin, destination } });
  }

  aircraft(): Observable<Aircraft[]> {
    return this.http.get<Aircraft[]>(`${this.base}/aircraft`);
  }

  availableAircraft(pax: number, category?: string): Observable<Aircraft[]> {
    const params: any = { pax };
    if (category) params.category = category;
    return this.http.get<Aircraft[]>(`${this.base}/aircraft/available`, { params });
  }

  recommend(payload: { pax: number; budget: number; distanceKm: number; category?: string }): Observable<RecommendationResult> {
    return this.http.post<RecommendationResult>(`${this.base}/aircraft/recommend`, payload);
  }
}