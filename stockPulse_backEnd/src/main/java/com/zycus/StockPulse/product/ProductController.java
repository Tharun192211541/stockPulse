package com.zycus.StockPulse.product;

import com.zycus.StockPulse.commerce.CommerceAdvisor;
import com.zycus.StockPulse.commerce.CommerceAdvisorFactory;
import com.zycus.StockPulse.suggestion.PricingSuggestion;
import com.zycus.StockPulse.suggestion.ReorderSuggestion;
import com.zycus.StockPulse.suggestion.SuggestionService;
import com.zycus.StockPulse.suggestion.TriggerReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private CommerceAdvisorFactory advisorFactory;

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) com.zycus.StockPulse.product.enums.ProductStatus status,
            @RequestParam(required = false) com.zycus.StockPulse.product.enums.ProductCategory category) {
        List<Product> products;
        if (status != null && category != null) {
            products = productService.getProductsByStatusAndCategory(status, category);
        } else if (status != null) {
            products = productService.getProductsByStatus(status);
        } else if (category != null) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> updateProductStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        Product updatedProduct = productService.updateProductStock(id, request.getStockLevel());
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @PostMapping("/{id}/orders")
    public ResponseEntity<Product> processSale(@PathVariable Long id, @Valid @RequestBody SaleRequest request) {
        Product updatedProduct = productService.processSale(id, request.getQuantity());
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @PostMapping("/{id}/suggest-pricing")
    public ResponseEntity<PricingSuggestion> requestPricingSuggestion(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (suggestionService.existsPendingPricingSuggestion(product, TriggerReason.MANUAL)) {
                return ResponseEntity.ok(suggestionService.getPricingSuggestionsByStatus(com.zycus.StockPulse.suggestion.SuggestionStatus.PENDING)
                        .stream().filter(s -> s.getProduct().getId().equals(id) && s.getTriggerReason() == TriggerReason.MANUAL).findFirst().orElseThrow());
            }
            CommerceAdvisor advisor = advisorFactory.getAdvisor();
            PricingSuggestion suggestion = advisor.generatePricingSuggestion(product, TriggerReason.MANUAL);
            PricingSuggestion savedSuggestion = suggestionService.createPricingSuggestionIfAbsent(suggestion);
            return new ResponseEntity<>(savedSuggestion, savedSuggestion == suggestion ? HttpStatus.CREATED : HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/suggest-reorder")
    public ResponseEntity<ReorderSuggestion> requestReorderSuggestion(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (suggestionService.existsPendingReorderSuggestion(product, TriggerReason.MANUAL)) {
                return ResponseEntity.ok(suggestionService.getReorderSuggestionsByStatus(com.zycus.StockPulse.suggestion.SuggestionStatus.PENDING)
                        .stream().filter(s -> s.getProduct().getId().equals(id) && s.getTriggerReason() == TriggerReason.MANUAL).findFirst().orElseThrow());
            }
            CommerceAdvisor advisor = advisorFactory.getAdvisor();
            ReorderSuggestion suggestion = advisor.generateReorderSuggestion(product, TriggerReason.MANUAL);
            ReorderSuggestion savedSuggestion = suggestionService.createReorderSuggestionIfAbsent(suggestion);
            return new ResponseEntity<>(savedSuggestion, savedSuggestion == suggestion ? HttpStatus.CREATED : HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // DTOs
    public static class StockUpdateRequest {
        @NotNull(message = "Stock level is required")
        @Min(value = 0, message = "Stock level cannot be negative")
        private Integer stockLevel;

        public Integer getStockLevel() {
            return stockLevel;
        }

        public void setStockLevel(Integer stockLevel) {
            this.stockLevel = stockLevel;
        }
    }

    public static class SaleRequest {
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be positive")
        private Integer quantity;

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}