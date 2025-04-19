package com.example.examapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.examapp.R;
import com.example.examapp.adapter.TestAdapter;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityTestBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.TestModel;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.TestViewModel;

import java.util.ArrayList;

public class TestActivity extends AppCompatActivity {
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

        if (DbQuery.g_categoryList != null && !DbQuery.g_categoryList.isEmpty()) {
            getSupportActionBar().setTitle(DbQuery.g_categoryList.get(DbQuery.g_selectedCatIndex).getName());
        } else {
            getSupportActionBar().setTitle("Tests");
        }

        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.show("Loading ...");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rcvTest.setLayoutManager(layoutManager);
        adapter = new TestAdapter(new ArrayList<>());
        binding.rcvTest.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TestViewModel.class);

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

        DbQuery.initFirestore();
        if (DbQuery.g_categoryList.isEmpty()) {
            DbQuery.loadCategories(new MyCompleteListener() {
                @Override
                public void onSuccess() {
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