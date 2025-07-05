import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, CreateOrderRequest } from '../models/order.model';
import { CartItem } from '../models/cart.model';
import { environment } from '../../environments/environment';

export interface CheckoutRequest {
  customerName: string;
  customerEmail: string;
  shippingAddress: string;
  customerPhone: string;
  paymentMethod: string;
  cartItems: {
    productId: number;
    quantity: number;
  }[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly API_URL = `${environment.apiUrl}/api/orders`;

  constructor(private http: HttpClient) { }

  /**
   * Create new order
   */
  createOrder(orderRequest: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.API_URL, orderRequest);
  }

  /**
   * Get all orders for current user
   */
  getUserOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.API_URL);
  }

  /**
   * Get order by ID
   */
  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.API_URL}/${id}`);
  }

  /**
   * Get all orders with pagination, status filter, and sort (Admin only)
   */
  getAllOrders(page: number = 0, size: number = 10, status?: string, sort: string = 'orderDate,desc', keyword?: string): Observable<any> {
    let url = `${this.API_URL}/admin/all?page=${page}&size=${size}&sort=${sort}`;
    if (status && status !== 'ALL') {
      url += `&status=${status}`;
    }
    if (keyword && keyword.trim() !== '') {
      url += `&keyword=${encodeURIComponent(keyword.trim())}`;
    }
    return this.http.get<any>(url);
  }

  /**
   * Update order status (Admin only)
   */
  updateOrderStatus(id: number, status: string): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/admin/${id}/status?status=${status}`, {});
  }

  /**
   * Cancel order
   */
  cancelOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Cancel order by user (only if status is PENDING)
   * Returns response with success status and message
   */
  cancelOrderByUser(id: number): Observable<any> {
    return this.http.put<any>(`${environment.apiUrl}/api/orders/${id}/cancel`, {});
  }

  /**
   * Create order from cart (checkout)
   * Converts cart items to order and saves to database
   */
  checkout(checkoutRequest: CheckoutRequest): Observable<Order> {
    return this.http.post<Order>(`${environment.apiUrl}/api/orders/checkout`, checkoutRequest);
  }

  /**
   * Get all orders for current user
   */
  getMyOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${environment.apiUrl}/api/orders/my-orders`);
  }

  /**
   * Convert cart items to checkout request format
   */
  convertCartToCheckoutRequest(
    cartItems: CartItem[],
    customerName: string,
    customerEmail: string,
    shippingAddress: string,
    customerPhone: string,
    paymentMethod: string
  ): CheckoutRequest {
    return {
      customerName,
      customerEmail,
      shippingAddress,
      customerPhone,
      paymentMethod,
      cartItems: cartItems.map(item => ({
        productId: item.product.id!,
        quantity: item.quantity
      }))
    };
  }

  /**
   * Get all orders for current user with filtering, sorting and pagination
   */
  getMyOrdersWithFilter(params: any): Observable<any> {
    let url = `${environment.apiUrl}/api/orders/my-orders?`;
    const queryParams = new URLSearchParams();
    
    if (params.status) queryParams.append('status', params.status);
    if (params.startDate) queryParams.append('startDate', params.startDate);
    if (params.endDate) queryParams.append('endDate', params.endDate);
    if (params.sort) queryParams.append('sort', params.sort);
    if (params.page !== undefined) queryParams.append('page', params.page.toString());
    if (params.size !== undefined) queryParams.append('size', params.size.toString());
    
    url += queryParams.toString();
    return this.http.get<any>(url);
  }
}
