package com.example.examapp.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.examapp.R;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivitySplashBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding binding;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Typeface typeface = ResourcesCompat.getFont(this, R.font.font1);
        binding.appName.setTypeface(typeface);

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.myappim);
        binding.appName.setAnimation(animation);

        mAuth = FirebaseAuth.getInstance();
        DbQuery.g_firestore = FirebaseFirestore.getInstance();


        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    if (mAuth.getCurrentUser() != null) {
                        DbQuery.loadData(new MyCompleteListener() {
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
            }
        }.start();

    }
}