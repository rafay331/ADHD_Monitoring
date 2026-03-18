package com.example.adhd_monitor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adhd_monitor.ReportGenerator.PdfReportGenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportProgressActivity extends AppCompatActivity {

    private LineChart lineChart;
    private ReportViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_progress);

        // ----- Toolbar -----
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ----- ViewModel -----
        vm = new ViewModelProvider(this).get(ReportViewModel.class);

        // ✅ IMPORTANT: set BOTH IDs
        // focusUserId -> focus_sessions.userId (where your focus sessions were saved)
        // childId     -> tasks.childId (where your tasks belong)
        long focusUserId = getIntent().getLongExtra("focusUserId", 1L); // default 1L
        long childId     = getIntent().getLongExtra("childId", 2L);     // default 2L
        vm.setIds(focusUserId, childId);



        // ----- Summary views -----
        TextView tvFocus = findViewById(R.id.tvFocusTime);
        TextView tvCompleted = findViewById(R.id.tvCompletedTasks);
        TextView tvDistr = findViewById(R.id.tvDistractionScore);

        // ----- Chart -----
        lineChart = findViewById(R.id.lineChart);
        setupChartUi(lineChart);

        // ----- Buttons -----

        MaterialButton btnExportPdf = findViewById(R.id.btnExportPdf);

        btnExportPdf.setOnClickListener(v -> exportReportPdf());

        // ----- Period chips -----
        ChipGroup chipGroup = findViewById(R.id.chipGroupPeriod);
        chipGroup.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids == null || ids.isEmpty()) return;
            int id = ids.get(0);
            if (id == R.id.chipDaily)       vm.setPeriod("daily");
            else if (id == R.id.chipWeekly) vm.setPeriod("weekly");
            else                            vm.setPeriod("monthly");
        });

        // ----- Goals list -----


        // ----- Sessions list -----
        RecyclerView rvSessions = findViewById(R.id.rvSessions);
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        rvSessions.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        SessionAdapter sessionAdapter = new SessionAdapter(new ArrayList<>());
        rvSessions.setAdapter(sessionAdapter);

        // ----- LiveData observers -----
        vm.focusMinutes.observe(this, mins -> tvFocus.setText(formatMinutes(mins == null ? 0 : mins)));
        vm.completed.observe(this, c -> tvCompleted.setText(String.valueOf(c == null ? 0 : c)));
        vm.distractionScore.observe(this, score -> {
            int s = score == null ? 0 : score;
            tvDistr.setText(s + " " + bandLabel(s));
        });
        vm.sessions.observe(this, list -> {
            renderChart(list);
            sessionAdapter.submit(list == null ? Collections.emptyList() : list);
        });
    }

    // ----- Buttons implementation -----

    private void showAddGoalDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("E.g., Study 45m without phone");

        new AlertDialog.Builder(this)
                .setTitle("Set Improvement Goal")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String text = input.getText().toString().trim();
                    if (TextUtils.isEmpty(text)) {
                        Toast.makeText(this, "Goal cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // TODO: Persist via GoalDao insert
                    Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportReportPdf() {
        String period = "weekly";
        ChipGroup chipGroup = findViewById(R.id.chipGroupPeriod);
        int checkedId = chipGroup.getCheckedChipId();
        if (checkedId == R.id.chipDaily) period = "daily";
        else if (checkedId == R.id.chipMonthly) period = "monthly";

        Integer focus   = vm.focusMinutes.getValue();
        Integer done    = vm.completed.getValue();
        Integer score   = vm.distractionScore.getValue();
        List<FocusSessionEntity> sessions = vm.sessions.getValue();
        List<TaskEntity> recent   = vm.recentCompletions.getValue();
        List<GoalEntity> goals    = vm.goals.getValue();

        File pdf = PdfReportGenerator.generateProgressReportPdf(
                this, period, focus, done, score, sessions, recent, goals
        );

        if (pdf == null || !pdf.exists() || pdf.length() == 0) {
            Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        sharePdf(pdf);
    }

    // Inside ReportViewModel (below your other setters)
//    public void setIds(long focusUid, long childId) {
//        this.focusUserId.setValue(focusUid);
//        this.taskChildId.setValue(childId);
//    }

    private void sharePdf(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.setClipData(android.content.ClipData.newRawUri("PDF", uri));
            startActivity(Intent.createChooser(share, "Share report"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to share PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ----- UI helpers -----

    private String bandLabel(int score) {
        if (score <= 33) return "(Low)";
        if (score <= 66) return "(Med)";
        return "(High)";
    }

    private String formatMinutes(int mins) {
        int h = mins / 60;
        int m = mins % 60;
        return h == 0 ? (m + "m") : (h + "h " + m + "m");
    }

    private void setupChartUi(LineChart chart) {
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getAxisRight().setEnabled(false);
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        chart.getLegend().setEnabled(false);
        Description d = new Description();
        d.setText("");
        chart.setDescription(d);
    }

    private void renderChart(List<FocusSessionEntity> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            chartNoData(lineChart);
            return;
        }
        Map<String, Integer> minsPerDay = new LinkedHashMap<>();
        Calendar c = Calendar.getInstance();

        for (FocusSessionEntity s : sessions) {
            long startMs = s.startTime;
            long endMs   = s.endTime;
            int mins = (int) ((endMs - startMs) / 60000);

            c.setTimeInMillis(startMs);
            String key = (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
            Integer prev = minsPerDay.get(key);
            minsPerDay.put(key, (prev == null ? 0 : prev) + mins);
        }

        List<Entry> entries = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> e : minsPerDay.entrySet()) {
            entries.add(new Entry(i++, e.getValue()));
        }

        LineDataSet ds = new LineDataSet(entries, "Focus Minutes");
        ds.setLineWidth(2f);
        ds.setCircleRadius(3f);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setDrawValues(false);

        lineChart.setData(new LineData(ds));
        lineChart.animateX(300);
        lineChart.invalidate();
    }

    private void chartNoData(LineChart chart) {
        chart.clear();
        Description d = new Description();
        d.setText("No chart data available.");
        chart.setDescription(d);
        chart.invalidate();
    }

    // ----- Simple adapters -----

    static class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.VH> {
        List<GoalEntity> items;

        GoalAdapter(List<GoalEntity> items) { this.items = items; }

        void submit(List<GoalEntity> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(View v) {
                super(v);
                t1 = v.findViewById(R.id.tvGoalTitle);
                t2 = v.findViewById(R.id.tvGoalMeta);
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_goal, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            GoalEntity g = items.get(pos);
            h.t1.setText(g.title);
            String meta = (g.targetMinutesPerDay != null ? "Target: " + g.targetMinutesPerDay + "m/day" : "");
            if (g.deadline != null) {
                meta += (meta.isEmpty() ? "" : " • ") + "Deadline: " + new Date(g.deadline).toString();
            }
            h.t2.setText(meta);
        }

        @Override
        public int getItemCount() { return items == null ? 0 : items.size(); }
    }

    static class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.VH> {
        List<FocusSessionEntity> items;

        SessionAdapter(List<FocusSessionEntity> items) { this.items = items; }

        void submit(List<FocusSessionEntity> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(View v) {
                super(v);
                t1 = v.findViewById(R.id.tvSessionTitle);
                t2 = v.findViewById(R.id.tvSessionMeta);
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_session, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            FocusSessionEntity s = items.get(pos);
            int mins = (int) ((s.endTime - s.startTime) / 60000);
            h.t1.setText("Focus Session • " + mins + "m");
            h.t2.setText(new Date(s.startTime) + " • " + s.distractions + " distractions");
        }

        @Override
        public int getItemCount() { return items == null ? 0 : items.size(); }
    }
}
