package com.example.adhd_monitor;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TaskManagementActivity
 *
 * Parent-facing screen:
 *  - Create task for a child (title, deadline, priority, reminder, steps...)
 *  - View all tasks you've assigned to that child
 *  - See progress summary
 */
public class TaskManagementActivity extends AppCompatActivity {

    // --------------------------
    // Fake logged-in context (replace with real IDs)
    // --------------------------
    private final long currentParentId = 1L;
    private final long currentChildIdForAssignment = 2L;

    // --------------------------
    // DB / DAO / threading
    // --------------------------
    private AppDatabase db;
    private TaskDao taskDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    // --------------------------
    // Top filter
    // --------------------------
    @Nullable private Spinner categorySelector;

    // --------------------------
    // "Create Task" card
    // --------------------------
    @Nullable private EditText etTaskTitle;
    @Nullable private EditText etStep;
    @Nullable private DatePicker datePicker;
    @Nullable private TimePicker timePicker;
    @Nullable private Spinner prioritySpinner;
    @Nullable private Spinner reminderLeadSpinner;
    @Nullable private Switch  reminderSwitch;
    @Nullable private LinearLayout stepsContainer;
    @Nullable private Button btnAddStep;
    @Nullable private Button btnSaveTask;

    // --------------------------
    // Summary card
    // --------------------------
    @Nullable private ProgressBar overallProgress;
    @Nullable private TextView tvCompleted;
    @Nullable private TextView tvPending;
    @Nullable private TextView tvOverdue;
    @Nullable private TextView tvRewardPoints;

    // --------------------------
    // Task list
    // --------------------------
    @Nullable private RecyclerView recyclerTasks;
    @Nullable private TextView tvEmptyStateParent;
    @Nullable private Button btnClearCompleted;
    @Nullable private Button btnAddQuickTask;

    // --------------------------
    // Adapter data
    // --------------------------
    private final List<UiTask> uiTasks = new ArrayList<>();
    @Nullable private TaskAdapter adapter;

    // temp buffer for step chips before saving task
    private final List<String> draftSteps = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_management);

        // 1) init Room
        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "adhd_monitor_db"
        ).build();
        taskDao = db.taskDao();

        // 2) normalize any legacy second-based timestamps -> millis (run once)
        normalizeOldTimestamps();

        // 3) bind views
        bindViews();

        // 4) setup UI
        setupSpinners();
        setupRecycler();
        setupCreateTaskActions();
        setupFooterActions();

        // 5) initial load
        loadTasksFromDbAndRefresh();
        refreshSummaryFromDb();
    }

    /* ---------------------- Normalization ---------------------- */
    private void normalizeOldTimestamps() {
        ioExecutor.execute(() -> {
            try {
                taskDao.normalizeCompletedAtToMillis();
            } catch (Throwable ignored) { }
            try {
                taskDao.normalizeDueAtToMillis();
            } catch (Throwable ignored) { }
        });
    }

    /* ---------------------- Bind views ---------------------- */
    private void bindViews() {
        categorySelector    = safeFindSpinner(R.id.categorySelector);

        etTaskTitle         = safeFindEditText(R.id.etTaskTitle);
        etStep              = safeFindEditText(R.id.etStep);
        datePicker          = safeFindDatePicker(R.id.datePicker);
        timePicker          = safeFindTimePicker(R.id.timePicker);
        prioritySpinner     = safeFindSpinner(R.id.prioritySpinner);
        reminderLeadSpinner = safeFindSpinner(R.id.reminderLeadSpinner);
        reminderSwitch      = safeFindSwitch(R.id.reminderSwitch);
        stepsContainer      = safeFindLinearLayout(R.id.stepsContainer);
        btnAddStep          = safeFindButton(R.id.btnAddStep);
        btnSaveTask         = safeFindButton(R.id.btnSaveTask);

        overallProgress     = safeFindProgressBar(R.id.overallProgress);
        tvCompleted         = safeFindTextView(R.id.tvCompleted);
        tvPending           = safeFindTextView(R.id.tvPending);
        tvOverdue           = safeFindTextView(R.id.tvOverdue);
        tvRewardPoints      = safeFindTextView(R.id.tvRewardPoints);

        recyclerTasks       = safeFindRecyclerView(R.id.recyclerTasks);
        tvEmptyStateParent  = safeFindTextView(R.id.tvEmptyStateParent);
        btnClearCompleted   = safeFindButton(R.id.btnClearCompleted);
        btnAddQuickTask     = safeFindButton(R.id.btnAddQuickTask);
    }

    /* ---------------------- Spinners ---------------------- */
    private void setupSpinners() {
        if (categorySelector != null) {
            try {
                ArrayAdapter<CharSequence> catAdapter =
                        ArrayAdapter.createFromResource(
                                this, R.array.task_categories, android.R.layout.simple_spinner_item);
                catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySelector.setAdapter(catAdapter);
            } catch (Exception ignored) { }
        }

        if (prioritySpinner != null) {
            int prioritiesArray = R.array.notification_priority_options;
            try { getResources().getStringArray(R.array.task_priorities); prioritiesArray = R.array.task_priorities; }
            catch (Exception ignored) { }
            try {
                ArrayAdapter<CharSequence> prioAdapter =
                        ArrayAdapter.createFromResource(
                                this, prioritiesArray, android.R.layout.simple_spinner_item);
                prioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                prioritySpinner.setAdapter(prioAdapter);
            } catch (Exception ignored) { }
        }

        if (reminderLeadSpinner != null) {
            try {
                ArrayAdapter<CharSequence> leadAdapter =
                        ArrayAdapter.createFromResource(
                                this, R.array.reminder_lead_times, android.R.layout.simple_spinner_item);
                leadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                reminderLeadSpinner.setAdapter(leadAdapter);
            } catch (Exception ignored) { }
        }
    }

    /* ---------------------- Recycler ---------------------- */
    private void setupRecycler() {
        if (recyclerTasks == null) return;

        adapter = new TaskAdapter(uiTasks, (position, checked) -> {
            UiTask tapped = uiTasks.get(position);
            markTaskDoneInDb(tapped, checked);
        });

        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setAdapter(adapter);
    }

    /* ---------------------- Create / Save Task ---------------------- */
    private void setupCreateTaskActions() {
        if (btnAddStep != null) {
            btnAddStep.setOnClickListener(v -> {
                if (etStep == null) return;
                String s = etStep.getText().toString().trim();
                if (s.isEmpty()) {
                    Toast.makeText(this, "Step cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                draftSteps.add(s);
                addStepChipView(s);
                etStep.setText("");
            });
        }

        if (btnSaveTask != null) {
            btnSaveTask.setOnClickListener(v -> {
                String title = etTaskTitle != null ? etTaskTitle.getText().toString().trim() : "";
                if (TextUtils.isEmpty(title)) {
                    if (etTaskTitle != null) {
                        etTaskTitle.setError("Title required");
                        etTaskTitle.requestFocus();
                    } else {
                        Toast.makeText(this, "Task title required", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                long dueAt = getSelectedDateTimeMillis();

                String priority = "Medium";
                if (prioritySpinner != null && prioritySpinner.getSelectedItem() != null) {
                    priority = prioritySpinner.getSelectedItem().toString();
                }

                String category = "General";
                if (categorySelector != null && categorySelector.getSelectedItem() != null) {
                    category = categorySelector.getSelectedItem().toString();
                }

                boolean remindersEnabledVal = reminderSwitch != null && reminderSwitch.isChecked();

                int leadMinutes = 0;
                if (reminderLeadSpinner != null) {
                    try {
                        int[] mins = getResources().getIntArray(R.array.reminder_lead_minutes);
                        int idx = reminderLeadSpinner.getSelectedItemPosition();
                        if (idx >= 0 && idx < mins.length) leadMinutes = mins[idx];
                    } catch (Exception ignored) { }
                }

                final TaskEntity taskEntity = new TaskEntity();
                taskEntity.childId = currentChildIdForAssignment;
                taskEntity.parentId = currentParentId;
                taskEntity.title = title;
                taskEntity.dueAtMillis = dueAt;
                taskEntity.priority = priority;
                taskEntity.category = category;
                taskEntity.remindersEnabled = remindersEnabledVal;
                taskEntity.reminderLeadMinutes = leadMinutes;
                taskEntity.done = false;
                taskEntity.pointsEarned = 0;
                taskEntity.createdAtMillis = System.currentTimeMillis();
                taskEntity.completedAtMillis = 0L;

                ioExecutor.execute(() -> {
                    long newTaskId = taskDao.insertTask(taskEntity);

                    if (!draftSteps.isEmpty()) {
                        List<TaskStepEntity> stepRows = new ArrayList<>();
                        for (String s : draftSteps) {
                            TaskStepEntity st = new TaskStepEntity();
                            st.taskOwnerId = newTaskId;
                            st.stepText = s;
                            st.isDone = false;
                            stepRows.add(st);
                        }
                        taskDao.insertSteps(stepRows);
                    }

                    runOnUiThread(() -> {
                        clearCreateForm();
                        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                    });

                    loadTasksFromDbAndRefresh();
                    refreshSummaryFromDb();
                });
            });
        }
    }

    /* ---------------------- Footer ---------------------- */
    private void setupFooterActions() {
        if (btnClearCompleted != null) {
            btnClearCompleted.setOnClickListener(v ->
                    ioExecutor.execute(() -> {
                        taskDao.deleteCompletedTasksForParent(currentParentId);
                        loadTasksFromDbAndRefresh();
                        refreshSummaryFromDb();
                    })
            );
        }

        if (btnAddQuickTask != null) {
            btnAddQuickTask.setOnClickListener(v -> {
                final TaskEntity quick = new TaskEntity();
                quick.childId = currentChildIdForAssignment;
                quick.parentId = currentParentId;
                quick.title = "Quick task";
                quick.dueAtMillis = System.currentTimeMillis() + 2 * 60 * 60 * 1000L; // +2h
                quick.priority = "Medium";
                quick.category = "All";
                quick.remindersEnabled = false;
                quick.reminderLeadMinutes = 0;
                quick.done = false;
                quick.pointsEarned = 0;
                quick.createdAtMillis = System.currentTimeMillis();
                quick.completedAtMillis = 0L;

                ioExecutor.execute(() -> {
                    taskDao.insertTask(quick);
                    loadTasksFromDbAndRefresh();
                    refreshSummaryFromDb();
                });
            });
        }
    }

    /* ---------------------- Load list ---------------------- */
    private void loadTasksFromDbAndRefresh() {
        ioExecutor.execute(() -> {
            List<TaskEntity> rows = taskDao.getTasksForParentAndChild(
                    currentParentId, currentChildIdForAssignment);

            final List<UiTask> freshUi = new ArrayList<>();
            for (TaskEntity row : rows) {
                List<TaskStepEntity> steps = taskDao.getStepsForTask(row.taskId);
                UiTask ui = UiTask.from(row, steps);
                freshUi.add(ui);
            }

            runOnUiThread(() -> {
                uiTasks.clear();
                uiTasks.addAll(freshUi);
                if (adapter != null) adapter.notifyDataSetChanged();
                if (tvEmptyStateParent != null) {
                    tvEmptyStateParent.setVisibility(uiTasks.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        });
    }

    /* ---------------------- Toggle done ---------------------- */
    private void markTaskDoneInDb(UiTask tapped, boolean checked) {
        ioExecutor.execute(() -> {
            long completedAt = checked ? System.currentTimeMillis() : 0L; // ✅ milliseconds
            int points = checked ? 10 : 0;

            taskDao.markTaskDone(tapped.taskId, checked, completedAt, points);

            loadTasksFromDbAndRefresh();
            refreshSummaryFromDb();
        });
    }

    /* ---------------------- Summary ---------------------- */
    private void refreshSummaryFromDb() {
        ioExecutor.execute(() -> {
            long now = System.currentTimeMillis();

            int completed = taskDao.getCompletedCount(currentChildIdForAssignment);
            int pending   = taskDao.getPendingCount(currentChildIdForAssignment, now);
            int overdue   = taskDao.getOverdueCount(currentChildIdForAssignment, now);
            int points    = taskDao.getTotalPoints(currentChildIdForAssignment);

            int totalTasks = completed + pending + overdue;
            int progressPct = (totalTasks == 0) ? 0
                    : (int) Math.round((completed * 100.0) / totalTasks);

            runOnUiThread(() -> {
                if (tvCompleted != null)  tvCompleted.setText("Completed: " + completed);
                if (tvPending != null)    tvPending.setText("Pending: " + pending);
                if (tvOverdue != null)    tvOverdue.setText("Overdue: " + overdue);
                if (overallProgress != null) overallProgress.setProgress(progressPct);
                if (tvRewardPoints != null) tvRewardPoints.setText("Reward Points: " + points);
            });
        });
    }

    /* ---------------------- Helpers ---------------------- */

    private void addStepChipView(String text) {
        if (stepsContainer == null) return;
        TextView tv = new TextView(this);
        tv.setText("• " + text);
        tv.setTextSize(14f);
        int pad = (int) (6 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad / 2, pad, pad / 2);
        tv.setOnClickListener(v -> {
            stepsContainer.removeView(v);
            draftSteps.remove(text);
        });
        stepsContainer.addView(tv);
    }

    private void clearCreateForm() {
        if (etTaskTitle != null) etTaskTitle.setText("");
        if (etStep != null) etStep.setText("");
        draftSteps.clear();
        if (stepsContainer != null) stepsContainer.removeAllViews();

        Calendar c = Calendar.getInstance();
        if (datePicker != null) {
            datePicker.updateDate(
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
        }
        if (timePicker != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(c.get(Calendar.HOUR_OF_DAY));
                timePicker.setMinute(c.get(Calendar.MINUTE));
            } else {
                timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
            }
        }
        if (prioritySpinner != null) prioritySpinner.setSelection(0);
        if (reminderSwitch != null) reminderSwitch.setChecked(false);
        if (reminderLeadSpinner != null) reminderLeadSpinner.setSelection(0);
    }

    private long getSelectedDateTimeMillis() {
        Calendar c = Calendar.getInstance();
        if (datePicker != null) {
            c.set(Calendar.YEAR, datePicker.getYear());
            c.set(Calendar.MONTH, datePicker.getMonth());
            c.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
        }
        if (timePicker != null) {
            int hour, minute;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = timePicker.getHour(); minute = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour(); minute = timePicker.getCurrentMinute();
            }
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
        }
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /* ---------------------- UI models / adapter ---------------------- */

    private static class UiTask {
        long taskId;
        String title;
        long dueAtMillis;
        String priority;
        boolean done;
        int stepCount;
        int stepDoneCount;

        static UiTask from(TaskEntity entity, List<TaskStepEntity> steps) {
            UiTask u = new UiTask();
            u.taskId = entity.taskId;
            u.title = entity.title;
            u.dueAtMillis = entity.dueAtMillis;
            u.priority = entity.priority;
            u.done = entity.done;
            if (steps != null) {
                u.stepCount = steps.size();
                int doneC = 0;
                for (TaskStepEntity st : steps) if (st.isDone) doneC++;
                u.stepDoneCount = doneC;
            } else {
                u.stepCount = 0; u.stepDoneCount = 0;
            }
            return u;
        }

        String prettyDue() {
            DateFormat dfDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            DateFormat dfTime = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dueAtMillis);
            return "Due: " + dfDate.format(c.getTime()) + " • " + dfTime.format(c.getTime());
        }

        int progressPercent() {
            if (done) return 100;
            if (stepCount > 0) return (int) Math.round((stepDoneCount * 100.0) / stepCount);
            return 0;
        }
    }

    private static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

        interface Listener { void onCheckedChanged(int position, boolean checked); }

        private final List<UiTask> data;
        @Nullable private final Listener listener;

        TaskAdapter(List<UiTask> data, @Nullable Listener listener) {
            this.data = data; this.listener = listener;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            UiTask t = data.get(position);
            h.tvTitle.setText(t.title);
            h.tvDue.setText(t.prettyDue());
            h.chipPriority.setText(t.priority);

            h.cbDone.setOnCheckedChangeListener(null);
            h.cbDone.setChecked(t.done);
            h.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCheckedChanged(pos, isChecked);
                }
            });

            h.progressSteps.setProgress(t.progressPercent());
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            CheckBox cbDone; TextView tvTitle; TextView tvDue; TextView chipPriority; ProgressBar progressSteps;
            VH(@NonNull View itemView) {
                super(itemView);
                cbDone = itemView.findViewById(R.id.cbDone);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDue = itemView.findViewById(R.id.tvDue);
                chipPriority = itemView.findViewById(R.id.chipPriority);
                progressSteps = itemView.findViewById(R.id.progressSteps);
            }
        }
    }

    /* ---------------------- Safe find helpers ---------------------- */

    @Nullable private Spinner safeFindSpinner(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private EditText safeFindEditText(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private DatePicker safeFindDatePicker(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private TimePicker safeFindTimePicker(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private Switch safeFindSwitch(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private LinearLayout safeFindLinearLayout(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private Button safeFindButton(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private ProgressBar safeFindProgressBar(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private TextView safeFindTextView(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
    @Nullable private RecyclerView safeFindRecyclerView(int id) { try { return findViewById(id); } catch (Exception e) { return null; } }
}
