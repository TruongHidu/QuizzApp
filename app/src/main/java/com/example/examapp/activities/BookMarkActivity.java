package com.example.examapp.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.adapter.AnswerAdapter;
import com.example.examapp.adapter.BookMarkAdapter;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.ActivityBookMarkBinding;
import com.example.examapp.handlerlistener.MyCompleteListener;

public class BookMarkActivity extends AppCompatActivity {
    private ActivityBookMarkBinding binding;
    private BookMarkAdapter adapter;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBookMarkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Saved Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new Dialog(BookMarkActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.txtDialog);
        dialogText.setText("Loading ...");
        progressDialog.show();

        // Khởi tạo adapter rỗng ngay từ đầu
        adapter = new BookMarkAdapter(DbQuery.g_bookmarkList);
        binding.rcvAnswerBookmark.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvAnswerBookmark.setAdapter(adapter);

        DbQuery.loadBookMarks(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                adapter.notifyDataSetChanged();  // Bây giờ adapter đã sẵn sàng
                progressDialog.dismiss();
            }

            @Override
            public void onFailture() {
                progressDialog.dismiss();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == android.R.id.home){
            BookMarkActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
    }