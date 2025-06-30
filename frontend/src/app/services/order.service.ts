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
  private readonly API_URL = 'http://localhost:8080/api/orders';

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
   * Get all orders (Admin only)
   */
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.API_URL}/admin/all`);
  }

  /**
   * Update order status (Admin only)
   */
  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${this.API_URL}/admin/${id}/status?status=${status}`, {});
  }

  /**
   * Cancel order
   */
  cancelOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
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
}
