package com.example.examapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.examapp.databinding.ActivityMyProfileBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.ProfileModel;
import com.example.examapp.utils.ProgressDialogUtil;
import com.example.examapp.viewmodel.AuthViewModel;

public class MyProfileActivity extends AppCompatActivity {
    private ActivityMyProfileBinding binding;
    private AuthViewModel viewModel;
    private ProgressDialogUtil progressDialog;
    private String nameStr;

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

        // Load user profile data
        viewModel.loadUserProfile();

        // Observe profile data
        viewModel.getUserProfile().observe(this, profile -> {
            if (profile != null) {
                disableEditing(profile);
            } else {
                Toast.makeText(MyProfileActivity.this, "Failed to load profile data", Toast.LENGTH_LONG).show();
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            progressDialog.dismiss();
            Toast.makeText(MyProfileActivity.this, error, Toast.LENGTH_LONG).show();
        });

        addEvent();
    }

    private void disableEditing(ProfileModel profile) {
        binding.txtProfileEmail.setEnabled(false);
        binding.txtProfileName.setEnabled(false);
        binding.btnSave.setVisibility(View.GONE);
        binding.btnEdit.setVisibility(View.VISIBLE);
        binding.btnCancel.setVisibility(View.GONE);

        binding.txtProfileName.setText(profile.getName());
        binding.txtProfileEmail.setText(profile.getEmail());
        String profileName = profile.getName();
        binding.profileText.setText(profileName != null && !profileName.isEmpty() ?
                profileName.toUpperCase().substring(0, 1) : "NA");
    }

    private void addEvent() {
        binding.btnCancel.setOnClickListener(view -> viewModel.loadUserProfile()); // Reload profile to reset fields

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
        viewModel.saveUserData(nameStr, new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(MyProfileActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                viewModel.loadUserProfile(); // Reload profile to update UI
            }
            @Override
            public void onFailture() {
                // Error handled via errorMessage LiveData
            }
        });
    }

    private void enableEditing() {
        binding.txtProfileName.setEnabled(true);
        binding.btnSave.setVisibility(View.VISIBLE);
        binding.btnEdit.setVisibility(View.GONE);
        binding.btnCancel.setVisibility(View.VISIBLE);
    }

    private boolean validate() {
        nameStr = binding.txtProfileName.getText().toString().trim();
        if (nameStr.isEmpty()) {
            binding.txtProfileName.setError("Please enter your name");
            return false;
        }
        return true;
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