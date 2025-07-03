import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css']
})
export class AdminLayoutComponent implements OnInit {
  currentUser: any = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Lấy thông tin user hiện tại từ localStorage
    const userStr = localStorage.getItem('currentUser');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
  }

  /**
   * Toggle sidebar cho mobile view
   * Hiển thị/ẩn sidebar trên thiết bị di động
   */
  toggleSidebar(): void {
    const sidebar = document.querySelector('.sidebar') as HTMLElement;
    if (sidebar) {
      sidebar.classList.toggle('open');
    }
  }

  /**
   * Đăng xuất khỏi hệ thống
   * Xóa token và chuyển về trang login
   */
  logout(): void {
    // Xóa token và user info
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    
    // Chuyển về trang login
    this.router.navigate(['/login']);
  }
} 