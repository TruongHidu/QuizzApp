package com.example.examapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.examapp.R;
import com.example.examapp.databinding.ActivityAddQuestionBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class AddQuestionActivity extends AppCompatActivity {

    private Spinner spinnerCategory, spinnerTest;
    private EditText edtQuestion, edtOptionA, edtOptionB, edtOptionC, edtOptionD;
    private RadioGroup radioGroup;
    private Button btnAddQuestion;
    private ActivityAddQuestionBinding binding;

    private FirebaseFirestore db;

    private List<String> categoryList = new ArrayList<>();
    private List<String> testList = new ArrayList<>();
    private Map<String, String> categoryMap = new HashMap<>();
    private Map<String, String> testMap = new HashMap<>();

    private String selectedCategoryId = "";
    private String selectedTestId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        setSupportActionBar(binding.toolbar1);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Add Test");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        initViews();
        loadCategories();

        btnAddQuestion.setOnClickListener(view -> addQuestion());
    }

    private void initViews() {
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerTest = findViewById(R.id.spinner_test);
        edtQuestion = findViewById(R.id.edt_question);
        edtOptionA = findViewById(R.id.edt_optionA);
        edtOptionB = findViewById(R.id.edt_optionB);
        edtOptionC = findViewById(R.id.edt_optionC);
        edtOptionD = findViewById(R.id.edt_optionD);
        radioGroup = findViewById(R.id.radio_group);
        btnAddQuestion = findViewById(R.id.btn_add_question);
    }

    private void loadCategories() {
        db.collection("QUIZ").get().addOnSuccessListener(querySnapshot -> {
            categoryList.clear();
            categoryMap.clear();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String name = doc.getString("NAME");
                String id = doc.getId();
                if (name != null && id != null) {
                    categoryList.add(name);
                    categoryMap.put(name, id);
                }
            }

            if (categoryList.isEmpty()) {
                categoryList.add("Không có danh mục");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
            spinnerCategory.setAdapter(adapter);

            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCatName = categoryList.get(position);
                    selectedCategoryId = categoryMap.get(selectedCatName);
                    if (selectedCategoryId != null) loadTests(selectedCategoryId);
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });

        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show());
    }

    private void loadTests(String categoryId) {
        db.collection("QUIZ").document(categoryId)
                .collection("TEST_LIST").document("TEST_INFO")
                .get()
                .addOnSuccessListener(snapshot -> {
                    testList.clear();
                    testMap.clear();

                    if (snapshot.exists() && snapshot.contains("TESTs")) {
                        List<Map<String, Object>> tests = (List<Map<String, Object>>) snapshot.get("TESTs");
                        if (tests != null) {
                            for (Map<String, Object> test : tests) {
                                String id = (String) test.get("id");
                                if (id != null) {
                                    testList.add(id);
                                    testMap.put(id, id);  // Có thể cập nhật để liên kết ID khác nếu cần
                                }
                            }
                        }
                    }

                    if (testList.isEmpty()) {
                        testList.add("Không có bài kiểm tra");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, testList);
                    spinnerTest.setAdapter(adapter);

                    spinnerTest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedTestId = testList.get(position);
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });

                }).addOnFailureListener(e -> Log.e("FirestoreError", "Lỗi tải TESTs", e));
    }

    private void addQuestion() {
        String question = edtQuestion.getText().toString().trim();
        String optionA = edtOptionA.getText().toString().trim();
        String optionB = edtOptionB.getText().toString().trim();
        String optionC = edtOptionC.getText().toString().trim();
        String optionD = edtOptionD.getText().toString().trim();

        int selectedAnswer = -1;
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_A) selectedAnswer = 1;
        else if (checkedId == R.id.radio_B) selectedAnswer = 2;
        else if (checkedId == R.id.radio_C) selectedAnswer = 3;
        else if (checkedId == R.id.radio_D) selectedAnswer = 4;

        if (question.isEmpty() || optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty() || selectedAnswer == -1) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> questionData = new HashMap<>();
        questionData.put("QUESTION", question);
        questionData.put("A", optionA);
        questionData.put("B", optionB);
        questionData.put("C", optionC);
        questionData.put("D", optionD);
        questionData.put("ANSWER", selectedAnswer);
        questionData.put("CATEGORY", selectedCategoryId);
        questionData.put("TEST", selectedTestId);

        db.collection("Question").add(questionData)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Thêm câu hỏi thành công!", Toast.LENGTH_SHORT).show();
                    edtQuestion.setText("");
                    edtOptionA.setText("");
                    edtOptionB.setText("");
                    edtOptionC.setText("");
                    edtOptionD.setText("");
                    radioGroup.clearCheck();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi thêm câu hỏi!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
