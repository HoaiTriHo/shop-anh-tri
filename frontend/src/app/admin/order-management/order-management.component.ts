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
    { value: 'CANCELLED', label: 'Đã hủy' }
  ];
  selectedStatusFilter = 'ALL';

  sortField = 'orderDate';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    const sortParam = `${this.sortField},${this.sortDirection}`;
    this.orderService.getAllOrders(this.currentPage, this.pageSize, this.selectedStatusFilter, sortParam).subscribe({
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

  onStatusChange(event: Event, order: Order): void {
    const selectElement = event.target as HTMLSelectElement;
    const newStatus = selectElement.value;
    this.updateOrderStatus(order, newStatus);
  }

  updateOrderStatus(order: Order, newStatus: string): void {
    if (confirm(`Bạn có chắc muốn cập nhật trạng thái đơn hàng #${order.id} thành "${newStatus}"?`)) {
      this.isLoading = true;
      this.orderService.updateOrderStatus(order.id!, newStatus).subscribe({
        next: (updatedOrder) => {
          // Update the order in the list
          const index = this.orders.findIndex(o => o.id === order.id);
          if (index !== -1) {
            this.orders[index] = updatedOrder;
          }
          this.applyStatusFilter();
          this.isLoading = false;
          alert('Cập nhật trạng thái thành công!');
        },
        error: (err) => {
          this.error = 'Failed to update order status';
          this.isLoading = false;
        }
      });
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'CONFIRMED': return 'status-confirmed';
      case 'PROCESSING': return 'status-processing';
      case 'SHIPPING': return 'status-shipping';
      case 'DELIVERED': return 'status-delivered';
      case 'CANCELLED': return 'status-cancelled';
      default: return 'status-pending';
    }
  }

  getStatusLabel(status: string): string {
    const option = this.statusOptions.find(opt => opt.value === status);
    return option ? option.label : status;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('vi-VN');
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
