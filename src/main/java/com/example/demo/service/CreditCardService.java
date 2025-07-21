package com.example.demo.service;

import com.example.demo.model.CreditCard;
import com.example.demo.util.ExcelReader;
import com.example.demo.util.SpellChecking;
import com.example.demo.util.Trie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CreditCardService {

    private final List<CreditCard> creditCards;
    private final Trie trie;
    private final SpellChecking spellChecker;

    // NEW: Page ranking data structures
    private final Map<String, Double> idfMap;
    private final Map<CreditCard, Map<String, Integer>> cardTermFrequencies;
    private final Map<CreditCard, Double> cardNorms;

    @Autowired
    private SearchHistoryService searchHistoryService;

    public CreditCardService() {
        try {
            InputStream fileStream = getClass().getClassLoader()
                    .getResourceAsStream("Credit_Card_Details.xlsx");
            if (fileStream == null) {
                throw new RuntimeException("Excel file not found in resources folder.");
            }
            this.creditCards = ExcelReader.readCreditCardsFromExcel(fileStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }

        this.trie = new Trie();
        this.spellChecker = new SpellChecking(creditCards);
        buildTrie();
        
        // Initialize page ranking structures
        this.idfMap = new HashMap<>();
        this.cardTermFrequencies = new HashMap<>();
        this.cardNorms = new HashMap<>();
        buildPageRankingModel();
    }

    // NEW: Build page ranking model using TF-IDF
    private void buildPageRankingModel() {
        // 1. Calculate term frequencies for each card
        Pattern wordPattern = Pattern.compile("\\b\\w+\\b");
        for (CreditCard card : creditCards) {
            String content = (card.getCardTitle() + " " + card.getProductValueProp() + " " + 
                            card.getProductBenefits() + " " + card.getBankName()).toLowerCase();
            
            Map<String, Integer> termFreq = new HashMap<>();
            Matcher matcher = wordPattern.matcher(content);
            while (matcher.find()) {
                String word = matcher.group();
                termFreq.put(word, termFreq.getOrDefault(word, 0) + 1);
            }
            cardTermFrequencies.put(card, termFreq);
        }
        
        // 2. Calculate inverse document frequency (IDF)
        int totalCards = creditCards.size();
        Map<String, Integer> docFrequency = new HashMap<>();
        
        for (Map<String, Integer> termFreq : cardTermFrequencies.values()) {
            for (String term : termFreq.keySet()) {
                docFrequency.put(term, docFrequency.getOrDefault(term, 0) + 1);
            }
        }
        
        for (String term : docFrequency.keySet()) {
            double idf = Math.log((double) totalCards / docFrequency.get(term));
            idfMap.put(term, idf);
        }
        
        // 3. Precompute document norms for cosine similarity
        for (CreditCard card : creditCards) {
            double norm = 0.0;
            Map<String, Integer> termFreq = cardTermFrequencies.get(card);
            
            for (String term : termFreq.keySet()) {
                double tf = termFreq.get(term);
                double idf = idfMap.getOrDefault(term, 0.0);
                double tfidf = tf * idf;
                norm += tfidf * tfidf;
            }
            cardNorms.put(card, Math.sqrt(norm));
        }
    }

    // NEW: Page ranking using TF-IDF and cosine similarity
    public List<CreditCard> rankCardsByRelevance(List<CreditCard> cards, String query) {
        if (query == null || query.trim().isEmpty()) {
            return cards;
        }
        
        // Tokenize query
        String[] queryTerms = query.toLowerCase().split("\\s+");
        Map<String, Integer> queryTf = new HashMap<>();
        for (String term : queryTerms) {
            queryTf.put(term, queryTf.getOrDefault(term, 0) + 1);
        }
        
        // Calculate query vector norm
        double queryNorm = 0.0;
        for (String term : queryTf.keySet()) {
            double idf = idfMap.getOrDefault(term, 0.0);
            double tf = queryTf.get(term);
            double tfidf = tf * idf;
            queryNorm += tfidf * tfidf;
        }
        queryNorm = Math.sqrt(queryNorm);
        
        // Calculate cosine similarity for each card
        Map<CreditCard, Double> scores = new HashMap<>();
        for (CreditCard card : cards) {
            if (!cardTermFrequencies.containsKey(card)) continue;
            
            double dotProduct = 0.0;
            Map<String, Integer> cardTf = cardTermFrequencies.get(card);
            
            for (String term : queryTf.keySet()) {
                if (cardTf.containsKey(term)) {
                    double cardTfidf = cardTf.get(term) * idfMap.getOrDefault(term, 0.0);
                    double queryTfidf = queryTf.get(term) * idfMap.getOrDefault(term, 0.0);
                    dotProduct += cardTfidf * queryTfidf;
                }
            }
            
            double cardNorm = cardNorms.get(card);
            double similarity = (queryNorm > 0 && cardNorm > 0) ? 
                dotProduct / (queryNorm * cardNorm) : 0;
            
            scores.put(card, similarity);
        }
        
        // Rank by similarity score
        return cards.stream()
            .filter(card -> scores.getOrDefault(card, 0.0) > 0)
            .sorted((c1, c2) -> Double.compare(scores.get(c2), scores.get(c1)))
            .collect(Collectors.toList());
    }

    // Updated search method to use page ranking
    public List<CreditCard> getCardsBySearchTerm(List<CreditCard> cards, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return cards;
        }
        
        searchHistoryService.recordSearch(searchTerm);
        return rankCardsByRelevance(cards, searchTerm);
    }

    // ... rest of existing methods unchanged ...
    public List<CreditCard> getAllCards() {
        return creditCards;
    }

    public List<CreditCard> getCardsByBank(List<CreditCard> cards, String bankName) {
        return cards.stream()
                .filter(card -> card.getBankName().trim().equalsIgnoreCase(bankName.trim()))
                .collect(Collectors.toList());
    }

    public List<CreditCard> getCardsByAnnualFees(List<CreditCard> cards, Double minFee, Double maxFee) {
        return cards.stream()
                .filter(card -> {
                    try {
                        double fee = Double.parseDouble(card.getAnnualFees().replace("$", "").trim());
                        return fee >= minFee && fee <= maxFee;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid annual fee format: " + card.getAnnualFees());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public List<CreditCard> getCardsByPurchaseInterestRate(List<CreditCard> cards,
                                                            Double minInterest, Double maxInterest) {
        return cards.stream()
                .filter(card -> {
                    try {
                        double interestRate = Double.parseDouble(card.getPurchaseInterestRate()) * 100;
                        return interestRate >= minInterest && interestRate <= maxInterest;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid purchase interest rate format: " + card.getPurchaseInterestRate());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private void buildTrie() {
        for (CreditCard card : creditCards) {
            String[] tokens = (card.getCardTitle() + " " + card.getProductValueProp() + " " + card.getProductBenefits())
                    .toLowerCase().split("\\s+");
            for (String token : tokens) {
                trie.insert(token);
            }
        }
    }

    public List<String> getAutocompleteSuggestions(String prefix) {
        return trie.searchPrefix(prefix.toLowerCase());
    }

    public List<String> getSpellingSuggestions(String word) {
        return spellChecker.getSuggestions(word, 2, 3);
    }

    public int getWordFrequency(String word) {
        return spellChecker.getWordFrequency(word);
    }

    public List<CreditCard> rankCards(List<CreditCard> cards) {
    return rankCardsByRelevance(cards, ""); // or some default logic
}
// Add this method to CreditCardService.java
public Map<String, Object> getRankedSearchResults(String searchTerm) {
    Map<String, Object> result = new LinkedHashMap<>();
    List<CreditCard> cards = getCardsBySearchTerm(getAllCards(), searchTerm);
    
    // Calculate TF-IDF scores
    Map<CreditCard, Double> scores = new HashMap<>();
    Map<CreditCard, Integer> occurrences = new HashMap<>();
    
    String[] terms = searchTerm.toLowerCase().split("\\s+");
    int totalCards = creditCards.size();
    
    for (CreditCard card : cards) {
        double score = 0.0;
        int termCount = 0;
        
        // Get card content
        String content = (card.getCardTitle() + " " + 
                         card.getProductValueProp() + " " + 
                         card.getProductBenefits() + " " + 
                         card.getBankName()).toLowerCase();
        
        for (String term : terms) {
            // Count term occurrences
            int count = 0;
            int index = content.indexOf(term);
            while (index >= 0) {
                count++;
                index = content.indexOf(term, index + term.length());
            }
            
            termCount += count;
            
            // Calculate TF-IDF
            if (count > 0) {
                double tf = (double) count / content.split("\\s+").length;
                double idf = Math.log((double) totalCards / 
                                     cardTermFrequencies.values().stream()
                                        .filter(tfMap -> tfMap.containsKey(term))
                                        .count());
                score += tf * idf;
            }
        }
        
        scores.put(card, score);
        occurrences.put(card, termCount);
    }
    
    // Sort cards by relevance score
    List<CreditCard> sortedCards = cards.stream()
        .sorted((c1, c2) -> Double.compare(scores.get(c2), scores.get(c1)))
        .collect(Collectors.toList());
    
    // Prepare results
    result.put("searchTerm", searchTerm);
    
    List<Map<String, Object>> rankedResults = new ArrayList<>();
    for (CreditCard card : sortedCards) {
        Map<String, Object> cardResult = new LinkedHashMap<>();
        cardResult.put("title", card.getCardTitle());
        cardResult.put("bank", card.getBankName());
        cardResult.put("url", card.getCardLink());
        cardResult.put("relevance", scores.get(card));
        cardResult.put("occurrences", occurrences.get(card));
        rankedResults.add(cardResult);
    }
    
    result.put("results", rankedResults);
    return result;
}


    
}