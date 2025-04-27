package com.example.examapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityMyProfileBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.AuthViewModel;

public class MyProfileActivity extends AppCompatActivity {
    private ActivityMyProfileBinding binding;
    private AuthViewModel viewModel;
    private ProgressDialogUtil progressDialog;
    private String nameStr, phoneStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressDialog = new ProgressDialogUtil(this);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        disableEditing();
        addEvent();

        viewModel.getErrorMessage().observe(this, error -> {
            progressDialog.dismiss();
            Toast.makeText(MyProfileActivity.this, error, Toast.LENGTH_LONG).show();
        });
    }

    private void disableEditing() {
        binding.txtProfileEmail.setEnabled(false);
        binding.txtProfileName.setEnabled(false);
        binding.txtProfilePhone.setEnabled(false);
        binding.btnSave.setVisibility(View.GONE);
        binding.btnEdit.setVisibility(View.VISIBLE);
        binding.btnCancel.setVisibility(View.GONE);

        binding.txtProfileName.setText(DbQuery.myProfile.getName());
        binding.txtProfileEmail.setText(DbQuery.myProfile.getEmail());
        if (DbQuery.myProfile.getPhone() != null) {
            binding.txtProfilePhone.setText(DbQuery.myProfile.getPhone());
        }
        String profileName = DbQuery.myProfile.getName();
        binding.profileText.setText(profileName.toUpperCase().substring(0, 1));
    }

    private void addEvent() {
        binding.btnCancel.setOnClickListener(view -> disableEditing());

        binding.btnEdit.setOnClickListener(view -> {
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();
            enableEditing();
        });

        binding.btnSave.setOnClickListener(view -> {
            if (validate()) {
                saveData();
            }
        });
    }

    private void saveData() {
        progressDialog.show("Saving...");
        if (phoneStr.isEmpty()) {
            phoneStr = null;
        }
        viewModel.saveUserData(nameStr, phoneStr, new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(MyProfileActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                disableEditing();
            }
            @Override
            public void onFailture() {
                // Lỗi được xử lý qua errorMessage LiveData
            }
        });
    }

    private void enableEditing() {
        binding.txtProfileName.setEnabled(true);
        binding.txtProfilePhone.setEnabled(true);
        binding.btnSave.setVisibility(View.VISIBLE);
        binding.btnEdit.setVisibility(View.GONE);
        binding.btnCancel.setVisibility(View.VISIBLE);
    }

    private boolean validate() {
        nameStr = binding.txtProfileName.getText().toString().trim();
        phoneStr = binding.txtProfilePhone.getText().toString().trim();
        if (nameStr.isEmpty()) {
            binding.txtProfileName.setError("Please enter your name");
            return false;
        }
        if (!phoneStr.isEmpty()) {
            if (phoneStr.length() != 10 || !phoneStr.matches("[0-9]+")) {
                binding.txtProfilePhone.setError("Phone number must be 10 digits !");
                return false;
            }
        }else{
            binding.txtProfilePhone.setError("Please enter your phone number");
            return false;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}