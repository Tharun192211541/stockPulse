package com.zycus.StockPulse.event;

import com.zycus.StockPulse.commerce.CommerceAdvisor;
import com.zycus.StockPulse.commerce.CommerceAdvisorFactory;
import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.ProductService;
import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.suggestion.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.mockito.Mockito.*;

class InventoryEventListenerTest {
    @Test void eventGeneratesBothSuggestionTypesThroughActiveAdvisor() {
        ProductService productService = mock(ProductService.class);
        SuggestionService suggestionService = mock(SuggestionService.class);
        CommerceAdvisorFactory factory = mock(CommerceAdvisorFactory.class);
        CommerceAdvisor advisor = mock(CommerceAdvisor.class);
        Product product = new Product("SKU-1", "Keyboard", ProductCategory.ELECTRONICS);
        product.setCurrentPrice(new BigDecimal("100"));
        product.setStockLevel(2);
        product.setReorderThreshold(5);
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        when(factory.getAdvisor()).thenReturn(advisor);
        PricingSuggestion pricing = new PricingSuggestion(product, new BigDecimal("100"), new BigDecimal("110"),
                PriceDirection.INCREASE, new BigDecimal("0.8"), "low stock", TriggerReason.INVENTORY_LOW);
        ReorderSuggestion reorder = new ReorderSuggestion(product, 2, 13, 7,
                new BigDecimal("0.8"), "low stock", TriggerReason.INVENTORY_LOW);
        when(advisor.generatePricingSuggestion(product, TriggerReason.INVENTORY_LOW)).thenReturn(pricing);
        when(advisor.generateReorderSuggestion(product, TriggerReason.INVENTORY_LOW)).thenReturn(reorder);

        new InventoryEventListener(productService, suggestionService, factory)
                .handleInventoryEvent(new InventoryEvent(1L, TriggerReason.INVENTORY_LOW));

        verify(suggestionService).createPricingSuggestionIfAbsent(pricing);
        verify(suggestionService).createReorderSuggestionIfAbsent(reorder);
    }
}
