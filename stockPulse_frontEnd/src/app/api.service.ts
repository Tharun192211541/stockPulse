import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CommerceStrategy,
  PricingSuggestion,
  Product,
  ProductCategory,
  ProductStatus,
  ReorderSuggestion,
  StrategyResponse,
  SuggestionStatus
} from './models';
import { ProductService } from './core/services/product.service';
import { SuggestionService } from './core/services/suggestion.service';
import { StrategyService } from './core/services/strategy.service';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly productService = inject(ProductService);
  private readonly suggestionService = inject(SuggestionService);
  private readonly strategyService = inject(StrategyService);

  readonly baseUrl = this.productService.baseUrl;

  getProducts(filters?: { status?: ProductStatus | ''; category?: ProductCategory | '' }): Observable<Product[]> {
    return this.productService.getProducts(filters);
  }

  getProduct(id: number): Observable<Product> {
    return this.productService.getProductById(id);
  }

  createProduct(product: Omit<Product, 'id'>): Observable<Product> {
    return this.productService.createProduct(product);
  }

  updateStock(id: number, stockLevel: number): Observable<Product> {
    return this.productService.updateStock(id, stockLevel);
  }

  processSale(id: number, quantity: number): Observable<Product> {
    return this.productService.processSale(id, quantity);
  }

  generatePricingSuggestion(id: number): Observable<PricingSuggestion> {
    return this.productService.requestPricingSuggestion(id);
  }

  generateReorderSuggestion(id: number): Observable<ReorderSuggestion> {
    return this.productService.requestReorderSuggestion(id);
  }

  getPricingSuggestions(status?: SuggestionStatus | '' | 'ALL'): Observable<PricingSuggestion[]> {
    return this.suggestionService.getPricingSuggestions(status);
  }

  getPricingSuggestion(id: number): Observable<PricingSuggestion> {
    return this.suggestionService.getPricingSuggestionById(id);
  }

  updatePricingSuggestion(id: number, status: 'ACCEPTED' | 'REJECTED'): Observable<PricingSuggestion> {
    return this.suggestionService.updatePricingSuggestionStatus(id, status);
  }

  getReorderSuggestions(status?: SuggestionStatus | '' | 'ALL'): Observable<ReorderSuggestion[]> {
    return this.suggestionService.getReorderSuggestions(status);
  }

  getReorderSuggestion(id: number): Observable<ReorderSuggestion> {
    return this.suggestionService.getReorderSuggestionById(id);
  }

  updateReorderSuggestion(id: number, status: 'ACCEPTED' | 'REJECTED'): Observable<ReorderSuggestion> {
    return this.suggestionService.updateReorderSuggestionStatus(id, status);
  }

  getStrategy(): Observable<StrategyResponse> {
    return this.strategyService.getStrategy();
  }

  setStrategy(strategy: CommerceStrategy | string): Observable<StrategyResponse> {
    return this.strategyService.setStrategy(strategy);
  }
}
