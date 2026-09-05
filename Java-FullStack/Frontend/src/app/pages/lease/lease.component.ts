import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LeaseService } from '../../core/services/lease.service';
import { ToastService } from '../../core/services/toast.service';
import { Lease } from '../../core/models/models';
import { fmtDate, statusClass } from '../../core/util/validators';

/** Adapted from lease.html + lease.js's viewLease/signLease/downloadLease — as a
 * per-lease detail route (matching this app's /lease/:id routing) rather than the
 * reference's single list+modal page; the dashboard's "Lease Agreements" tab already
 * provides that list view and links here to open one. */
@Component({
  selector: 'app-lease',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './lease.component.html',
})
export class LeaseComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private leaseService = inject(LeaseService);
  private toast = inject(ToastService);

  lease = signal<Lease | null>(null);
  contractText = signal('');
  submitting = signal(false);
  leaseId = '';
  notFound = false;

  signModalOpen = false;
  legalName = '';

  fmtDate = fmtDate;
  statusClass = statusClass;

  ngOnInit(): void {
    this.leaseId = this.route.snapshot.paramMap.get('id') || '';
    this.load();
  }

  load() {
    this.leaseService.get(this.leaseId).subscribe({
      next: (res) => {
        this.lease.set(res.lease);
        this.contractText.set(res.contractText);
      },
      error: () => (this.notFound = true),
    });
  }

  openSignModal() {
    this.legalName = '';
    this.signModalOpen = true;
  }
  closeSignModal() {
    this.signModalOpen = false;
  }

  sign() {
    const name = this.legalName.trim();
    if (name.length < 3) {
      this.toast.error('Signature name required.');
      return;
    }
    this.submitting.set(true);
    this.leaseService.sign(this.leaseId, name).subscribe({
      next: () => {
        this.toast.success('Agreement signed. Awaiting admin approval.');
        this.signModalOpen = false;
        this.submitting.set(false);
        this.load();
      },
      error: () => (this.submitting.set(false)),
    });
  }

  download() {
    const blob = new Blob([this.contractText()], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `JetLease-Agreement-${this.leaseId}.txt`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  }
}