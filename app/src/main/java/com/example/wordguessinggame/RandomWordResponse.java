package com.example.wordguessinggame;

import java.util.List;

// API response wrapper for random word endpoint
public class RandomWordResponse {
    private List<String> word;  // List containing single word from API

    // Required for Retrofit JSON deserialization
    @SuppressWarnings("unused")
    public void setWord(List<String> word) {
        this.word = word;
    }

    // Returns first word in lowercase for game logic
    public String getWord() {
        if (word != null && !word.isEmpty()) {
            return word.get(0).toLowerCase();
        }
        return ""; // Handle empty responses
    }
}