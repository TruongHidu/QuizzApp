package com.example.examapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.examapp.R;
import com.example.examapp.databinding.ActivityStartTestBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.CategoryModel;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.model.TestModel;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.TestViewModel;

import java.util.List;

public class StartTestActivity extends AppCompatActivity {
    private ActivityStartTestBinding binding;
    private TestViewModel viewModel;
    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize progress dialog
        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.show("Loading ...");

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(TestViewModel.class);

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            progressDialogUtil.dismiss();
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize UI
        init();
        loadQuestions();
    }

    private void init() {
        binding.btnback.setOnClickListener(view -> finish());

        binding.btnStart.setOnClickListener(view -> {
            progressDialogUtil.dismiss();
            Intent intent = new Intent(StartTestActivity.this, QuestionActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadQuestions() {
        viewModel.loadQuestions(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                if (viewModel.getCurrentQuestionList().isEmpty()) {
                    Toast.makeText(StartTestActivity.this, "No questions available", Toast.LENGTH_SHORT).show();
                }
                setData();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onFailture() {
                progressDialogUtil.dismiss();
                // Error message handled by ViewModel
            }
        });
    }

    private void setData() {
        List<CategoryModel> categoryList = viewModel.getCurrentCategoryList();
        if (categoryList == null || categoryList.isEmpty()) {
            binding.txtCatName.setText("Unknown Category");
        } else {
            binding.txtCatName.setText(categoryList.get(viewModel.getSelectedCategoryIndex()).getName());
        }

        List<TestModel> testList = viewModel.getCurrentTestList();
        int selectedTestIndex = viewModel.getSelectedTestIndex();
        if (testList != null && selectedTestIndex < testList.size()) {
            binding.txtTestNO.setText("Test " + testList.get(selectedTestIndex).getTestId());
            binding.txtBestScore.setText(String.valueOf(testList.get(selectedTestIndex).getTopScore()));
            binding.txtTotaltime.setText(testList.get(selectedTestIndex).getTime() + " m");
        } else {
            binding.txtTestNO.setText("Unknown Test");
            binding.txtBestScore.setText("N/A");
            binding.txtTotaltime.setText("N/A");
        }

        List<QuestionModel> questionList = viewModel.getCurrentQuestionList();
        binding.txtTotalQuestion.setText(questionList != null ? String.valueOf(questionList.size()) : "0");
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressDialogUtil.dismiss();
    }
}