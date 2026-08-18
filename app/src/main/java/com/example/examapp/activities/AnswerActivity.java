package com.example.examapp.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.examapp.R;
import com.example.examapp.adapter.AnswerAdapter;
import com.example.examapp.databinding.ActivityAnswerBinding;
import com.example.examapp.viewmodel.TestViewModel;

public class AnswerActivity extends AppCompatActivity {
    private ActivityAnswerBinding binding;
    private AnswerAdapter adapter;
    private TestViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAnswerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Answer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup RecyclerView
        binding.rcvAnswer.setLayoutManager(new LinearLayoutManager(this));
        viewModel = new ViewModelProvider(this).get(TestViewModel.class);
        adapter = new AnswerAdapter(viewModel.getCurrentQuestionList(), viewModel);
        binding.rcvAnswer.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}