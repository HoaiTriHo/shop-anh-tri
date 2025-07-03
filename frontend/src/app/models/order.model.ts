import { Product } from './product.model';

export interface OrderItem {
  id?: number;
  productId: number;
  productName: string;
  productImageUrl?: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id?: number;
  userId: number;
  customerName?: string;
  customerEmail?: string;
  shippingAddress: string;
  customerPhone?: string;
  totalPrice: number;
  orderDate?: string;
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED';
  paymentMethod?: string;
  paymentStatus?: string;
  orderItems: OrderItem[];
}

export interface CreateOrderRequest {
  customerName?: string;
  customerEmail?: string;
  shippingAddress: string;
  customerPhone?: string;
  paymentMethod?: string;
  cartItems: Array<{
    productId: number;
    quantity: number;
  }>;
} 