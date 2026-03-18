package com.example.adhd_monitor;

public class ExerciseItem {
    private final String title;
    private final String duration;

    public ExerciseItem(String title, String duration) {
        this.title = title;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }
}
