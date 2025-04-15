package com.example.examapp.activities;

import static com.example.examapp.database.DbQuery.HIGHTLIGHTED;
import static com.example.examapp.database.DbQuery.NOT_VISITED;
import static com.example.examapp.database.DbQuery.g_questionList;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.examapp.R;
import com.example.examapp.adapter.QuestionAdapter;
import com.example.examapp.adapter.QuestionGridAdapter;
import com.example.examapp.database.DbQuery;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity {
    private QuestionAdapter adapter;
    private int questionId;
    private DrawerLayout drawerLayout;
    private ConstraintLayout constraintList;
    private RecyclerView rcvQuestion;
    private TextView txtQuestId, txtCatName, txtTime;
    private Button btnClear, btnMark, btnSubmit;
    private ImageButton btnPre, btnNext, btnCloseList;
    private ImageView btnListQuestion, btnMarkImage, btnBookMark;
    private QuestionGridAdapter questionGridAdapter;
    private CountDownTimer timer;
    private long timeLeft;


    private GridView gvQuestionList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_list_layout);

        init();

        questionGridAdapter = new QuestionGridAdapter(this,DbQuery.g_questionList.size());
        gvQuestionList.setAdapter(questionGridAdapter);

        questionGridAdapter.notifyDataSetChanged();
        adapter = new QuestionAdapter(DbQuery.g_questionList);
        rcvQuestion.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rcvQuestion.setLayoutManager(layoutManager);
        rcvQuestion.setItemViewCacheSize(DbQuery.g_questionList.size());



        setSnapHelper();
        setClickListeners();
        startTimer();
    }

    private void init(){
        drawerLayout = findViewById(R.id.drawer_layout12);
        constraintList = findViewById(R.id.contrainList);
        btnCloseList = findViewById(R.id.btnCloseList);
        rcvQuestion = findViewById(R.id.rcvQuestion);
        txtQuestId = findViewById(R.id.txtQuestId);
        txtCatName = findViewById(R.id.txtCatName);
        txtTime = findViewById(R.id.txtTime);
        btnPre = findViewById(R.id.btnPre);
        btnNext = findViewById(R.id.btnNext);
        btnClear = findViewById(R.id.btnClear);
        btnListQuestion = findViewById(R.id.btnListQuestion);
        gvQuestionList = findViewById(R.id.gvQuestionList);
        btnMarkImage = findViewById(R.id.imgMarked);
        btnMark = findViewById(R.id.btnMark);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBookMark = findViewById(R.id.btnBookMark);

        questionId = 0;
        txtQuestId.setText((questionId + 1) + "/" + DbQuery.g_questionList.size());
        txtCatName.setText(DbQuery.g_categoryList.get(DbQuery.g_selectedCatIndex).getName());

        DbQuery.g_questionList.get(questionId).setStatus(DbQuery.UNANSWERED);

        if(g_questionList.get(0).isBookMarked()){
            btnBookMark.setImageResource(R.drawable.ic_bookmark);
        }else{
            btnBookMark.setImageResource(R.drawable.ic_unbookmrked);
        }



    }

    private void startTimer() {
        long totalTime = DbQuery.g_testList.get(DbQuery.g_selected_test_index).getTime() * 60 * 1000;
        timer = new CountDownTimer(totalTime + 1000, 1000) {
            @Override
            public void onTick(long remainTime) {
                timeLeft = remainTime;
                String time = String.format("%02d:%02d min",
                        TimeUnit.MILLISECONDS.toMinutes(remainTime),
                        TimeUnit.MILLISECONDS.toSeconds(remainTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainTime)));
                txtTime.setText(time);
            }
            @Override
            public void onFinish() {

                Intent intent = new Intent(QuestionActivity.this, ScoreActivity.class);
                long totalTime = DbQuery.g_testList.get(DbQuery.g_selected_test_index).getTime() * 60 * 1000;
                intent.putExtra("TIME_TAKEN", totalTime - timeLeft);
                startActivity(intent);
                QuestionActivity.this.finish();

            }
        };
        timer.start();
    }

    private void setClickListeners() {

        btnSubmit.setOnClickListener(view -> {
            submitTest();
        });

        btnPre.setOnClickListener(view -> {
            if (questionId > 0) {
                questionId--;
                rcvQuestion.smoothScrollToPosition(questionId);
            }
        });
        btnNext.setOnClickListener(view -> {
            if (questionId == DbQuery.g_questionList.size() - 1) {
                submitTest();
            } else {
                questionId++;
                rcvQuestion.smoothScrollToPosition(questionId);
            }
        });
        btnClear.setOnClickListener(view -> {
            DbQuery.g_questionList.get(questionId).setSelectedOption(-1);
            DbQuery.g_questionList.get(questionId).setStatus(DbQuery.UNANSWERED);
            adapter.notifyItemChanged(questionId);
        });
        btnListQuestion.setOnClickListener(view -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        btnListQuestion.setOnClickListener(view -> {
            if(!drawerLayout.isDrawerOpen(GravityCompat.END)){
                questionGridAdapter.notifyDataSetChanged();
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
        btnCloseList.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        btnMark.setOnClickListener(view -> {
            if(btnMarkImage.getVisibility() != View.VISIBLE){
                btnMarkImage.setVisibility(View.VISIBLE);
                DbQuery.g_questionList.get(questionId).setStatus(DbQuery.HIGHTLIGHTED);
            }else{
                btnMarkImage.setVisibility(View.GONE);
                if(DbQuery.g_questionList.get(questionId).getSelectedOption() != -1){
                    DbQuery.g_questionList.get(questionId).setStatus(DbQuery.ANSWERED);
                }else{
                    DbQuery.g_questionList.get(questionId).setStatus(DbQuery.UNANSWERED);
                }
            }
        });

        btnBookMark.setOnClickListener(view -> {
            addToBookMark();
        });
    }

    private void addToBookMark() {
        if(g_questionList.get(questionId).isBookMarked()){
            g_questionList.get(questionId).setBookMarked(false);
            btnBookMark.setImageResource(R.drawable.ic_unbookmrked);

        }else{
            g_questionList.get(questionId).setBookMarked(true);
            btnBookMark.setImageResource(R.drawable.ic_bookmark);

        }

    }

    private void submitTest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(QuestionActivity.this);
        builder.setCancelable(true);
        View view = getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        btnCancel.setOnClickListener(view1 -> {
            dialog.dismiss();
        });
        btnConfirm.setOnClickListener(view1 -> {
            timer.cancel();
            dialog.dismiss();

            Intent intent = new Intent(QuestionActivity.this, ScoreActivity.class);
            long totalTime = DbQuery.g_testList.get(DbQuery.g_selected_test_index).getTime() * 60 * 1000;
            intent.putExtra("TIME_TAKEN", totalTime - timeLeft);
            startActivity(intent);
            QuestionActivity.this.finish();

        });
        dialog.show();
    }

    public void goToQuestion(int position){
        rcvQuestion.smoothScrollToPosition(position);
        if (drawerLayout.isDrawerOpen(GravityCompat.END)){
            drawerLayout.closeDrawer(GravityCompat.END) ;
        }
    }
    private void setSnapHelper() {
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rcvQuestion);

        rcvQuestion.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                View view = snapHelper.findSnapView(rcvQuestion.getLayoutManager());
                questionId = rcvQuestion.getLayoutManager().getPosition(view);
                if(DbQuery.g_questionList.get(questionId).getStatus() == NOT_VISITED){
                    DbQuery.g_questionList.get(questionId).setStatus(DbQuery.UNANSWERED);
                }
                if(DbQuery.g_questionList.get(questionId).getStatus() == HIGHTLIGHTED) {
                    if(btnMarkImage.getVisibility() != View.VISIBLE){
                        btnMarkImage.setVisibility(View.VISIBLE);
                    }
                }else{
                    btnMarkImage.setVisibility(View.GONE);
                }

                txtQuestId.setText((questionId + 1) + "/" + DbQuery.g_questionList.size());

                if(g_questionList.get(questionId).isBookMarked()){
                    btnBookMark.setImageResource(R.drawable.ic_bookmark);
                }else{
                    btnBookMark.setImageResource(R.drawable.ic_unbookmrked);
                }
            }
        });
    }
}
