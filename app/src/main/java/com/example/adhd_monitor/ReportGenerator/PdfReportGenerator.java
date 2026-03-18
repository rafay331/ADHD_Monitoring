package com.example.adhd_monitor.ReportGenerator;

import android.content.Context;
import android.os.Environment;

import com.example.adhd_monitor.FocusSessionEntity;
import com.example.adhd_monitor.GoalEntity;
import com.example.adhd_monitor.MedicalHistory.BehavioralHistoryEntity;
import com.example.adhd_monitor.MedicalHistory.MedicationHistoryEntity;
import com.example.adhd_monitor.Questionnaire.Question;
import com.example.adhd_monitor.TaskEntity;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    // 1) ADHD Assessment Report (existing)
    public static File generateReport(Context context, int totalScore, String category, List<Question> questions) {
        try {
            String fileName = "ADHD_Report_" + System.currentTimeMillis() + ".pdf";
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ADHDReports");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, fileName);
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("ADHD Assessment Report").setBold().setFontSize(18));
            document.add(new Paragraph("Date: " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date())));
            document.add(new Paragraph("Total Score: " + totalScore));
            document.add(new Paragraph("ADHD Category: " + category));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Assessment Details:").setBold());
            if (questions != null) {
                for (Question q : questions) {
                    String answerText = q.getAnswer() == null ? "Unanswered" : (q.getAnswer() ? "Yes" : "No");
                    document.add(new Paragraph("• " + q.getText() + " → " + answerText));
                }
            }

            document.add(new Paragraph("\nReviewed by: ____________________________"));
            document.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 2) Medication History Report (existing)
    public static File generateMedicationHistoryPdf(Context context, List<MedicationHistoryEntity> meds) {
        try {
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MedicalReports");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "med_report.pdf");

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Medication History Report").setBold().setFontSize(18));
            document.add(new Paragraph("Generated: " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date())));
            document.add(new Paragraph(" "));

            if (meds != null && !meds.isEmpty()) {
                for (MedicationHistoryEntity med : meds) {
                    document.add(new Paragraph("• Date: " + med.date));
                    document.add(new Paragraph("  Medicine: " + med.medicineName));
                    document.add(new Paragraph("  Dosage: " + med.dosage));
                    document.add(new Paragraph("  Notes: " + med.notes));
                    document.add(new Paragraph("----------------------"));
                }
            } else {
                document.add(new Paragraph("No records available"));
            }

            document.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 3) Behavioral History Report (existing)
    public static File generateBehavioralHistoryPdf(Context context, List<BehavioralHistoryEntity> behs) {
        try {
            String fileName = "Behavioral_History_" + System.currentTimeMillis() + ".pdf";
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BehavioralReports");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, fileName);
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Behavioral History Report").setBold().setFontSize(18));
            document.add(new Paragraph("Generated: " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date())));
            document.add(new Paragraph(" "));

            if (behs == null || behs.isEmpty()) {
                document.add(new Paragraph("No behavioral history found."));
            } else {
                for (BehavioralHistoryEntity beh : behs) {
                    document.add(new Paragraph("• Date: " + beh.date));
                    document.add(new Paragraph("  Symptoms: " + beh.symptoms));
                    document.add(new Paragraph("  Notes: " + beh.notes));
                    document.add(new Paragraph("-----------------------------"));
                }
            }

            document.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 4) NEW: Progress Report (Focus/Tasks/Distractions/Sessions/Goals)
    public static File generateProgressReportPdf(
            Context context,
            String period,
            Integer focusMinutes,
            Integer completedTasks,
            Integer distractionScore,
            List<FocusSessionEntity> sessions,
            List<TaskEntity> recentTasks,
            List<GoalEntity> goals
    ) {
        try {
            String fileName = "Progress_Report_" + period + "_" + System.currentTimeMillis() + ".pdf";
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, fileName);
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header
            document.add(new Paragraph("ADHD Monitor – Progress Report").setBold().setFontSize(18));
            document.add(new Paragraph("Generated: " +
                    new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date())));
            document.add(new Paragraph("Period: " + (period == null ? "-" : period)));
            document.add(new Paragraph(" "));

            // Summary
            document.add(new Paragraph("Summary").setBold());
            document.add(new Paragraph("• Focus Time: " + (focusMinutes == null ? 0 : focusMinutes) + " min"));
            document.add(new Paragraph("• Completed Tasks: " + (completedTasks == null ? 0 : completedTasks)));
            document.add(new Paragraph("• Distraction Score: " + (distractionScore == null ? 0 : distractionScore)));
            document.add(new Paragraph(" "));

            // Goals
            document.add(new Paragraph("Improvement Goals").setBold());
            if (goals == null || goals.isEmpty()) {
                document.add(new Paragraph("No goals recorded."));
            } else {
                for (GoalEntity g : goals) {
                    String meta = "";
                    if (g.targetMinutesPerDay != null) meta += "target " + g.targetMinutesPerDay + "m/day";
                    if (g.deadline != null) {
                        String d = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(g.deadline));
                        meta += (meta.isEmpty() ? "" : ", ") + "deadline " + d;
                    }
                    document.add(new Paragraph("• " + g.title + (meta.isEmpty() ? "" : " (" + meta + ")")));
                }
            }
            document.add(new Paragraph(" "));

            // Recent tasks (completed)
            document.add(new Paragraph("Recent Completed Tasks").setBold());
            if (recentTasks == null || recentTasks.isEmpty()) {
                document.add(new Paragraph("No recent completions."));
            } else {
                int cap = Math.min(10, recentTasks.size());
                for (int i = 0; i < cap; i++) {
                    TaskEntity t = recentTasks.get(i);
                    String when = t.completedAtMillis > 0
                            ? new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                            .format(new Date(t.completedAtMillis))
                            : "-";
                    document.add(new Paragraph("• " + (t.title == null ? "Task" : t.title) +
                            "  — completed: " + when +
                            (t.pointsEarned > 0 ? ("  — +" + t.pointsEarned + " pts") : "")));
                }
            }
            document.add(new Paragraph(" "));

            // Recent focus sessions (compact list)
            document.add(new Paragraph("Recent Focus Sessions").setBold());
            if (sessions == null || sessions.isEmpty()) {
                document.add(new Paragraph("No focus sessions recorded."));
            } else {
                int cap = Math.min(10, sessions.size());
                for (int i = 0; i < cap; i++) {
                    FocusSessionEntity s = sessions.get(i);
                    int mins = (int) ((s.endTime - s.startTime) / 60000);
                    String when = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                            .format(new Date(s.startTime));
                    document.add(new Paragraph("• " + when +
                            " — " + mins + " min, " + s.distractions + " distractions"));
                }
            }

            document.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
