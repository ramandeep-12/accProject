package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking and managing search history.
 * Maintains counts of search terms and provides functionality to:
 * - Record searches
 * - Retrieve popular searches
 * - Clear search history
 * 
 * Uses thread-safe ConcurrentHashMap for concurrent access.
 */
@Service
public class SearchHistoryService {

    // =========================================
    // Fields
    // =========================================

    // Thread-safe map to store search terms and their counts
    private final Map<String, Integer> searchCounts = new ConcurrentHashMap<>();

    // =========================================
    // Public Methods
    // =========================================

    /**
     * Records a search term in the history.
     * Normalizes the term (lowercase and trim) before recording.
     * 
     * @param term The search term to record
     */
    public void recordSearch(String term) {
        if (term == null || term.trim().isEmpty()) return;
        
        String normalizedTerm = term.toLowerCase().trim();
        searchCounts.merge(normalizedTerm, 1, Integer::sum);
    }

    /**
     * Retrieves the most popular search terms.
     * 
     * @param limit Maximum number of popular searches to return
     * @return Map of search terms to their counts, ordered by popularity (descending)
     */
    public Map<String, Integer> getPopularSearches(int limit) {
        return searchCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(LinkedHashMap::new, 
                    (map, entry) -> map.put(entry.getKey(), entry.getValue()), 
                    Map::putAll);
    }

    /**
     * Clears all search history.
     */
    public void clearHistory() {
        searchCounts.clear();
    }
}