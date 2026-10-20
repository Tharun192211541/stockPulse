package com.zycus.StockPulse.suggestion;

import com.zycus.StockPulse.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, Long> {
    List<PricingSuggestion> findByStatus(SuggestionStatus status);
    List<PricingSuggestion> findByProduct(Product product);
    Optional<PricingSuggestion> findByProductAndStatusAndTriggerReason(Product product, SuggestionStatus status, TriggerReason triggerReason);

    @Query("SELECT COUNT(p) > 0 FROM PricingSuggestion p WHERE p.product = ?1 AND p.status = ?2 AND p.triggerReason = ?3")
    boolean existsByProductAndStatusAndTriggerReason(Product product, SuggestionStatus status, TriggerReason triggerReason);

    // Fixed: Removed duplicate method and added the missing one
    List<PricingSuggestion> findByProductAndStatus(Product product, SuggestionStatus status);
    Optional<PricingSuggestion> findFirstByProductAndStatusAndTriggerReasonOrderByIdAsc(Product product, SuggestionStatus status, TriggerReason triggerReason);
}