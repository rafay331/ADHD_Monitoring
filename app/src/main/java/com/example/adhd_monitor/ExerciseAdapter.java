package com.example.adhd_monitor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExViewHolder> {

    public interface OnExerciseClickListener {
        void onExerciseClicked(int position);
    }

    private final ExerciseItem[] items;
    private final OnExerciseClickListener listener;

    private int selectedPos = RecyclerView.NO_POSITION;

    public ExerciseAdapter(ExerciseItem[] items, OnExerciseClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExViewHolder holder, int position) {
        ExerciseItem item = items[position];

        holder.tvTitle.setText(item.getTitle());
        holder.tvDuration.setText(item.getDuration());

        // ✅ Highlight selected item
        if (position == selectedPos) {
            holder.root.setBackgroundColor(Color.parseColor("#667eea")); // selected
        } else {
            holder.root.setBackgroundColor(Color.parseColor("#6b7280")); // normal
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPos;
            selectedPos = holder.getAdapterPosition();

            // refresh old + new to update highlight
            if (oldPos != RecyclerView.NO_POSITION) notifyItemChanged(oldPos);
            notifyItemChanged(selectedPos);

            if (listener != null) {
                listener.onExerciseClicked(selectedPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    static class ExViewHolder extends RecyclerView.ViewHolder {
        LinearLayout root;
        TextView tvTitle, tvDuration;

        public ExViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.cardRoot);
            tvTitle = itemView.findViewById(R.id.tvExerciseTitle);
            tvDuration = itemView.findViewById(R.id.tvExerciseDuration);
        }
    }
}
