package com.zycus.StockPulse.suggestion;

import com.zycus.StockPulse.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, Long> {
    List<ReorderSuggestion> findByStatus(SuggestionStatus status);
    List<ReorderSuggestion> findByProduct(Product product);
    Optional<ReorderSuggestion> findByProductAndStatusAndTriggerReason(Product product, SuggestionStatus status, TriggerReason triggerReason);

    @Query("SELECT COUNT(r) > 0 FROM ReorderSuggestion r WHERE r.product = ?1 AND r.status = ?2 AND r.triggerReason = ?3")
    boolean existsByProductAndStatusAndTriggerReason(Product product, SuggestionStatus status, TriggerReason triggerReason);

    // Fixed: Removed duplicate method
    List<ReorderSuggestion> findByProductAndStatus(Product product, SuggestionStatus status);
    Optional<ReorderSuggestion> findFirstByProductAndStatusAndTriggerReasonOrderByIdAsc(Product product, SuggestionStatus status, TriggerReason triggerReason);
}