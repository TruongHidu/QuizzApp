package com.example.examapp.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.examapp.R;
import com.example.examapp.activities.LoginActivity;
import com.example.examapp.activities.MainActivity;
import com.example.examapp.databinding.ActivitySplashBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.viewmodel.SplashViewModel;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private SplashViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        // Thiết lập font và animation
        Typeface typeface = ResourcesCompat.getFont(this, R.font.font1);
        binding.appName.setTypeface(typeface);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.myappim);
        binding.appName.setAnimation(animation);

        // Kiểm tra trạng thái đăng nhập
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                if (viewModel.isUserLoggedIn()) {
                    viewModel.loadData(new MyCompleteListener() {
                        @Override
                        public void onSuccess() {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailture() {
                            Toast.makeText(SplashActivity.this, "Something went wrong! Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            });
        }).start();
    }
}