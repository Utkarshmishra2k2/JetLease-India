import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Exact port of app.js#mountFooter(). */
@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer class="site-footer">
      <div class="container">
        <div class="footer-grid">
          <div>
            <a routerLink="/" class="brand">
              <span class="mark">JL</span>
              <span>JETLEASE<small>Private Aviation · India</small></span>
            </a>
            <p style="margin-top:14px;max-width:300px;">Premium private jet and helicopter charter and aircraft lease management — built for India's boardrooms and beyond.</p>
          </div>
          <div>
            <h4>Explore</h4>
            <ul>
              <li><a routerLink="/" fragment="fleet">Fleet</a></li>
              <li><a routerLink="/booking">Book a Flight</a></li>
              <li><a routerLink="/" fragment="faq">FAQ</a></li>
            </ul>
          </div>
          <div>
            <h4>Account</h4>
            <ul>
              <li><a routerLink="/login">Log In</a></li>
              <li><a routerLink="/register">Register</a></li>
              <li><a routerLink="/dashboard">Customer Dashboard</a></li>
              <li><a routerLink="/admin-login">Admin Portal</a></li>
            </ul>
          </div>
          <div>
            <h4>Contact</h4>
            <ul>
              <li>ops&#64;jetlease.in</li>
              <li>+91 22 6789 0000</li>
              <li>BKC, Mumbai, India</li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <span>&copy; {{ year }} JetLease India.</span>
          <span>Team Uddan</span>
        </div>
      </div>
    </footer>
  `,
})
export class FooterComponent {
  year = new Date().getFullYear();
}
