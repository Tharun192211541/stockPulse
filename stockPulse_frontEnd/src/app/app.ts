import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { ApiService } from './api.service';
import {
  CommerceStrategy,
  ConfirmationDialogData,
  PricingSuggestion,
  Product,
  ProductCategory,
  ProductStatus,
  ReorderSuggestion,
  SuggestionStatus
} from './models';

export type Tab = 'overview' | 'products' | 'pricing' | 'reorder' | 'strategy';

@Component({
  selector: 'sp-root, app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html'
})
export class App implements OnInit {
  private readonly api = inject(ApiService);

  tab: Tab = 'overview';
  products: Product[] = [];
  pricingSuggestions: PricingSuggestion[] = [];
  reorderSuggestions: ReorderSuggestion[] = [];
  selectedProduct: Product | null = null;

  loading = false;
  saving = false;
  error = '';
  notice = '';
  private noticeTimer: ReturnType<typeof setTimeout> | null = null;
  backendConnected = true;

  strategy: CommerceStrategy = 'RULE';
  selectedStrategy: CommerceStrategy = 'RULE';
  strategySaving = false;

  // Search & Filter state
  productSearchQuery = '';
  productStatusFilter: ProductStatus | '' = '';
  productCategoryFilter: ProductCategory | '' = '';
  pricingStatusFilter: SuggestionStatus | '' | 'ALL' = 'PENDING';
  reorderStatusFilter: SuggestionStatus | '' | 'ALL' = 'PENDING';

  showCreateForm = false;
  createForm = this.emptyProductForm();
  stockEdit: Record<number, number> = {};
  saleQuantity: Record<number, number> = {};
  actionBusy: Record<string, boolean> = {};

  // Confirmation dialog state
  confirmDialog: ConfirmationDialogData | null = null;
  confirmBusy = false;

  readonly categories: ProductCategory[] = ['ELECTRONICS', 'APPAREL', 'HOME'];
  readonly productStatuses: ProductStatus[] = ['ACTIVE', 'PRICE_REVIEW_PENDING', 'OUT_OF_STOCK'];
  readonly suggestionStatuses: (SuggestionStatus | 'ALL')[] = ['ALL', 'PENDING', 'ACCEPTED', 'REJECTED'];

  ngOnInit(): void {
    this.refreshAll();
    this.loadStrategy();
  }

  // Dashboard calculations from real API data
  get totalProducts(): number {
    return this.products.length;
  }

  get totalStockUnits(): number {
    return this.products.reduce((acc, p) => acc + (p.stockLevel || 0), 0);
  }

  get totalInventoryValuation(): number {
    return this.products.reduce((acc, p) => acc + ((p.stockLevel || 0) * (p.currentPrice || 0)), 0);
  }

  get pendingPricingCount(): number {
    return this.pricingSuggestions.filter(s => s.status === 'PENDING').length;
  }

  get pendingReorderCount(): number {
    return this.reorderSuggestions.filter(s => s.status === 'PENDING').length;
  }

  get lowStockCount(): number {
    return this.products.filter(p => p.stockLevel <= p.reorderThreshold && p.stockLevel > 0).length;
  }

  get outOfStockCount(): number {
    return this.products.filter(p => p.stockLevel === 0).length;
  }

  get lowStockProducts(): Product[] {
    return this.products.filter(p => p.stockLevel <= p.reorderThreshold);
  }

  get recentPricingSuggestions(): PricingSuggestion[] {
    return this.pricingSuggestions.slice(0, 4);
  }

  get recentReorderSuggestions(): ReorderSuggestion[] {
    return this.reorderSuggestions.slice(0, 4);
  }

  get filteredProducts(): Product[] {
    const q = this.productSearchQuery.trim().toLowerCase();
    return this.products.filter(p => {
      const matchesSearch = !q || p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q);
      const matchesStatus = !this.productStatusFilter || p.status === this.productStatusFilter;
      const matchesCategory = !this.productCategoryFilter || p.category === this.productCategoryFilter;
      return matchesSearch && matchesStatus && matchesCategory;
    });
  }

  setTab(tab: Tab): void {
    this.tab = tab;
    this.clearMessages();
    if (tab === 'pricing') this.loadPricing();
    if (tab === 'reorder') this.loadReorder();
    if (tab === 'products') this.loadProducts();
    if (tab === 'strategy') this.loadStrategy();
  }

  refreshAll(): void {
    this.loading = true;
    this.error = '';
    this.api.getProducts().subscribe({
      next: products => {
        this.products = products;
        this.backendConnected = true;
        this.syncProductInputs();
        this.loadPricing(false);
        this.loadReorder(false);
        this.loadStrategy();
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.backendConnected = false;
        this.handleError(err, 'Backend unreachable at http://localhost:8080. Please ensure Spring Boot is running.');
      }
    });
  }

  loadProducts(): void {
    this.loading = true;
    this.error = '';
    this.api.getProducts({
      status: this.productStatusFilter,
      category: this.productCategoryFilter
    }).pipe(finalize(() => this.loading = false)).subscribe({
      next: products => {
        this.products = products;
        this.backendConnected = true;
        this.syncProductInputs();
      },
      error: err => this.handleError(err, 'Could not load products.')
    });
  }

  private syncProductInputs(): void {
    for (const p of this.products) {
      if (this.stockEdit[p.id] === undefined) {
        this.stockEdit[p.id] = p.stockLevel;
      }
      if (this.saleQuantity[p.id] === undefined) {
        this.saleQuantity[p.id] = 1;
      }
    }
  }

  loadPricing(showLoading = true): void {
    if (showLoading) this.loading = true;
    this.api.getPricingSuggestions(this.pricingStatusFilter)
      .pipe(finalize(() => { if (showLoading) this.loading = false; }))
      .subscribe({
        next: rows => {
          this.pricingSuggestions = rows;
          this.backendConnected = true;
        },
        error: err => this.handleError(err, 'Could not load pricing suggestions.')
      });
  }

  loadReorder(showLoading = true): void {
    if (showLoading) this.loading = true;
    this.api.getReorderSuggestions(this.reorderStatusFilter)
      .pipe(finalize(() => { if (showLoading) this.loading = false; }))
      .subscribe({
        next: rows => {
          this.reorderSuggestions = rows;
          this.backendConnected = true;
        },
        error: err => this.handleError(err, 'Could not load reorder suggestions.')
      });
  }

  loadStrategy(): void {
    this.api.getStrategy().subscribe({
      next: result => {
        this.strategy = result.strategy;
        this.selectedStrategy = result.strategy;
        this.backendConnected = true;
      },
      error: err => this.handleError(err, 'Could not read the commerce strategy.')
    });
  }

  changeStrategy(newStrategy?: CommerceStrategy): void {
    const targetStrategy = newStrategy || this.selectedStrategy;
    this.strategySaving = true;
    this.clearMessages();
    this.api.setStrategy(targetStrategy).pipe(finalize(() => this.strategySaving = false)).subscribe({
      next: result => {
        this.strategy = result.strategy;
        this.selectedStrategy = result.strategy;
        this.showSuccess(`Commerce advisor switched to ${result.strategy} mode.`);
      },
      error: err => this.handleError(err, 'Could not update commerce strategy.')
    });
  }

  createProduct(): void {
    this.clearMessages();
    const sku = this.createForm.sku.trim();
    const name = this.createForm.name.trim();

    if (!sku) {
      this.error = 'SKU is required.';
      return;
    }
    if (!name) {
      this.error = 'Product name is required.';
      return;
    }
    if (this.createForm.currentPrice == null || this.createForm.currentPrice <= 0) {
      this.error = 'Current price must be greater than zero.';
      return;
    }
    if (this.createForm.costPrice != null && this.createForm.costPrice < 0) {
      this.error = 'Cost price cannot be negative.';
      return;
    }
    if (this.createForm.stockLevel == null || this.createForm.stockLevel < 0) {
      this.error = 'Stock level cannot be negative.';
      return;
    }
    if (this.createForm.reorderThreshold == null || this.createForm.reorderThreshold < 0) {
      this.error = 'Reorder threshold cannot be negative.';
      return;
    }
    if (this.createForm.demandVelocity == null || this.createForm.demandVelocity < 0) {
      this.error = 'Demand velocity cannot be negative.';
      return;
    }

    this.saving = true;
    this.api.createProduct({
      ...this.createForm,
      sku,
      name
    }).pipe(finalize(() => this.saving = false)).subscribe({
      next: product => {
        this.showSuccess(`Product "${product.name}" (${product.sku}) created successfully.`);
        this.showCreateForm = false;
        this.createForm = this.emptyProductForm();
        this.loadProducts();
      },
      error: err => this.handleError(err, 'Could not create product.')
    });
  }

  updateStock(product: Product): void {
    const stock = Number(this.stockEdit[product.id]);
    if (!Number.isInteger(stock) || stock < 0) {
      this.error = 'Stock must be a whole number greater than or equal to zero.';
      return;
    }
    this.runProductAction(
      `stock-${product.id}`,
      () => this.api.updateStock(product.id, stock),
      `Stock for "${product.name}" updated to ${stock} units. Any triggered suggestions will appear automatically.`,
      () => this.reloadAfterProductAction()
    );
  }

  processSale(product: Product): void {
    const quantity = Number(this.saleQuantity[product.id]);
    if (!Number.isInteger(quantity) || quantity < 1) {
      this.error = 'Order quantity must be a positive whole number.';
      return;
    }
    if (quantity > product.stockLevel) {
      this.error = `Cannot sell ${quantity} units: insufficient stock (available: ${product.stockLevel}).`;
      return;
    }
    this.runProductAction(
      `sale-${product.id}`,
      () => this.api.processSale(product.id, quantity),
      `Order processed: sold ${quantity} units of "${product.name}".`,
      () => this.reloadAfterProductAction()
    );
  }

  generatePricing(product: Product): void {
    this.runProductAction(
      `price-${product.id}`,
      () => this.api.generatePricingSuggestion(product.id),
      `Pricing recommendation generated for "${product.name}".`,
      () => {
        this.loadPricing(false);
        this.loadProducts();
      }
    );
  }

  generateReorder(product: Product): void {
    this.runProductAction(
      `reorder-${product.id}`,
      () => this.api.generateReorderSuggestion(product.id),
      `Reorder recommendation generated for "${product.name}".`,
      () => {
        this.loadReorder(false);
        this.loadProducts();
      }
    );
  }

  // Confirmation dialog handlers
  openConfirmPricing(suggestion: PricingSuggestion, status: 'ACCEPTED' | 'REJECTED'): void {
    const isAccept = status === 'ACCEPTED';
    this.confirmDialog = {
      title: isAccept ? 'Confirm Price Adjustment' : 'Reject Pricing Suggestion',
      message: isAccept
        ? `Are you sure you want to accept this pricing recommendation? The product price will be updated on the backend.`
        : `Are you sure you want to reject this pricing recommendation? This suggestion will be marked as REJECTED.`,
      confirmLabel: isAccept ? 'Accept Price' : 'Reject Suggestion',
      confirmVariant: isAccept ? 'success' : 'danger',
      meta: [
        { label: 'Product', value: `${suggestion.product.name} (${suggestion.product.sku})` },
        { label: 'Current Price', value: `$${suggestion.currentPrice.toFixed(2)}` },
        { label: 'Recommended Price', value: `$${suggestion.recommendedPrice.toFixed(2)}` },
        { label: 'Direction', value: suggestion.direction },
        { label: 'Confidence', value: `${Math.round(suggestion.confidence * 100)}%` }
      ],
      action: () => {
        this.confirmBusy = true;
        this.api.updatePricingSuggestion(suggestion.id, status)
          .pipe(finalize(() => {
            this.confirmBusy = false;
            this.confirmDialog = null;
          }))
          .subscribe({
            next: updated => {
              this.showSuccess(
                isAccept
                  ? `Pricing recommendation accepted. Price updated to $${updated.recommendedPrice.toFixed(2)}.`
                  : 'Pricing recommendation rejected.'
              );
              this.loadPricing(false);
              this.loadProducts();
            },
            error: err => this.handleError(err, 'Failed to update pricing suggestion.')
          });
      }
    };
  }

  openConfirmReorder(suggestion: ReorderSuggestion, status: 'ACCEPTED' | 'REJECTED'): void {
    const isAccept = status === 'ACCEPTED';
    const newStock = (suggestion.currentStock || 0) + (suggestion.recommendedQuantity || 0);
    this.confirmDialog = {
      title: isAccept ? 'Approve Replenishment Order' : 'Reject Reorder Suggestion',
      message: isAccept
        ? `Are you sure you want to approve this replenishment order? Stock will be incremented on the backend.`
        : `Are you sure you want to reject this reorder recommendation? This suggestion will be marked as REJECTED.`,
      confirmLabel: isAccept ? 'Approve Order' : 'Reject Suggestion',
      confirmVariant: isAccept ? 'primary' : 'danger',
      meta: [
        { label: 'Product', value: `${suggestion.product.name} (${suggestion.product.sku})` },
        { label: 'Current Stock', value: `${suggestion.currentStock} units` },
        { label: 'Order Quantity', value: `+${suggestion.recommendedQuantity} units` },
        { label: 'Projected Stock', value: `${newStock} units` },
        { label: 'Lead Time', value: `${suggestion.suggestedLeadTimeDays} days` }
      ],
      action: () => {
        this.confirmBusy = true;
        this.api.updateReorderSuggestion(suggestion.id, status)
          .pipe(finalize(() => {
            this.confirmBusy = false;
            this.confirmDialog = null;
          }))
          .subscribe({
            next: updated => {
              this.showSuccess(
                isAccept
                  ? `Replenishment order approved (+${updated.recommendedQuantity} units). Stock refreshed.`
                  : 'Reorder recommendation rejected.'
              );
              this.loadReorder(false);
              this.loadProducts();
            },
            error: err => this.handleError(err, 'Failed to update reorder suggestion.')
          });
      }
    };
  }

  executeConfirm(): void {
    if (this.confirmDialog && !this.confirmBusy) {
      this.confirmDialog.action();
    }
  }

  cancelConfirm(): void {
    if (!this.confirmBusy) {
      this.confirmDialog = null;
    }
  }

  viewProduct(id: number): void {
    this.clearMessages();
    this.api.getProduct(id).subscribe({
      next: product => this.selectedProduct = product,
      error: err => this.handleError(err, `Could not load product details for ID #${id}.`)
    });
  }

  closeProductDetails(): void {
    this.selectedProduct = null;
  }

  private runProductAction<T>(
    key: string,
    request: () => import('rxjs').Observable<T>,
    message: string,
    after: () => void
  ): void {
    this.clearMessages();
    this.actionBusy[key] = true;
    request().pipe(finalize(() => this.actionBusy[key] = false)).subscribe({
      next: () => {
        this.showSuccess(message);
        after();
      },
      error: err => this.handleError(err, 'Action failed.')
    });
  }

  private reloadAfterProductAction(): void {
    this.loadProducts();
    this.loadPricing(false);
    this.loadReorder(false);
  }

  private emptyProductForm() {
    return {
      sku: '',
      name: '',
      category: 'ELECTRONICS' as ProductCategory,
      currentPrice: 49.99,
      costPrice: 25.0,
      stockLevel: 25,
      reorderThreshold: 15,
      demandVelocity: 5,
      status: 'ACTIVE' as ProductStatus
    };
  }

  private showSuccess(message: string): void {
    this.notice = message;
    this.error = '';
    if (this.noticeTimer) {
      clearTimeout(this.noticeTimer);
    }
    this.noticeTimer = setTimeout(() => {
      this.notice = '';
      this.noticeTimer = null;
    }, 5000);
  }

  clearMessages(): void {
    this.notice = '';
    this.error = '';
  }

  private handleError(err: unknown, fallback: string): void {
    const httpErr = err as HttpErrorResponse;
    const body = httpErr?.error;
    let detail = '';

    if (body) {
      if (typeof body === 'string') {
        detail = body;
      } else if (body.errors && typeof body.errors === 'object') {
        const errorList = Object.entries(body.errors).map(([field, msg]) => `${field}: ${msg}`);
        detail = errorList.join(', ');
      } else if (body.message) {
        detail = body.message;
      } else if (body.error) {
        detail = body.error;
      }
    }

    if (httpErr?.status === 0) {
      this.error = `${fallback} (Backend unavailable: Check if Spring Boot is listening on port 8080)`;
      this.backendConnected = false;
    } else if (detail) {
      this.error = `${fallback} Detail: ${detail}`;
    } else if (httpErr?.status) {
      this.error = `${fallback} (HTTP ${httpErr.status}: ${httpErr.statusText || 'Error'})`;
    } else {
      this.error = fallback;
    }

    this.notice = '';
  }

  stockBarWidth(product: Product): number {
    if (!product.reorderThreshold || product.reorderThreshold <= 0) return 100;
    return Math.max(0, Math.min(100, Math.round((product.stockLevel / (product.reorderThreshold * 2.5)) * 100)));
  }

  calculatePriceDiff(s: PricingSuggestion): { amount: number; percent: number } {
    const amount = s.recommendedPrice - s.currentPrice;
    const percent = s.currentPrice > 0 ? (amount / s.currentPrice) * 100 : 0;
    return { amount, percent };
  }

  trackProduct(_: number, item: Product): number { return item.id; }
  trackPricing(_: number, item: PricingSuggestion): number { return item.id; }
  trackReorder(_: number, item: ReorderSuggestion): number { return item.id; }
}
