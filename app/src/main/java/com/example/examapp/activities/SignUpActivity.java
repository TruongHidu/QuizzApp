package com.example.examapp.activities;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivitySignUpBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;

    private String email, password, confirmPassword, fullName;
    Dialog progressDialog;
    TextView dialogText;

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



        progressDialog = new Dialog(SignUpActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Registering user...");





        mAuth = FirebaseAuth.getInstance();


        binding.btnSignUp.setOnClickListener(view -> {
            if(validate()) {
                signUpNewUser();
            }
        });

    }

    private boolean validate() {
        email = binding.txtEmail.getText().toString().trim();
        password = binding.txtPassword.getText().toString().trim();
        confirmPassword = binding.txtConfirmPassword.getText().toString().trim();
        fullName = binding.txtFullName.getText().toString().trim();
        if(fullName.isEmpty()) {
            binding.txtFullName.setError("Enter Full Name");
            return false;
        }
        if(email.isEmpty()) {
            binding.txtEmail.setError("Enter Email");
            return false;
        }
        if(password.isEmpty()) {
            binding.txtPassword.setError("Enter Password");
            return false;
        }
        if(confirmPassword.isEmpty()) {
            binding.txtConfirmPassword.setError("Enter Confirm Password");
            return false;

        }
        if(!password.equals(confirmPassword)) {
            binding.txtConfirmPassword.setError("Password not match");
            return false;
        }
        return true;

    }

    private void signUpNewUser() {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Sig Up Success", Toast.LENGTH_SHORT).show();
                            DbQuery.createUserData(email, fullName, new MyCompleteListener(){
                                @Override
                                public void onSuccess() {
                                    progressDialog.dismiss();

                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    SignUpActivity.this.finish();
                                }

                                @Override
                                public void onFailture() {
                                    Toast.makeText(SignUpActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}