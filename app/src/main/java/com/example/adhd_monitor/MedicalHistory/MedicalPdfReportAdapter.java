package com.example.adhd_monitor.MedicalHistory;

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

public class MedicalPdfReportAdapter extends RecyclerView.Adapter<MedicalPdfReportAdapter.PdfViewHolder> {

    private final Context context;
    private final List<MedicalHistoryPdfEntity> reportList;

    public MedicalPdfReportAdapter(Context context, List<MedicalHistoryPdfEntity> reports) {
        this.context = context;
        this.reportList = reports;
    }

    @Override
    public PdfViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pdf_report, parent, false);
        return new PdfViewHolder(view);
    }


    @Override
    public void onBindViewHolder(PdfViewHolder holder, int position) {
        MedicalHistoryPdfEntity report = reportList.get(position);

        holder.txtUserId.setText("User ID: " + report.userId);
        holder.txtType.setText("Type: " + report.type + " Report");
        holder.txtDate.setText("Generated on: " + report.dateGenerated);

        holder.btnViewPdf.setOnClickListener(v -> {
            File file = new File(report.filePath);
            if (file.exists()) {
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "PDF file not found", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserId, txtType, txtDate;

        Button btnViewPdf;

        public PdfViewHolder(View itemView) {
            super(itemView);
            txtUserId = itemView.findViewById(R.id.txtUserId);
            txtType = itemView.findViewById(R.id.txtType);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnViewPdf = itemView.findViewById(R.id.btnViewPdf);

            btnViewPdf = new Button(itemView.getContext());
            btnViewPdf.setText("View PDF");

            ((ViewGroup) itemView).addView(btnViewPdf);
        }
    }
}
