import { Routes } from '@angular/router';
import { customerGuard, adminGuard, guestOnlyGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
    canActivate: [guestOnlyGuard],
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component').then((m) => m.RegisterComponent),
    canActivate: [guestOnlyGuard],
  },
  {
    path: 'admin-login',
    loadComponent: () => import('./pages/admin-login/admin-login.component').then((m) => m.AdminLoginComponent),
    canActivate: [guestOnlyGuard],
  },
  {
    path: 'booking',
    loadComponent: () => import('./pages/booking/booking.component').then((m) => m.BookingComponent),
    canActivate: [customerGuard],
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [customerGuard],
  },
  {
    path: 'payment/:bookingId',
    loadComponent: () => import('./pages/payment/payment.component').then((m) => m.PaymentComponent),
    canActivate: [customerGuard],
  },
  {
    path: 'lease/:id',
    loadComponent: () => import('./pages/lease/lease.component').then((m) => m.LeaseComponent),
    canActivate: [customerGuard],
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin/admin.component').then((m) => m.AdminComponent),
    canActivate: [adminGuard],
  },
  { path: '**', redirectTo: '' },
];