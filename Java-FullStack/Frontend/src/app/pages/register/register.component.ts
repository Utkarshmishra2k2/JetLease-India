import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { VALIDATORS, isAdult, passwordStrength, todayISO } from '../../core/util/validators';

/** Exact port of register.html + auth.js#initRegisterFlow (3-step: details -> mock OTP -> done). */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  step = signal(1);
  loading = signal(false);
  today = todayISO();

  fullName = '';
  email = '';
  country = '';
  phone = '';
  dob = '';
  emergencyContact = '';
  password = '';
  confirmPassword = '';

  emailOtp = '';
  phoneOtp = '';
  emailOtpErr = false;
  phoneOtpErr = false;

  errors: Record<string, string> = {};
  strengthPct = 0;
  strengthLabel = 'Use 8+ characters with upper, lower, number & symbol.';
  strengthColor = '';

  onPasswordInput() {
    const s = passwordStrength(this.password);
    this.strengthPct = s.pct;
    this.strengthColor = s.score <= 1 ? 'var(--red)' : s.score <= 3 ? 'var(--amber)' : 'var(--green)';
    this.strengthLabel = 'Strength: ' + s.label;
  }

  fieldClass(key: string): string {
    if (!(key in this.errors)) return '';
    return this.errors[key] ? 'invalid' : 'valid';
  }

  submitDetails() {
    const errors: Record<string, string> = {};
    errors['fullName'] = VALIDATORS.name(this.fullName);
    errors['email'] = VALIDATORS.email(this.email);
    errors['country'] = this.country ? '' : 'Select your country.';
    errors['phone'] = VALIDATORS.phone10(this.phone);
    errors['dob'] = this.dob && isAdult(this.dob) ? '' : 'You must be 18 or older.';
    errors['emergency'] = VALIDATORS.phone10(this.emergencyContact);
    const strong = passwordStrength(this.password).score >= 3;
    errors['password'] = strong ? '' : "Password is too weak — add uppercase, numbers & symbols.";
    const matchOk = this.password === this.confirmPassword && this.confirmPassword.length > 0;
    errors['confirm'] = matchOk ? '' : 'Passwords do not match.';
    this.errors = errors;

    if (Object.values(errors).some((e) => e)) {
      this.toast.error('Please fix the highlighted fields.');
      return;
    }

    this.step.set(2);
    this.toast.success('Mock OTPs sent to email & phone. Use 123456.');
  }

  resendEmail() {
    this.toast.success('Email OTP resent.');
  }
  resendPhone() {
    this.toast.success('Phone OTP resent.');
  }

  submitOtp() {
    const emailOk = this.emailOtp === '123456';
    const phoneOk = this.phoneOtp === '123456';
    this.emailOtpErr = !emailOk;
    this.phoneOtpErr = !phoneOk;
    if (!emailOk || !phoneOk) {
      this.toast.error('Incorrect OTP. Use 123456 for this demo.');
      return;
    }

    this.loading.set(true);
    this.auth
      .register({
        fullName: this.fullName.trim(),
        email: this.email.trim().toLowerCase(),
        phone: this.phone.trim(),
        dob: this.dob,
        emergencyContact: this.emergencyContact.trim(),
        password: this.password,
        confirmPassword: this.confirmPassword,
      })
      .subscribe({
        next: () => {
          this.step.set(3);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          // Duplicate-email or other server-side validation failure — send the
          // person back to step 1 so they can see and fix the offending field.
          if (err?.error?.message?.toLowerCase().includes('email')) {
            this.errors = { ...this.errors, email: err.error.message };
          }
          this.step.set(1);
        },
      });
  }
}