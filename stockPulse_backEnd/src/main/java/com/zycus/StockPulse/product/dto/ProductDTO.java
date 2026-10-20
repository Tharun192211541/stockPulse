package com.zycus.StockPulse.product.dto;

import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.product.enums.ProductStatus;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductDTO {
    private Long id;
    @NotBlank(message = "SKU is required")
    private String sku;
    @NotBlank(message = "Name is required")
    private String name;
    @NotNull(message = "Category is required")
    private ProductCategory category;
    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be positive")
    private BigDecimal currentPrice;
    @DecimalMin(value = "0.0", inclusive = true, message = "Cost price must be non-negative")
    private BigDecimal costPrice;
    @NotNull(message = "Stock level is required")
    @Min(value = 0, message = "Stock level cannot be negative")
    private Integer stockLevel;
    @NotNull(message = "Reorder threshold is required")
    @Min(value = 0, message = "Reorder threshold cannot be negative")
    private Integer reorderThreshold;
    @NotNull(message = "Demand velocity is required")
    @Min(value = 0, message = "Demand velocity cannot be negative")
    private Integer demandVelocity;
    private ProductStatus status;

    // Constructors
    public ProductDTO() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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