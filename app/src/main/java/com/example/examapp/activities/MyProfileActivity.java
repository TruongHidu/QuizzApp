package com.example.examapp.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.examapp.R;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityMyProfileBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;

public class MyProfileActivity extends AppCompatActivity {
    ActivityMyProfileBinding binding;
    private String nameStr, phoneStr;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new Dialog(MyProfileActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Saving ...");



        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        disableEditing();

        addEvent();

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
        if(DbQuery.myProfile.getPhone() != null) {
            binding.txtProfilePhone.setText(DbQuery.myProfile.getPhone());
        }
        String profileName = DbQuery.myProfile.getName();
        binding.profileText.setText(profileName.toUpperCase().substring(0, 1));
    }

    private void addEvent() {
        binding.btnCancel.setOnClickListener(view -> {
            disableEditing();

        });

        binding.btnEdit.setOnClickListener(view -> {
            enableEditing();

        });
        binding.btnSave.setOnClickListener(view -> {
            if (validate()) {
                saveData();
            }
        });
    }

    private void saveData() {
        progressDialog.show();
        if(phoneStr.isEmpty()){
            phoneStr = null;
        }

        DbQuery.saveUserData(nameStr, phoneStr, new MyCompleteListener(){
                @Override
                public void onSuccess() {
                    Toast.makeText(MyProfileActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    disableEditing();
                }
                @Override
                public void onFailture() {
                    Toast.makeText(MyProfileActivity.this, "Failed to save data!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
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
        nameStr = binding.txtProfileName.getText().toString();
        phoneStr = binding.txtProfilePhone.getText().toString();
        if(nameStr.isEmpty()) {
            binding.txtProfileName.setError("Please enter your name");
            return false;
        }
        if( phoneStr.isEmpty()) {
            binding.txtProfilePhone.setError("Please enter your phone");
            return false;
        }else{
            if( !((phoneStr.length()) == 10) || (!phoneStr.matches("[0-9]+"))) {
                binding.txtProfilePhone.setError("Please enter a valid phone number");
                return false;
            }

        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Đóng activity hiện tại để quay lại activity trước
        return true;
    }
}