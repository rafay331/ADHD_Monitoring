package com.example.adhd_monitor;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "mood_journal",
        indices = {@Index(value = {"userId"}, name = "index_mood_journal_userId")}
)
public class MoodJournalEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String mood;
    public int intensity;
    public String note;
    public long createdAt;

    public MoodJournalEntity(int userId, String mood, int intensity, String note, long createdAt) {
        this.userId = userId;
        this.mood = mood;
        this.intensity = intensity;
        this.note = note;
        this.createdAt = createdAt;
    }
}
