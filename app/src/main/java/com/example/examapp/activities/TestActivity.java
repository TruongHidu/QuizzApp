package com.example.examapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.examapp.adapter.TestAdapter;
import com.example.examapp.databinding.ActivityTestBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.TestViewModel;

public class TestActivity extends AppCompatActivity implements TestAdapter.OnTestClickListener {
    private ActivityTestBinding binding;
    private TestViewModel viewModel;
    private TestAdapter adapter;
    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.show("Loading ...");

        viewModel = new ViewModelProvider(this).get(TestViewModel.class);

        // Set toolbar title using ViewModel
        if (!viewModel.getCurrentCategoryList().isEmpty()) {
            getSupportActionBar().setTitle(viewModel.getCurrentCategoryList().get(viewModel.getSelectedCategoryIndex()).getName());
        } else {
            getSupportActionBar().setTitle("Tests");
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rcvTest.setLayoutManager(layoutManager);
        adapter = new TestAdapter(viewModel.getCurrentTestList(), this);
        binding.rcvTest.setAdapter(adapter);

        viewModel.getTests().observe(this, tests -> {
            progressDialogUtil.dismiss();
            if (tests != null) {
                adapter.updateData(tests);
                binding.rcvTest.setVisibility(tests.isEmpty() ? View.GONE : View.VISIBLE);
                binding.emptyView.setVisibility(tests.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            progressDialogUtil.dismiss();
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load categories if empty, then load tests
        if (viewModel.getCurrentCategoryList().isEmpty()) {
            viewModel.loadCategories(new MyCompleteListener() {
                @Override
                public void onSuccess() {
                    // Update toolbar title after categories load
                    if (!viewModel.getCurrentCategoryList().isEmpty()) {
                        getSupportActionBar().setTitle(viewModel.getCurrentCategoryList().get(viewModel.getSelectedCategoryIndex()).getName());
                    }
                    viewModel.loadTestData();
                }

                @Override
                public void onFailture() {
                    progressDialogUtil.dismiss();
                    Toast.makeText(TestActivity.this, "Failed to load categories!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            viewModel.loadTestData();
        }
    }

    @Override
    public void onTestClicked(int position) {
        viewModel.setSelectedTestIndex(position);
        Intent intent = new Intent(this, StartTestActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        progressDialogUtil.dismiss();
        finish();
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        progressDialogUtil.show("Loading ...");
        viewModel.loadTestData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressDialogUtil.dismiss();
    }
}