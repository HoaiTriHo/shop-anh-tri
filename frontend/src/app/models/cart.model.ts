import { Product } from './product.model';

export interface CartItem {
  id?: number;
  product: Product;
  quantity: number;
  price: number; // Price at time of adding to cart
}

export interface Cart {
  id?: number;
  userId: number;
  items: CartItem[];
  totalAmount: number;
  itemCount: number;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface AddToCartRequest {
  productId: number;
  quantity: number;
}

export interface UpdateCartItemRequest {
  itemId: number;
  quantity: number;
} 