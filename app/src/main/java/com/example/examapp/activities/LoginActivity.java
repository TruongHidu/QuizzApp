package com.example.examapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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

        binding.gSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, AddQuestionActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        });
    }

    private void googleSignIn() {
    }

    private void login() {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(binding.txtEmail.getText().toString().trim(), binding.txtPassword.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, "Login success.",Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    }
                });


    }

    private boolean validateData() {
        boolean status = false;
        if(binding.txtEmail.getText().toString().isEmpty()){
            binding.txtEmail.setError("Enter Email");
        }else if(binding.txtPassword.getText().toString().isEmpty()){
            binding.txtPassword.setError("Enter Password");
        }else{
            status = true;
        }
        return status;
    }
}