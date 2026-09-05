import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PaymentService } from '../../core/services/payment.service';
import { ToastService } from '../../core/services/toast.service';
import { Booking, Payment } from '../../core/models/models';
import { fmtINR, fmtDate, statusClass } from '../../core/util/validators';

const BANK_DETAILS = {
  bankName: 'HDFC Bank, Bandra Kurla Complex Branch',
  accountHolder: 'JetLease Aviation Private Limited',
  accountNumber: '50200012345678',
  ifsc: 'HDFC0000123',
};

/** Exact port of payment.html + payment.js. */
@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './payment.component.html',
})
export class PaymentComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private paymentService = inject(PaymentService);
  private toast = inject(ToastService);

  bookingId = '';
  booking: Booking | null = null;
  notFound = false;
  existingPayment: Payment | null = null;
  isResubmission = false;
  transactionId = '';
  submitting = false;

  bank = BANK_DETAILS;
  fmtINR = fmtINR;
  fmtDate = fmtDate;
  statusClass = statusClass;

  ngOnInit(): void {
    this.bookingId = this.route.snapshot.paramMap.get('bookingId') || '';
    this.paymentService.payable(this.bookingId).subscribe({
      next: (b) => {
        this.booking = b;
        this.loadExistingPayment();
      },
      error: () => (this.notFound = true),
    });
  }

  private loadExistingPayment() {
    this.paymentService.my().subscribe((payments) => {
      const attempts = payments
        .filter((p) => p.bookingId === this.bookingId)
        .sort((a, b) => +new Date(a.submittedAt) - +new Date(b.submittedAt));
      const latest = attempts[attempts.length - 1] || null;
      this.isResubmission = attempts.length > 0;

      if (latest && latest.status !== 'Rejected') {
        this.existingPayment = latest;
      } else {
        this.existingPayment = null;
      }
    });
  }

  get statusMessage(): string {
    const s = this.existingPayment?.status;
    if (s === 'PENDING_VERIFICATION') return "Our finance desk verifies bank transfers within 2 business hours. You'll be notified once confirmed.";
    if (s === 'VERIFIED') return 'Payment verified. Your lease agreement (if applicable) is being prepared.';
    if (s === 'RETURNED') return 'This payment has been returned because the lease agreement for this booking was rejected. Contact support if you have questions about your refund.';
    return 'This payment was rejected. Please contact support or resubmit with a valid transaction ID.';
  }

  submit() {
    const txn = this.transactionId.trim();
    if (txn.length < 5) {
      this.toast.error('Enter a valid transaction ID.');
      return;
    }
    this.submitting = true;
    this.paymentService.pay(this.bookingId, txn).subscribe({
      next: (payment) => {
        this.toast.success('Payment submitted — awaiting verification.');
        this.existingPayment = payment;
        if (this.booking) this.booking = { ...this.booking, status: 'Pending Verification' };
        this.submitting = false;
      },
      error: () => (this.submitting = false),
    });
  }
}