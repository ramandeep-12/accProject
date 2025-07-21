package com.example.demo.util;

import com.example.demo.model.CreditCard;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Provides spell checking functionality for credit card search terms.
 * Features include:
 * - Building a vocabulary from credit card data
 * - Calculating word frequencies
 * - Finding spelling suggestions using Levenshtein distance
 * - Providing word frequency statistics
 */
public class SpellChecking {
    // =========================================
    // Fields
    // =========================================

    private final Set<String> vocabulary;      // Set of all known words
    private final List<CreditCard> creditCards; // Reference to credit card data
    private final Map<String, Integer> wordFrequency = new HashMap<>(); // Word occurrence counts

    // =========================================
    // Constructor
    // =========================================

    /**
     * Initializes spell checker with credit card data.
     * Builds vocabulary and word frequency map during construction.
     * 
     * @param creditCards List of credit cards to build vocabulary from
     */
    public SpellChecking(List<CreditCard> creditCards) {
        this.creditCards = creditCards;
        this.vocabulary = buildVocabulary();
    }

    // =========================================
    // Public API Methods
    // =========================================

    /**
     * Gets spelling suggestions for a potentially misspelled word.
     * 
     * @param word The word to check
     * @param maxDistance Maximum allowed edit distance
     * @param maxSuggestions Maximum number of suggestions to return
     * @return List of suggested corrections (empty if none found)
     */
    public List<String> getSuggestions(String word, int maxDistance, int maxSuggestions) {
        List<String> suggestions = new ArrayList<>();
        
        for (String dictWord : vocabulary) {
            // Skip words with length difference > maxDistance for efficiency
            if (Math.abs(dictWord.length() - word.length()) > maxDistance) {
                continue;
            }
            
            int distance = levenshteinDistance(word.toLowerCase(), dictWord);
            if (distance <= maxDistance) {
                suggestions.add(dictWord);
                if (suggestions.size() >= maxSuggestions) {
                    break;
                }
            }
        }
        
        return suggestions;
    }

    /**
     * Gets the frequency count of a word in the vocabulary.
     * 
     * @param word The word to check
     * @return Number of times the word appears (0 if not found)
     */
    public int getWordFrequency(String word) {
        return wordFrequency.getOrDefault(word.toLowerCase(), 0);
    }

    // =========================================
    // Vocabulary Building Methods
    // =========================================

    /**
     * Builds vocabulary from credit card data.
     * Extracts words from titles, descriptions, and bank names.
     * 
     * @return Set of unique words in lowercase
     */
    private Set<String> buildVocabulary() {
        Set<String> vocabulary = new HashSet<>();
        Pattern wordPattern = Pattern.compile("\\b[a-zA-Z]+\\b");

        for (CreditCard card : creditCards) {
            addWordsToVocabulary(card.getCardTitle(), vocabulary, wordPattern);
            addWordsToVocabulary(card.getProductValueProp(), vocabulary, wordPattern);
            addWordsToVocabulary(card.getProductBenefits(), vocabulary, wordPattern);
            addWordsToVocabulary(card.getBankName(), vocabulary, wordPattern);
        }
        
        return vocabulary;
    }

    /**
     * Extracts words from text and adds them to vocabulary.
     * Also updates word frequency counts.
     * 
     * @param text The text to process
     * @param vocabulary Set to add words to
     * @param pattern Regex pattern for word matching
     */
    private void addWordsToVocabulary(String text, Set<String> vocabulary, Pattern pattern) {
        if (text != null) {
            Matcher matcher = pattern.matcher(text.toLowerCase());
            while (matcher.find()) {
                String word = matcher.group();
                vocabulary.add(word);
                wordFrequency.merge(word, 1, Integer::sum);
            }
        }
    }

    // =========================================
    // String Distance Calculation
    // =========================================

    /**
     * Calculates Levenshtein edit distance between two words.
     * (Number of edits needed to change word1 into word2)
     * 
     * @param word1 First word
     * @param word2 Second word
     * @return Edit distance (number of changes required)
     */
    private int levenshteinDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];

        for (int i = 0; i <= word1.length(); i++) {
            for (int j = 0; j <= word2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int substitutionCost = word1.charAt(i - 1) == word2.charAt(j - 1) ? 0 : 1;
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + substitutionCost
                    );
                }
            }
        }
        
        return dp[word1.length()][word2.length()];
    }
}