import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../services/order.service';
import { Order } from '../../models/order.model';

@Component({
  selector: 'app-my-orders',
  templateUrl: './my-orders.component.html',
  styleUrls: ['./my-orders.component.css']
})
export class MyOrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  error = '';
  
  // Filter options
  selectedStatus = '';
  startDate = '';
  endDate = '';
  sortBy = 'orderDate,desc';
  
  // Pagination
  currentPage = 0;
  pageSize = 5;
  totalElements = 0;
  totalPages = 0;
  
  // Cancel order state
  cancellingOrderId: number | null = null;
  
  // Math object for template
  Math = Math;
  
  // Available status options
  statusOptions = [
    { value: '', label: 'Tất cả trạng thái' },
    { value: 'PENDING', label: 'Chờ xác nhận' },
    { value: 'CONFIRMED', label: 'Đã xác nhận' },
    { value: 'PROCESSING', label: 'Đang xử lý' },
    { value: 'SHIPPING', label: 'Đang giao' },
    { value: 'DELIVERED', label: 'Đã giao' },
    { value: 'CANCELLED', label: 'Admin hủy' },
    { value: 'CUSTOMER_CANCELLED', label: 'Khách hàng hủy' }
  ];

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    // Đảm bảo mặc định sort theo thời gian mới nhất
    this.sortBy = 'orderDate,desc';
    this.fetchOrders();
  }

  fetchOrders(): void {
    this.loading = true;
    this.error = '';
    
    // Build query parameters
    const params: any = {
      page: this.currentPage,
      size: this.pageSize
    };
    if (this.selectedStatus) params.status = this.selectedStatus;
    if (this.startDate) params.startDate = this.startDate;
    if (this.endDate) params.endDate = this.endDate;
    if (this.sortBy) params.sort = this.sortBy;
    
    console.log('Fetching orders with params:', params);
    
    this.orderService.getMyOrdersWithFilter(params).subscribe({
      next: (response) => {
        console.log('Orders response:', response);
        this.orders = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.currentPage;
        this.loading = false;
        
        console.log(`Loaded ${this.orders.length} orders, total: ${this.totalElements}, pages: ${this.totalPages}`);
      },
      error: (err) => {
        console.error('Error fetching orders:', err);
        this.error = 'Không thể tải danh sách đơn hàng.';
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.currentPage = 0; // Reset to first page when filter changes
    this.fetchOrders();
  }

  clearFilters(): void {
    this.selectedStatus = '';
    this.startDate = '';
    this.endDate = '';
    this.sortBy = 'orderDate,desc';
    this.currentPage = 0;
    this.fetchOrders();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.fetchOrders();
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    
    if (this.totalPages <= maxVisiblePages) {
      for (let i = 0; i < this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      let start = Math.max(0, this.currentPage - 2);
      let end = Math.min(this.totalPages - 1, start + maxVisiblePages - 1);
      
      if (end - start < maxVisiblePages - 1) {
        start = Math.max(0, end - maxVisiblePages + 1);
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  // Chuyển đổi status từ backend sang tiếng Việt
  getStatusLabel(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'Chờ xác nhận',
      'CONFIRMED': 'Đã xác nhận', 
      'PROCESSING': 'Đang xử lý',
      'SHIPPING': 'Đang giao',
      'DELIVERED': 'Đã giao',
      'CANCELLED': 'Đã hủy',
      'CUSTOMER_CANCELLED': 'Khách hàng hủy'
    };
    return statusMap[status] || status;
  }

  // Lấy CSS class cho status badge
  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'status-pending';
      case 'CONFIRMED':
        return 'status-confirmed';
      case 'PROCESSING':
        return 'status-processing';
      case 'SHIPPING':
        return 'status-shipping';
      case 'DELIVERED':
        return 'status-delivered';
      case 'CANCELLED':
        return 'status-cancelled';
      case 'CUSTOMER_CANCELLED':
        return 'status-customer-cancelled';
      default:
        return 'status-default';
    }
  }

  /**
   * Format ngày đặt hàng theo định dạng Việt Nam
   */
  formatOrderDate(dateString: string | undefined): string {
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
   * Hủy đơn hàng
   * Chỉ cho phép hủy khi status là PENDING (đang chờ xử lý)
   */
  cancelOrder(orderId: number): void {
    if (this.cancellingOrderId !== null) {
      return; // Đang xử lý hủy đơn hàng khác
    }

    // Log debug
    const order = this.orders.find(o => o.id === orderId);
    console.log('[DEBUG] Hủy đơn hàng:', orderId, 'Status:', order?.status);

    this.cancellingOrderId = orderId;
    
    this.orderService.cancelOrderByUser(orderId).subscribe({
      next: (response) => {
        if (response.success) {
          this.showToast('Đơn hàng đã được hủy thành công!', 'success');
          this.fetchOrders();
        } else {
          // Nếu BE trả về message lỗi, show message đó, nếu không thì show mặc định
          const msg = response.message || 'Đơn hàng đã xác nhận, không được hủy';
          this.showToast(msg, 'error');
        }
        this.cancellingOrderId = null;
      },
      error: (err) => {
        console.error('[DEBUG] Error cancelling order:', err);
        // Luôn show toast rõ ràng cho user
        let errorMessage = 'Đơn hàng đã xác nhận, không được hủy';
        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        }
        this.showToast(errorMessage, 'error');
        this.cancellingOrderId = null;
      }
    });
  }

  /**
   * Hiển thị toast notification
   */
  private showToast(message: string, type: 'success' | 'error'): void {
    // Tạo toast element
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <div class="toast-content">
        <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i>
        <span>${message}</span>
      </div>
    `;
    
    // Thêm CSS cho toast
    toast.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      background: ${type === 'success' ? '#28a745' : '#dc3545'};
      color: white;
      padding: 16px 20px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.3);
      z-index: 9999;
      max-width: 400px;
      animation: slideInRight 0.3s ease-out;
    `;
    
    // Thêm CSS animation
    const style = document.createElement('style');
    style.textContent = `
      @keyframes slideInRight {
        from {
          transform: translateX(100%);
          opacity: 0;
        }
        to {
          transform: translateX(0);
          opacity: 1;
        }
      }
      .toast-content {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .toast-content i {
        font-size: 16px;
      }
    `;
    document.head.appendChild(style);
    
    // Thêm vào DOM
    document.body.appendChild(toast);
    
    // Tự động xóa sau 4 giây
    setTimeout(() => {
      toast.style.animation = 'slideInRight 0.3s ease-out reverse';
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
        if (style.parentNode) {
          style.parentNode.removeChild(style);
        }
      }, 300);
    }, 4000);
  }
}
