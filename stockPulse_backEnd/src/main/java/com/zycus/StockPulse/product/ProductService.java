package com.zycus.StockPulse.product;

import com.zycus.StockPulse.event.InventoryEventService;
import com.zycus.StockPulse.exception.ResourceNotFoundException;
import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.product.enums.ProductStatus;
import com.zycus.StockPulse.suggestion.TriggerReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryEventService inventoryEventService;

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByStatus(ProductStatus status) {
        return productRepository.findByStatus(status);
    }

    public List<Product> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsByStatusAndCategory(ProductStatus status, ProductCategory category) {
        return productRepository.findByStatusAndCategory(status, category);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProductStock(Long productId, Integer newStockLevel) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (newStockLevel == null || newStockLevel < 0) {
                throw new IllegalArgumentException("Stock level must be zero or greater");
            }
            Integer oldStockLevel = product.getStockLevel();
            product.setStockLevel(newStockLevel);

            Product savedProduct = productRepository.save(product);

            // Check if inventory crossed the reorder threshold
            if (newStockLevel <= product.getReorderThreshold()) {
                inventoryEventService.publishInventoryEvent(productId, TriggerReason.INVENTORY_LOW);
            }

            return savedProduct;
        } else {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
    }

    public Product processSale(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Integer oldStockLevel = product.getStockLevel();

            product.decreaseStock(quantity);
            product.increaseDemandVelocity(quantity);

            Product savedProduct = productRepository.save(product);

            // Check if this sale triggered a low inventory situation
            if (oldStockLevel > product.getReorderThreshold() && product.getStockLevel() <= product.getReorderThreshold()) {
                inventoryEventService.publishInventoryEvent(productId, TriggerReason.INVENTORY_LOW);
            }

            // Check for demand spike (simplified detection)
            if (product.getDemandVelocity() > 20) { // Arbitrary threshold for demo
                inventoryEventService.publishInventoryEvent(productId, TriggerReason.DEMAND_SPIKE);
            }

            return savedProduct;
        } else {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
    }
}