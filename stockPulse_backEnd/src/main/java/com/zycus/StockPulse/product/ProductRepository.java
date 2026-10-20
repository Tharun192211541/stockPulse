
package com.zycus.StockPulse.product;

import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.product.enums.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByStatusAndCategory(
            ProductStatus status,
            ProductCategory category
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdForUpdate(
            @Param("productId") Long productId
    );
}
