import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { UserOnlyGuard } from './services/user-only.guard';

const routes: Routes = [
  { path: '', component: HomeComponent, canActivate: [UserOnlyGuard] },
  { path: 'home', component: HomeComponent, canActivate: [UserOnlyGuard] },
  { path: 'auth', loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule), canActivate: [UserOnlyGuard] },
  { path: 'products', loadChildren: () => import('./product/product.module').then(m => m.ProductModule), canActivate: [UserOnlyGuard] },
  { path: 'cart', loadChildren: () => import('./cart/cart.module').then(m => m.CartModule), canActivate: [UserOnlyGuard] },
  { path: 'admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule) },
  { path: '**', redirectTo: '/home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
