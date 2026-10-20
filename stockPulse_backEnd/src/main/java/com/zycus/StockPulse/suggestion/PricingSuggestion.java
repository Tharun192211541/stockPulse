package com.zycus.StockPulse.suggestion;

import com.zycus.StockPulse.product.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_suggestions")
public class PricingSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal recommendedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceDirection direction;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal confidence;

    @Column(nullable = false, length = 2000)
    private String reasoning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuggestionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TriggerReason triggerReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PricingSuggestion() {
        // Required by JPA
    }

    public PricingSuggestion(
            Product product,
            BigDecimal currentPrice,
            BigDecimal recommendedPrice,
            PriceDirection direction,
            BigDecimal confidence,
            String reasoning,
            TriggerReason triggerReason) {

        this.product = product;
        this.currentPrice = currentPrice;
        this.recommendedPrice = recommendedPrice;
        this.direction = direction;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.triggerReason = triggerReason;
        this.status = SuggestionStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = SuggestionStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
}
