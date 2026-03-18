package com.example.adhd_monitor;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Child-facing task list:
 * - Lists all tasks assigned to the logged-in child
 * - Shows per-task progress (steps done / total)
 * - Lets child toggle individual steps
 * - Auto-marks a task complete and awards points when all steps are done
 */
public class UserTasksActivity extends AppCompatActivity {

    // --------------------------
    // Replace with real child id from your session / link
    // --------------------------
    private long currentChildId = 2L; // TODO: wire to actual logged-in child id

    // --------------------------
    // DB / DAO / threading
    // --------------------------
    private AppDatabase db;
    private TaskDao taskDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    // --------------------------
    // Views
    // --------------------------
    @Nullable private RecyclerView recyclerTasks;
    @Nullable private TextView tvEmptyState;

    // --------------------------
    // Adapter state
    // --------------------------
    private final List<UiTaskChild> uiTasks = new ArrayList<>();
    @Nullable private TaskAdapter adapter;

    // ============================================================
    // Lifecycle
    // ============================================================
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tasks);

        // 1) Init Room
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "adhd_monitor_db").build();
        taskDao = db.taskDao();

        // (Optional) receive childId via Intent extra
        long maybeChildId = getIntent().getLongExtra("child_id", -1L);
        if (maybeChildId > 0) currentChildId = maybeChildId;

        // 2) Bind views
        recyclerTasks = findViewById(R.id.recyclerTasks);
        tvEmptyState  = findViewById(R.id.tvEmptyState);

        // 3) Setup Recycler
        if (recyclerTasks != null) {
            adapter = new TaskAdapter(uiTasks, stepToggleListener);
            recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
            recyclerTasks.setAdapter(adapter);
        }

        // 4) Load initial data
        loadTasksForChild();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh in case state changed
        loadTasksForChild();
    }

    // ============================================================
    // Data Loading
    // ============================================================
    private void loadTasksForChild() {
        ioExecutor.execute(() -> {
            List<TaskEntity> rows = taskDao.getTasksForChild(currentChildId);

            List<UiTaskChild> fresh = new ArrayList<>();
            for (TaskEntity t : rows) {
                List<TaskStepEntity> steps = taskDao.getStepsForTask(t.taskId);
                int total = taskDao.countStepsForTask(t.taskId);
                int done  = taskDao.countDoneStepsForTask(t.taskId);

                UiTaskChild ui = UiTaskChild.from(t, steps, done, total);
                fresh.add(ui);
            }

            runOnUiThread(() -> {
                uiTasks.clear();
                uiTasks.addAll(fresh);
                if (adapter != null) adapter.notifyDataSetChanged();
                if (tvEmptyState != null) {
                    tvEmptyState.setVisibility(uiTasks.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        });
    }

    // ============================================================
    // Step Toggle Handling (core logic)
    // ============================================================
    private final TaskAdapter.StepToggleListener stepToggleListener = (taskPosition, stepPosition, stepId, newChecked) -> {
        if (taskPosition < 0 || taskPosition >= uiTasks.size()) return;
        UiTaskChild uiTask = uiTasks.get(taskPosition);

        ioExecutor.execute(() -> {
            // 1) Toggle step
            taskDao.setStepDone(stepId, newChecked);

            // 2) Recompute counts
            int total = taskDao.countStepsForTask(uiTask.taskId);
            int done  = taskDao.countDoneStepsForTask(uiTask.taskId);

            // 3) Auto-complete rule:
            //    If all steps are done and there is at least one step -> mark task done and award points
            boolean shouldMarkDone = (total > 0 && done == total);
            boolean shouldUnmark   = (!shouldMarkDone); // if any step unchecked, unset completion

            if (shouldMarkDone) {
                taskDao.markTaskDone(uiTask.taskId, true, System.currentTimeMillis(), 10 /* points */);
            } else {
                // If previously complete but a step got unticked -> revert completion
                taskDao.markTaskDone(uiTask.taskId, false, 0L, 0);
            }

            // 4) Refresh list item in memory + full reload for simplicity
            List<TaskStepEntity> newSteps = taskDao.getStepsForTask(uiTask.taskId);
            UiTaskChild updated = UiTaskChild.from(
                    taskDao.getTasksForChild(currentChildId) // not ideal to scan all; we’ll reuse existing entity where possible
                            .stream()
                            .filter(t -> t.taskId == uiTask.taskId)
                            .findFirst()
                            .orElse(uiTask.asTaskEntity()),
                    newSteps,
                    done,
                    total
            );

            runOnUiThread(() -> {
                // Update that one row in memory
                uiTasks.set(taskPosition, updated);
                if (adapter != null) adapter.notifyItemChanged(taskPosition);
                // Optional toast feedback
                if (shouldMarkDone) {
                    Toast.makeText(this, "Great job! Task completed. +10 points 🎉", Toast.LENGTH_SHORT).show();
                }
            });
        });
    };

    // ============================================================
    // UI Models & Adapters
    // ============================================================
    private static class UiTaskChild {
        long taskId;
        String title;
        long dueAtMillis;
        String priority;
        boolean done;
        List<UiStep> steps;
        int totalSteps;
        int doneSteps;

        static UiTaskChild from(TaskEntity t, List<TaskStepEntity> stepRows, int doneSteps, int totalSteps) {
            UiTaskChild ui = new UiTaskChild();
            ui.taskId = t.taskId;
            ui.title = t.title;
            ui.dueAtMillis = t.dueAtMillis;
            ui.priority = t.priority;
            ui.done = t.done;
            ui.totalSteps = totalSteps;
            ui.doneSteps = doneSteps;

            ui.steps = new ArrayList<>();
            if (stepRows != null) {
                for (TaskStepEntity s : stepRows) {
                    UiStep us = new UiStep();
                    us.stepId = s.stepId;
                    us.text = s.stepText;
                    us.isDone = s.isDone;
                    ui.steps.add(us);
                }
            }
            return ui;
        }

        // Fallback in case we need a TaskEntity instance
        TaskEntity asTaskEntity() {
            TaskEntity t = new TaskEntity();
            t.taskId = taskId;
            t.title = title;
            t.dueAtMillis = dueAtMillis;
            t.priority = priority;
            t.done = done;
            return t;
        }

        // inside UiTaskChild
        String metaText(Context ctx) {
            java.text.DateFormat dfDate =
                    java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault());
            java.text.DateFormat dfTime =
                    java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, Locale.getDefault());

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dueAtMillis);

            return "Due: " + dfDate.format(c.getTime())
                    + " • " + dfTime.format(c.getTime())
                    + "   |   Priority: " + priority;
        }


        int progressPercent() {
            if (done) return 100;
            if (totalSteps > 0) {
                return Math.round((doneSteps * 100f) / totalSteps);
            }
            return 0;
        }

        String progressLabel() {
            return done ? "Completed" : (doneSteps + " / " + totalSteps + " steps");
        }
    }

    private static class UiStep {
        long stepId;
        String text;
        boolean isDone;
    }

    private static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

        interface StepToggleListener {
            void onStepToggled(int taskPosition, int stepPosition, long stepId, boolean newChecked);
        }

        private final List<UiTaskChild> data;
        @Nullable private final StepToggleListener listener;

        TaskAdapter(List<UiTaskChild> data, @Nullable StepToggleListener listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_child, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            UiTaskChild t = data.get(position);

            h.tvTitle.setText(t.title);
            h.tvMeta.setText(t.metaText(h.itemView.getContext()));


            h.progressSteps.setProgress(t.progressPercent());
            h.tvProgressLabel.setText(t.progressLabel());

            // Steps Recycler
            if (h.recyclerSteps.getAdapter() == null) {
                h.recyclerSteps.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
                h.recyclerSteps.setAdapter(new StepsAdapter(position, t.steps, listener));
            } else {
                ((StepsAdapter) h.recyclerSteps.getAdapter()).bind(position, t.steps);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMeta, tvProgressLabel;
            ProgressBar progressSteps;
            RecyclerView recyclerSteps;
            CardView card;

            VH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvMeta = itemView.findViewById(R.id.tvMeta);
                tvProgressLabel = itemView.findViewById(R.id.tvProgressLabel);
                progressSteps = itemView.findViewById(R.id.progressSteps);
                recyclerSteps = itemView.findViewById(R.id.recyclerSteps);
                card = (CardView) itemView;
            }
        }
    }

    private static class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.VH> {
        private int parentTaskPosition;
        private List<UiStep> steps;
        @Nullable private final TaskAdapter.StepToggleListener listener;

        StepsAdapter(int parentTaskPosition, List<UiStep> steps, @Nullable TaskAdapter.StepToggleListener listener) {
            this.parentTaskPosition = parentTaskPosition;
            this.steps = steps != null ? steps : new ArrayList<>();
            this.listener = listener;
        }

        void bind(int parentTaskPosition, List<UiStep> steps) {
            this.parentTaskPosition = parentTaskPosition;
            this.steps = steps != null ? steps : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            UiStep s = steps.get(position);

            h.cbStep.setOnCheckedChangeListener(null);
            h.cbStep.setChecked(s.isDone);
            h.tvStepText.setText(s.text);

            h.cbStep.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onStepToggled(parentTaskPosition, position, s.stepId, isChecked);
                }
            });
        }

        @Override
        public int getItemCount() {
            return steps.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            CheckBox cbStep;
            TextView tvStepText;

            VH(@NonNull View itemView) {
                super(itemView);
                cbStep = itemView.findViewById(R.id.cbStep);
                tvStepText = itemView.findViewById(R.id.tvStepText);
            }
        }
    }
}
