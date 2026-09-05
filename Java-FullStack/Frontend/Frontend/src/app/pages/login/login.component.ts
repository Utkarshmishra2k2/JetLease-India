import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

type LoginMode = 'email' | 'phone' | 'otp';

/** Exact port of login.html + auth.js#initLoginFlow / requestPasswordReset. */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  mode = signal<LoginMode>('email');
  loading = signal(false);

  loginEmail = '';
  loginPassword = '';
  loginPhone = '';
  loginPhonePassword = '';
  otpIdentifier = '';
  loginOtp = '';
  showPassword = false;
  showPhonePassword = false;

  forgotOpen = signal(false);
  forgotEmail = '';

  setMode(m: LoginMode) {
    this.mode.set(m);
  }

  togglePw(which: 'email' | 'phone') {
    if (which === 'email') this.showPassword = !this.showPassword;
    else this.showPhonePassword = !this.showPhonePassword;
  }

  sendLoginOtp() {
    const id = this.otpIdentifier.trim();
    if (!id) {
      this.toast.error('Enter your registered email or phone first.');
      return;
    }
    this.auth.loginOtpRequest().subscribe({
      next: () => this.toast.success('OTP sent (demo code: 123456).'),
      error: () => this.toast.error('Could not send OTP right now.'),
    });
  }

  submit() {
    const mode = this.mode();

    if (mode === 'otp') {
      // The backend does not expose a passwordless OTP-login completion endpoint
      // (only /login/otp/request exists, for sending the demo code). Verifying the
      // code client-side and forging a session would mean fabricating a fake login
      // instead of a real one, so this path is intentionally not wired further.
      if (this.loginOtp.trim() !== '123456') {
        this.toast.error('Incorrect OTP. Use 123456 for this demo.');
        return;
      }
      this.toast.error("OTP login isn't available yet \u2014 please use Email or Phone to log in.");
      return;
    }

    this.loading.set(true);
    const payload =
      mode === 'phone'
        ? { identifierType: 'phone', identifier: this.loginPhone.trim(), password: this.loginPhonePassword }
        : { identifierType: 'email', identifier: this.loginEmail.trim().toLowerCase(), password: this.loginPassword };

    this.auth.login(payload).subscribe({
      next: (s) => {
        this.toast.success('Welcome back, ' + s.fullName.split(' ')[0] + '.');
        setTimeout(() => this.router.navigate(['/dashboard']), 400);
      },
      error: () => this.loading.set(false),
    });
  }

  openForgot() {
    this.forgotOpen.set(true);
  }
  closeForgot() {
    this.forgotOpen.set(false);
  }

  requestPasswordReset() {
    const email = this.forgotEmail.trim().toLowerCase();
    if (!email) {
      this.toast.error('Enter your account email.');
      return;
    }
    this.auth.forgotPasswordRequest(email).subscribe({
      next: () => {
        this.toast.success('Password reset link sent (demo). Check your inbox.');
        this.forgotOpen.set(false);
      },
      error: () => this.toast.error('No account found with that email.'),
    });
  }
}