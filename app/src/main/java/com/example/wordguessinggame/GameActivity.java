package com.example.wordguessinggame;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GameActivity extends AppCompatActivity {
    // UI Components
    private TextView tvScore, tvTimer, tvAttempts, tvWordLength, tvSynonymHint;
    private EditText etGuess;
    private TextView tvCheckedLetters;

    // Game state variables
    private String secretWord = "";
    private int score = 100;
    private int attemptsLeft = 10;
    private long timeElapsed = 0;
    private int totalAttempts = 0;
    private Prefs prefs;
    private ApiService apiService;
    private CountDownTimer timer;

    // Game config constants
    private static final int LETTER_COST = 5;
    private static final int TIP_COST = 10;
    private static final int MAX_SYNONYM_RETRIES = 5;

    // Feature flags
    private boolean isWordLengthRevealed = false;
    private boolean isTipUsedThisWord = false;
    private int synonymRetryCount = 0;
    private final HashMap<Character, Integer> checkedLetters = new HashMap<>();

    // Check network connectivity
    private boolean isOffline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo == null || !netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initializeViews();
        setupGame();
    }

    // Bind UI components
    private void initializeViews() {
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvAttempts = findViewById(R.id.tvAttempts);
        etGuess = findViewById(R.id.etGuess);
        tvWordLength = findViewById(R.id.tvWordLength);
        tvSynonymHint = findViewById(R.id.tvSynonymHint);
        tvCheckedLetters = findViewById(R.id.tvCheckedLetters);
    }

    // Initialize game environment
    private void setupGame() {
        if (isOffline()) {
            showNetworkErrorAndFinish();
            return;
        }

        prefs = new Prefs(this);
        setupRetrofit();
        fetchNewWord();
        startTimer();
        updateUI();
    }

    // Show network error dialog and close activity
    private void showNetworkErrorAndFinish() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("This game requires an active internet connection to play.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    // Configure API client
    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.api-ninjas.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    // Request random word from API
    private void fetchNewWord() {
        if (isOffline()) {
            showNetworkErrorAndFinish();
            return;
        }

        apiService.getRandomWord().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RandomWordResponse> call,
                                   @NonNull Response<RandomWordResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    secretWord = response.body().getWord().toLowerCase();
                    Log.d("API", "Checking synonyms for: " + secretWord);
                    validateWordHasSynonyms();
                } else {
                    handleWordFetchFailure();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RandomWordResponse> call,
                                  @NonNull Throwable t) {
                handleWordFetchFailure();
            }
        });
    }

    // Check if the word has valid synonyms
    private void validateWordHasSynonyms() {
        apiService.getSynonyms(secretWord).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ThesaurusResponse> call,
                                   @NonNull Response<ThesaurusResponse> response) {
                if (response.isSuccessful()) {
                    ThesaurusResponse thesaurusResponse = response.body();
                    if (hasValidSynonyms(thesaurusResponse)) {
                        synonymRetryCount = 0;
                        resetWordSpecificState();
                    } else {
                        handleInvalidSynonyms();
                    }
                } else {
                    handleInvalidSynonyms();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ThesaurusResponse> call,
                                  @NonNull Throwable t) {
                handleInvalidSynonyms();
            }
        });
    }

    // Verify synonym quality
    private boolean hasValidSynonyms(ThesaurusResponse response) {
        return response != null &&
                response.synonyms != null &&
                !filterValidSynonyms(response.synonyms).isEmpty();
    }

    // Retry word fetch if synonyms are invalid
    private void handleInvalidSynonyms() {
        if (synonymRetryCount < MAX_SYNONYM_RETRIES) {
            synonymRetryCount++;
            Log.d("API", "Retrying word fetch (" + synonymRetryCount + "/" + MAX_SYNONYM_RETRIES + ")");
            fetchNewWord();
        } else {
            useFallbackWord();
        }
    }

    // Handle API failures gracefully
    private void handleWordFetchFailure() {
        Log.e("API", "Failed to fetch word");
        runOnUiThread(() -> {
            if (isOffline()) {
                showNetworkErrorAndFinish();
            } else {
                Toast.makeText(GameActivity.this,
                        "Failed to fetch word. Please try again.",
                        Toast.LENGTH_LONG).show();
                showGameOverDialog();
            }
        });
    }

    // Filter invalid synonyms from API response
    private List<String> filterValidSynonyms(List<String> synonyms) {
        List<String> valid = new ArrayList<>();
        for (String syn : synonyms) {
            if (isValidSynonym(syn)) {
                valid.add(syn);
            }
        }
        return valid;
    }

    // Validate synonym format and uniqueness
    private boolean isValidSynonym(String synonym) {
        return synonym != null &&
                !synonym.trim().isEmpty() &&
                !synonym.equalsIgnoreCase(secretWord) &&
                synonym.matches("^[a-zA-Z-']+(\\s+[a-zA-Z-']+)*$");
    }

    // Reset state for a new word
    private void resetWordSpecificState() {
        isWordLengthRevealed = false;
        isTipUsedThisWord = false;
        checkedLetters.clear();

        runOnUiThread(() -> {
            tvWordLength.setVisibility(View.GONE);
            tvSynonymHint.setVisibility(View.GONE);
            findViewById(R.id.btnWordLength).setEnabled(true);
            findViewById(R.id.btnTip).setEnabled(true);
            findViewById(R.id.btnTip).setVisibility(View.GONE);
            tvCheckedLetters.setVisibility(View.GONE);
            tvCheckedLetters.setText("");
        });

        totalAttempts = 0;
        Log.d("GameState", "Resetting attempts counter: " + totalAttempts);
    }

    // Fallback method when word fetching fails
    private void useFallbackWord() {
        showNetworkErrorAndFinish();
    }

    // Handle user's guess submission
    public void onSubmitGuess(View view) {
        String guess = etGuess.getText().toString().trim().toLowerCase();
        if (guess.isEmpty()) return;

        attemptsLeft--;
        totalAttempts++;

        // Show tip button after 5 attempts
        if (totalAttempts >= 5) {
            runOnUiThread(() -> {
                findViewById(R.id.btnTip).setVisibility(View.VISIBLE);
                Log.d("HintButton", "Showing tip button at attempt: " + totalAttempts);
            });
        }

        if (guess.equals(secretWord)) {
            handleCorrectGuess();
        } else {
            handleIncorrectGuess();
        }

        updateUI();
        checkGameStatus();
    }

    // Process correct guess
    private void handleCorrectGuess() {
        score += 50;
        postToLeaderboard();
        showSuccessDialog();
        resetRound();
    }

    // Process incorrect guess
    private void handleIncorrectGuess() {
        score = Math.max(0, score - 10);
    }

    // Handle letter check hint feature
    public void onCheckLetter(View view) {
        if (score < LETTER_COST) {
            showPointsWarning();
            return;
        }

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.letter_prompt)
                .setView(input)
                .setPositiveButton(R.string.check, (d, which) -> processLetterCheck(input.getText().toString()))
                .create();

        // Handle keyboard Enter key
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                return true;
            }
            return false;
        });

        dialog.show();

        // Show keyboard automatically
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }

    // Update the checked letters display
    private void updateCheckedLettersDisplay() {
        StringBuilder sb = new StringBuilder("Checked letters: ");
        for (Map.Entry<Character, Integer> entry : checkedLetters.entrySet()) {
            sb.append(entry.getKey())
                    .append("(")
                    .append(entry.getValue())
                    .append(") ");
        }

        runOnUiThread(() -> {
            tvCheckedLetters.setText(sb.toString().trim());
            tvCheckedLetters.setVisibility(View.VISIBLE);
        });
    }

    // Process letter check and update score
    private void processLetterCheck(String input) {
        if (input.length() == 1) {
            score = Math.max(0, score - LETTER_COST);
            char letter = input.toLowerCase().charAt(0);
            int count = countLetterOccurrences(letter);

            checkedLetters.put(letter, count);
            updateCheckedLettersDisplay();

            updateUI();
        }
    }

    // Count occurrences of a letter in secret word
    private int countLetterOccurrences(char letter) {
        return (int) secretWord.chars()
                .filter(c -> c == letter)
                .count();
    }

    // Handle word length hint feature
    public void onWordLength(View view) {
        if (isWordLengthRevealed || score < LETTER_COST) {
            if (score < LETTER_COST) showPointsWarning();
            return;
        }

        score -= LETTER_COST;
        isWordLengthRevealed = true;

        runOnUiThread(() -> {
            tvWordLength.setText(getString(R.string.word_length, secretWord.length()));
            tvWordLength.setVisibility(View.VISIBLE);
            findViewById(R.id.btnWordLength).setEnabled(false);
        });
        updateUI();
    }

    // Handle synonym tip feature
    public void onRequestTip(View view) {
        if (isTipUsedThisWord || score < TIP_COST) {
            if (score < TIP_COST) showPointsWarning();
            return;
        }

        score -= TIP_COST;
        isTipUsedThisWord = true;

        runOnUiThread(() -> findViewById(R.id.btnTip).setEnabled(false));

        fetchSynonyms();
        updateUI();
    }

    // Fetch synonyms for hint
    private void fetchSynonyms() {
        if (isOffline()) {
            Toast.makeText(this, "Internet connection required for tips", Toast.LENGTH_LONG).show();
            return;
        }

        apiService.getSynonyms(secretWord).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ThesaurusResponse> call,
                                   @NonNull Response<ThesaurusResponse> response) {
                if (response.isSuccessful()) {
                    ThesaurusResponse thesaurusResponse = response.body();
                    if (thesaurusResponse != null && thesaurusResponse.synonyms != null) {
                        showSingleSynonym(thesaurusResponse.synonyms);
                    } else {
                        Toast.makeText(GameActivity.this, "No synonyms found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response with proper resource management
                    String errorBody = "Unknown error";
                    ResponseBody errorResponse = response.errorBody();
                    if (errorResponse != null) {
                        try (errorResponse) {
                            errorBody = errorResponse.string();
                        } catch (IOException e) {
                            Log.e("API", "Error reading error body", e);
                            errorBody = "Error parsing error message";
                        }
                    }
                    Log.e("API", "API error: " + response.code() + " - " + errorBody);
                    Toast.makeText(GameActivity.this, "API error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ThesaurusResponse> call,
                                  @NonNull Throwable t) {
                Log.e("API", "Network error: " + t.getMessage(), t);
                Toast.makeText(GameActivity.this,
                        "Network error: " + t.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Display a random synonym as hint
    private void showSingleSynonym(List<String> synonyms) {
        runOnUiThread(() -> {
            List<String> validSynonyms = filterValidSynonyms(synonyms);

            if (!validSynonyms.isEmpty()) {
                String randomSynonym = validSynonyms.get(new Random().nextInt(validSynonyms.size()));
                tvSynonymHint.setText(getString(R.string.synonym_hint, randomSynonym));
                tvSynonymHint.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "No valid synonyms for this word", Toast.LENGTH_SHORT).show();
                if (synonymRetryCount < MAX_SYNONYM_RETRIES) {
                    fetchNewWord();
                }
            }
        });
    }

    // Start game timer
    private void startTimer() {
        if (timer != null) timer.cancel();

        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            public void onTick(long millisUntilFinished) {
                timeElapsed += 1000;
                runOnUiThread(() ->
                        tvTimer.setText(getString(R.string.time, timeElapsed / 1000)));
            }
            public void onFinish() {}
        }.start();
    }

    // Update score and attempts display
    private void updateUI() {
        runOnUiThread(() -> {
            tvScore.setText(getString(R.string.score, score));
            tvAttempts.setText(getString(R.string.attempts, attemptsLeft));
            etGuess.setText("");
        });
    }

    // Check for game over conditions
    private void checkGameStatus() {
        if (score <= 0 || attemptsLeft <= 0) {
            showGameOverDialog();
        }
    }

    // Display game over dialog
    private void showGameOverDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over)
                .setMessage(getString(R.string.final_score, score))
                .setPositiveButton(R.string.new_game, (d, w) -> resetRound())
                .setCancelable(false)
                .show();
    }

    // Reset game for a new round
    private void resetRound() {
        if (isOffline()) {
            showNetworkErrorAndFinish();
            return;
        }
        if (timer != null) timer.cancel();
        score = 100;
        attemptsLeft = 10;
        totalAttempts = 0;
        timeElapsed = 0;
        findViewById(R.id.btnTip).setVisibility(View.GONE);
        fetchNewWord();
        startTimer();
        updateUI();
        runOnUiThread(() -> findViewById(R.id.btnTip).setVisibility(View.GONE));
        Log.d("GameState", "Reset round - totalAttempts: " + totalAttempts);
    }

    // Submit score to online leaderboard
    private void postToLeaderboard() {
        String userName = prefs.getUserName();
        if (userName == null || userName.isEmpty()) {
            Toast.makeText(this, R.string.username_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://dreamlo.com/lb/"+BuildConfig.Private_Code+"/add/" +
                userName + "/" + score + "/" + (timeElapsed / 1000);

        new OkHttpClient().newCall(new Request.Builder().url(url).build())
                .enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(GameActivity.this,
                                        R.string.leaderboard_fail,
                                        Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull okhttp3.Call call,
                                           @NonNull okhttp3.Response response) {
                        Log.d("Leaderboard", "Score posted successfully");
                    }
                });
    }

    // Clean up resources
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    // Show insufficient points warning
    private void showPointsWarning() {
        Toast.makeText(this, R.string.not_enough_points, Toast.LENGTH_SHORT).show();
    }

    // Display success dialog when word is guessed
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.correct_guess)
                .setMessage(getString(R.string.success_message,
                        secretWord,
                        timeElapsed / 1000))
                .setPositiveButton(R.string.continue_game, null)
                .show();
    }
}