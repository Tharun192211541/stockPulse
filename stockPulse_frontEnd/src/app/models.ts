export type ProductCategory = 'ELECTRONICS' | 'APPAREL' | 'HOME';
export type ProductStatus = 'ACTIVE' | 'PRICE_REVIEW_PENDING' | 'OUT_OF_STOCK';
export type SuggestionStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';
export type TriggerReason = 'INITIAL' | 'INVENTORY_LOW' | 'DEMAND_SPIKE' | 'MANUAL';
export type PriceDirection = 'INCREASE' | 'DECREASE' | 'HOLD';
export type CommerceStrategy = 'RULE' | 'AI';

export interface Product {
  id: number;
  sku: string;
  name: string;
  category: ProductCategory;
  currentPrice: number;
  costPrice?: number | null;
  stockLevel: number;
  reorderThreshold: number;
  demandVelocity: number;
  status: ProductStatus;
  lowInventory?: boolean;
}

export interface PricingSuggestion {
  id: number;
  product: Product;
  currentPrice: number;
  recommendedPrice: number;
  direction: PriceDirection;
  confidence: number;
  reasoning: string;
  status: SuggestionStatus;
  triggerReason: TriggerReason;
  createdAt: string;
}

export interface ReorderSuggestion {
  id: number;
  product: Product;
  currentStock: number;
  recommendedQuantity: number;
  suggestedLeadTimeDays: number;
  confidence: number;
  reasoning: string;
  status: SuggestionStatus;
  triggerReason: TriggerReason;
  createdAt: string;
}

export interface StrategyResponse {
  strategy: CommerceStrategy;
}

export interface StrategyRequest {
  strategy: string;
}

export interface StockUpdateRequest {
  stockLevel: number;
}

export interface SaleRequest {
  quantity: number;
}

export interface SuggestionStatusUpdateRequest {
  status: SuggestionStatus;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  errors?: Record<string, string>;
}

export interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmLabel: string;
  confirmVariant?: 'primary' | 'danger' | 'success';
  meta?: {
    label: string;
    value: string;
  }[];
  action: () => void;
}
