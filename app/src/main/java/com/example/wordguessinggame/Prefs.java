package com.example.wordguessinggame;

import android.content.Context;
import android.content.SharedPreferences;

// Manages persistent storage for user preferences using SharedPreferences
public class Prefs {
    private final SharedPreferences sharedPref;

    // Initialize preferences with private storage
    public Prefs(Context context) {
        sharedPref = context.getSharedPreferences("WORD_GAME_PREFS", Context.MODE_PRIVATE);
    }

    // Persist username across app sessions
    public void saveUserName(String name) {
        sharedPref.edit().putString("USER_NAME", name).apply(); // Async save
    }

    // Retrieve stored username (returns null if never saved)
    public String getUserName() {
        return sharedPref.getString("USER_NAME", null); // Default null indicates new user
    }
}