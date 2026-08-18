package com.example.examapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.examapp.R;
import com.example.examapp.databinding.ActivityScoreBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.TestModel;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.TestViewModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScoreActivity extends AppCompatActivity {
    private ActivityScoreBinding binding;
    private TestViewModel viewModel;
    private ProgressDialogUtil progressDialogUtil;
    private long timeTaken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityScoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.show("Loading...");

        viewModel = new ViewModelProvider(this).get(TestViewModel.class);
        timeTaken = getIntent().getLongExtra("TIME_TAKEN", 0);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Result");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadData();
        setClickListeners();
    }

    private void loadData() {
        viewModel.calculateAndSaveScore(timeTaken,
                scoreResult -> {
                    binding.txtCorrectQuestion.setText(String.valueOf(scoreResult.correctQuestions));
                    binding.txtWrongQuestion.setText(String.valueOf(scoreResult.wrongQuestions));
                    binding.txtUnAttemptQuestion.setText(String.valueOf(scoreResult.unAttemptedQuestions));
                    binding.txtTotalQuestions.setText(String.valueOf(scoreResult.totalQuestions));
                    binding.txtTotalScore.setText(String.valueOf(scoreResult.finalScore));
                    List<TestModel> testList = viewModel.getCurrentTestList();
                    int selectedTestIndex = viewModel.getSelectedTestIndex();
                    binding.txtTestNum.setText(testList.get(selectedTestIndex).getTestId());

                    String time = String.format("%02d:%02d min",
                            TimeUnit.MILLISECONDS.toMinutes(timeTaken),
                            TimeUnit.MILLISECONDS.toSeconds(timeTaken) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeTaken)));
                    binding.txtTimeTaken.setText(time);
                },
                new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        progressDialogUtil.dismiss();
                    }

                    @Override
                    public void onFailture() {
                        progressDialogUtil.dismiss();
                        Toast.makeText(ScoreActivity.this, "Failed to save score", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setClickListeners() {
        binding.btnReAttempt.setOnClickListener(view -> reAttemptTest());
        binding.btnViewAnswer.setOnClickListener(view -> {
            Intent intent = new Intent(ScoreActivity.this, AnswerActivity.class);
            startActivity(intent);
        });
        binding.btnLeaderboard.setOnClickListener(view -> {
            Intent intent = new Intent(ScoreActivity.this, MainActivity.class);
            intent.putExtra("FRAGMENT_TO_LOAD", "LEADERBOARD");
            startActivity(intent);
            finish();
        });
    }

    private void reAttemptTest() {
        viewModel.resetQuestionsForReAttempt();
        Intent intent = new Intent(ScoreActivity.this, StartTestActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}