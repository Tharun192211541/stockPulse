package com.zycus.StockPulse.suggestion;

import com.zycus.StockPulse.exception.InvalidSuggestionStateException;
import com.zycus.StockPulse.exception.ResourceNotFoundException;
import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.ProductRepository;
import com.zycus.StockPulse.product.ProductService;
import com.zycus.StockPulse.product.enums.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SuggestionService {
    private final PricingSuggestionRepository pricingRepository;
    private final ReorderSuggestionRepository reorderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public SuggestionService(PricingSuggestionRepository pricingRepository,
                             ReorderSuggestionRepository reorderRepository,
                             ProductRepository productRepository,
                             ProductService productService) {
        this.pricingRepository = pricingRepository;
        this.reorderRepository = reorderRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<PricingSuggestion> getPricingSuggestionsByStatus(SuggestionStatus status) { return pricingRepository.findByStatus(status); }
    @Transactional(readOnly = true)
    public List<ReorderSuggestion> getReorderSuggestionsByStatus(SuggestionStatus status) { return reorderRepository.findByStatus(status); }
    @Transactional(readOnly = true)
    public Optional<PricingSuggestion> getPricingSuggestionById(Long id) { return pricingRepository.findById(id); }
    @Transactional(readOnly = true)
    public Optional<ReorderSuggestion> getReorderSuggestionById(Long id) { return reorderRepository.findById(id); }

    public boolean existsPendingPricingSuggestion(Product product, TriggerReason reason) {
        return pricingRepository.existsByProductAndStatusAndTriggerReason(product, SuggestionStatus.PENDING, reason);
    }
    public boolean existsPendingReorderSuggestion(Product product, TriggerReason reason) {
        return reorderRepository.existsByProductAndStatusAndTriggerReason(product, SuggestionStatus.PENDING, reason);
    }

    public PricingSuggestion createPricingSuggestion(PricingSuggestion suggestion) {
        return createPricingSuggestionIfAbsent(suggestion);
    }

    /** Serializes pending-suggestion creation per product so concurrent event/manual requests cannot duplicate it. */
    public PricingSuggestion createPricingSuggestionIfAbsent(PricingSuggestion suggestion) {
        Product product = lockProduct(suggestion.getProduct().getId());
        Optional<PricingSuggestion> existing = pricingRepository.findFirstByProductAndStatusAndTriggerReasonOrderByIdAsc(
                product, SuggestionStatus.PENDING, suggestion.getTriggerReason());
        if (existing.isPresent()) return existing.get();
        suggestion.setProduct(product);
        PricingSuggestion saved = pricingRepository.save(suggestion);
        if (product.getStockLevel() > 0 && product.getStatus() != ProductStatus.OUT_OF_STOCK)
            product.setStatus(ProductStatus.PRICE_REVIEW_PENDING);
        productRepository.save(product);
        return saved;
    }

    public ReorderSuggestion createReorderSuggestion(ReorderSuggestion suggestion) {
        return createReorderSuggestionIfAbsent(suggestion);
    }

    public ReorderSuggestion createReorderSuggestionIfAbsent(ReorderSuggestion suggestion) {
        Product product = lockProduct(suggestion.getProduct().getId());
        Optional<ReorderSuggestion> existing = reorderRepository.findFirstByProductAndStatusAndTriggerReasonOrderByIdAsc(
                product, SuggestionStatus.PENDING, suggestion.getTriggerReason());
        if (existing.isPresent()) return existing.get();
        suggestion.setProduct(product);
        return reorderRepository.save(suggestion);
    }

    private Product lockProduct(Long productId) {
        return  productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    public PricingSuggestion updatePricingSuggestionStatus(Long id, SuggestionStatus status) {
        requireFinalStatus(status);
        PricingSuggestion suggestion = pricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing suggestion not found with id: " + id));
        requirePending(suggestion.getStatus());
        suggestion.setStatus(status);
        return pricingRepository.save(suggestion);
    }

    public ReorderSuggestion updateReorderSuggestionStatus(Long id, SuggestionStatus status) {
        requireFinalStatus(status);
        ReorderSuggestion suggestion = reorderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reorder suggestion not found with id: " + id));
        requirePending(suggestion.getStatus());
        suggestion.setStatus(status);
        return reorderRepository.save(suggestion);
    }

    public PricingSuggestion acceptPricingSuggestion(Long id) {
        PricingSuggestion suggestion = pricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing suggestion not found with id: " + id));
        requirePending(suggestion.getStatus());
        Product product = lockProduct(suggestion.getProduct().getId());
        product.setCurrentPrice(suggestion.getRecommendedPrice());
        suggestion.setStatus(SuggestionStatus.ACCEPTED);
        pricingRepository.save(suggestion);
        refreshPricingReviewStatus(product);
        productService.updateProduct(product);
        return suggestion;
    }

    public ReorderSuggestion acceptReorderSuggestion(Long id) {
        ReorderSuggestion suggestion = reorderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reorder suggestion not found with id: " + id));
        requirePending(suggestion.getStatus());
        Product product = lockProduct(suggestion.getProduct().getId());
        product.increaseStock(suggestion.getRecommendedQuantity());
        suggestion.setStatus(SuggestionStatus.ACCEPTED);
        reorderRepository.save(suggestion);
        productService.updateProduct(product);
        return suggestion;
    }

    public PricingSuggestion rejectPricingSuggestion(Long id) {
        PricingSuggestion suggestion = pricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing suggestion not found with id: " + id));
        requirePending(suggestion.getStatus());
        Product product = lockProduct(suggestion.getProduct().getId());
        suggestion.setStatus(SuggestionStatus.REJECTED);
        pricingRepository.save(suggestion);
        refreshPricingReviewStatus(product);
        productService.updateProduct(product);
        return suggestion;
    }

    public ReorderSuggestion rejectReorderSuggestion(Long id) {
        ReorderSuggestion suggestion = reorderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reorder suggestion not found with id: " + id));
        requirePending(suggestion.getStatus());
        suggestion.setStatus(SuggestionStatus.REJECTED);
        return reorderRepository.save(suggestion);
    }

    private void refreshPricingReviewStatus(Product product) {
        if (product.getStockLevel() != null && product.getStockLevel() > 0
                && product.getStatus() == ProductStatus.PRICE_REVIEW_PENDING
                && pricingRepository.findByProductAndStatus(product, SuggestionStatus.PENDING).isEmpty()) {
            product.setStatus(ProductStatus.ACTIVE);
        }
    }

    private void requirePending(SuggestionStatus status) {
        if (status != SuggestionStatus.PENDING)
            throw new InvalidSuggestionStateException("Only pending suggestions can be updated");
    }

    private void requireFinalStatus(SuggestionStatus status) {
        if (status != SuggestionStatus.ACCEPTED && status != SuggestionStatus.REJECTED)
            throw new InvalidSuggestionStateException("Status must be ACCEPTED or REJECTED");
    }
}
