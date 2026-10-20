package com.zycus.StockPulse.product;

import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.product.enums.ProductStatus;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "SKU is required")
    @Column(unique = true, nullable = false)
    private String sku;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    @Column(nullable = false)
    private ProductCategory category;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be positive")
    @Column(precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost price must be non-negative")
    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Min(value = 0, message = "Stock level cannot be negative")
    @Column(nullable = false)
    private Integer stockLevel = 0;

    @Min(value = 0, message = "Reorder threshold cannot be negative")
    @Column(nullable = false)
    private Integer reorderThreshold = 0;

    @Min(value = 0, message = "Demand velocity cannot be negative")
    @Column(nullable = false)
    private Integer demandVelocity = 0;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    // Constructors
    public Product() {
        // Default constructor required by JPA
    }

    public Product(String sku, String name, ProductCategory category) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.stockLevel = 0;
        this.reorderThreshold = 0;
        this.demandVelocity = 0;
        this.status = ProductStatus.ACTIVE;
    }

    // Business methods
    public void decreaseStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (this.stockLevel < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stockLevel -= quantity;
        if (this.stockLevel == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
    }

    public void increaseStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stockLevel += quantity;
        if (this.stockLevel > 0 && this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public void increaseDemandVelocity(Integer increment) {
        if (increment != null && increment > 0) {
            this.demandVelocity += increment;
        }
    }

    public boolean isLowInventory() {
        return this.stockLevel <= this.reorderThreshold;
    }

    public boolean isDemandSpike(Integer categoryAverageDemand) {
        return categoryAverageDemand != null && categoryAverageDemand > 0
                && this.demandVelocity > 3 * categoryAverageDemand;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
        // Update status based on stock level
        if (this.stockLevel != null && this.stockLevel == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.stockLevel != null && this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public Integer getDemandVelocity() {
        return demandVelocity;
    }

    public void setDemandVelocity(Integer demandVelocity) {
        this.demandVelocity = demandVelocity;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}