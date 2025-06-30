import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CartViewComponent } from './cart-view/cart-view.component';
import { CheckoutComponent } from './checkout/checkout.component';

const routes: Routes = [
  { path: '', component: CartViewComponent },
  { path: 'checkout', component: CheckoutComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CartRoutingModule { }
