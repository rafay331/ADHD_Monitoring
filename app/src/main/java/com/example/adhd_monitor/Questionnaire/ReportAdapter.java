package com.example.adhd_monitor.Questionnaire;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adhd_monitor.AppDatabase;
import com.example.adhd_monitor.Questionnaire.database.AdhdReportEntity;
import com.example.adhd_monitor.R;

import java.io.File;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<AdhdReportEntity> reportList;
    private final Context context;

    public ReportAdapter(Context context, List<AdhdReportEntity> reports) {
        this.context = context;
        this.reportList = reports;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.report_item, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        AdhdReportEntity report = reportList.get(position);

        holder.txtUserId.setText("User ID: " + report.userId);
        holder.txtDate.setText("Date: " + report.date);
        holder.txtScore.setText("Score: " + report.score);
        holder.txtSpectrum.setText("Spectrum: " + report.spectrum);

        if (report.comments != null && !report.comments.isEmpty()) {
            holder.txtComment.setText("Notes: " + report.comments);
        } else {
            holder.txtComment.setText("Notes: (None)");
        }

        holder.btnViewPdf.setOnClickListener(v -> {
            File pdfFile = new File(report.filePath);
            if (pdfFile.exists()) {
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "PDF file not found.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnNotes.setOnClickListener(v -> showEditNotesDialog(report, position));
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserId, txtDate, txtScore, txtSpectrum, txtComment;
        Button btnViewPdf, btnNotes;

        public ReportViewHolder(View itemView) {
            super(itemView);
            txtUserId = itemView.findViewById(R.id.txtUserId);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtScore = itemView.findViewById(R.id.txtScore);
            txtSpectrum = itemView.findViewById(R.id.txtSpectrum);
            txtComment = itemView.findViewById(R.id.txtComment);
            btnViewPdf = itemView.findViewById(R.id.btnViewPdf);
            btnNotes = itemView.findViewById(R.id.btnNotes);
        }
    }

    private void showEditNotesDialog(AdhdReportEntity report, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Psychologist Notes");

        final EditText input = new EditText(context);
        input.setText(report.comments);
        input.setLines(4);
        input.setPadding(40, 40, 40, 40);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newComment = input.getText().toString();
            report.comments = newComment;

            // Update in DB
            new Thread(() -> {
                AppDatabase.getInstance(context).adhdReportDao().updateReport(report);
            }).start();

            Toast.makeText(context, "Notes saved", Toast.LENGTH_SHORT).show();
            notifyItemChanged(position);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
