import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

/** Exact port of admin-login.html + auth.js#initAdminLoginFlow. */
@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-login.component.html',
})
export class AdminLoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  loading = signal(false);
  showPassword = false;
  email = 'admin@jetlease.in';
  password = 'Admin@123';

  submit() {
    this.loading.set(true);
    this.auth.adminLogin({ email: this.email.trim().toLowerCase(), password: this.password }).subscribe({
      next: () => {
        this.toast.success('Welcome back, Admin.');
        setTimeout(() => this.router.navigate(['/admin']), 400);
      },
      error: () => this.loading.set(false),
    });
  }
}