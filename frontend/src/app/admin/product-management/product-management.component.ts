import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Product, ProductCreateRequest, ProductUpdateRequest } from '../../models/product.model';

@Component({
  selector: 'app-product-management',
  templateUrl: './product-management.component.html',
  styleUrls: ['./product-management.component.css']
})
export class ProductManagementComponent implements OnInit {
  products: Product[] = [];
  filteredProducts: Product[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  
  // Form properties
  productForm!: FormGroup;
  isEditing = false;
  editingProductId: number | null = null;
  selectedImage: File | null = null;
  imagePreview: string | null = null;
  
  // Search and filter
  productSearchTerm = '';
  showAddForm = false;

  constructor(
    private productService: ProductService,
    private authService: AuthService,
    private formBuilder: FormBuilder
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.loadProducts();
  }

  /**
   * Initialize the product form
   */
  private initForm(): void {
    this.productForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: ['', [Validators.required, Validators.min(0)]],
      category: ['', [Validators.required]],
      brand: [''],
      stockQuantity: ['', [Validators.required, Validators.min(0)]],
      active: [true]
    });
  }

  /**
   * Load all products
   */
  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.filteredProducts = [...products];
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.errorMessage = 'Failed to load products. Please try again.';
        this.isLoading = false;
      }
    });
  }

  /**
   * Open add product form
   */
  openAddProductModal(): void {
    this.isEditing = false;
    this.editingProductId = null;
    this.resetForm();
    this.showAddForm = true;
  }

  /**
   * Edit product
   */
  editProduct(product: Product): void {
    this.isEditing = true;
    this.editingProductId = product.id!;
    this.showAddForm = true;
    
    // Populate form with product data
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
  }

  /**
   * View product details
   */
  viewProduct(product: Product): void {
    console.log('Viewing product:', product);
    // You could open a modal or navigate to a detail page
  }

  /**
   * Delete product
   */
  deleteProduct(product: Product): void {
    if (confirm(`Are you sure you want to delete "${product.name}"?`)) {
      this.productService.deleteProduct(product.id!).subscribe({
        next: () => {
          this.successMessage = 'Product deleted successfully!';
          this.loadProducts();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (error) => {
          console.error('Error deleting product:', error);
          this.errorMessage = 'Failed to delete product. Please try again.';
        }
      });
    }
  }

  /**
   * Toggle product status
   */
  toggleProductStatus(product: Product): void {
    const updatedProduct: ProductUpdateRequest = {
      id: product.id!,
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      brand: product.brand || '',
      stockQuantity: product.stockQuantity,
      active: !product.active
    };

    this.productService.updateProduct(product.id!, updatedProduct).subscribe({
      next: (updated) => {
        product.active = updated.active;
        this.successMessage = `Product ${updated.active ? 'activated' : 'deactivated'} successfully!`;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        console.error('Error updating product status:', error);
        this.errorMessage = 'Failed to update product status. Please try again.';
      }
    });
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.productForm.valid) {
      const formData = this.productForm.value;
      
      if (this.isEditing && this.editingProductId) {
        // Update existing product
        const updateRequest: ProductUpdateRequest = {
          id: this.editingProductId,
          ...formData
        };
        
        this.productService.updateProduct(this.editingProductId, updateRequest, this.selectedImage || undefined)
          .subscribe({
            next: (updatedProduct) => {
              this.successMessage = 'Product updated successfully!';
              this.closeForm();
              this.loadProducts();
            },
            error: (error) => {
              console.error('Error updating product:', error);
              this.errorMessage = 'Failed to update product. Please try again.';
            }
          });
      } else {
        // Create new product
        const createRequest: ProductCreateRequest = formData;
        
        this.productService.createProduct(createRequest, this.selectedImage || undefined)
          .subscribe({
            next: (newProduct) => {
              this.successMessage = 'Product created successfully!';
              this.closeForm();
              this.loadProducts();
            },
            error: (error) => {
              console.error('Error creating product:', error);
              this.errorMessage = 'Failed to create product. Please try again.';
            }
          });
      }
    } else {
      this.markFormGroupTouched();
    }
  }

  /**
   * Handle image selection
   */
  onImageSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedImage = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  /**
   * Remove selected image
   */
  removeImage(): void {
    this.selectedImage = null;
    this.imagePreview = null;
  }

  /**
   * Close form modal
   */
  closeForm(): void {
    this.showAddForm = false;
    this.resetForm();
  }

  /**
   * Reset form to initial state
   */
  private resetForm(): void {
    this.productForm.reset({
      active: true
    });
    this.selectedImage = null;
    this.imagePreview = null;
  }

  /**
   * Search products
   */
  onProductSearch(): void {
    if (this.productSearchTerm.trim()) {
      const search = this.productSearchTerm.toLowerCase();
      this.filteredProducts = this.products.filter(product => 
        product.name.toLowerCase().includes(search) ||
        product.description.toLowerCase().includes(search) ||
        product.category.toLowerCase().includes(search)
      );
    } else {
      this.filteredProducts = [...this.products];
    }
  }

  /**
   * Export products (placeholder)
   */
  exportProducts(): void {
    console.log('Exporting products...');
    // Implement export functionality
  }

  /**
   * Check if form field is invalid
   */
  isFieldInvalid(fieldName: string): boolean {
    const field = this.productForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  /**
   * Mark all form controls as touched
   */
  private markFormGroupTouched(): void {
    Object.keys(this.productForm.controls).forEach(key => {
      const control = this.productForm.get(key);
      control?.markAsTouched();
    });
  }
}
