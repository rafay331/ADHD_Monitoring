package com.example.adhd_monitor.Questionnaire;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.adhd_monitor.BaseActivity;
import com.example.adhd_monitor.R;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionnaireActivity extends BaseActivity {

    private LinearLayout questionnaireLayout;
    private Button btnSubmit;
    private List<Question> questions;
    private int totalScore = 0;

    // Store references to each RadioGroup
    private final Map<Question, RadioGroup> questionRadioMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        questionnaireLayout = findViewById(R.id.questionnaireLayout);
        btnSubmit = findViewById(R.id.btnSubmit);

        questions = Questionnaire.getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            final Question question = questions.get(i);

            TextView questionText = new TextView(this);
            questionText.setText((i + 1) + ". " + question.getText());
            questionText.setTextSize(16);
            questionText.setPadding(0, 16, 0, 8);
            questionnaireLayout.addView(questionText);

            RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.HORIZONTAL);

            RadioButton radioYes = new RadioButton(this);
            radioYes.setText("Yes");
            radioYes.setId(View.generateViewId());

            RadioButton radioNo = new RadioButton(this);
            radioNo.setText("No");
            radioNo.setId(View.generateViewId());

            radioGroup.addView(radioYes);
            radioGroup.addView(radioNo);
            questionnaireLayout.addView(radioGroup);

            // Map question to its RadioGroup
            questionRadioMap.put(question, radioGroup);
        }

        btnSubmit.setOnClickListener(v -> {
            totalScore = 0;

            for (Question question : questions) {
                RadioGroup group = questionRadioMap.get(question);
                if (group != null) {
                    int selectedId = group.getCheckedRadioButtonId();
                    if (selectedId != -1) {
                        RadioButton selected = group.findViewById(selectedId);
                        String answer = selected.getText().toString();

                        if ("Yes".equalsIgnoreCase(answer)) {
                            totalScore += question.getScore();
                            question.setAnswer(true);
                        } else {
                            question.setAnswer(false);
                        }
                    }
                }
            }

            String userId = getIntent().getStringExtra("userId");
            Intent intent = new Intent(QuestionnaireActivity.this, ResultActivity.class);
            intent.putExtra("totalScore", totalScore);
            intent.putExtra("userId", userId);
            intent.putExtra("answeredQuestions", (Serializable) questions); // ✅ send answered list
            startActivity(intent);
        });

    }
}
