package com.example.examapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.examapp.R;
import com.example.examapp.database.DbQuery;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddQuestionActivity extends AppCompatActivity {

    private Spinner spinnerCategory, spinnerTest;
    private EditText edtQuestion, edtOptionA, edtOptionB, edtOptionC, edtOptionD;
    private RadioGroup radioGroup;
    private Button btnAddQuestion;

    private FirebaseFirestore db;
    private List<String> categoryList = new ArrayList<>();
    private List<String> testList = new ArrayList<>();
    private Map<String, String> categoryMap = new HashMap<>();
    private Map<String, String> testMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerTest = findViewById(R.id.spinner_test);
        edtQuestion = findViewById(R.id.edt_question);
        edtOptionA = findViewById(R.id.edt_optionA);
        edtOptionB = findViewById(R.id.edt_optionB);
        edtOptionC = findViewById(R.id.edt_optionC);
        edtOptionD = findViewById(R.id.edt_optionD);
        radioGroup = findViewById(R.id.radio_group);
        btnAddQuestion = findViewById(R.id.btn_add_question);

        // Load danh mục và bài kiểm tra
        loadCategories();

        // Xử lý khi nhấn nút "Thêm Câu Hỏi"
        btnAddQuestion.setOnClickListener(view -> addQuestion());
    }

    private void loadCategories() {
        DbQuery.g_firestore.collection("QUIZ").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            categoryMap.clear();

            for (var doc : queryDocumentSnapshots) {
                String catName = doc.getString("NAME");
                String catId = doc.getId();

                if (catName != null && catId != null) { // Kiểm tra null trước khi thêm vào danh sách
                    categoryList.add(catName);
                    categoryMap.put(catName, catId);
                }
            }

            if (categoryList.isEmpty()) {
                categoryList.add("Không có danh mục"); // Tránh lỗi khi danh sách rỗng
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
            spinnerCategory.setAdapter(adapter);

            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCategoryId = categoryMap.get(categoryList.get(position));
                    if (selectedCategoryId != null) {
                        loadTests(selectedCategoryId);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Không làm gì nếu không chọn mục nào
                }
            });
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show());
    }

    private void loadTests(String categoryId) {
        DbQuery.g_firestore.collection("QUIZ").document(categoryId)
                .collection("TEST_LIST").document("TEST_INFO")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    testList.clear();
                    testMap.clear();

                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData(); // Kiểm tra dữ liệu
                        Log.d("FirestoreData", "Dữ liệu TEST_INFO: " + data);

                        for (int i = 1; i <= 3; i++) {
                            String testIdKey = "TEST" + i + "_ID";
                            String testTimeKey = "TEST" + i + "_TIME";

                            if (documentSnapshot.contains(testIdKey) && documentSnapshot.contains(testTimeKey)) {
                                String testId = documentSnapshot.getString(testIdKey);
                                Long testTime = documentSnapshot.getLong(testTimeKey);

                                if (testId != null && testTime != null) {
                                    testList.add(testId);
                                    testMap.put(testId, testId);
                                }
                            } else {
                                Log.e("FirestoreError", "Không tìm thấy trường: " + testIdKey);
                            }
                        }
                    } else {
                        Log.e("FirestoreError", "Không tìm thấy tài liệu TEST_INFO");
                    }

                    if (testList.isEmpty()) {
                        testList.add("Không có bài kiểm tra");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, testList);
                    spinnerTest.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Lỗi tải bài kiểm tra", e));
    }



    private void addQuestion() {
        String selectedCategory = categoryMap.get(spinnerCategory.getSelectedItem().toString());
        String selectedTest = testMap.get(spinnerTest.getSelectedItem().toString());

        String question = edtQuestion.getText().toString().trim();
        String optionA = edtOptionA.getText().toString().trim();
        String optionB = edtOptionB.getText().toString().trim();
        String optionC = edtOptionC.getText().toString().trim();
        String optionD = edtOptionD.getText().toString().trim();

        int selectedAnswer = -1;
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId != -1) {
            RadioButton selectedRadioButton = findViewById(checkedId);
            if (checkedId == R.id.radio_A) {
                selectedAnswer = 1;
            } else if (checkedId == R.id.radio_B) {
                selectedAnswer = 2;
            } else if (checkedId == R.id.radio_C) {
                selectedAnswer = 3;
            } else if (checkedId == R.id.radio_D) {
                selectedAnswer = 4;
            }

        }

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
        questionData.put("CATEGORY", selectedCategory);
        questionData.put("TEST", selectedTest);

        db.collection("Question").add(questionData).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Thêm câu hỏi thành công!", Toast.LENGTH_SHORT).show();
            // Làm trống các ô nhập liệu
            edtQuestion.setText("");
            edtOptionA.setText("");
            edtOptionB.setText("");
            edtOptionC.setText("");
            edtOptionD.setText("");

            // Bỏ chọn RadioGroup
            radioGroup.clearCheck();

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi khi thêm câu hỏi!", Toast.LENGTH_SHORT).show()
        );
    }
}
