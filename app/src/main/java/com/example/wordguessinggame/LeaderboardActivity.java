package com.example.wordguessinggame;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

// Displays player rankings from Dreamlo leaderboard API
public class LeaderboardActivity extends AppCompatActivity {

    private LeaderboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        RecyclerView recyclerView = findViewById(R.id.rvLeaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize with empty list - populated after API call
        adapter = new LeaderboardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        fetchLeaderboardData();
    }

    // Gets leaderboard data from Dreamlo API
    private void fetchLeaderboardData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dreamlo.com/lb/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DreamloLeaderboardService service = retrofit.create(DreamloLeaderboardService.class);

        String PUBLIC_CODE = BuildConfig.PUBLIC_CODE;
        Call<DreamloResponse> call = service.getLeaderboard(PUBLIC_CODE);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DreamloResponse> call, @NonNull Response<DreamloResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LeaderboardEntry> entries = response.body().dreamlo.leaderboard.entry;

                    if (entries != null) {
                        adapter.updateData(entries);
                    } else {
                        Toast.makeText(LeaderboardActivity.this, "Leaderboard is empty", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DreamloResponse> call, @NonNull Throwable t) {
                Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Retrofit interface for Dreamlo API endpoint
    public interface DreamloLeaderboardService {
        @GET("{publicCode}/json")
        Call<DreamloResponse> getLeaderboard(@retrofit2.http.Path("publicCode") String publicCode);
    }

    // Data classes that map to Dreamlo JSON response structure
    public static class DreamloResponse {
        public Leaderboard dreamlo;
    }

    public static class Leaderboard {
        public LeaderboardEntries leaderboard;
    }

    public static class LeaderboardEntries {
        public List<LeaderboardEntry> entry;
    }

    // Single leaderboard entry with player data
    public static class LeaderboardEntry {
        public String name;    // Player name
        public int score;      // Player score
        public int seconds;    // Time taken in seconds

        @SuppressWarnings("unused")
        public String text;    // Optional metadata field

        @SuppressWarnings("unused")
        public String date;    // Entry date field

        // Converts seconds to MM:SS format
        public String getFormattedTime() {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds);
        }
    }
}