package com.zycus.StockPulse.suggestion.dto;

import com.zycus.StockPulse.suggestion.PriceDirection;
import com.zycus.StockPulse.suggestion.SuggestionStatus;
import com.zycus.StockPulse.suggestion.TriggerReason;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PricingSuggestionDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal currentPrice;
    private BigDecimal recommendedPrice;
    private PriceDirection direction;
    private BigDecimal confidence;
    private String reasoning;
    private SuggestionStatus status;
    private TriggerReason triggerReason;
    private LocalDateTime createdAt;

    // Constructors
    public PricingSuggestionDTO() {}

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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getRecommendedPrice() {
        return recommendedPrice;
    }

    public void setRecommendedPrice(BigDecimal recommendedPrice) {
        this.recommendedPrice = recommendedPrice;
    }

    public PriceDirection getDirection() {
        return direction;
    }

    public void setDirection(PriceDirection direction) {
        this.direction = direction;
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