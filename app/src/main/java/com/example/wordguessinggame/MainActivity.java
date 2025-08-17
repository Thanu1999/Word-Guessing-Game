package com.example.wordguessinggame;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Main entry point handling user authentication and navigation
public class MainActivity extends AppCompatActivity {
    private Prefs prefs;          // Persistent storage for user preferences
    private Button btnStartGame, btnLeaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new Prefs(this);  // Initialize preferences helper
        initializeViews();
        checkUserName();          // Verify user registration on launch
    }

    private void checkUserName() {
        if (prefs.getUserName() == null) {
            showNameDialog();     // First-time user flow
        } else {
            enableNavigationButtons();  // Returning user
        }
    }

    private void showNameDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Enter Your Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        prefs.saveUserName(name);  // Persist username
                        enableNavigationButtons();
                    } else {
                        showEmptyNameError();      // Validation feedback
                    }
                })
                .setCancelable(false)  // Mandatory user input
                .show();
    }

    private void enableNavigationButtons() {
        btnStartGame.setVisibility(View.VISIBLE);     // Game entry
        btnLeaderboard.setVisibility(View.VISIBLE);    // Scores view
    }

    private void initializeViews() {
        btnStartGame = findViewById(R.id.btn_start_game);
        btnLeaderboard = findViewById(R.id.btn_leaderboard);
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Launch game screen
        btnStartGame.setOnClickListener(v ->
                startActivity(new Intent(this, GameActivity.class)));

        // Show leaderboard
        btnLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class)));
    }

    private void showEmptyNameError() {
        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
    }
}