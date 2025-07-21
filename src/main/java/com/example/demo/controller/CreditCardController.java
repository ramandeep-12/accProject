package com.example.demo.controller;

import com.example.demo.model.CreditCard;
import com.example.demo.service.CreditCardService;
import com.example.demo.service.SearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/creditcards")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private SearchHistoryService searchHistoryService;

@CrossOrigin(origins = "http://127.0.0.1:5500")
@GetMapping("/page-ranking")
public ResponseEntity<Map<String, Object>> getPageRanking(@RequestParam String term) {
    return ResponseEntity.ok(creditCardService.getRankedSearchResults(term));
}
    @GetMapping
    public List<CreditCard> getAllCards(
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) Double minFee,
            @RequestParam(required = false) Double maxFee,
            @RequestParam(required = false) Double minInterest,
            @RequestParam(required = false) Double maxInterest,
            @RequestParam(required = false) String search,
             @RequestParam(required = false) Boolean rank) {

        List<CreditCard> filteredCards = creditCardService.getAllCards();

        if (bankName != null) {
            filteredCards = creditCardService.getCardsByBank(filteredCards, bankName);
        }
        if (minFee != null && maxFee != null) {
            filteredCards = creditCardService.getCardsByAnnualFees(filteredCards, minFee, maxFee);
        }
        if (minInterest != null && maxInterest != null) {
            filteredCards = creditCardService.getCardsByPurchaseInterestRate(filteredCards, minInterest, maxInterest);
        }
        if (search != null && !search.trim().isEmpty()) {
            filteredCards = creditCardService.getCardsBySearchTerm(filteredCards, search.trim().toLowerCase());
        }
            if (rank != null && rank && search != null && !search.trim().isEmpty()) {
        return creditCardService.rankCardsByRelevance(filteredCards, search);
    }
    

        return filteredCards;
    }

    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @GetMapping("/autocomplete")
    public List<String> getAutocompleteSuggestions(@RequestParam String prefix) {
        return creditCardService.getAutocompleteSuggestions(prefix);
    }

    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @GetMapping("/spelling-suggestions")
    public List<String> getSpellingSuggestions(@RequestParam String word) {
        return creditCardService.getSpellingSuggestions(word);
    }

    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @GetMapping("/word-frequency")
    public ResponseEntity<?> getWordFrequency(@RequestParam String word) {
        int count = creditCardService.getWordFrequency(word);
        return ResponseEntity.ok(Map.of("word", word, "count", count));
    }

    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @GetMapping("/search-history")
    public ResponseEntity<Map<String, Integer>> getSearchHistory(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchHistoryService.getPopularSearches(limit));
    }

    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @DeleteMapping("/search-history")
    public ResponseEntity<Void> clearSearchHistory() {
        searchHistoryService.clearHistory();
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @PostMapping("/search-history")
    public ResponseEntity<Void> recordSearch(@RequestBody Map<String, String> request) {
        String term = request.get("term");
        if (term != null && !term.trim().isEmpty()) {
            searchHistoryService.recordSearch(term);
        }
        return ResponseEntity.ok().build();
    }
    @CrossOrigin(origins = "http://127.0.0.1:5500")
@PostMapping("/rank")
public ResponseEntity<List<CreditCard>> rankCards(@RequestBody List<CreditCard> cards) {
    List<CreditCard> rankedCards = creditCardService.rankCards(cards);
    return ResponseEntity.ok(rankedCards);
}
}
