package com.example.adhd_monitor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.yourapp.R;
//import com.yourapp.data.entity.MoodJournalEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodJournalAdapter extends RecyclerView.Adapter<MoodJournalAdapter.VH> {

    private final List<MoodJournalEntity> list;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public MoodJournalAdapter(List<MoodJournalEntity> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_journal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MoodJournalEntity e = list.get(position);

        h.tvMood.setText("Mood: " + e.mood + " (Intensity " + e.intensity + ")");
        h.tvDate.setText(sdf.format(new Date(e.createdAt)));

        String note = (e.note == null || e.note.trim().isEmpty()) ? "(No note)" : e.note.trim();
        h.tvNotePreview.setText(note);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMood, tvDate, tvNotePreview;
        VH(@NonNull View itemView) {
            super(itemView);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNotePreview = itemView.findViewById(R.id.tvNotePreview);
        }
    }
}
