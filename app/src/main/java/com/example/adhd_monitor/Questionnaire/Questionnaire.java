package com.example.adhd_monitor.Questionnaire;

import java.util.ArrayList;
import java.util.List;

public class Questionnaire {

    public static List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();

        // Attention
        questions.add(new Question("Do you often find yourself easily distracted by unrelated stimuli when performing tasks?", ADHDCategory.ATTENTION, 2));
        questions.add(new Question("Do you have difficulty sustaining attention in tasks or activities, even ones that you find interesting?", ADHDCategory.ATTENTION, 2));
        questions.add(new Question("Do you make careless mistakes in schoolwork or other activities because of lack of attention to detail?", ADHDCategory.ATTENTION, 2));
        questions.add(new Question("Do you find it challenging to follow instructions, especially those that require multiple steps?", ADHDCategory.ATTENTION, 2));
        questions.add(new Question("Do you often forget appointments or obligations, even when you try to keep track of them?", ADHDCategory.ATTENTION, 2));


        // Hyperactivity
        questions.add(new Question("Do you feel restless or fidgety, even when you're expected to remain still?", ADHDCategory.HYPERACTIVITY, 2));
        questions.add(new Question("Do you find it hard to relax or calm down, even in situations where others are relaxed?", ADHDCategory.HYPERACTIVITY, 2));
        questions.add(new Question("Do you often talk excessively or interrupt others when they are speaking?", ADHDCategory.HYPERACTIVITY, 2));
        questions.add(new Question("Do you frequently find yourself moving around or getting up when you’re not supposed to?", ADHDCategory.HYPERACTIVITY, 2));


        // Impulsivity
        questions.add(new Question("Do you frequently interrupt conversations or activities, even when it's inappropriate?", ADHDCategory.IMPULSIVITY, 2));
        questions.add(new Question("Do you often make decisions without thinking about the consequences?", ADHDCategory.IMPULSIVITY, 2));

        // Emotional Regulation
        questions.add(new Question("Do you find yourself feeling overly emotional, such as getting frustrated or upset more easily than others?", ADHDCategory.EMOTIONAL_REGULATION, 2));
        questions.add(new Question("Do you struggle with maintaining consistent motivation, particularly when working on long-term projects?", ADHDCategory.EMOTIONAL_REGULATION, 2));
        questions.add(new Question("Do you experience mood swings, ranging from irritability to extreme enthusiasm, in a short period of time?", ADHDCategory.EMOTIONAL_REGULATION, 2));
        questions.add(new Question("Do you have difficulty handling stressful situations without becoming overwhelmed?", ADHDCategory.EMOTIONAL_REGULATION, 2));

        // Social Behavior
        questions.add(new Question("Do you find it difficult to follow through with social commitments or maintain consistent relationships due to your attention span?", ADHDCategory.SOCIAL_BEHAVIOR, 2));
        questions.add(new Question("Do you feel out of place or misunderstood in social settings because of your behavior or responses?", ADHDCategory.SOCIAL_BEHAVIOR, 2));

        return questions;
    }
}
