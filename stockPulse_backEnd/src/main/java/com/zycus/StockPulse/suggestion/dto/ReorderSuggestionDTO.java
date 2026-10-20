package com.zycus.StockPulse.suggestion.dto;

import com.zycus.StockPulse.suggestion.SuggestionStatus;
import com.zycus.StockPulse.suggestion.TriggerReason;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReorderSuggestionDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private Integer currentStock;
    private Integer recommendedQuantity;
    private Integer suggestedLeadTimeDays;
    private BigDecimal confidence;
    private String reasoning;
    private SuggestionStatus status;
    private TriggerReason triggerReason;
    private LocalDateTime createdAt;

    // Constructors
    public ReorderSuggestionDTO() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getRecommendedQuantity() {
        return recommendedQuantity;
    }

    public void setRecommendedQuantity(Integer recommendedQuantity) {
        this.recommendedQuantity = recommendedQuantity;
    }

    public Integer getSuggestedLeadTimeDays() {
        return suggestedLeadTimeDays;
    }

    public void setSuggestedLeadTimeDays(Integer suggestedLeadTimeDays) {
        this.suggestedLeadTimeDays = suggestedLeadTimeDays;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }

    public TriggerReason getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(TriggerReason triggerReason) {
        this.triggerReason = triggerReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}