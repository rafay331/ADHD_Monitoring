package com.example.adhd_monitor.MedicalHistory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adhd_monitor.R;

import java.io.File;
import java.util.List;

public class PdfReportAdapter extends RecyclerView.Adapter<PdfReportAdapter.ViewHolder> {

    private final Context context;
    private final List<MedicalHistoryPdfEntity> reportList;

    public PdfReportAdapter(Context context, List<MedicalHistoryPdfEntity> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pdf_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MedicalHistoryPdfEntity report = reportList.get(position);
        holder.txtType.setText(report.type + " Report");
        holder.txtDate.setText("Generated: " + report.dateGenerated);

        holder.itemView.setOnClickListener(v -> {
            File file = new File(report.filePath);
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserId, txtType, txtDate;
        Button btnViewPdf;

        public ViewHolder(View itemView) {
            super(itemView);
            txtUserId = itemView.findViewById(R.id.txtUserId);
            txtType = itemView.findViewById(R.id.txtType);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnViewPdf = itemView.findViewById(R.id.btnViewPdf);
        }
    }
}
