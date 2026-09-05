import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models/models';

const STORAGE_KEY = 'jl_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = `${environment.apiUrl}/auth`;
  session = signal<AuthResponse | null>(this.readStoredSession());

  isLoggedIn = computed(() => !!this.session());
  isCustomer = computed(() => this.session()?.role === 'customer');
  isAdmin = computed(() => this.session()?.role === 'admin');

  constructor(private http: HttpClient) {}

  private readStoredSession(): AuthResponse | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as AuthResponse) : null;
  }

  private storeSession(session: AuthResponse) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.session.set(session);
  }

  get token(): string | null {
    return this.session()?.token ?? null;
  }

  register(payload: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/register`, payload).pipe(tap((s) => this.storeSession(s)));
  }

  login(payload: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, payload).pipe(tap((s) => this.storeSession(s)));
  }

  loginOtpRequest(): Observable<any> {
    return this.http.post(`${this.base}/login/otp/request`, {});
  }

  adminLogin(payload: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/admin-login`, payload).pipe(tap((s) => this.storeSession(s)));
  }

  forgotPasswordRequest(email: string): Observable<any> {
    return this.http.post(`${this.base}/forgot-password/request`, { email });
  }

  logout() {
    this.http.post(`${this.base}/logout`, {}).subscribe({
      complete: () => this.clearSession(),
      error: () => this.clearSession(),
    });
  }

  private clearSession() {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
  }
}