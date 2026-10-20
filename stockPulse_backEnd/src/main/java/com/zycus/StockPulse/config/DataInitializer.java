package com.zycus.StockPulse.config;

import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.ProductRepository;
import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.product.enums.ProductStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private ProductRepository productRepository;

    @PostConstruct
    public void init() {
        // Check if products already exist
        if (productRepository.count() > 0) {
            return; // Don't reinitialize if products already exist
        }

        // Create the exact 8 products as specified
        List<Product> products = Arrays.asList(
            createProduct("SKU-ELEC-001", "Wireless Earbuds Pro", ProductCategory.ELECTRONICS, 
                         new BigDecimal("79.99"), 45, 20, 3, ProductStatus.ACTIVE),
            
            createProduct("SKU-ELEC-002", "USB-C Hub 7-Port", ProductCategory.ELECTRONICS, 
                         new BigDecimal("34.99"), 120, 30, 1, ProductStatus.ACTIVE),
            
            createProduct("SKU-APP-001", "Organic Cotton T-Shirt", ProductCategory.APPAREL, 
                         new BigDecimal("24.99"), 8, 15, 12, ProductStatus.PRICE_REVIEW_PENDING),
            
            createProduct("SKU-APP-002", "Running Shorts — Navy", ProductCategory.APPAREL, 
                         new BigDecimal("39.99"), 55, 20, 2, ProductStatus.ACTIVE),
            
            createProduct("SKU-HOME-001", "Ceramic Pour-Over Set", ProductCategory.HOME, 
                         new BigDecimal("49.99"), 22, 10, 4, ProductStatus.ACTIVE),
            
            createProduct("SKU-HOME-002", "LED Desk Lamp — Dimmable", ProductCategory.HOME, 
                         new BigDecimal("59.99"), 0, 15, 0, ProductStatus.OUT_OF_STOCK),
            
            createProduct("SKU-ELEC-003", "Portable Charger 20K", ProductCategory.ELECTRONICS, 
                         new BigDecimal("44.99"), 18, 25, 8, ProductStatus.ACTIVE),
            
            createProduct("SKU-APP-003", "Hoodie — Heather Grey", ProductCategory.APPAREL, 
                         new BigDecimal("54.99"), 11, 12, 15, ProductStatus.ACTIVE)
        );

        // Save all products
        productRepository.saveAll(products);
    }

    private Product createProduct(String sku, String name, ProductCategory category, 
                                 BigDecimal price, int stock, int threshold, int demand, ProductStatus status) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setCategory(category);
        product.setCurrentPrice(price);
        product.setStockLevel(stock);
        product.setReorderThreshold(threshold);
        product.setDemandVelocity(demand);
        product.setStatus(status);
        return product;
    }
}