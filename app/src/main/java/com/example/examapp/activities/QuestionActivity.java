package com.example.examapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.examapp.R;
import com.example.examapp.adapter.QuestionAdapter;
import com.example.examapp.adapter.QuestionGridAdapter;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.utils.QuestionStatus;
import com.example.examapp.viewmodel.TestViewModel;

import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity {
    private static final String TAG = "QuestionActivity";
    private TestViewModel viewModel;
    private QuestionAdapter adapter;
    private QuestionGridAdapter questionGridAdapter;
    private ProgressDialogUtil progressDialogUtil;
    private int questionId;
    private DrawerLayout drawerLayout;
    private RecyclerView rcvQuestion;
    private TextView txtQuestId, txtCatName, txtTime;
    private Button btnClear, btnMark, btnSubmit;
    private ImageButton btnPre, btnNext, btnCloseList;
    private ImageView btnListQuestion, btnMarkImage, btnBookMark;
    private CountDownTimer timer;
    private long timeLeft;
    private GridView gvQuestionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_list_layout);

        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.show("Loading questions...");

        viewModel = new ViewModelProvider(this).get(TestViewModel.class);

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                progressDialogUtil.dismiss();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadQuestions(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialogUtil.dismiss();
                init();
                setupUI();
                startTimer();
            }

            @Override
            public void onFailture() {
                progressDialogUtil.dismiss();
                Toast.makeText(QuestionActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void init() {
        drawerLayout = findViewById(R.id.drawer_layout12);
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
        btnCloseList = findViewById(R.id.btnCloseList);

        questionId = 0;
    }

    private void setupUI() {
        txtCatName.setText(viewModel.getCurrentCategoryList().get(viewModel.getSelectedCategoryIndex()).getName());
        txtQuestId.setText((questionId + 1) + "/" + viewModel.getCurrentQuestionList().size());

        viewModel.getCurrentQuestionList().get(questionId).setStatus(QuestionStatus.UNANSWERED);
        updateBookmarkUI();

        questionGridAdapter = new QuestionGridAdapter(this, viewModel.getCurrentQuestionList(), position -> goToQuestion(position));
        gvQuestionList.setAdapter(questionGridAdapter);

        adapter = new QuestionAdapter(viewModel.getCurrentQuestionList(), new QuestionAdapter.OnOptionSelectedListener() {
            @Override
            public void onOptionSelected(int position, int selectedOption) {
                viewModel.getCurrentQuestionList().get(position).setSelectedOption(selectedOption);
                viewModel.getCurrentQuestionList().get(position).setStatus(QuestionStatus.ANSWERED);
                adapter.notifyItemChanged(position);
                questionGridAdapter.notifyDataSetChanged();
            }

            @Override
            public void onOptionCleared(int position) {
                viewModel.getCurrentQuestionList().get(position).setSelectedOption(-1);
                viewModel.getCurrentQuestionList().get(position).setStatus(QuestionStatus.UNANSWERED);
                adapter.notifyItemChanged(position);
                questionGridAdapter.notifyDataSetChanged();
            }
        });
        rcvQuestion.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rcvQuestion.setLayoutManager(layoutManager);
        rcvQuestion.setItemAnimator(null);
        rcvQuestion.setItemViewCacheSize(viewModel.getCurrentQuestionList().size());

        setSnapHelper();
        setClickListeners();
    }

    private void startTimer() {
        long totalTime = viewModel.getCurrentTestList().get(viewModel.getSelectedTestIndex()).getTime() * 60 * 1000;
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
                submitTest(true);
            }
        };
        timer.start();
    }

    private void setClickListeners() {
        btnSubmit.setOnClickListener(view -> submitTest(false));

        btnPre.setOnClickListener(view -> {
            if (questionId > 0) {
                questionId--;
                rcvQuestion.smoothScrollToPosition(questionId);
            }
        });

        btnNext.setOnClickListener(view -> {
            if (questionId == viewModel.getCurrentQuestionList().size() - 1) {
                submitTest(false);
            } else {
                questionId++;
                rcvQuestion.smoothScrollToPosition(questionId);
            }
        });

        btnClear.setOnClickListener(view -> {
            viewModel.clearSelection(questionId);
            adapter.notifyItemChanged(questionId);
            questionGridAdapter.notifyDataSetChanged();
        });

        btnListQuestion.setOnClickListener(view -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
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
            boolean isMarked = btnMarkImage.getVisibility() != View.VISIBLE;
            viewModel.markForReview(questionId, isMarked);
            btnMarkImage.setVisibility(isMarked ? View.VISIBLE : View.GONE);
            adapter.notifyItemChanged(questionId);
            questionGridAdapter.notifyDataSetChanged();
        });

        btnBookMark.setOnClickListener(view -> {
            Log.d(TAG, "Bookmark clicked for questionId: " + questionId);
            boolean isBookmarked = !viewModel.getCurrentQuestionList().get(questionId).isBookMarked();
            viewModel.updateBookmark(questionId, isBookmarked);
            updateBookmarkUI();
            adapter.notifyItemChanged(questionId);
            questionGridAdapter.notifyDataSetChanged();
            // Hiển thị Toast dựa trên trạng thái bookmark
            Toast.makeText(
                    QuestionActivity.this,
                    isBookmarked ? "Saved this question!" : "Unsaved this question!",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void updateBookmarkUI() {
        if (viewModel.getCurrentQuestionList().get(questionId).isBookMarked()) {
            btnBookMark.setImageResource(R.drawable.ic_bookmark);
        } else {
            btnBookMark.setImageResource(R.drawable.ic_unbookmrked);
        }
    }

    private void submitTest(boolean isTimerFinished) {
        if (!isTimerFinished) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            View view = getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);
            Button btnCancel = view.findViewById(R.id.btnCancel);
            Button btnConfirm = view.findViewById(R.id.btnConfirm);
            builder.setView(view);
            final AlertDialog dialog = builder.create();
            btnCancel.setOnClickListener(view1 -> dialog.dismiss());
            btnConfirm.setOnClickListener(view1 -> {
                timer.cancel();
                dialog.dismiss();
                navigateToScoreActivity();
            });
            dialog.show();
        } else {
            navigateToScoreActivity();
        }
    }

    private void navigateToScoreActivity() {
        Intent intent = new Intent(QuestionActivity.this, ScoreActivity.class);
        long totalTime = viewModel.getCurrentTestList().get(viewModel.getSelectedTestIndex()).getTime() * 60 * 1000;
        intent.putExtra("TIME_TAKEN", totalTime - timeLeft);
        startActivity(intent);
        finish();
    }

    public void goToQuestion(int position) {
        questionId = position;
        rcvQuestion.smoothScrollToPosition(position);
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        updateBookmarkUI();
        adapter.notifyItemChanged(position);
        questionGridAdapter.notifyDataSetChanged();
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
                if (viewModel.getCurrentQuestionList().get(questionId).getStatus() == QuestionStatus.NOT_VISITED) {
                    viewModel.getCurrentQuestionList().get(questionId).setStatus(QuestionStatus.UNANSWERED);
                }
                btnMarkImage.setVisibility(
                        viewModel.getCurrentQuestionList().get(questionId).getStatus() == QuestionStatus.HIGHTLIGHTED
                                ? View.VISIBLE : View.GONE
                );
                txtQuestId.setText((questionId + 1) + "/" + viewModel.getCurrentQuestionList().size());
                updateBookmarkUI();
                adapter.notifyItemChanged(questionId);
                questionGridAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressDialogUtil.dismiss();
    }
}