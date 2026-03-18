package com.example.adhd_monitor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentRecentExpenseAdapter extends RecyclerView.Adapter<ParentRecentExpenseAdapter.VH> {

    private final List<ExpenseEntity> data;

    public ParentRecentExpenseAdapter(List<ExpenseEntity> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExpenseEntity e = data.get(position);

        h.t1.setText(e.category + " - " + e.amount + " PKR");

        String when = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(new Date(e.createdAt));
        h.t2.setText(when);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView t1, t2;
        VH(@NonNull View itemView) {
            super(itemView);
            t1 = itemView.findViewById(android.R.id.text1);
            t2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
