import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../services/auth.service';

/**
 * Dashboard admin: Hiển thị tổng quan shop, biểu đồ, danh sách nhanh.
 * Gọi các API backend để lấy số liệu dashboard.
 */
@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  summary: any = {};
  revenueByDay: any[] = [];
  orderStatusStats: any = {};
  topProducts: any[] = [];
  recentOrders: any[] = [];
  loading = true;
  error = '';
  currentUser: any = null;
  exporting = false;
  exportFromDate: string = '';
  exportToDate: string = '';
  exportMessage: string = '';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Lấy thông tin user hiện tại
    const userStr = localStorage.getItem('currentUser');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    
    this.fetchDashboardData();
  }

  /**
   * Gọi tất cả API dashboard song song với authentication
   */
  fetchDashboardData() {
    this.loading = true;
    this.error = '';
    
    // Lấy token từ AuthService
    const token = this.authService.getToken();
    if (!token) {
      this.error = 'Chưa đăng nhập';
      this.loading = false;
      return;
    }

    // Tạo headers với token
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    const base = environment.apiUrl + '/api/admin/dashboard';
    
    Promise.all([
      this.http.get(base + '/summary', { headers }).toPromise(),
      this.http.get(base + '/revenue', { headers }).toPromise(),
      this.http.get(base + '/order-status', { headers }).toPromise(),
      this.http.get(base + '/top-products', { headers }).toPromise(),
      this.http.get(base + '/recent-orders', { headers }).toPromise(),
    ]).then(([summary, revenue, status, top, recent]) => {
      this.summary = summary;
      this.revenueByDay = revenue as any[];
      this.orderStatusStats = status;
      this.topProducts = top as any[];
      this.recentOrders = recent as any[];
      this.loading = false;
    }).catch(err => {
      console.error('Dashboard API error:', err);
      this.error = 'Lỗi tải dữ liệu dashboard: ' + (err.status || 'Unknown error');
      this.loading = false;
    });
  }

  /**
   * Tính % chiều cao cột doanh thu (so với max 7 ngày)
   */
  getRevenuePercent(value: number): number {
    if (!this.revenueByDay || this.revenueByDay.length === 0) return 0;
    const max = Math.max(...this.revenueByDay.map(d => +d.revenue || 0));
    if (max === 0) return 0;
    return Math.round((+value / max) * 100);
  }

  /**
   * Lấy danh sách key trạng thái đơn hàng (giữ nguyên từ DB)
   */
  getOrderStatusKeys(): string[] {
    return Object.keys(this.orderStatusStats || {});
  }

  /**
   * Lấy CSS class cho status badge
   */
  getStatusBadgeClass(status: string): string {
    const statusClassMap: { [key: string]: string } = {
      'PENDING': 'status-pending',
      'CONFIRMED': 'status-confirmed',
      'PROCESSING': 'status-processing',
      'SHIPPED': 'status-shipping',
      'DELIVERED': 'status-delivered',
      'CANCELLED': 'status-cancelled'
    };
    return statusClassMap[status] || 'status-pending';
  }

  /**
   * Lấy CSS class cho status item
   */
  getStatusItemClass(statusKey: string): string {
    const statusClassMap: { [key: string]: string } = {
      'PENDING': 'pending',
      'CONFIRMED': 'confirmed',
      'PROCESSING': 'processing',
      'SHIPPED': 'shipping',
      'DELIVERED': 'delivered',
      'CANCELLED': 'cancelled'
    };
    return statusClassMap[statusKey] || 'pending';
  }

  /**
   * Format ngày đặt hàng theo định dạng Việt Nam
   */
  formatOrderDate(dateString: string): string {
    if (!dateString) return 'Không có ngày';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
      return 'Ngày không hợp lệ';
    }
    // Cộng thêm 7 tiếng (25200000 ms)
    const vnDate = new Date(date.getTime() + 7 * 60 * 60 * 1000);
    const day = vnDate.getDate().toString().padStart(2, '0');
    const month = (vnDate.getMonth() + 1).toString().padStart(2, '0');
    const year = vnDate.getFullYear();
    const hours = vnDate.getHours().toString().padStart(2, '0');
    const minutes = vnDate.getMinutes().toString().padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }

  /**
   * Export báo cáo doanh thu ra Excel
   */
  onExportReport() {
    this.exporting = true;
    this.exportMessage = '';
    const params: any = {};
    if (this.exportFromDate) params.fromDate = this.exportFromDate;
    if (this.exportToDate) params.toDate = this.exportToDate;
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    const base = environment.apiUrl + '/api/admin/dashboard/export-report';
    this.http.get(base, { headers, params, responseType: 'blob' }).subscribe({
      next: (blob) => {
        // Tạo tên file
        const from = this.exportFromDate || 'all';
        const to = this.exportToDate || 'now';
        const filename = `shop-report-${from}-${to}.xlsx`;
        // Tải file
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);
        this.exporting = false;
        this.exportMessage = 'Xuất báo cáo thành công!';
      },
      error: (err) => {
        this.exporting = false;
        this.exportMessage = 'Lỗi xuất báo cáo!';
      }
    });
  }
} 