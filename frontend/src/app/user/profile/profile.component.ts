import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  profileForm: FormGroup;
  currentUser: any = null;
  isLoading = false;
  isEditing = false;
  message = '';
  messageType = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {
    this.profileForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.pattern(/^\d{10,}$/)]],
      address: ['']
    });
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.authService.getProfile().subscribe({
      next: (profile) => {
        this.currentUser = profile;
        this.profileForm.patchValue({
          firstName: profile.firstName || '',
          lastName: profile.lastName || '',
          email: profile.email || '',
          phone: profile.phoneNumber || '',
          address: profile.address || ''
        });
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.message = 'Không thể tải thông tin hồ sơ.';
        this.messageType = 'danger';
      }
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (!this.isEditing) {
      this.loadUserProfile(); // Reset form
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    this.message = '';
    const formValue = this.profileForm.value;
    const updateDto = {
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      email: formValue.email,
      phoneNumber: formValue.phone,
      address: formValue.address
    };
    this.authService.updateProfile(updateDto).subscribe({
      next: (profile) => {
        this.isLoading = false;
        this.isEditing = false;
        this.currentUser = profile;
        this.message = 'Cập nhật thông tin thành công!';
        this.messageType = 'success';
        this.profileForm.patchValue({
          firstName: profile.firstName || '',
          lastName: profile.lastName || '',
          email: profile.email || '',
          phone: profile.phoneNumber || '',
          address: profile.address || ''
        });
        setTimeout(() => { this.message = ''; }, 3000);
      },
      error: (err) => {
        this.isLoading = false;
        this.message = 'Cập nhật thất bại. Vui lòng thử lại.';
        this.messageType = 'danger';
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
} 