package com.zycus.StockPulse.event;

import com.zycus.StockPulse.commerce.CommerceAdvisor;
import com.zycus.StockPulse.commerce.CommerceAdvisorFactory;
import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.ProductService;
import com.zycus.StockPulse.suggestion.PricingSuggestion;
import com.zycus.StockPulse.suggestion.ReorderSuggestion;
import com.zycus.StockPulse.suggestion.SuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private final ProductService productService;
    private final SuggestionService suggestionService;
    private final CommerceAdvisorFactory advisorFactory;

    public InventoryEventListener(ProductService productService, SuggestionService suggestionService,
                                  CommerceAdvisorFactory advisorFactory) {
        this.productService = productService;
        this.suggestionService = suggestionService;
        this.advisorFactory = advisorFactory;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInventoryEvent(InventoryEvent event) {
        try {
            Product product = productService.getProductById(event.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + event.getProductId()));
            CommerceAdvisor advisor = advisorFactory.getAdvisor();
            PricingSuggestion pricing = advisor.generatePricingSuggestion(product, event.getTriggerReason());
            ReorderSuggestion reorder = advisor.generateReorderSuggestion(product, event.getTriggerReason());
            suggestionService.createPricingSuggestionIfAbsent(pricing);
            suggestionService.createReorderSuggestionIfAbsent(reorder);
            log.info("Processed {} inventory event for product {}", event.getTriggerReason(), event.getProductId());
        } catch (Exception ex) {
            // AiCommerceAdvisor catches model errors and falls back to rules; this catches persistence/domain failures.
            log.error("Could not process inventory event for product {}", event.getProductId(), ex);
        }
    }
}
