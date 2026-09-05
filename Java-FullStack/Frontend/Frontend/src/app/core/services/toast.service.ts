import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  text: string;
  type: 'success' | 'error' | 'info';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private counter = 0;
  toasts = signal<ToastMessage[]>([]);

  show(text: string, type: ToastMessage['type'] = 'info') {
    const id = ++this.counter;
    this.toasts.update((list) => [...list, { id, text, type }]);
    setTimeout(() => this.dismiss(id), 3400);
  }

  success(text: string) { this.show(text, 'success'); }
  error(text: string) { this.show(text, 'error'); }
  info(text: string) { this.show(text, 'info'); }

  dismiss(id: number) {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }
}