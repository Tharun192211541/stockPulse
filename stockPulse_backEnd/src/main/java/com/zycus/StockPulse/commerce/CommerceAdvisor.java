package com.zycus.StockPulse.commerce;

import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.suggestion.PricingSuggestion;
import com.zycus.StockPulse.suggestion.ReorderSuggestion;
import com.zycus.StockPulse.suggestion.TriggerReason;

public interface CommerceAdvisor {
    PricingSuggestion generatePricingSuggestion(Product product, TriggerReason triggerReason);
    ReorderSuggestion generateReorderSuggestion(Product product, TriggerReason triggerReason);
}
