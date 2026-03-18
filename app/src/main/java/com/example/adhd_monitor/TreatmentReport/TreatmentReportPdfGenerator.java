package com.example.adhd_monitor.TreatmentReport;

import android.content.Context;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;

public class TreatmentReportPdfGenerator {

    public static File generate(Context context, TreatmentReportEntity report) {
        try {
            File file = new File(context.getExternalFilesDir(null), "treatment_report.pdf");
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            doc.add(new Paragraph("Treatment Report").setBold().setFontSize(18));
            doc.add(new Paragraph("Psychologist: " + safe(report.psychologistName)));
            doc.add(new Paragraph("Date: " + safe(report.dateGenerated)));

            doc.add(new Paragraph("\nTreatment Note:").setBold());
            doc.add(new Paragraph(safe(report.note)));

            // ✅ NEW SECTION: ADHD Coping Plan
            doc.add(new Paragraph("\nADHD Coping Plan:").setBold());
            doc.add(new Paragraph(safe(report.copingPlan)));

            doc.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}
