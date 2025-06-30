import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, ProductCreateRequest, ProductUpdateRequest } from '../models/product.model';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly API_URL = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) { }

  /**
   * Get all products
   */
  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.API_URL);
  }

  /**
   * Get product by ID
   */
  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.API_URL}/${id}`);
  }

  /**
   * Create new product (Admin only)
   */
  createProduct(product: ProductCreateRequest, imageFile?: File): Observable<Product> {
    const formData = new FormData();
    formData.append('product', JSON.stringify(product));
    
    if (imageFile) {
      formData.append('image', imageFile);
    }
    
    return this.http.post<Product>(this.API_URL, formData);
  }

  /**
   * Update product (Admin only)
   */
  updateProduct(id: number, product: ProductUpdateRequest, imageFile?: File): Observable<Product> {
    const formData = new FormData();
    formData.append('product', JSON.stringify(product));
    
    if (imageFile) {
      formData.append('image', imageFile);
    }
    
    return this.http.put<Product>(`${this.API_URL}/${id}`, formData);
  }

  /**
   * Delete product (Admin only)
   */
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Search products by name
   */
  searchProducts(searchTerm: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.API_URL}/search?q=${searchTerm}`);
  }

  /**
   * Get products by category
   */
  getProductsByCategory(category: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.API_URL}/category/${category}`);
  }

  /**
   * Get all categories
   */
  getAllCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/categories`);
  }

  /**
   * Get products with pagination, search, and filter support
   * @param options Object with page, size, query, category, price, sort
   */
  getProducts(options: {
    page?: number,
    size?: number,
    query?: string,
    category?: string,
    price?: string,
    sort?: string
  } = {}): Observable<{ products: Product[], total: number }> {
    let params = new HttpParams();
    if (options.page !== undefined) params = params.set('page', options.page.toString());
    if (options.size !== undefined) params = params.set('size', options.size.toString());
    if (options.query) params = params.set('query', options.query);
    if (options.category) params = params.set('category', options.category);
    if (options.price) params = params.set('price', options.price);
    if (options.sort) params = params.set('sort', options.sort);
    return this.http.get<{ products: Product[], total: number }>(this.API_URL, { params })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any) {
    console.error('An error occurred:', error);
    return throwError('Something bad happened; please try again later.');
  }
}
