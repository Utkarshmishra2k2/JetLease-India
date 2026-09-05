import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

/** Exact port of app.js#toast() — .toast-wrap / .toast[.success|.error] markup. */
@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-wrap">
      <div class="toast" [ngClass]="t.type" *ngFor="let t of toast.toasts()" (click)="toast.dismiss(t.id)">
        {{ t.text }}
      </div>
    </div>
  `,
})
export class ToastComponent {
  toast = inject(ToastService);
}
