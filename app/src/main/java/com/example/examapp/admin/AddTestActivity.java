package com.example.examapp.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examapp.databinding.ActivityAddTestBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTestActivity extends AppCompatActivity {

    private ActivityAddTestBinding binding;
    private FirebaseFirestore db;

    private List<String> categoryNames = new ArrayList<>();
    private List<String> categoryIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar1);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Add Test");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadCategories();

        binding.addTestButton.setOnClickListener(v -> addTest());
    }

    private void loadCategories() {
        db.collection("QUIZ").get().addOnSuccessListener(querySnapshot -> {
            categoryNames.clear();
            categoryIds.clear();

            for (var doc : querySnapshot.getDocuments()) {
                String name = doc.getString("NAME");
                String id = doc.getId();

                if (name != null && id != null) {
                    categoryNames.add(name);
                    categoryIds.add(id);
                }
            }

            if (categoryNames.isEmpty()) {
                categoryNames.add("Không có danh mục");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    categoryNames
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.categorySpinner.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
        });
    }

    private void addTest() {
        if (categoryIds.isEmpty()) {
            Toast.makeText(this, "Không có danh mục nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = binding.categorySpinner.getSelectedItemPosition();
        String selectedCatId = categoryIds.get(selectedIndex);

        String testId = binding.testIdEditText.getText().toString().trim();
        String timeStr = binding.testTimeEditText.getText().toString().trim();

        if (testId.isEmpty() || timeStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        int time;
        try {
            time = Integer.parseInt(timeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời gian phải là số!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference testInfoRef = db.collection("QUIZ")
                .document(selectedCatId)
                .collection("TEST_LIST")
                .document("TEST_INFO");

        DocumentReference categoryRef = db.collection("QUIZ").document(selectedCatId);

        testInfoRef.get().addOnSuccessListener(snapshot -> {
            // Sử dụng mảng để tránh lỗi effectively final
            final List<Map<String, Object>>[] currentTests = new List[]{new ArrayList<>()};

            if (snapshot.exists() && snapshot.contains("TESTs")) {
                currentTests[0] = (List<Map<String, Object>>) snapshot.get("TESTs");
            }

            // Tạo bài test mới
            Map<String, Object> newTest = new HashMap<>();
            newTest.put("id", testId);
            newTest.put("time", time);
            currentTests[0].add(newTest);

            // Cập nhật TESTS trong Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("TESTs", currentTests[0]);

            testInfoRef.set(updates)
                    .addOnSuccessListener(unused -> {
                        // Cập nhật NO_OF_TEST lên document chính
                        categoryRef.update("NO_OF_TEST", currentTests[0].size())
                                .addOnSuccessListener(unused2 -> {
                                    Toast.makeText(this, "Đã thêm bài test thành công!", Toast.LENGTH_SHORT).show();
                                    binding.testIdEditText.setText("");
                                    binding.testTimeEditText.setText("");
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Không cập nhật được NO_OF_TEST", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Thêm bài test thất bại!", Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi kiểm tra TEST_INFO", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
