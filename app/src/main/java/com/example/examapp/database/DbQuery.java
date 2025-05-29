package com.example.examapp.database;

import android.util.ArrayMap;

import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.CategoryModel;
import com.example.examapp.model.ProfileModel;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.model.RankModel;
import com.example.examapp.model.TestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
    public static List<String> g_bmIdList = new ArrayList<>();
    public static List<QuestionModel> g_bookmarkList = new ArrayList<>();
    public static List<QuestionModel> g_questionList = new ArrayList<>();
    public static int g_selectedCatIndex = 0;
    public static List<RankModel> g_userList = new ArrayList<>();
    public static int g_userCount = 0;
    public static RankModel myPerformanece = new RankModel(0, 0, "");
    public static final int NOT_VISITED = 0;
    public static final int UNANSWERED = 1;
    public static final int ANSWERED = 2;
    public static final int HIGHTLIGHTED = 3;
    public static boolean isMeOnTopList = false;
    public static ProfileModel myProfile = new ProfileModel("NA", null, null, 0);

    public static void initFirestore() {
        if (g_firestore == null) {
            g_firestore = FirebaseFirestore.getInstance();
        }
    }

    private static void ensureFirestoreInitialized() {
        if (g_firestore == null) {
            g_firestore = FirebaseFirestore.getInstance();
        }
    }

    public static void getUserData(MyCompleteListener listener) {
        ensureFirestoreInitialized();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            myProfile = new ProfileModel("NA", null, null, 0);
            myPerformanece = new RankModel(0, 0, "");
            listener.onFailture();
            return;
        }

        g_firestore.collection("USERS").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    myProfile.setName(documentSnapshot.getString("NAME") != null ? documentSnapshot.getString("NAME") : "NA");
                    myProfile.setEmail(documentSnapshot.getString("EMAIL_ID"));
                    if (documentSnapshot.getString("PHONE") != null) {
                        myProfile.setPhone(documentSnapshot.getString("PHONE"));
                    }
                    if (documentSnapshot.getLong("BOOKMARKS") != null) {
                        myProfile.setBookmarkCount(documentSnapshot.getLong("BOOKMARKS").intValue());
                    }
                    int score = documentSnapshot.getLong("TOTAL_SCORE") != null ?
                            documentSnapshot.getLong("TOTAL_SCORE").intValue() : 0;
                    myPerformanece = new RankModel(score, 0, myProfile.getName());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    myProfile = new ProfileModel("NA", null, null, 0);
                    myPerformanece = new RankModel(0, 0, "");
                    listener.onFailture();
                });
    }

    public static void saveUserData(String name, MyCompleteListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            listener.onFailture();
            return;
        }

        Map<String, Object> profileData = new ArrayMap<>();
        profileData.put("NAME", name);
        ensureFirestoreInitialized();
        g_firestore.collection("USERS").document(userId)
                .update(profileData)
                .addOnSuccessListener(unused -> {
                    myProfile.setName(name);
                    myPerformanece.setName(name);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailture());
    }

    public static void createUserData(String email, String name, MyCompleteListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            listener.onFailture();
            return;
        }

        Map<String, Object> userData = new ArrayMap<>();
        userData.put("EMAIL_ID", email);
        userData.put("NAME", name);
        userData.put("TOTAL_SCORE", 0);
        userData.put("BOOKMARKS", 0);

        ensureFirestoreInitialized();
        DocumentReference userDoc = g_firestore.collection("USERS").document(userId);
        WriteBatch batch = g_firestore.batch();
        batch.set(userDoc, userData);

        DocumentReference countDoc = g_firestore.collection("USERS").document("TOTAL_USERS");
        batch.update(countDoc, "COUNT", FieldValue.increment(1));
        batch.commit()
                .addOnSuccessListener(unused -> {
                    myProfile = new ProfileModel(name, email, null, 0);
                    myPerformanece = new RankModel(0, 0, name);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailture());
    }

    public static void loadBmIds(MyCompleteListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            g_bmIdList.clear();
            listener.onFailture();
            return;
        }

        ensureFirestoreInitialized();
        g_bmIdList.clear();
        g_firestore.collection("USERS").document(userId)
                .collection("USER_DATA").document("BOOKMARKS")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    int count = myProfile.getBookmarkCount();
                    for (int i = 0; i < count; i++) {
                        String bmId = documentSnapshot.getString("BM" + String.valueOf(i + 1));
                        if (bmId != null) {
                            g_bmIdList.add(bmId);
                        }
                    }
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailture());
    }

    public static void loadBookMarks(MyCompleteListener listener) {
        g_bookmarkList.clear();
        AtomicInteger tmp = new AtomicInteger(0);

        if (g_bmIdList.size() == 0) {
            listener.onSuccess();
            return;
        }

        ensureFirestoreInitialized();
        for (String docID : g_bmIdList) {
            g_firestore.collection("Question").document(docID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String categoryId = documentSnapshot.getString("CATEGORY");
                            String testId = documentSnapshot.getString("TEST");
                            String categoryName = "Unknown";
                            for (CategoryModel category : g_categoryList) {
                                if (category.getDocId().equals(categoryId)) {
                                    categoryName = category.getName();
                                    break;
                                }
                            }
                            g_bookmarkList.add(new QuestionModel(
                                    categoryName,
                                    testId,
                                    documentSnapshot.getId(),
                                    documentSnapshot.getString("QUESTION"),
                                    documentSnapshot.getString("A"),
                                    documentSnapshot.getString("B"),
                                    documentSnapshot.getString("C"),
                                    documentSnapshot.getString("D"),
                                    documentSnapshot.getLong("ANSWER").intValue(),
                                    -1,
                                    NOT_VISITED,
                                    true
                            ));
                        }
                        if (tmp.incrementAndGet() == g_bmIdList.size()) {
                            listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> listener.onFailture());
        }
    }

    public static void getTopUsers(MyCompleteListener listener) {
        ensureFirestoreInitialized();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            g_userList.clear();
            myPerformanece = new RankModel(0, 0, "");
            isMeOnTopList = false;
            listener.onFailture();
            return;
        }

        g_firestore.collection("USERS")
                .whereGreaterThan("TOTAL_SCORE", 0)
                .orderBy("TOTAL_SCORE", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    g_userList.clear();
                    isMeOnTopList = false;
                    int rank = 1;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        g_userList.add(new RankModel(
                                doc.getLong("TOTAL_SCORE").intValue(),
                                rank,
                                doc.getString("NAME")
                        ));
                        if (userId.equals(doc.getId())) {
                            isMeOnTopList = true;
                            myPerformanece.setRank(rank);
                        }
                        rank++;
                    }

                    if (!isMeOnTopList) {
                        g_firestore.collection("USERS").document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    int score = documentSnapshot.getLong("TOTAL_SCORE") != null ?
                                            documentSnapshot.getLong("TOTAL_SCORE").intValue() : 0;
                                    String name = documentSnapshot.getString("NAME") != null ?
                                            documentSnapshot.getString("NAME") : "";
                                    myPerformanece = new RankModel(score, 0, name);
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    myPerformanece = new RankModel(0, 0, myProfile.getName());
                                    listener.onSuccess();
                                });
                    } else {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    g_userList.clear();
                    myPerformanece = new RankModel(0, 0, myProfile.getName());
                    listener.onSuccess();
                });
    }

    public static void getUsersCount(MyCompleteListener listener) {
        ensureFirestoreInitialized();
        g_firestore.collection("USERS").document("TOTAL_USERS")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    g_userCount = documentSnapshot.getLong("COUNT") != null ?
                            documentSnapshot.getLong("COUNT").intValue() : 0;
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailture());
    }

    public static void loadCategories(MyCompleteListener listener) {
        ensureFirestoreInitialized();
        g_firestore.collection("QUIZ").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    g_categoryList.clear();
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
                        if (noOfTest > 0) { // Only add categories with NO_OF_TEST > 0
                            String catName = catDoc.getString("NAME");
                            g_categoryList.add(new CategoryModel(catID, catName, noOfTest));
                        }
                    }
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailture());
    }

    public static void loadTestData(MyCompleteListener listener) {
        if (g_categoryList.isEmpty() || g_selectedCatIndex >= g_categoryList.size()) {
            listener.onFailture();
            return;
        }

        g_testList.clear();
        String catDocId = g_categoryList.get(g_selectedCatIndex).getDocId();

        ensureFirestoreInitialized();
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
                .addOnFailureListener(e -> listener.onFailture());
    }

    public static void loadData(MyCompleteListener listener) {
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

    public static void loadQuestions(MyCompleteListener listener) {
        g_questionList.clear();
        ensureFirestoreInitialized();
        String categoryName = g_categoryList.get(g_selectedCatIndex).getName();
        String testId = g_testList.get(g_selected_test_index).getTestId();
        g_firestore.collection("Question")
                .whereEqualTo("CATEGORY", g_categoryList.get(g_selectedCatIndex).getDocId())
                .whereEqualTo("TEST", testId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        boolean isBookMarked = g_bmIdList.contains(doc.getId());
                        g_questionList.add(new QuestionModel(
                                categoryName,
                                testId,
                                doc.getId(),
                                doc.getString("QUESTION"),
                                doc.getString("A"),
                                doc.getString("B"),
                                doc.getString("C"),
                                doc.getString("D"),
                                doc.getLong("ANSWER").intValue(),
                                -1,
                                NOT_VISITED,
                                isBookMarked
                        ));
                    }
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailture());
    }

    public static void loadMyScore(MyCompleteListener listener) {
        ensureFirestoreInitialized();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            for (int i = 0; i < g_testList.size(); i++) {
                g_testList.get(i).setTopScore(0);
            }
            listener.onFailture();
            return;
        }

        DocumentReference scoreDoc = g_firestore.collection("USERS")
                .document(userId)
                .collection("USER_DATA").document("MY_SCORES");

        scoreDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                for (int i = 0; i < g_testList.size(); i++) {
                    g_testList.get(i).setTopScore(0);
                }
                listener.onSuccess();
                return;
            }

            Map<String, Object> allScores = documentSnapshot.getData();
            if (allScores == null) {
                allScores = new HashMap<>();
            }

            for (int i = 0; i < g_testList.size(); i++) {
                String testId = g_testList.get(i).getTestId();
                int topScore = 0;
                if (allScores.containsKey(testId)) {
                    Object value = allScores.get(testId);
                    if (value instanceof Long) {
                        topScore = ((Long) value).intValue();
                    } else if (value instanceof Integer) {
                        topScore = (Integer) value;
                    }
                }
                g_testList.get(i).setTopScore(topScore);
            }
            listener.onSuccess();
        }).addOnFailureListener(e -> {
            for (int i = 0; i < g_testList.size(); i++) {
                g_testList.get(i).setTopScore(0);
            }
            listener.onFailture();
        });
    }

    public static void saveResult(int score, MyCompleteListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            listener.onFailture();
            return;
        }

        ensureFirestoreInitialized();
        DocumentReference userDoc = g_firestore.collection("USERS").document(userId);
        DocumentReference scoreDoc = userDoc.collection("USER_DATA").document("MY_SCORES");

        Map<String, Object> bmData = new ArrayMap<>();
        for (int i = 0; i < g_bmIdList.size(); i++) {
            bmData.put("BM" + (i + 1), g_bmIdList.get(i));
        }
        DocumentReference bmDoc = userDoc.collection("USER_DATA").document("BOOKMARKS");

        WriteBatch batch = g_firestore.batch();
        batch.set(bmDoc, bmData);

        scoreDoc.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> testData = new ArrayMap<>();
            AtomicInteger totalScore = new AtomicInteger(0);

            Map<String, Object> allScores = documentSnapshot.getData();
            if (allScores == null) {
                allScores = new HashMap<>();
            }

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

                if (i == g_selected_test_index || hasPreviousScore) {
                    int newTopScore = (i == g_selected_test_index && score > savedTopScore) ? score : savedTopScore;
                    g_testList.get(i).setTopScore(newTopScore);
                    testData.put(testId, (long) newTopScore);
                    allScores.put(testId, newTopScore);
                }
            }

            for (Object value : allScores.values()) {
                if (value instanceof Long) {
                    totalScore.addAndGet(((Long) value).intValue());
                } else if (value instanceof Integer) {
                    totalScore.addAndGet((Integer) value);
                }
            }

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