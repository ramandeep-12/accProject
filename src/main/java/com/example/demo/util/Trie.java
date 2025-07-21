package com.example.demo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trie (prefix tree) data structure implementation.
 * Supports efficient insertion and prefix-based searching of words.
 * Used for autocomplete functionality in the application.
 */
public class Trie {

    // =========================================
    // Inner Node Class
    // =========================================

    /**
     * Represents a single node in the Trie.
     * Contains child nodes and marks word endings.
     */
    private static class TrieNode {
        Map<Character, TrieNode> children; // Child nodes mapped by character
        boolean isEndOfWord;             // Marks completion of a valid word

        TrieNode() {
            this.children = new HashMap<>();
            this.isEndOfWord = false;
        }
    }

    // =========================================
    // Trie Fields and Initialization
    // =========================================

    private final TrieNode root; // Root node of the Trie

    /**
     * Constructs an empty Trie.
     */
    public Trie() {
        this.root = new TrieNode();
    }

    // =========================================
    // Public API Methods
    // =========================================

    /**
     * Inserts a word into the Trie.
     * 
     * @param word The word to insert (case-sensitive)
     */
    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, __ -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    /**
     * Finds all words in the Trie that start with the given prefix.
     * 
     * @param prefix The prefix to search for
     * @return List of matching words (empty if none found)
     */
    public List<String> searchPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode prefixNode = findNode(prefix);
        
        if (prefixNode != null) {
            findAllWords(prefixNode, prefix, results);
        }
        
        return results;
    }

    // =========================================
    // Private Helper Methods
    // =========================================

    /**
     * Locates the node corresponding to the end of a prefix.
     * 
     * @param prefix The prefix to locate
     * @return The node at the end of the prefix, or null if not found
     */
    private TrieNode findNode(String prefix) {
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * Recursively collects all words starting from a given node.
     * 
     * @param node The starting node
     * @param currentPrefix The accumulated prefix
     * @param results List to store found words
     */
    private void findAllWords(TrieNode node, String currentPrefix, List<String> results) {
        if (node.isEndOfWord) {
            results.add(currentPrefix);
        }
        
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            findAllWords(entry.getValue(), currentPrefix + entry.getKey(), results);
        }
    }
}