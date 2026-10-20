package com.zycus.StockPulse.suggestion;

import com.zycus.StockPulse.exception.InvalidSuggestionStateException;
import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.ProductRepository;
import com.zycus.StockPulse.product.ProductService;
import com.zycus.StockPulse.product.enums.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SuggestionServiceTest {
    private PricingSuggestionRepository pricing;
    private ReorderSuggestionRepository reorder;
    private ProductRepository products;
    private ProductService productService;
    private SuggestionService service;
    private Product product;

    @BeforeEach void setup() {
        pricing = mock(PricingSuggestionRepository.class);
        reorder = mock(ReorderSuggestionRepository.class);
        products = mock(ProductRepository.class);
        productService = mock(ProductService.class);
        service = new SuggestionService(pricing, reorder, products, productService);
        product = new Product("SKU-1", "Keyboard", ProductCategory.ELECTRONICS);
        product.setCurrentPrice(new BigDecimal("100.00"));
        product.setStockLevel(10);
        product.setReorderThreshold(4);
        product.setDemandVelocity(2);
        ReflectionTestUtils.setField(product, "id", 1L);
        when(products.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
    }

    @Test void duplicatePendingPricingSuggestionReturnsExisting() {
        PricingSuggestion existing = new PricingSuggestion(product, new BigDecimal("100"), new BigDecimal("110"),
                PriceDirection.INCREASE, new BigDecimal("0.8"), "existing", TriggerReason.MANUAL);
        PricingSuggestion incoming = new PricingSuggestion(product, new BigDecimal("100"), new BigDecimal("105"),
                PriceDirection.INCREASE, new BigDecimal("0.7"), "new", TriggerReason.MANUAL);
        when(pricing.findFirstByProductAndStatusAndTriggerReasonOrderByIdAsc(product, SuggestionStatus.PENDING, TriggerReason.MANUAL))
                .thenReturn(Optional.of(existing));
        assertSame(existing, service.createPricingSuggestionIfAbsent(incoming));
        verify(pricing, never()).save(incoming);
    }

    @Test void acceptingPricingUpdatesProductAndSuggestion() {
        PricingSuggestion suggestion = new PricingSuggestion(product, new BigDecimal("100"), new BigDecimal("115"),
                PriceDirection.INCREASE, new BigDecimal("0.9"), "increase", TriggerReason.MANUAL);
        when(pricing.findById(10L)).thenReturn(Optional.of(suggestion));
        when(pricing.save(suggestion)).thenReturn(suggestion);
        when(productService.updateProduct(product)).thenReturn(product);
        PricingSuggestion result = service.acceptPricingSuggestion(10L);
        assertEquals(SuggestionStatus.ACCEPTED, result.getStatus());
        assertEquals(new BigDecimal("115"), product.getCurrentPrice());
        verify(productService).updateProduct(product);
    }

    @Test void acceptedSuggestionCannotBeRejectedAfterward() {
        PricingSuggestion suggestion = new PricingSuggestion(product, new BigDecimal("100"), new BigDecimal("115"),
                PriceDirection.INCREASE, new BigDecimal("0.9"), "increase", TriggerReason.MANUAL);
        suggestion.setStatus(SuggestionStatus.ACCEPTED);
        when(pricing.findById(10L)).thenReturn(Optional.of(suggestion));
        assertThrows(InvalidSuggestionStateException.class, () -> service.rejectPricingSuggestion(10L));
    }

    @Test void acceptingReorderIncreasesStock() {
        ReorderSuggestion suggestion = new ReorderSuggestion(product, 10, 20, 7,
                new BigDecimal("0.8"), "replenish", TriggerReason.INVENTORY_LOW);
        when(reorder.findById(20L)).thenReturn(Optional.of(suggestion));
        when(reorder.save(suggestion)).thenReturn(suggestion);
        when(productService.updateProduct(product)).thenReturn(product);
        service.acceptReorderSuggestion(20L);
        assertEquals(30, product.getStockLevel());
        assertEquals(SuggestionStatus.ACCEPTED, suggestion.getStatus());
    }
}
