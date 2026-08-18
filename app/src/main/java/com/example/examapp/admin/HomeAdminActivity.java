package com.example.examapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.examapp.R;
import com.example.examapp.databinding.ActivityHomeAdminBinding;

public class HomeAdminActivity extends AppCompatActivity {
    ActivityHomeAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.btnAddTest.setOnClickListener(view -> {
            Intent intent = new Intent(HomeAdminActivity.this, AddTestActivity.class);
            startActivity(intent);

        });

        binding.btnAddQuestion.setOnClickListener(view -> {
            Intent intent = new Intent(HomeAdminActivity.this, AddQuestionActivity.class);
            startActivity(intent);

        });
    }
}