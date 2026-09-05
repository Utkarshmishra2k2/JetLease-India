import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GuestService } from '../../core/services/guest.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { Aircraft, Faq, Testimonial } from '../../core/models/models';
import { VALIDATORS, fmtINR, statusClass } from '../../core/util/validators';

/** Exact port of index.html + its inline jl:ready script. */
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  private guestService = inject(GuestService);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  fleet: Aircraft[] = [];
  categories: string[] = ['All'];
  activeCategory = 'All';
  filteredFleet: Aircraft[] = [];

  faqs: Faq[] = [];
  openFaqIndex: number | null = null;
  testimonials: Testimonial[] = [];

  stats = [
    ['12', 'Aircraft Across 6 Categories'],
    ['2,400+', 'Hours Flown'],
    ['96%', 'On-time Dispatch'],
    ['24/7', 'Ops Desk Coverage'],
  ];

  cName = '';
  cPhone = '';
  cEmail = '';
  cMessage = '';
  errors: Record<string, string> = {};
  submitting = false;

  fmtINR = fmtINR;
  statusClass = statusClass;

  initials(name: string): string {
    return (name || '')
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0]?.toUpperCase())
      .join('');
  }

  ngOnInit(): void {
    this.guestService.fleet().subscribe((f) => {
      this.fleet = f;
      this.categories = ['All', ...Array.from(new Set(f.map((a) => a.category)))];
      this.renderFleet('All');
    });
    this.guestService.faq().subscribe((f) => (this.faqs = f));
    this.guestService.testimonials().subscribe((t) => (this.testimonials = t));
  }

  renderFleet(cat: string) {
    this.activeCategory = cat;
    this.filteredFleet = cat === 'All' ? this.fleet : this.fleet.filter((a) => a.category === cat);
  }

  toggleFaq(i: number) {
    this.openFaqIndex = this.openFaqIndex === i ? null : i;
  }

  fieldClass(key: string): string {
    if (!(key in this.errors)) return '';
    return this.errors[key] ? 'invalid' : 'valid';
  }

  submitContact() {
    const errors: Record<string, string> = {};
    errors['cName'] = VALIDATORS.name(this.cName);
    errors['cPhone'] = VALIDATORS.phone10(this.cPhone);
    errors['cEmail'] = VALIDATORS.email(this.cEmail);
    errors['cMessage'] = VALIDATORS.message(this.cMessage);
    this.errors = errors;

    if (Object.values(errors).some((e) => e)) {
      this.toast.error('Please fix the highlighted fields.');
      return;
    }

    this.submitting = true;
    this.guestService
      .contact({ name: this.cName, phone: this.cPhone, email: this.cEmail, message: this.cMessage })
      .subscribe({
        next: () => {
          this.toast.success('Message sent — our ops desk will respond within 4 hours.');
          this.cName = this.cPhone = this.cEmail = this.cMessage = '';
          this.errors = {};
          this.submitting = false;
        },
        error: () => {
          this.toast.error('Something went wrong sending your message. Please try again.');
          this.submitting = false;
        },
      });
  }
}