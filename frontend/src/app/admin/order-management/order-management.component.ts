import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../services/order.service';
import { Order } from '../../models/order.model';

@Component({
  selector: 'app-order-management',
  templateUrl: './order-management.component.html',
  styleUrls: ['./order-management.component.css']
})
export class OrderManagementComponent implements OnInit {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  isLoading = false;
  error: string | null = null;
  
  // Modal states
  showOrderDetail = false;
  selectedOrder: Order | null = null;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  hasNext = false;
  hasPrevious = false;
  
  // Status options for dropdown & filter
  statusOptions = [
    { value: 'ALL', label: 'Tất cả' },
    { value: 'PENDING', label: 'Chờ xử lý' },
    { value: 'CONFIRMED', label: 'Đã xác nhận' },
    { value: 'PROCESSING', label: 'Đang xử lý' },
    { value: 'SHIPPING', label: 'Đang giao hàng' },
    { value: 'DELIVERED', label: 'Đã giao hàng' },
    { value: 'CANCELLED', label: 'Admin hủy' },
    { value: 'CUSTOMER_CANCELLED', label: 'Khách hàng hủy' }
  ];
  selectedStatusFilter = 'ALL';

  sortField = 'orderDate';
  sortDirection: 'asc' | 'desc' = 'desc';

  keyword: string = '';

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    const sortParam = `${this.sortField},${this.sortDirection}`;
    this.orderService.getAllOrders(this.currentPage, this.pageSize, this.selectedStatusFilter, sortParam, this.keyword).subscribe({
      next: (response) => {
        this.orders = response.content || [];
        this.filteredOrders = this.orders;
        this.totalPages = response.totalPages || 0;
        this.totalElements = response.totalElements || 0;
        this.hasNext = response.hasNext || false;
        this.hasPrevious = response.hasPrevious || false;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load orders';
        this.isLoading = false;
      }
    });
  }

  searchOrders(): void {
    this.currentPage = 0;
    this.loadOrders();
  }

  // Filter orders by selected status
  applyStatusFilter(): void {
    this.currentPage = 0; // Reset to first page when changing filter
    this.loadOrders();
  }

  // Khi nhấn nút filter
  onStatusFilterChange(status: string): void {
    this.selectedStatusFilter = status;
    this.applyStatusFilter();
  }

  // Pagination methods
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadOrders();
    }
  }

  nextPage(): void {
    if (this.hasNext) {
      this.goToPage(this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.hasPrevious) {
      this.goToPage(this.currentPage - 1);
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    
    if (this.totalPages <= maxVisiblePages) {
      // Hiển thị tất cả các trang nếu tổng số trang <= 5
      for (let i = 0; i < this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Hiển thị 5 trang xung quanh trang hiện tại
      let startPage = Math.max(0, this.currentPage - 2);
      let endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);
      
      // Điều chỉnh startPage nếu endPage đã đạt giới hạn
      if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
      }
      
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  getEndIndex(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  showOrderDetails(order: Order): void {
    this.selectedOrder = order;
    this.showOrderDetail = true;
  }

  closeOrderDetail(): void {
    this.showOrderDetail = false;
    this.selectedOrder = null;
  }

  // Sửa lại hàm này để nhận trực tiếp newStatus thay vì event
  onStatusChange(newStatus: string, order: Order): void {
    // Gọi hàm updateOrderStatus với status mới
    this.updateOrderStatus(order, newStatus);
  }

  updateOrderStatus(order: Order, newStatus: string): void {
    if (confirm(`Bạn có chắc muốn cập nhật trạng thái đơn hàng #${order.id} thành "${newStatus}"?`)) {
      this.isLoading = true;
      this.orderService.updateOrderStatus(order.id!, newStatus).subscribe({
        next: (response) => {
          if (response.success) {
            // Update the order in the list
            const index = this.orders.findIndex(o => o.id === order.id);
            if (index !== -1) {
              this.orders[index] = response.order;
            }
            this.applyStatusFilter();
            this.showToast('Cập nhật trạng thái thành công!', 'success');
          } else {
            this.showToast(response.message || 'Không thể cập nhật trạng thái', 'error');
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error updating order status:', err);
          let errorMessage = 'Không thể cập nhật trạng thái đơn hàng';
          if (err.error && err.error.message) {
            errorMessage = err.error.message;
          }
          this.showToast(errorMessage, 'error');
          this.isLoading = false;
        }
      });
    }
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

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'CONFIRMED': return 'status-confirmed';
      case 'PROCESSING': return 'status-processing';
      case 'SHIPPING': return 'status-shipping';
      case 'DELIVERED': return 'status-delivered';
      case 'CANCELLED': return 'status-cancelled';
      case 'CUSTOMER_CANCELLED': return 'status-customer-cancelled';
      default: return 'status-pending';
    }
  }

  getStatusLabel(status: string): string {
    const option = this.statusOptions.find(opt => opt.value === status);
    return option ? option.label : status;
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'Không có ngày';
    // Parse ngày từ backend (UTC), cộng thêm 7 tiếng để ra giờ Việt Nam
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

  getTotalQuantity(orderItems: any[]): number {
    return orderItems.reduce((sum, item) => sum + item.quantity, 0);
  }

  getStatusBtnClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'btn-pending';
      case 'CONFIRMED': return 'btn-confirmed';
      case 'PROCESSING': return 'btn-processing';
      case 'SHIPPING': return 'btn-shipping';
      case 'DELIVERED': return 'btn-delivered';
      case 'CANCELLED': return 'btn-cancelled';
      case 'CUSTOMER_CANCELLED': return 'btn-customer-cancelled';
      case 'ALL': return 'btn-all';
      default: return '';
    }
  }

  onSort(field: string): void {
    if (this.sortField === field) {
      // Đảo chiều sort nếu nhấn lại cùng field
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0;
    this.loadOrders();
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) return '';
    return this.sortDirection === 'asc' ? '▲' : '▼';
  }
}
