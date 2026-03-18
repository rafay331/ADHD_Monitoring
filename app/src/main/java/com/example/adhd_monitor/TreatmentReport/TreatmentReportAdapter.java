package com.example.adhd_monitor.TreatmentReport;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adhd_monitor.R;

import java.io.File;
import java.util.List;

public class TreatmentReportAdapter extends RecyclerView.Adapter<TreatmentReportAdapter.ViewHolder> {

    private final List<TreatmentReportEntity> reports;
    private final Context context;

    public TreatmentReportAdapter(Context context, List<TreatmentReportEntity> reports) {
        this.context = context;
        this.reports = reports;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPsychologist, txtNote, txtDate;
        Button btnViewPdf;

        public ViewHolder(View itemView) {
            super(itemView);
            txtPsychologist = itemView.findViewById(R.id.txtPsychologist);
            txtNote = itemView.findViewById(R.id.txtNote);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnViewPdf = itemView.findViewById(R.id.btnViewPdf);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_treatment_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TreatmentReportEntity report = reports.get(position);

        String psychologistName = report.psychologistName != null ? report.psychologistName : "Unknown";
        String note = report.note != null ? report.note : "No notes provided";
        String date = report.dateGenerated != null ? report.dateGenerated : "N/A";

        holder.txtPsychologist.setText("Psychologist: " + psychologistName);
        holder.txtNote.setText("Note: " + note);
        holder.txtDate.setText("Date: " + date);

        holder.btnViewPdf.setOnClickListener(v -> {
            if (report.filePath != null && !report.filePath.isEmpty()) {
                File pdfFile = new File(report.filePath);
                if (pdfFile.exists()) {
                    Uri uri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".provider",
                            pdfFile
                    );

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/pdf");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Intent chooser = Intent.createChooser(intent, "Open Treatment Report");
                    context.startActivity(chooser);
                } else {
                    Toast.makeText(context, "PDF file not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Invalid PDF path.", Toast.LENGTH_SHORT).show();
            }
        });



    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }
}
