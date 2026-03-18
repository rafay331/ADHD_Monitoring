package com.example.adhd_monitor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CommunityChatAdapter extends RecyclerView.Adapter<CommunityChatAdapter.VH> {

    private final ArrayList<CommunityMessageEntity> list = new ArrayList<>();
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("hh:mm a", Locale.getDefault());

    // ---------------- Encourage Listener ----------------
    public interface OnEncourageClickListener {
        void onEncourageClick(CommunityMessageEntity msg);
    }

    private OnEncourageClickListener encourageListener;

    public void setOnEncourageClickListener(OnEncourageClickListener listener) {
        this.encourageListener = listener;
    }

    // ---------------- Data ----------------

    public void setData(java.util.List<CommunityMessageEntity> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    public void addMessage(CommunityMessageEntity msg) {
        list.add(msg);
        notifyItemInserted(list.size() - 1);
    }

    // ---------------- Recycler ----------------

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CommunityMessageEntity m = list.get(position);

        // Username (already anonymous alias)
        h.tvUsername.setText(m.username);

        // Points
        h.tvPoints.setText(m.participationPoints + " pts");

        // Budget rating stars
        h.rbBudget.setRating(m.budgetRating);

        // Message text
        h.tvMessage.setText(m.message);

        // Time
        h.tvTime.setText(sdf.format(new Date(m.createdAt)));

        // Encouragement count
        h.tvEncourageCount.setText("Encouragements: " + m.encouragementCount);

        // Encourage click
        h.tvEncourage.setOnClickListener(v -> {
            if (encourageListener != null) {
                encourageListener.onEncourageClick(m);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ---------------- ViewHolder ----------------

    static class VH extends RecyclerView.ViewHolder {

        TextView tvUsername, tvPoints, tvMessage, tvTime;
        TextView tvEncourage, tvEncourageCount;
        RatingBar rbBudget;

        VH(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            rbBudget = itemView.findViewById(R.id.rbBudget);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);

            // NEW
            tvEncourage = itemView.findViewById(R.id.tvEncourage);
            tvEncourageCount = itemView.findViewById(R.id.tvEncourageCount);
        }
    }
}
