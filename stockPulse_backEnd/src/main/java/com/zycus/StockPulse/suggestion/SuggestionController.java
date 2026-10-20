package com.zycus.StockPulse.suggestion;

import com.zycus.StockPulse.exception.InvalidSuggestionStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/suggestions")
public class SuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    // Pricing Suggestions Endpoints
    @GetMapping("/pricing")
    public ResponseEntity<List<PricingSuggestion>> getPricingSuggestions(
            @RequestParam(required = false) SuggestionStatus status) {
        List<PricingSuggestion> suggestions;
        if (status != null) {
            suggestions = suggestionService.getPricingSuggestionsByStatus(status);
        } else {
            suggestions = suggestionService.getPricingSuggestionsByStatus(SuggestionStatus.PENDING);
        }
        return new ResponseEntity<>(suggestions, HttpStatus.OK);
    }

    @GetMapping("/pricing/{id}")
    public ResponseEntity<PricingSuggestion> getPricingSuggestionById(@PathVariable Long id) {
        Optional<PricingSuggestion> suggestion = suggestionService.getPricingSuggestionById(id);
        return suggestion.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PatchMapping("/pricing/{id}")
    public ResponseEntity<?> updatePricingSuggestionStatus(
            @PathVariable Long id, @RequestBody SuggestionStatusUpdateRequest request) {
        try {
            PricingSuggestion updatedSuggestion;
            switch (request.getStatus()) {
                case ACCEPTED:
                    updatedSuggestion = suggestionService.acceptPricingSuggestion(id);
                    break;
                case REJECTED:
                    updatedSuggestion = suggestionService.rejectPricingSuggestion(id);
                    break;
                default:
                    updatedSuggestion = suggestionService.updatePricingSuggestionStatus(id, request.getStatus());
                    break;
            }
            return new ResponseEntity<>(updatedSuggestion, HttpStatus.OK);
        } catch (InvalidSuggestionStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // Reorder Suggestions Endpoints
    @GetMapping("/reorder")
    public ResponseEntity<List<ReorderSuggestion>> getReorderSuggestions(
            @RequestParam(required = false) SuggestionStatus status) {
        List<ReorderSuggestion> suggestions;
        if (status != null) {
            suggestions = suggestionService.getReorderSuggestionsByStatus(status);
        } else {
            suggestions = suggestionService.getReorderSuggestionsByStatus(SuggestionStatus.PENDING);
        }
        return new ResponseEntity<>(suggestions, HttpStatus.OK);
    }

    @GetMapping("/reorder/{id}")
    public ResponseEntity<ReorderSuggestion> getReorderSuggestionById(@PathVariable Long id) {
        Optional<ReorderSuggestion> suggestion = suggestionService.getReorderSuggestionById(id);
        return suggestion.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PatchMapping("/reorder/{id}")
    public ResponseEntity<?> updateReorderSuggestionStatus(
            @PathVariable Long id, @RequestBody SuggestionStatusUpdateRequest request) {
        try {
            ReorderSuggestion updatedSuggestion;
            switch (request.getStatus()) {
                case ACCEPTED:
                    updatedSuggestion = suggestionService.acceptReorderSuggestion(id);
                    break;
                case REJECTED:
                    updatedSuggestion = suggestionService.rejectReorderSuggestion(id);
                    break;
                default:
                    updatedSuggestion = suggestionService.updateReorderSuggestionStatus(id, request.getStatus());
                    break;
            }
            return new ResponseEntity<>(updatedSuggestion, HttpStatus.OK);
        } catch (InvalidSuggestionStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // Additional compatibility endpoints
    @PatchMapping("/pricing-suggestions/{id}")
    public ResponseEntity<?> updatePricingSuggestionStatusCompat(
            @PathVariable Long id, @RequestBody SuggestionStatusUpdateRequest request) {
        return updatePricingSuggestionStatus(id, request);
    }

    @PatchMapping("/reorder-suggestions/{id}")
    public ResponseEntity<?> updateReorderSuggestionStatusCompat(
            @PathVariable Long id, @RequestBody SuggestionStatusUpdateRequest request) {
        return updateReorderSuggestionStatus(id, request);
    }

    // DTOs
    public static class SuggestionStatusUpdateRequest {
        private SuggestionStatus status;

        public SuggestionStatus getStatus() {
            return status;
        }

        public void setStatus(SuggestionStatus status) {
            this.status = status;
        }
    }
}