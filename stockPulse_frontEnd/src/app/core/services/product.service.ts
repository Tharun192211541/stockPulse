import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Product,
  ProductCategory,
  ProductStatus,
  PricingSuggestion,
  ReorderSuggestion,
  StockUpdateRequest,
  SaleRequest
} from '../../models';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  readonly baseUrl = 'http://localhost:8080';

  getProducts(filters?: { status?: ProductStatus | ''; category?: ProductCategory | '' }): Observable<Product[]> {
    let params = new HttpParams();
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.category) params = params.set('category', filters.category);
    return this.http.get<Product[]>(`${this.baseUrl}/products`, { params });
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/products/${id}`);
  }

  createProduct(product: Omit<Product, 'id'>): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/products`, product);
  }

  updateStock(id: number, stockLevel: number): Observable<Product> {
    const body: StockUpdateRequest = { stockLevel };
    return this.http.patch<Product>(`${this.baseUrl}/products/${id}/stock`, body);
  }

  processSale(id: number, quantity: number): Observable<Product> {
    const body: SaleRequest = { quantity };
    return this.http.post<Product>(`${this.baseUrl}/products/${id}/orders`, body);
  }

  requestPricingSuggestion(id: number): Observable<PricingSuggestion> {
    return this.http.post<PricingSuggestion>(`${this.baseUrl}/products/${id}/suggest-pricing`, {});
  }

  requestReorderSuggestion(id: number): Observable<ReorderSuggestion> {
    return this.http.post<ReorderSuggestion>(`${this.baseUrl}/products/${id}/suggest-reorder`, {});
  }
}
