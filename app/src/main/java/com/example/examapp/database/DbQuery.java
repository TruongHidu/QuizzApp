package com.example.examapp.database;

import android.util.ArrayMap;
import android.widget.Toast;

import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.CategoryModel;
import com.example.examapp.model.ProfileModel;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.model.RankModel;
import com.example.examapp.model.TestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DbQuery {
    public static FirebaseFirestore g_firestore;
    public static List<CategoryModel> g_categoryList = new ArrayList<>();
    public static List<TestModel> g_testList = new ArrayList<>();
    public static int g_selected_test_index = 0;
    public static List<String>  g_bmIdList = new ArrayList<>();
    public static List<QuestionModel> g_bookmarkList = new ArrayList<>();
    public static List<QuestionModel> g_questionList = new ArrayList<>();
    public static int g_selectedCatIndex = 0;
    public static List<RankModel> g_userList = new ArrayList<>();
    public static int g_userCount = 0;
    public static RankModel myPerformanece = new RankModel(0, -1,null);

    public static final int NOT_VISITED = 0;
    public static final int UNANSWERED = 1;
    public static final int ANSWERED = 2;
    public static final int HIGHTLIGHTED = 3;
    public static boolean isMeOnTopList = false;
//    static int tmp;

    public static ProfileModel myProfile = new ProfileModel("NA", null, null, 0);
    public static void initFirestore() {
        if (g_firestore == null) {
            g_firestore = FirebaseFirestore.getInstance();
        }
    }

    public static void getUserData(MyCompleteListener listener) {
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    myProfile.setName(documentSnapshot.getString("NAME"));
                    myProfile.setEmail(documentSnapshot.getString("EMAIL_ID"));
                    if(documentSnapshot.getString("PHONE") != null) {
                        myProfile.setPhone(documentSnapshot.getString("PHONE"));
                    }
                    if(documentSnapshot.getLong("BOOKMARKS") != null) {
                    myProfile.setBookmarkCount(documentSnapshot.getLong("BOOKMARKS").intValue());
                    }

                    myPerformanece.setScore(documentSnapshot.getLong("TOTAL_SCORE").intValue());
                    myPerformanece.setName(documentSnapshot.getString("NAME"));

                    listener.onSuccess();

                }).addOnFailureListener(e -> {
                    listener.onFailture();
                });
    }
    public static void saveUserData(String name, String phone, MyCompleteListener listener) {
        Map<String, Object> profileData  =new ArrayMap<>();
        profileData.put("NAME", name);
        if (phone != null){
            profileData.put("PHONE", phone);
        }
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .update(profileData)
                .addOnSuccessListener(unused -> {
                    myProfile.setName(name);
                    if(phone != null) {
                        myProfile.setPhone(phone);
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(runnable -> {
                    listener.onFailture();
                        });
    }



    public static void createUserData(String email, String name, MyCompleteListener listener) {
        Map<String, Object> userData = new ArrayMap<>();
        userData.put("EMAIL_ID", email);
        userData.put("NAME", name);
        userData.put("TOTAL_SCORE", 0);
        userData.put("BOOKMARKS", 0);

        DocumentReference userDoc = g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        WriteBatch batch = g_firestore.batch();
        batch.set(userDoc, userData);

        DocumentReference countDoc = g_firestore.collection("USERS").document("TOTAL_USERS");
        batch.update(countDoc, "COUNT", FieldValue.increment(1));
        batch.commit().addOnSuccessListener(unused -> {

                    listener.onSuccess();

                }

        ).addOnFailureListener(e -> {
            listener.onFailture();


        });

    }

    public static void loadBmIds(MyCompleteListener listener){
        g_bmIdList.clear();
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("USER_DATA").document("BOOKMARKS")
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    int  count = myProfile.getBookmarkCount();

                    for(int i = 0; i < count; i++){
                        String bmId = documentSnapshot.getString("BM" + String.valueOf(i + 1));
                        g_bmIdList.add(bmId);
                    }
                    listener.onSuccess();


                })
                .addOnFailureListener(runnable -> {
                    listener.onFailture();

                });
    }

    public static void loadBookMarks(MyCompleteListener listener){
        g_bookmarkList.clear();
        AtomicInteger tmp = new AtomicInteger(0); // Dùng biến cục bộ

        if (g_bmIdList.size() == 0) {
            listener.onSuccess();
            return;
        }

        for (String docID : g_bmIdList) {
            g_firestore.collection("Question").document(docID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            g_bookmarkList.add(new QuestionModel(
                                    documentSnapshot.getId(),
                                    documentSnapshot.getString("QUESTION"),
                                    documentSnapshot.getString("A"),
                                    documentSnapshot.getString("B"),
                                    documentSnapshot.getString("C"),
                                    documentSnapshot.getString("D"),
                                    documentSnapshot.getLong("ANSWER").intValue(),
                                    0,
                                    -1,
                                    false
                            ));
                        }

                        if (tmp.incrementAndGet() == g_bmIdList.size()) {
                            listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> listener.onFailture());
        }
    }


    public static void getTopUsers(MyCompleteListener listener){
        g_userList.clear();
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        g_firestore.collection("USERS")
                .whereGreaterThan("TOTAL_SCORE", 0)
                .orderBy("TOTAL_SCORE", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int rank = 1;
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        g_userList.add(new RankModel(doc.getLong("TOTAL_SCORE").intValue(),
                                rank,
                                doc.getString("NAME")));

                        if(myUID.compareTo(doc.getId()) == 0){
                            isMeOnTopList = true;
                            myPerformanece.setRank(rank);
                        }

                        rank++;
                    }
                    listener.onSuccess();

                })
                .addOnFailureListener(runnable -> {
                    listener.onFailture();

                });


    }

    public static void getUsersCount(MyCompleteListener listener){
        g_firestore.collection("USERS").document("TOTAL_USERS")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    g_userCount = documentSnapshot.getLong("COUNT").intValue();
                    listener.onSuccess();
                })
                .addOnFailureListener(runnable -> {
                    listener.onFailture();
                });

    }

    public static void loadCategories(MyCompleteListener listener) {
        g_categoryList.clear();
        g_firestore.collection("QUIZ").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        docList.put(doc.getId(), doc);

                    }

                    QueryDocumentSnapshot catListDoc = docList.get("Categories");

                    long catCount = catListDoc.getLong("COUNT");
                    for (int i = 1; i <= catCount; i++) {
                        String catID = catListDoc.getString("CAT" + i + "_ID");
                        QueryDocumentSnapshot catDoc = docList.get(catID);

                        int noOfTest = catDoc.getLong("NO_OF_TEST").intValue();
                        String catName = catDoc.getString("NAME");
                        g_categoryList.add(new CategoryModel(catID, catName, noOfTest));

                    }
                    listener.onSuccess();

                }).addOnFailureListener(e -> {
                    listener.onFailture();
                });
    }

    public static void loadTestData(MyCompleteListener listener) {
        if (g_categoryList.isEmpty() || g_selectedCatIndex >= g_categoryList.size()) {
            listener.onFailture();
            return;
        }

        g_testList.clear();

        String catDocId = g_categoryList.get(g_selectedCatIndex).getDocId();

        g_firestore.collection("QUIZ")
                .document(catDocId)
                .collection("TEST_LIST")
                .document("TEST_INFO")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> testsArray = (List<Map<String, Object>>) documentSnapshot.get("TESTs");

                    if (testsArray == null || testsArray.isEmpty()) {
                        listener.onFailture();
                        return;
                    }

                    AtomicInteger loadedCount = new AtomicInteger(0);
                    AtomicInteger totalToCheck = new AtomicInteger(testsArray.size());

                    for (Map<String, Object> test : testsArray) {
                        String testId = (String) test.get("id");
                        int testTime = ((Long) test.get("time")).intValue();

                        g_firestore.collection("Question")
                                .whereEqualTo("CATEGORY", catDocId)
                                .whereEqualTo("TEST", testId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        g_testList.add(new TestModel(testId, 0, testTime));
                                    }

                                    if (loadedCount.incrementAndGet() == totalToCheck.get()) {
                                        if (g_testList.isEmpty()) {
                                            listener.onFailture();
                                        } else {
                                            listener.onSuccess();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (loadedCount.incrementAndGet() == totalToCheck.get()) {
                                        if (g_testList.isEmpty()) {
                                            listener.onFailture();
                                        } else {
                                            listener.onSuccess();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onFailture();
                });
    }

    public static void loadData(MyCompleteListener listener) {
        loadCategories(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                getUserData(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        getUsersCount(new MyCompleteListener() {
                            @Override
                            public void onSuccess() {
                                loadBmIds(listener);


                            }
                            @Override
                            public void onFailture() {
                                listener.onFailture();
                            }
                        });


                    }
                    @Override
                    public void onFailture() {
                        listener.onFailture();
                    }

                });
            }

            @Override
            public void onFailture() {
                listener.onFailture();
            }
        });
    }

    public static void loadQuestions(MyCompleteListener listener) {
        g_questionList.clear();
        g_firestore.collection("Question")
                .whereEqualTo("CATEGORY", g_categoryList.get(g_selectedCatIndex).getDocId())
                .whereEqualTo("TEST", g_testList.get(g_selected_test_index).getTestId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        boolean isBookMarked = false;

                        if(g_bmIdList.contains(doc.getId())){
                            isBookMarked = true;

                        }
                        g_questionList.add(new QuestionModel(
                                doc.getId(),
                                doc.getString("QUESTION"),
                                doc.getString("A"),
                                doc.getString("B"),
                                doc.getString("C"),
                                doc.getString("D"),
                                doc.getLong("ANSWER").intValue(),-1,
                                NOT_VISITED,
                                isBookMarked
                        ));

                    }
                    listener.onSuccess();
                }).addOnFailureListener(e -> {
                    listener.onFailture();
                });

    }
    public static void loadMyScore(MyCompleteListener listener){
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("USER_DATA").document("MY_SCORES")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    for(int i = 0 ; i < g_testList.size(); i++){
                        int top = 0;
                        if(documentSnapshot.get(g_testList.get(i).getTestId()) != null){
                            top = documentSnapshot.getLong(g_testList.get(i).getTestId()).intValue();

                        }
                        g_testList.get(i).setTopScore(top);

                    }
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    listener.onFailture();
                });
    }
    public static void saveResult(int score, MyCompleteListener listener) {
        DocumentReference userDoc = g_firestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DocumentReference scoreDoc = userDoc.collection("USER_DATA").document("MY_SCORES");

        // 1. Cập nhật BOOKMARKS
        Map<String, Object> bmData = new ArrayMap<>();
        for (int i = 0; i < g_bmIdList.size(); i++) {
            bmData.put("BM" + (i + 1), g_bmIdList.get(i));
        }
        DocumentReference bmDoc = userDoc.collection("USER_DATA").document("BOOKMARKS");

        WriteBatch batch = g_firestore.batch();
        batch.set(bmDoc, bmData);

        // 2. Lấy điểm từ Firestore
        scoreDoc.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> testData = new ArrayMap<>();
            AtomicInteger totalScore = new AtomicInteger(0);

            Map<String, Object> allScores = documentSnapshot.getData();
            if (allScores == null) {
                allScores = new HashMap<>();
            }

            // 3. Cập nhật điểm bài test đã từng làm hoặc đang làm
            for (int i = 0; i < g_testList.size(); i++) {
                String testId = g_testList.get(i).getTestId();
                int savedTopScore = 0;
                boolean hasPreviousScore = allScores.containsKey(testId);

                if (hasPreviousScore) {
                    Object value = allScores.get(testId);
                    if (value instanceof Long) {
                        savedTopScore = ((Long) value).intValue();
                    } else if (value instanceof Integer) {
                        savedTopScore = (Integer) value;
                    }
                }

                // Nếu bài đang làm hoặc đã có điểm cũ, thì cập nhật
                if (i == g_selected_test_index || hasPreviousScore) {
                    int newTopScore = (i == g_selected_test_index && score > savedTopScore) ? score : savedTopScore;
                    g_testList.get(i).setTopScore(newTopScore);

                    testData.put(testId, (long) newTopScore);
                    allScores.put(testId, newTopScore);
                }
            }

            // 4. Tính tổng điểm từ tất cả bài đã làm
            for (Object value : allScores.values()) {
                if (value instanceof Long) {
                    totalScore.addAndGet(((Long) value).intValue());
                } else if (value instanceof Integer) {
                    totalScore.addAndGet((Integer) value);
                }
            }

            // 5. Ghi vào Firestore
            Map<String, Object> userData = new ArrayMap<>();
            userData.put("TOTAL_SCORE", totalScore.get());
            userData.put("BOOKMARKS", g_bmIdList.size());

            batch.update(userDoc, userData);
            batch.set(scoreDoc, testData, SetOptions.merge());

            batch.commit().addOnSuccessListener(unused -> {
                myPerformanece.setScore(totalScore.get());
                listener.onSuccess();
            }).addOnFailureListener(e -> listener.onFailture());

        }).addOnFailureListener(e -> listener.onFailture());
    }





}

