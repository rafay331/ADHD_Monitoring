package com.example.adhd_monitor.Questionnaire;

import java.io.Serializable;

public class Question implements Serializable {
    private String text;
    private ADHDCategory category;
    private Boolean answer; // null = unanswered
    private int score;

    // ✅ Constructor with default score = 1
    public Question(String text, ADHDCategory category) {
        this.text = text;
        this.category = category;
        this.answer = null;
        this.score = 1;
    }

    // ✅ Constructor with custom score
    public Question(String text, ADHDCategory category, int score) {
        this.text = text;
        this.category = category;
        this.answer = null;
        this.score = score;
    }

    // ✅ Getters and Setters
    public String getText() {
        return text;
    }

    public ADHDCategory getCategory() {
        return category;
    }

    public Boolean getAnswer() {
        return answer;
    }

    public void setAnswer(Boolean answer) {
        this.answer = answer;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
