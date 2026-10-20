package com.zycus.StockPulse.commerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.StockPulse.ai.LLMGateway;
import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.ProductRepository;
import com.zycus.StockPulse.suggestion.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AiCommerceAdvisor implements CommerceAdvisor {
    private final LLMGateway gateway;
    private final ObjectMapper mapper;
    private final RuleBasedCommerceAdvisor fallback;
    private final ProductRepository productRepository;

    public AiCommerceAdvisor(LLMGateway gateway, ObjectMapper mapper, RuleBasedCommerceAdvisor fallback, ProductRepository productRepository) {
        this.gateway = gateway;
        this.mapper = mapper;
        this.fallback = fallback;
        this.productRepository = productRepository;
    }

    @Override
    public PricingSuggestion generatePricingSuggestion(Product p, TriggerReason reason) {
        try {
            String prompt = """
                You are a cautious ecommerce merchandising advisor.
                Recommend a price for this product. Trigger: %s.
                For INVENTORY_LOW, explicitly weigh protecting scarce stock against clearance.
                For DEMAND_SPIKE, consider demand velocity and category context.
                Product: %s; category: %s; currentPrice: %s; stockLevel: %d;
                reorderThreshold: %d; demandVelocity (orders/24h): %d; categoryAverageDemand (orders/24h): %d.
                Return JSON only: {"recommendedPrice":29.99,"direction":"INCREASE|DECREASE|HOLD",
                "confidence":0.82,"reasoning":"plain English explanation"}.
                Price must be positive, confidence must be between 0 and 1.
                """.formatted(reason, p.getName(), p.getCategory(), p.getCurrentPrice(),
                    p.getStockLevel(), p.getReorderThreshold(), p.getDemandVelocity(), categoryAverageDemand(p));
            JsonNode n = mapper.readTree(stripFences(gateway.callLLM(prompt)));
            BigDecimal price = new BigDecimal(n.path("recommendedPrice").asText());
            BigDecimal confidence = new BigDecimal(n.path("confidence").asText());
            PriceDirection direction = PriceDirection.valueOf(n.path("direction").asText().toUpperCase());
            String reasoning = n.path("reasoning").asText("AI recommendation");
            BigDecimal current = p.getCurrentPrice();
            price = price.setScale(2, RoundingMode.HALF_UP);
            if (price.signum() <= 0 || current == null || current.signum() <= 0 ||
                    price.compareTo(current.multiply(new BigDecimal("2"))) > 0 ||
                    price.compareTo(current.multiply(new BigDecimal("0.5"))) < 0 ||
                    confidence.compareTo(BigDecimal.ZERO) < 0 ||
                    confidence.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("AI price/confidence outside allowed bounds");
            }
            int comparison = price.compareTo(current);
            boolean directionMatches = (comparison > 0 && direction == PriceDirection.INCREASE)
                    || (comparison < 0 && direction == PriceDirection.DECREASE)
                    || (comparison == 0 && direction == PriceDirection.HOLD);
            if (!directionMatches) throw new IllegalArgumentException("AI price direction does not match recommended price");
            return new PricingSuggestion(p, current, price, direction, confidence, reasoning, reason);
        } catch (Exception e) {
            return fallback.generatePricingSuggestion(p, reason);
        }
    }

    @Override
    public ReorderSuggestion generateReorderSuggestion(Product p, TriggerReason reason) {
        try {
            String prompt = """
                Recommend inventory replenishment. Trigger: %s.
                Product: %s; category: %s; stockLevel: %d; reorderThreshold: %d;
                demandVelocity (orders/24h): %d; categoryAverageDemand (orders/24h): %d.
                Return JSON only: {"recommendedQuantity":150,"confidence":0.78,
                "reasoning":"plain English explanation","suggestedLeadTimeDays":7}.
                Quantity must be a positive integer and confidence between 0 and 1.
                """.formatted(reason, p.getName(), p.getCategory(), p.getStockLevel(),
                    p.getReorderThreshold(), p.getDemandVelocity());
            JsonNode n = mapper.readTree(stripFences(gateway.callLLM(prompt)));
            int quantity = n.path("recommendedQuantity").asInt(-1);
            int leadDays = n.path("suggestedLeadTimeDays").asInt(7);
            BigDecimal confidence = new BigDecimal(n.path("confidence").asText());
            if (quantity <= 0 || quantity > 1_000_000 || leadDays < 0 || leadDays > 365 ||
                    confidence.compareTo(BigDecimal.ZERO) < 0 ||
                    confidence.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("AI reorder output outside allowed bounds");
            }
            return new ReorderSuggestion(p, p.getStockLevel(), quantity, leadDays, confidence,
                    n.path("reasoning").asText("AI replenishment recommendation"), reason);
        } catch (Exception e) {
            return fallback.generateReorderSuggestion(p, reason);
        }
    }

    private int categoryAverageDemand(Product product) {
        return (int) Math.round(productRepository.findByCategory(product.getCategory()).stream()
                .filter(peer -> !java.util.Objects.equals(peer.getId(), product.getId()))
                .mapToInt(peer -> peer.getDemandVelocity() == null ? 0 : peer.getDemandVelocity())
                .average().orElse(0.0));
    }

    private String stripFences(String value) {
        return value.trim().replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
    }
}
