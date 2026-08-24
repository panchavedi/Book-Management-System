import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  kind: ToastKind;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private nextId = 1;

  show(message: string, kind: ToastKind = 'info'): void {
    const toast = { id: this.nextId++, kind, message };
    this.toasts.update((items) => [...items, toast]);
    window.setTimeout(() => this.dismiss(toast.id), 3800);
  }

  dismiss(id: number): void {
    this.toasts.update((items) => items.filter((toast) => toast.id !== id));
  }
}
