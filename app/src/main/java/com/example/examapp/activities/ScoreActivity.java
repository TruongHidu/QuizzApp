package com.example.examapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.examapp.R;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityScoreBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.QuestionModel;
import com.google.firebase.firestore.WriteBatch;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class ScoreActivity extends AppCompatActivity {
    ActivityScoreBinding binding;
    private long timeTaken;
    private Dialog progressDialog;
    private TextView dialogText;
    private int finalScore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityScoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        timeTaken = getIntent().getLongExtra("TIME_TAKEN", 0);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Result");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        progressDialog = new Dialog(ScoreActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Loading ...");
        progressDialog.show();

        loadData();

        setBookMarks();

        setClickListeners();
        saveResult();


    }

    private void setBookMarks() {
        for(int i = 0; i < DbQuery.g_questionList.size(); i++){
            QuestionModel questionModel = DbQuery.g_questionList.get(i);

            if(questionModel.isBookMarked()){
                if(! DbQuery.g_bmIdList.contains(questionModel.getQuestionId())){
                    DbQuery.g_bmIdList.add(questionModel.getQuestionId());
                    DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());

                }

            }else{
                if(DbQuery.g_bmIdList.contains(questionModel.getQuestionId())){
                    DbQuery.g_bmIdList.remove(questionModel.getQuestionId());
                    DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());

                }
            }
        }

    }

    private void saveResult() {
        DbQuery.saveResult(finalScore, new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();

            }
            @Override
            public void onFailture() {

                Toast.makeText(ScoreActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }

    private void setClickListeners() {
        binding.btnReAttempt.setOnClickListener(view -> {
            reAttemptTest();
        });

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
        for(int i = 0; i< DbQuery.g_questionList.size(); i++){
            DbQuery.g_questionList.get(i).setSelectedOption(-1);
            DbQuery.g_questionList.get(i).setStatus(DbQuery.NOT_VISITED);
        }
        Intent intent = new Intent(ScoreActivity.this, StartTestActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadData(){
        progressDialog.dismiss();
        int correctQ = 0;
        int wrongQ = 0;
        int unAttemptQ = 0;

        for(int i = 0; i < DbQuery.g_questionList.size(); i++){
            if(DbQuery.g_questionList.get(i).getSelectedOption() == -1){
                unAttemptQ++;
            }else{
                if(DbQuery.g_questionList.get(i).getSelectedOption() == DbQuery.g_questionList.get(i).getCorrectOption()){
                    correctQ++;
                }else{
                    wrongQ++;
                }
            }
        }
        binding.txtCorrectQuestion.setText(String.valueOf(correctQ));
        binding.txtWrongQuestion.setText(String.valueOf(wrongQ));
        binding.txtUnAttemptQuestion.setText(String.valueOf(unAttemptQ));
        binding.txtTotalQuestions.setText(DbQuery.g_questionList.size() + "");

        finalScore = (correctQ * 100) / DbQuery.g_questionList.size();
        binding.txtTotalScore.setText(String.valueOf(finalScore));

        String time = String.format("%02d:%02d min",
                TimeUnit.MILLISECONDS.toMinutes(timeTaken),
                TimeUnit.MILLISECONDS.toSeconds(timeTaken) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeTaken)));

        binding.txtTimeTaken.setText(time);


    }
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Đóng activity hiện tại để quay lại activity trước
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == android.R.id.home){
            ScoreActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}