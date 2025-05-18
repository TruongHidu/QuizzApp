package com.example.examapp.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.examapp.databinding.ActivitySignUpBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.AuthViewModel;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private AuthViewModel viewModel;
    private ProgressDialogUtil progressDialog;

    private String email, password, confirmPassword, fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Sign up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressDialog = new ProgressDialogUtil(this);

        binding.btnSignUp.setOnClickListener(view -> {
            if (validate()) {
                signUpNewUser();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            progressDialog.dismiss();
            Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_LONG).show();
        });
    }

    private boolean validate() {
        email = binding.txtEmail.getText().toString().trim();
        password = binding.txtPassword.getText().toString().trim();
        confirmPassword = binding.txtConfirmPassword.getText().toString().trim();
        fullName = binding.txtFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            binding.txtFullName.setError("Enter Full Name");
            return false;
        }
        if (email.isEmpty()) {
            binding.txtEmail.setError("Enter Email");
            return false;
        }
        if (password.isEmpty()) {
            binding.txtPassword.setError("Enter Password");
            return false;
        }
        if (confirmPassword.isEmpty()) {
            binding.txtConfirmPassword.setError("Enter Confirm Password");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.txtConfirmPassword.setError("Password not match");
            return false;
        }
        return true;
    }

    private void signUpNewUser() {
        progressDialog.show("Registering user...");
        viewModel.signUp(email, password, fullName, new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(SignUpActivity.this, "Sign Up Success", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                SignUpActivity.this.finish();
            }
            @Override
            public void onFailture() {
                // Lỗi được xử lý qua errorMessage LiveData
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                float x = ev.getRawX() + v.getLeft() - location[0];
                float y = ev.getRawY() + v.getTop() - location[1];

                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    // Ẩn bàn phím
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    v.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}