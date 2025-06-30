import { Product } from './product.model';

export interface OrderItem {
  id?: number;
  product: Product;
  quantity: number;
  price: number;
}

export interface Order {
  id?: number;
  userId: number;
  items: OrderItem[];
  totalAmount: number;
  status: 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  shippingAddress: string;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface CreateOrderRequest {
  items: Array<{
    productId: number;
    quantity: number;
  }>;
  shippingAddress: string;
} 