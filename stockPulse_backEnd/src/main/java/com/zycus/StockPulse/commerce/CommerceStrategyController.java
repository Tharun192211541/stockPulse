package com.zycus.StockPulse.commerce;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class CommerceStrategyController {
    private final CommerceStrategyHolder strategyHolder;
    public CommerceStrategyController(CommerceStrategyHolder strategyHolder) { this.strategyHolder = strategyHolder; }

    @GetMapping("/strategy")
    public StrategyResponse getCurrentStrategy() {
        return new StrategyResponse(strategyHolder.getCurrentStrategy().name());
    }

    @PutMapping("/strategy")
    public ResponseEntity<?> setStrategy(@Valid @RequestBody StrategyRequest request) {
        try {
            CommerceStrategyHolder.CommerceStrategy strategy = CommerceStrategyHolder.CommerceStrategy.valueOf(
                    request.getStrategy().trim().toUpperCase());
            strategyHolder.setStrategy(strategy);
            return ResponseEntity.ok(new StrategyResponse(strategy.name()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("strategy must be one of: RULE, AI"));
        }
    }

    public static class StrategyRequest {
        @NotBlank(message = "Strategy is required")
        private String strategy;
        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
    }
    public static class StrategyResponse {
        private String strategy;
        public StrategyResponse() {}
        public StrategyResponse(String strategy) { this.strategy = strategy; }
        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
    }
    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
