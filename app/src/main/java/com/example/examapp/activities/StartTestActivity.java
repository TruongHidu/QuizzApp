package com.example.examapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.examapp.R;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityStartTestBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;

public class StartTestActivity extends AppCompatActivity {
    ActivityStartTestBinding binding;
    Dialog progressDialog;
    TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityStartTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new Dialog(StartTestActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Loading ...");

        progressDialog.show();

        init();
        loadQuestion(new MyCompleteListener(){
            @Override
            public void onSuccess() {
                setData();
                progressDialog.dismiss();
            }
            @Override
            public void onFailture() {
                progressDialog.dismiss();
                Toast.makeText(StartTestActivity.this, "Something went wrong! Please try again later.",
                        Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void setData() {
        if (DbQuery.g_categoryList == null || DbQuery.g_categoryList.isEmpty()) {
            binding.txtCatName.setText("Unknown Category");
        } else {
            binding.txtCatName.setText(DbQuery.g_categoryList.get(DbQuery.g_selectedCatIndex).getName());
        }

        binding.txtTestNO.setText("Test " + (DbQuery.g_selected_test_index + 1));

        if (DbQuery.g_testList != null && DbQuery.g_selected_test_index < DbQuery.g_testList.size()) {
            binding.txtBestScore.setText(String.valueOf(DbQuery.g_testList.get(DbQuery.g_selected_test_index).getTopScore()));
            binding.txtTotaltime.setText(DbQuery.g_testList.get(DbQuery.g_selected_test_index).getTime() + " m");
        } else {
            binding.txtBestScore.setText("N/A");
            binding.txtTotaltime.setText("N/A");
        }

        binding.txtTotalQuestion.setText(DbQuery.g_questionList != null ? String.valueOf(DbQuery.g_questionList.size()) : "0");
    }


    private void loadQuestion(MyCompleteListener listener) {
        DbQuery.loadQuestions(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                if (DbQuery.g_questionList == null || DbQuery.g_questionList.isEmpty()) {
                    Toast.makeText(StartTestActivity.this, "No questions available", Toast.LENGTH_SHORT).show();
                }
                listener.onSuccess();
            }

            @Override
            public void onFailture() {
                listener.onFailture();
                Toast.makeText(StartTestActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void init(){
        binding.btnback.setOnClickListener(view -> {
            StartTestActivity.this.finish();
        });

        binding.btnStart.setOnClickListener(view -> {
            Intent intent = new Intent(StartTestActivity.this, QuestionActivity.class);
            startActivity(intent);
            finish();
        });
    }

}