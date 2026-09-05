import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  theme = signal<'dark' | 'light'>((localStorage.getItem('jl_theme') as 'dark' | 'light') || 'dark');

  constructor() {
    document.documentElement.setAttribute('data-theme', this.theme());
  }

  toggle() {
    const next = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(next);
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('jl_theme', next);
  }
}