import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { CartRoutingModule } from './cart-routing.module';
import { CartViewComponent } from './cart-view/cart-view.component';
import { CheckoutComponent } from './checkout/checkout.component';


@NgModule({
  declarations: [
    CartViewComponent,
    CheckoutComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    CartRoutingModule,
    ReactiveFormsModule
  ]
})
export class CartModule { }
