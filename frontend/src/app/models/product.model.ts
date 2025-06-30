export interface Product {
  id?: number;
  name: string;
  description: string;
  price: number;
  imageUrl?: string;
  category: string;
  brand?: string;
  stockQuantity: number;
  active: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  isWishlisted?: boolean;
}

export interface ProductCreateRequest {
  name: string;
  description: string;
  price: number;
  category: string;
  brand?: string;
  stockQuantity: number;
  active?: boolean;
}

export interface ProductUpdateRequest extends ProductCreateRequest {
  id: number;
} 