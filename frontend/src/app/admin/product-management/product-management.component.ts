import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

@Component({
  selector: 'app-product-management',
  templateUrl: './product-management.component.html',
  styleUrls: ['./product-management.component.css']
})
export class ProductManagementComponent implements OnInit, OnDestroy {
  products: Product[] = [];
  filteredProducts: Product[] = [];
  isLoading = false;
  error: string | null = null;
  searchTerm = '';
  
  // Search debounce
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;
  
  // Math for template
  Math = Math;
  
  // Form management
  showAddForm = false;
  showEditForm = false;
  selectedProduct: Product | null = null;
  productForm: FormGroup;
  selectedImage: File | null = null;
  imagePreview: string | null = null;

  sortField = 'id';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(
    private productService: ProductService,
    private fb: FormBuilder
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0)]],
      category: ['', Validators.required],
      brand: ['', Validators.required],
      stockQuantity: ['', [Validators.required, Validators.min(0)]],
      active: [true]
    });
  }

  ngOnInit(): void {
    console.log('ProductManagementComponent initialized');
    this.setupSearchDebounce();
    // Load products directly on init
    this.loadProducts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupSearchDebounce(): void {
    this.searchSubject.pipe(
      debounceTime(500), // Wait 500ms after user stops typing
      takeUntil(this.destroy$)
    ).subscribe(searchTerm => {
      console.log('Search debounce triggered with term:', searchTerm);
      this.currentPage = 0; // Reset to first page when searching
      this.loadProducts();
    });
  }

  loadProducts(): void {
    console.log('Loading products with params:', {
      page: this.currentPage,
      size: this.pageSize,
      query: this.searchTerm,
      sort: `${this.sortField},${this.sortDirection}`
    });
    this.isLoading = true;
    this.error = null;
    
    this.productService.getProducts({ 
      page: this.currentPage, 
      size: this.pageSize,
      query: this.searchTerm,
      sort: `${this.sortField},${this.sortDirection}`
    }).subscribe({
      next: (res) => {
        console.log('Products loaded successfully:', res);
        this.products = res.products;
        this.filteredProducts = res.products;
        this.totalItems = res.total;
        this.totalPages = Math.ceil(this.totalItems / this.pageSize);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading products:', err);
        this.error = 'Failed to load products: ' + (err.message || err);
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    console.log('onSearch called with searchTerm:', this.searchTerm);
    // Force update searchTerm from the input value
    const inputElement = document.querySelector('.search-input') as HTMLInputElement;
    if (inputElement) {
      this.searchTerm = inputElement.value;
      console.log('Updated searchTerm from input:', this.searchTerm);
    }
    // Use debounce with 500ms delay
    this.searchSubject.next(this.searchTerm);
  }

  // Pagination methods
  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadProducts();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadProducts();
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const startPage = Math.max(0, this.currentPage - 2);
    const endPage = Math.min(this.totalPages - 1, this.currentPage + 2);
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  showAddProductForm(): void {
    this.showAddForm = true;
    this.showEditForm = false;
    this.selectedProduct = null;
    this.productForm.reset({ active: true });
    this.selectedImage = null;
    this.imagePreview = null;
  }

  showEditProductForm(product: Product): void {
    this.showEditForm = true;
    this.showAddForm = false;
    this.selectedProduct = product;
    this.productForm.patchValue({
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      brand: product.brand || '',
      stockQuantity: product.stockQuantity,
      active: product.active
    });
    this.imagePreview = product.imageUrl || null;
    this.selectedImage = null;
  }

  onImageSelect(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedImage = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    if (this.productForm.valid) {
      const productData = this.productForm.value;
      
      if (this.showAddForm) {
        this.addProduct(productData);
      } else if (this.showEditForm && this.selectedProduct) {
        this.updateProduct(this.selectedProduct.id!, productData);
      }
    }
  }

  addProduct(productData: any): void {
    console.log('Adding product with data:', productData);
    console.log('Selected image:', this.selectedImage);
    
    this.isLoading = true;
    this.productService.createProduct(productData, this.selectedImage || undefined).subscribe({
      next: (newProduct) => {
        console.log('Product created successfully:', newProduct);
        this.products.unshift(newProduct);
        this.onSearch();
        this.closeForm();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error creating product:', err);
        this.error = 'Failed to add product: ' + (err.message || err);
        this.isLoading = false;
      }
    });
  }

  updateProduct(productId: number, productData: any): void {
    this.isLoading = true;
    this.productService.updateProduct(productId, productData, this.selectedImage || undefined).subscribe({
      next: (updatedProduct) => {
        const index = this.products.findIndex(p => p.id === productId);
        if (index !== -1) {
          this.products[index] = updatedProduct;
          this.onSearch();
        }
        this.closeForm();
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to update product';
        this.isLoading = false;
      }
    });
  }

  deleteProduct(product: Product): void {
    if (confirm(`Are you sure you want to delete "${product.name}"?`)) {
      this.isLoading = true;
      this.productService.deleteProduct(product.id!).subscribe({
        next: () => {
          this.products = this.products.filter(p => p.id !== product.id);
          this.onSearch();
          this.isLoading = false;
        },
        error: (err) => {
          this.error = 'Failed to delete product';
          this.isLoading = false;
        }
      });
    }
  }

  toggleProductStatus(product: Product): void {
    const updateData = {
      id: product.id!,
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      brand: product.brand || '',
      stockQuantity: product.stockQuantity,
      active: !product.active
    };
    
    this.productService.updateProduct(product.id!, updateData).subscribe({
      next: (updatedProduct) => {
        const index = this.products.findIndex(p => p.id === product.id);
        if (index !== -1) {
          this.products[index] = updatedProduct;
          this.onSearch();
        }
      },
      error: (err) => {
        this.error = 'Failed to update product status';
      }
    });
  }

  closeForm(): void {
    this.showAddForm = false;
    this.showEditForm = false;
    this.selectedProduct = null;
    this.productForm.reset();
    this.selectedImage = null;
    this.imagePreview = null;
    this.error = null;
  }

  exportProducts(): void {
    const csvContent = this.convertToCSV(this.filteredProducts);
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'products.csv';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private convertToCSV(products: Product[]): string {
    const headers = ['ID', 'Name', 'Description', 'Price', 'Category', 'Brand', 'Stock', 'Active'];
    const rows = products.map(p => [
      p.id,
      p.name,
      p.description,
      p.price,
      p.category,
      p.brand,
      p.stockQuantity,
      p.active ? 'Yes' : 'No'
    ]);
    
    return [headers, ...rows].map(row => row.join(',')).join('\n');
  }

  onSort(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0;
    this.loadProducts();
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) return '';
    return this.sortDirection === 'asc' ? '▲' : '▼';
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString('vi-VN');
  }
}
