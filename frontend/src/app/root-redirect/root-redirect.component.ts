import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RoleRoutingService } from '../services/role-routing.service';

@Component({
  selector: 'app-root-redirect',
  template: '<div>Redirecting...</div>'
})
export class RootRedirectComponent implements OnInit {

  constructor(
    private router: Router,
    private authService: AuthService,
    private roleRoutingService: RoleRoutingService
  ) { }

  ngOnInit(): void {
    // Handle redirect based on user role
    this.handleRootRedirect();
  }

  /**
   * Handle redirect from root route based on user authentication and role
   */
  private handleRootRedirect(): void {
    if (!this.authService.isLoggedIn()) {
      // User not logged in, redirect to home page
      this.router.navigate(['/home']);
      return;
    }

    // User is logged in, use role-based routing
    this.roleRoutingService.navigateAfterLogin();
  }
} 