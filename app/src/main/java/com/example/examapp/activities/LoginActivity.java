package com.example.examapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.examapp.R;
import com.example.examapp.admin.AddQuestionActivity;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityLoginBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    FirebaseAuth mAuth;
    Dialog progressDialog;
    TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());





        progressDialog = new Dialog(LoginActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Signing in ...");




        mAuth = FirebaseAuth.getInstance();

        events();



    }

    private void events() {
        binding.btnLogin.setOnClickListener(view -> {
            if (validateData()){
                login();

            }

        });

        binding.txtSignUp.setOnClickListener(view -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });

        binding.gSignIn.setOnClickListener(view -> {
            googleSignIn();
        });
//
//        binding.gSignIn.setOnClickListener(view -> {
//            Intent intent = new Intent(LoginActivity.this, AddQuestionActivity.class);
//            startActivity(intent);
//            LoginActivity.this.finish();
//        });
    }

    private void googleSignIn() {
    }

    private void login() {
        progressDialog.show();
        String email = binding.txtEmail.getText().toString().trim();
        String password = binding.txtPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Toast.makeText(LoginActivity.this, "Login success.", Toast.LENGTH_SHORT).show();

                            DbQuery.loadData(new MyCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    LoginActivity.this.finish();
                                }

                                @Override
                                public void onFailture() {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Something went wrong! Please try again later.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            progressDialog.dismiss();

                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                // Email không tồn tại
                                Toast.makeText(LoginActivity.this, "Email not exists!", Toast.LENGTH_SHORT).show();
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Mật khẩu sai
                                Toast.makeText(LoginActivity.this, "Invalid password. Please try again!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    private boolean validateData() {
        String email = binding.txtEmail.getText().toString().trim();
        String password = binding.txtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.txtEmail.setError("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.txtEmail.setError("Email not qualified!");
            return false;
        }

        if (password.isEmpty()) {
            binding.txtPassword.setError("Input password");
            return false;
        }

        return true;
    }

}