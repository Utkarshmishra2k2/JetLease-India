import { Component, HostListener, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

/**
 * Exact port of app.js#mountNav(). Markup, classes and behavior match the
 * reference 1:1: an always-empty .nav-links container (unused in the
 * original too), a theme toggle, and either Log In/Join buttons or a
 * profile-icon dropdown depending on auth state. On the customer dashboard
 * page the right side is hidden entirely because the dashboard has its own
 * topbar with profile/logout controls — pass [activeSlug]="'dashboard'" there.
 */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <header class="nav">
      <div class="nav-inner">
        <a [routerLink]="logoHref" class="brand">
          <span class="mark">JL</span>
          <span>JETLEASE<small>Private Aviation · India</small></span>
        </a>
        <nav class="nav-links"></nav>
        <div class="nav-cta">
          <button class="theme-toggle" (click)="theme.toggle()" aria-label="Toggle theme" style="font-size:18px;">
            {{ theme.theme() === 'light' ? '☀️' : '🌙' }}
          </button>

          <ng-container *ngIf="activeSlug !== 'dashboard'">
            <div style="position:relative;" *ngIf="auth.isLoggedIn(); else guestLinks">
              <button class="theme-toggle" (click)="toggleMenu($event)" aria-label="Account menu" style="font-size:18px;">👤</button>
              <div class="card profile-menu" [style.display]="menuOpen ? 'flex' : 'none'" style="flex-direction: column">
                <div class="pm-header">
                  <div class="pm-avatar">👤</div>
                  <div>
                    <div class="pm-signed-in">Signed in as</div>
                    <p class="pm-email">{{ auth.session()?.email }}</p>
                  </div>
                </div>
                <button class="btn btn-primary btn-block btn-sm" (click)="logout()">Log Out</button>
              </div>
            </div>
            <ng-template #guestLinks>
              <a routerLink="/login" class="btn btn-ghost btn-sm">Log In</a>
              <a routerLink="/register" class="btn btn-primary btn-sm">Join JetLease</a>
            </ng-template>
          </ng-container>
        </div>
      </div>
    </header>
  `,
})
export class NavbarComponent {
  /** Pass 'dashboard' from the customer dashboard page to hide the right-side nav controls. */
  @Input() activeSlug?: string;

  auth = inject(AuthService);
  theme = inject(ThemeService);
  private router = inject(Router);
  menuOpen = false;

  get logoHref(): string {
    const user = this.auth.session();
    if (!user) return '/';
    return user.role === 'admin' ? '/admin' : '/dashboard';
  }

  toggleMenu(e: Event) {
    e.stopPropagation();
    this.menuOpen = !this.menuOpen;
  }

  @HostListener('document:click')
  closeMenu() {
    this.menuOpen = false;
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}