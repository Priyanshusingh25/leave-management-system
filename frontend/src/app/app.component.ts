import { Component, OnInit } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  constructor(
    public authService: AuthService,
    private router: Router
) {}

  ngOnInit() {
    if (this.authService.isAuthenticated()) {
      const user = this.authService.getCurrentUser();
      if (user?.role === 'MANAGER') {
        this.router.navigate(['/manager/dashboard']);
      } else {
        this.router.navigate(['/employee/dashboard']);
      }
    }
  }

  logout() {
    this.authService.logout();
  }
}
