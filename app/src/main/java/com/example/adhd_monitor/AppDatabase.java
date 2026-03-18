package com.example.adhd_monitor;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.adhd_monitor.MedicalHistory.BehavioralHistoryEntity;
import com.example.adhd_monitor.MedicalHistory.MedicalHistoryDAO;
import com.example.adhd_monitor.MedicalHistory.MedicalHistoryPdfEntity;
import com.example.adhd_monitor.MedicalHistory.MedicationHistoryEntity;
import com.example.adhd_monitor.Questionnaire.database.AdhdReportDAO;
import com.example.adhd_monitor.Questionnaire.database.AdhdReportEntity;
import com.example.adhd_monitor.TreatmentReport.TreatmentReportDAO;
import com.example.adhd_monitor.TreatmentReport.TreatmentReportEntity;

@Database(
        entities = {
                User.class,
                Psychologist.class,
                Parent.class,
                Admin.class,
                AdhdReportEntity.class,
                ParentChildLink.class,
                MedicationHistoryEntity.class,
                BehavioralHistoryEntity.class,
                MedicalHistoryPdfEntity.class,
                TreatmentReportEntity.class,
                TaskEntity.class,
                TaskStepEntity.class,
                GoalEntity.class,
                FocusSessionEntity.class,

                // ✅ Budget module
                ExpenseEntity.class,
                MonthlyBudgetLimitEntity.class,

                // ✅ Savings rating
                SavingsRatingEntity.class,

                // ✅ Community Support / Group Chat
                CommunityMessageEntity.class,

                // ✅ Emotional Regulation - Mood Tracking + Journaling
                MoodJournalEntity.class
        },
        version = 11   // ✅ UPDATED (was 10)
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    // -------------------- DAOs --------------------
    public abstract UserDAO userDao();
    public abstract PsychologistDAO psychologistDao();
    public abstract ParentDAO parentDao();
    public abstract AdminDAO adminDao();
    public abstract AdhdReportDAO adhdReportDao();
    public abstract ParentChildLinkDao parentChildLinkDao();
    public abstract MedicalHistoryDAO medicalHistoryDao();
    public abstract TreatmentReportDAO treatmentReportDao();

    public abstract TaskDao taskDao();
    public abstract FocusSessionDao focusSessionDao();
    public abstract GoalDao goalDao();

    // Budget
    public abstract ExpenseDao expenseDao();
    public abstract MonthlyBudgetLimitDao monthlyBudgetLimitDao();
    public abstract SavingsRatingDao savingsRatingDao();

    // Community Chat
    public abstract CommunityMessageDao communityMessageDao();

    // ✅ Emotional Regulation - Mood Tracking + Journaling
    public abstract MoodJournalDao moodJournalDao();

    // -------------------- MIGRATIONS --------------------

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `parent_table` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`username` TEXT NOT NULL, " +
                            "`email` TEXT NOT NULL, " +
                            "`password` TEXT NOT NULL)"
            );
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `adhd_reports` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`user_id` TEXT NOT NULL, " +
                            "`file_path` TEXT NOT NULL, " +
                            "`date` TEXT NOT NULL, " +
                            "`score` INTEGER NOT NULL, " +
                            "`spectrum` TEXT NOT NULL, " +
                            "`comments` TEXT)"
            );
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `medication_history` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` INTEGER NOT NULL, " +
                            "`date` TEXT, " +
                            "`medicineName` TEXT, " +
                            "`dosage` TEXT, " +
                            "`notes` TEXT)"
            );

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `behavioral_history` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` INTEGER NOT NULL, " +
                            "`date` TEXT, " +
                            "`symptoms` TEXT, " +
                            "`notes` TEXT)"
            );
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `expenses` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`category` TEXT, " +
                            "`createdAt` INTEGER NOT NULL)"
            );

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `monthly_budget_limit` (" +
                            "`childUserId` INTEGER NOT NULL, " +
                            "`monthKey` TEXT NOT NULL, " +
                            "`limitAmount` REAL NOT NULL, " +
                            "`lockEnabled` INTEGER NOT NULL, " +
                            "`updatedAt` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`childUserId`, `monthKey`))"
            );
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `savings_rating` (" +
                            "`userId` INTEGER NOT NULL, " +
                            "`monthKey` TEXT NOT NULL, " +
                            "`limitAmount` REAL NOT NULL, " +
                            "`spentAmount` INTEGER NOT NULL, " +
                            "`savedAmount` REAL NOT NULL, " +
                            "`stars` INTEGER NOT NULL, " +
                            "`updatedAt` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`userId`,`monthKey`))"
            );

            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_savings_rating_userId` " +
                            "ON `savings_rating`(`userId`)"
            );
        }
    };

    // ✅ Community Chat
    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `community_messages` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` INTEGER NOT NULL, " +
                            "`username` TEXT NOT NULL, " +
                            "`message` TEXT NOT NULL, " +
                            "`budgetRating` INTEGER NOT NULL, " +
                            "`participationPoints` INTEGER NOT NULL, " +
                            "`createdAt` INTEGER NOT NULL)"
            );

            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_community_messages_userId` " +
                            "ON `community_messages`(`userId`)"
            );
        }
    };

    // ✅ NEW: Encouragement count column for Progress Sharing & Encouragement
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE `community_messages` " +
                            "ADD COLUMN `encouragementCount` INTEGER NOT NULL DEFAULT 0"
            );
        }
    };

    // ✅ NEW: Mood Tracking + Journaling table
    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `mood_journal` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` INTEGER NOT NULL, " +
                            "`mood` TEXT, " +
                            "`intensity` INTEGER NOT NULL, " +
                            "`note` TEXT, " +
                            "`createdAt` INTEGER NOT NULL)"
            );

            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_mood_journal_userId` " +
                            "ON `mood_journal`(`userId`)"
            );
        }
    };

    // ✅ NEW: Add copingPlan column in treatment_reports
    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE `treatment_reports` " +
                            "ADD COLUMN `copingPlan` TEXT"
            );
        }
    };

    // -------------------- INSTANCE --------------------

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "adhd_monitor_db"
                    )
                    .addMigrations(
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7,
                            MIGRATION_7_8,
                            MIGRATION_8_9,
                            MIGRATION_9_10,
                            MIGRATION_10_11 // ✅ ADD THIS
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();

            new Thread(() -> {
                AdminDAO adminDao = instance.adminDao();
                if (adminDao.getAdminByUsername("admin") == null) {
                    adminDao.insert(new Admin("admin", hashPassword("Admin@123")));
                }
            }).start();
        }
        return instance;
    }

    private static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest =
                    java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return password;
        }
    }
}
