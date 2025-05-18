package com.example.examapp.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.examapp.R;
import com.example.examapp.activities.MainActivity;
import com.example.examapp.activities.SignUpActivity;
import com.example.examapp.admin.HomeAdminActivity;
import com.example.examapp.databinding.ActivityLoginBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Khởi tạo progress dialog
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Signing in ...");

        // Quan sát lỗi
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Thiết lập sự kiện
        setupEvents();
    }

    private void setupEvents() {
        binding.btnLogin.setOnClickListener(view -> {
            String email = binding.txtEmail.getText().toString().trim();
            String password = binding.txtPassword.getText().toString().trim();

            if (viewModel.validateData(email, password)) {
                progressDialog.show();
                viewModel.login(email, password, new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        if (email.equals("admin@gmail.com")) {
                            startActivity(new Intent(LoginActivity.this, HomeAdminActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                        finish();
                    }

                    @Override
                    public void onFailture() {
                        progressDialog.dismiss();
                    }
                });
            } else {
                if (email.isEmpty()) {
                    binding.txtEmail.setError("Enter email");
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.txtEmail.setError("Email not qualified!");
                }
                if (password.isEmpty()) {
                    binding.txtPassword.setError("Input password");
                }
            }
        });

        binding.txtSignUp.setOnClickListener(view -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });

        binding.gSignIn.setOnClickListener(view -> {
            // Xử lý Google Sign-In (chưa triển khai)
            Toast.makeText(this, "Google Sign-In not implemented", Toast.LENGTH_SHORT).show();
        });
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