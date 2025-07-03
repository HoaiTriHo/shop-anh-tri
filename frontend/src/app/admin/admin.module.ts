import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AdminRoutingModule } from './admin-routing.module';
import { AdminLayoutComponent } from './admin-layout.component';
import { ProductManagementComponent } from './product-management/product-management.component';
import { OrderManagementComponent } from './order-management/order-management.component';
import { AdminDashboardComponent } from './dashboard/admin-dashboard.component';

@NgModule({
  declarations: [
    AdminLayoutComponent,
    ProductManagementComponent,
    OrderManagementComponent,
    AdminDashboardComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    AdminRoutingModule
  ]
})
export class AdminModule { }
