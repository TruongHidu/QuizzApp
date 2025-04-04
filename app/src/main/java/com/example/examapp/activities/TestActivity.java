package com.example.examapp.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.CategoryFragment;
import com.example.examapp.R;
import com.example.examapp.adapter.CategoryAdapter;
import com.example.examapp.adapter.TestAdapter;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityTestBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.TestModel;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    ActivityTestBinding binding;
    private List<TestModel> testList;
    TestAdapter adapter;
    Dialog progressDialog;
    TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (DbQuery.g_categoryList != null && !DbQuery.g_categoryList.isEmpty()) {
            getSupportActionBar().setTitle(DbQuery.g_categoryList.get(DbQuery.g_selectedCatIndex).getName());
        } else {
            getSupportActionBar().setTitle("Tests");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new Dialog(TestActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Loading ...");

        progressDialog.show();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.rcvTest.setLayoutManager(layoutManager);
        DbQuery.initFirestore();

        if (DbQuery.g_categoryList.isEmpty()) {
            DbQuery.loadCategories(new MyCompleteListener() {
                @Override
                public void onSuccess() {
                    loadTests(); // Chỉ gọi khi có category
                }

                @Override
                public void onFailture() {
                    progressDialog.dismiss();
                    Toast.makeText(TestActivity.this, "Failed to load categories!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadTests();
        }
    }

    // Tách loadTests() ra ngoài onCreate()
    private void loadTests() {
        DbQuery.loadTestData(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                DbQuery.loadMyScore(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        if (DbQuery.g_testList != null) {
                            adapter = new TestAdapter(DbQuery.g_testList);
                            binding.rcvTest.setAdapter(adapter);
                        } else {
                            Toast.makeText(TestActivity.this, "No tests available", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailture(){
                        progressDialog.dismiss();
                        Toast.makeText(TestActivity.this, "Something went wrong! Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailture() {
                progressDialog.dismiss();
                Toast.makeText(TestActivity.this, "Something went wrong! Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        loadTests(); // Gọi lại để cập nhật danh sách bài kiểm tra
    }

}
