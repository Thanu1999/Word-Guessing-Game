package com.example.wordguessinggame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adapter for binding leaderboard entries to RecyclerView
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardActivity.LeaderboardEntry> entries;

    public LeaderboardAdapter(List<LeaderboardActivity.LeaderboardEntry> entries) {
        this.entries = entries;
    }

    // Updates data with D  iffUtil for efficient RecyclerView updates
    public void updateData(List<LeaderboardActivity.LeaderboardEntry> newEntries) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffCallback(entries, newEntries));
        entries = newEntries;
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardActivity.LeaderboardEntry entry = entries.get(position);
        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvName.setText(entry.name);
        holder.tvScore.setText(String.valueOf(entry.score));
        holder.tvTime.setText(entry.getFormattedTime());
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    // ViewHolder caches view references for efficient recycling
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPosition, tvName, tvScore, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvName = itemView.findViewById(R.id.tvName);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    // Calculates differences between old and new data for efficient updates
    private static class DiffCallback extends DiffUtil.Callback {
        private final List<LeaderboardActivity.LeaderboardEntry> oldList, newList;

        public DiffCallback(List<LeaderboardActivity.LeaderboardEntry> oldList,
                            List<LeaderboardActivity.LeaderboardEntry> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        // Identifies if items represent the same entity (using name as unique identifier)
        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).name.equals(newList.get(newPos).name);
        }

        // Checks if the content of items is identical
        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).score == newList.get(newPos).score &&
                    oldList.get(oldPos).seconds == newList.get(newPos).seconds;
        }
    }
}