package com.example.examapp.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.examapp.R;
import com.example.examapp.adapter.BookMarkAdapter;
import com.example.examapp.databinding.ActivityBookMarkBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.viewmodel.BookMarkViewModel;

public class BookMarkActivity extends AppCompatActivity {
    private ActivityBookMarkBinding binding;
    private BookMarkAdapter adapter;
    private BookMarkViewModel viewModel;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBookMarkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Saved Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new Dialog(BookMarkActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Loading ...");
        progressDialog.show();

        viewModel = new ViewModelProvider(this).get(BookMarkViewModel.class);
        adapter = new BookMarkAdapter(viewModel);
        binding.rcvAnswerBookmark.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvAnswerBookmark.setAdapter(adapter);

        viewModel.getBookmarkList().observe(this, questions -> {
            adapter.notifyDataSetChanged();
        });

        viewModel.loadBookMarks(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
            }

            @Override
            public void onFailture() {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}