package com.zycus.StockPulse.product;

import com.zycus.StockPulse.event.InventoryEventService;
import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.suggestion.TriggerReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;

class ProductServiceTest {
    private ProductRepository repository;
    private InventoryEventService events;
    private ProductService service;
    private Product product;

    @BeforeEach void setup() {
        repository = mock(ProductRepository.class);
        events = mock(InventoryEventService.class);
        service = new ProductService(repository, events, 3.0);
        product = new Product("SKU-1", "Keyboard", ProductCategory.ELECTRONICS);
        product.setCurrentPrice(new BigDecimal("100"));
        product.setStockLevel(20);
        product.setReorderThreshold(5);
        product.setDemandVelocity(5);
        ReflectionTestUtils.setField(product, "id", 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(product)).thenReturn(product);
    }

    @Test void stockBelowThresholdPublishesInventoryLowEvent() {
        service.updateProductStock(1L, 3);
        verify(events).publishInventoryEvent(1L, TriggerReason.INVENTORY_LOW);
    }

    @Test void crossingCategoryDemandThresholdPublishesDemandSpikeEvent() {
        Product peer = new Product("SKU-2", "Mouse", ProductCategory.ELECTRONICS);
        peer.setDemandVelocity(2);
        ReflectionTestUtils.setField(peer, "id", 2L);
        when(repository.findByCategory(ProductCategory.ELECTRONICS)).thenReturn(List.of(product, peer));
        service.processSale(1L, 2);
        verify(events).publishInventoryEvent(1L, TriggerReason.DEMAND_SPIKE);
    }
}
