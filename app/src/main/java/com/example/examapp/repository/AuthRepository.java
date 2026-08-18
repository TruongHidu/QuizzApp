package com.example.examapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.examapp.database.DbQuery;
import com.example.examapp.handlerlistener.MyCompleteListener;
import com.example.examapp.model.CategoryModel;
import com.example.examapp.model.ProfileModel;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.model.RankModel;
import com.example.examapp.model.TestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.util.ArrayMap;
import android.util.Log;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<ProfileModel> userProfile = new MutableLiveData<>();
    private MutableLiveData<RankModel> userPerformance = new MutableLiveData<>();
    private MutableLiveData<List<RankModel>> topUsers = new MutableLiveData<>();
    private MutableLiveData<Integer> userCount = new MutableLiveData<>();
    private MutableLiveData<List<CategoryModel>> categories = new MutableLiveData<>();
    private MutableLiveData<List<TestModel>> tests = new MutableLiveData<>();

    // Variables for tracking failed login attempts
    private int failedLoginAttempts = 0;
    private long blockTimestamp = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<Boolean> getLoginStatus() {
        return loginStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<ProfileModel> getUserProfile() {
        return userProfile;
    }

    public LiveData<RankModel> getUserPerformance() {
        return userPerformance;
    }

    public LiveData<List<RankModel>> getTopUsers() {
        return topUsers;
    }

    public LiveData<Integer> getUserCount() {
        return userCount;
    }

    public LiveData<List<CategoryModel>> getCategories() {
        return categories;
    }

    public LiveData<List<TestModel>> getTests() {
        return tests;
    }

    public void getUserData(MyCompleteListener listener) {
        DbQuery.getUserData(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                userProfile.postValue(new ProfileModel(
                        DbQuery.myProfile.getName(),
                        DbQuery.myProfile.getEmail(),
                        DbQuery.myProfile.getPhone(),
                        DbQuery.myProfile.getBookmarkCount()
                ));
                listener.onSuccess();
            }

            @Override
            public void onFailture() {
                userProfile.postValue(new ProfileModel("NA", null, null, 0));
                errorMessage.postValue("Failed to load user data");
                listener.onFailture();
            }
        });
    }

    public void login(String email, String password, MyCompleteListener listener) {
        // Check if user is blocked
        long currentTime = System.currentTimeMillis();
        if (failedLoginAttempts >= MAX_LOGIN_ATTEMPTS && (currentTime - blockTimestamp) < BLOCK_DURATION_MS) {
            long remainingTime = (BLOCK_DURATION_MS - (currentTime - blockTimestamp)) / 1000;
            errorMessage.postValue("Too many failed attempts. Please try again after " + remainingTime + " seconds.");
            listener.onFailture();
            return;
        }

        // Reset attempts if block duration has expired
        if (failedLoginAttempts >= MAX_LOGIN_ATTEMPTS && (currentTime - blockTimestamp) >= BLOCK_DURATION_MS) {
            failedLoginAttempts = 0;
            blockTimestamp = 0;
        }

        if (email.equals("admin@gmail.com") && password.equals("admin")) {
            failedLoginAttempts = 0; // Reset attempts on successful admin login
            listener.onSuccess();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        failedLoginAttempts = 0; // Reset attempts on successful login
                        loginStatus.postValue(true);
                        listener.onSuccess();
                    } else {
                        failedLoginAttempts++;
                        if (failedLoginAttempts >= MAX_LOGIN_ATTEMPTS) {
                            blockTimestamp = System.currentTimeMillis();
                            errorMessage.postValue("Too many failed attempts. Please try again after 5 minutes.");
                        } else {
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                errorMessage.postValue("Email not exists!");
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage.postValue("Invalid password. Please try again! (" + (MAX_LOGIN_ATTEMPTS - failedLoginAttempts) + " attempts left)");
                            } else {
                                errorMessage.postValue("Error: " + e.getMessage());
                            }
                        }
                        loginStatus.postValue(false);
                        listener.onFailture();
                    }
                });
    }

    public void signUp(String email, String password, String fullName, MyCompleteListener listener) {
        firestore.collection("USERS")
                .whereEqualTo("NAME", fullName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        errorMessage.postValue("Username already exists.");
                        listener.onFailture();
                    } else {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DbQuery.createUserData(email, fullName, new MyCompleteListener() {
                                            @Override
                                            public void onSuccess() {
                                                listener.onSuccess();
                                            }
                                            @Override
                                            public void onFailture() {
                                                errorMessage.postValue("Failed to save user data");
                                                listener.onFailture();
                                            }
                                        });
                                    } else {
                                        Exception e = task.getException();
                                        if (e instanceof FirebaseAuthWeakPasswordException) {
                                            errorMessage.postValue("Password is too weak. Use at least 6 characters.");
                                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                            errorMessage.postValue("Invalid email format.");
                                        } else if (e instanceof FirebaseAuthUserCollisionException) {
                                            errorMessage.postValue("Email already exists.");
                                        } else {
                                            errorMessage.postValue("Sign-up failed: " + e.getMessage());
                                        }
                                        listener.onFailture();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("Failed to check username: " + e.getMessage());
                    listener.onFailture();
                });
    }

    public void resetPassword(String email, MyCompleteListener listener) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.postValue("Email cannot be empty.");
            listener.onFailture();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.postValue("Invalid email format.");
            listener.onFailture();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        errorMessage.postValue("Password reset email sent. Check your inbox.");
                        listener.onSuccess();
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            errorMessage.postValue("Email not registered.");
                        } else {
                            errorMessage.postValue("Failed to send reset email: " + e.getMessage());
                        }
                        listener.onFailture();
                    }
                });
    }

    public void loadData(MyCompleteListener listener) {
        DbQuery.loadData(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                categories.postValue(DbQuery.g_categoryList);
                listener.onSuccess();
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Something went wrong! Please try again later.");
                listener.onFailture();
            }
        });
    }

    public void saveUserData(String name, MyCompleteListener listener) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            errorMessage.postValue("User not logged in.");
            listener.onFailture();
            return;
        }

        firestore.collection("USERS")
                .whereEqualTo("NAME", name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean nameExists = false;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (!doc.getId().equals(userId)) {
                            nameExists = true;
                            break;
                        }
                    }
                    if (nameExists) {
                        errorMessage.postValue("Username already exists.");
                        listener.onFailture();
                    } else {
                        DbQuery.saveUserData(name, new MyCompleteListener() {
                            @Override
                            public void onSuccess() {
                                listener.onSuccess();
                            }
                            @Override
                            public void onFailture() {
                                errorMessage.postValue("Failed to save profile data. Please try again.");
                                listener.onFailture();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("Failed to check username: " + e.getMessage());
                    listener.onFailture();
                });
    }

    public void loadTopUsers() {
        if (mAuth.getCurrentUser() == null) {
            userPerformance.postValue(null);
            topUsers.postValue(null);
            return;
        }
        DbQuery.getTopUsers(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                topUsers.postValue(DbQuery.g_userList);
                userPerformance.postValue(DbQuery.myPerformanece != null ? DbQuery.myPerformanece : new RankModel(0, 0, ""));
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Failed to load leaderboard");
                userPerformance.postValue(new RankModel(0, 0, ""));
            }
        });
    }

    public void checkName(String name, String currentUserId, MyCompleteListener listener) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.postValue("Username cannot be empty.");
            listener.onFailture();
            return;
        }

        firestore.collection("USERS")
                .whereEqualTo("NAME", name.trim())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean nameExists = false;
                    for (var doc : queryDocumentSnapshots) {
                        if (!doc.getId().equals(currentUserId)) {
                            nameExists = true;
                            break;
                        }
                    }
                    if (nameExists) {
                        errorMessage.postValue("Username already exists.");
                        listener.onFailture();
                    } else {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("Failed to check username: " + e.getMessage());
                    listener.onFailture();
                });
    }

    public void loadUserCount() {
        DbQuery.getUsersCount(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                userCount.postValue(DbQuery.g_userCount);
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Failed to load user count");
            }
        });
    }

    public void loadCategories(MyCompleteListener listener) {
        DbQuery.loadCategories(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                categories.postValue(DbQuery.g_categoryList);
                listener.onSuccess();
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Failed to load categories");
                listener.onFailture();
            }
        });
    }

    public void loadTestData() {
        DbQuery.loadTestData(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                DbQuery.loadMyScore(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        tests.postValue(DbQuery.g_testList);
                    }
                    @Override
                    public void onFailture() {
                        errorMessage.postValue("Failed to load scores. Please try again.");
                    }
                });
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Don't have any test of this Category!");
            }
        });
    }

    public void loadQuestions(MyCompleteListener listener) {
        DbQuery.loadQuestions(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Failed to load questions");
                listener.onFailture();
            }
        });
    }

    public void loadBmIds(MyCompleteListener listener) {
        DbQuery.loadBmIds(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Failed to load bookmark IDs");
                listener.onFailture();
            }
        });
    }

    public void loadBookMarks(MyCompleteListener listener) {
        DbQuery.loadCategories(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                DbQuery.loadBmIds(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        DbQuery.loadBookMarks(new MyCompleteListener() {
                            @Override
                            public void onSuccess() {
                                listener.onSuccess();
                            }
                            @Override
                            public void onFailture() {
                                errorMessage.postValue("Failed to load bookmarks");
                                listener.onFailture();
                            }
                        });
                    }
                    @Override
                    public void onFailture() {
                        errorMessage.postValue("Failed to load bookmark IDs");
                        listener.onFailture();
                    }
                });
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Failed to load categories");
                listener.onFailture();
            }
        });
    }

    public List<QuestionModel> getBookmarkList() {
        return DbQuery.g_bookmarkList;
    }

    public List<TestModel> getCurrentTestList() {
        return DbQuery.g_testList;
    }

    public List<CategoryModel> getCurrentCategoryList() {
        return DbQuery.g_categoryList;
    }

    public int getSelectedTestIndex() {
        return DbQuery.g_selected_test_index;
    }

    public void setSelectedTestIndex(int index) {
        DbQuery.g_selected_test_index = index;
    }

    public int getSelectedCategoryIndex() {
        return DbQuery.g_selectedCatIndex;
    }

    public List<QuestionModel> getCurrentQuestionList() {
        return DbQuery.g_questionList;
    }

    public void updateBookmark(int questionIndex, boolean isBookmarked) {
        if (questionIndex >= 0 && questionIndex < DbQuery.g_questionList.size()) {
            QuestionModel question = DbQuery.g_questionList.get(questionIndex);
            question.setBookMarked(isBookmarked);
            String questionId = question.getQuestionId();
            if (isBookmarked) {
                if (!DbQuery.g_bmIdList.contains(questionId)) {
                    DbQuery.g_bmIdList.add(questionId);
                    DbQuery.g_bookmarkList.add(new QuestionModel(
                            question.getCategoryName(),
                            question.getTestId(),
                            question.getQuestionId(),
                            question.getQuestion(),
                            question.getOptionA(),
                            question.getOptionB(),
                            question.getOptionC(),
                            question.getOptionD(),
                            question.getCorrectOption(),
                            question.getSelectedOption(),
                            question.getStatus(),
                            true
                    ));
                }
            } else {
                DbQuery.g_bmIdList.remove(questionId);
                for (int i = 0; i < DbQuery.g_bookmarkList.size(); i++) {
                    if (DbQuery.g_bookmarkList.get(i).getQuestionId().equals(questionId)) {
                        DbQuery.g_bookmarkList.remove(i);
                        break;
                    }
                }
            }
            DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());
            updateBookmarkInFirestore();
        }
    }

    public void unbookmarkByQuestionId(String questionId) {
        Log.d(TAG, "Unbookmark questionId: " + questionId);
        // Update g_questionList
        for (QuestionModel question : DbQuery.g_questionList) {
            if (question.getQuestionId().equals(questionId)) {
                question.setBookMarked(false);
                break;
            }
        }
        // Update g_bmIdList and g_bookmarkList
        DbQuery.g_bmIdList.remove(questionId);
        for (int i = 0; i < DbQuery.g_bookmarkList.size(); i++) {
            if (DbQuery.g_bookmarkList.get(i).getQuestionId().equals(questionId)) {
                DbQuery.g_bookmarkList.remove(i);
                break;
            }
        }
        DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());
        updateBookmarkInFirestore();
    }

    public void rebookmarkByQuestionId(QuestionModel question) {
        Log.d(TAG, "Rebookmark questionId: " + question.getQuestionId());
        String questionId = question.getQuestionId();
        // Update g_questionList
        for (QuestionModel q : DbQuery.g_questionList) {
            if (q.getQuestionId().equals(questionId)) {
                q.setBookMarked(true);
                break;
            }
        }
        // Update g_bmIdList and g_bookmarkList
        if (!DbQuery.g_bmIdList.contains(questionId)) {
            DbQuery.g_bmIdList.add(questionId);
            DbQuery.g_bookmarkList.add(new QuestionModel(
                    question.getCategoryName(),
                    question.getTestId(),
                    question.getQuestionId(),
                    question.getQuestion(),
                    question.getOptionA(),
                    question.getOptionB(),
                    question.getOptionC(),
                    question.getOptionD(),
                    question.getCorrectOption(),
                    question.getSelectedOption(),
                    question.getStatus(),
                    true
            ));
        }
        DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());
        updateBookmarkInFirestore();
    }

    private void updateBookmarkInFirestore() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.w(TAG, "User not logged in, skipping Firestore update");
            return;
        }
        Map<String, Object> bmData = new ArrayMap<>();
        for (int i = 0; i < DbQuery.g_bmIdList.size(); i++) {
            bmData.put("BM" + (i + 1), DbQuery.g_bmIdList.get(i));
        }
        firestore.collection("USERS").document(userId)
                .collection("USER_DATA").document("BOOKMARKS")
                .set(bmData)
                .addOnSuccessListener(aVoid -> {
                    firestore.collection("USERS").document(userId)
                            .update("BOOKMARKS", DbQuery.g_bmIdList.size());
                    Log.d(TAG, "Firestore bookmark update successful");
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("Failed to update bookmark: " + e.getMessage());
                    Log.e(TAG, "Firestore bookmark update failed: " + e.getMessage());
                });
    }

    public void saveTestResult(int score, MyCompleteListener listener) {
        DbQuery.saveResult(score, listener);
    }

    public void updateBookmarks() {
        for (QuestionModel question : DbQuery.g_questionList) {
            if (question.isBookMarked()) {
                if (!DbQuery.g_bmIdList.contains(question.getQuestionId())) {
                    DbQuery.g_bmIdList.add(question.getQuestionId());
                    DbQuery.g_bookmarkList.add(new QuestionModel(
                            question.getCategoryName(),
                            question.getTestId(),
                            question.getQuestionId(),
                            question.getQuestion(),
                            question.getOptionA(),
                            question.getOptionB(),
                            question.getOptionC(),
                            question.getOptionD(),
                            question.getCorrectOption(),
                            question.getSelectedOption(),
                            question.getStatus(),
                            true
                    ));
                }
            } else {
                if (DbQuery.g_bmIdList.contains(question.getQuestionId())) {
                    DbQuery.g_bmIdList.remove(question.getQuestionId());
                    for (int i = 0; i < DbQuery.g_bookmarkList.size(); i++) {
                        if (DbQuery.g_bookmarkList.get(i).getQuestionId().equals(question.getQuestionId())) {
                            DbQuery.g_bookmarkList.remove(i);
                            break;
                        }
                    }
                }
            }
        }
        DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());
        updateBookmarkInFirestore();
    }

    public ScoreResult calculateScore() {
        int correctQ = 0;
        int wrongQ = 0;
        int unAttemptQ = 0;
        int totalQuestions = DbQuery.g_questionList.size();

        for (QuestionModel question : DbQuery.g_questionList) {
            if (question.getSelectedOption() == -1) {
                unAttemptQ++;
            } else {
                if (question.getSelectedOption() == question.getCorrectOption()) {
                    correctQ++;
                } else {
                    wrongQ++;
                }
            }
        }
        int finalScore = (correctQ * 100) / totalQuestions;
        return new ScoreResult(finalScore, correctQ, wrongQ, unAttemptQ, totalQuestions);
    }

    public void clearData() {
        loginStatus.setValue(null);
        errorMessage.setValue(null);
        userProfile.setValue(null);
        userPerformance.setValue(null);
        topUsers.setValue(null);
        userCount.setValue(null);
        categories.setValue(null);
        tests.setValue(null);
        DbQuery.myProfile = new ProfileModel("NA", null, null, 0);
        DbQuery.myPerformanece = new RankModel(0, 0, "");
        DbQuery.g_userList = new ArrayList<>();
        DbQuery.g_userCount = 0;
        DbQuery.g_categoryList = new ArrayList<>();
        DbQuery.g_testList = new ArrayList<>();
        DbQuery.g_questionList = new ArrayList<>();
        DbQuery.g_bmIdList.clear();
        DbQuery.g_bookmarkList.clear();
        DbQuery.g_selectedCatIndex = 0;
        DbQuery.g_selected_test_index = 0;
        // Reset login attempt counter
        failedLoginAttempts = 0;
        blockTimestamp = 0;
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public static class ScoreResult {
        public final int finalScore;
        public final int correctQuestions;
        public final int wrongQuestions;
        public final int unAttemptedQuestions;
        public final int totalQuestions;

        public ScoreResult(int finalScore, int correctQuestions, int wrongQuestions, int unAttemptedQuestions, int totalQuestions) {
            this.finalScore = finalScore;
            this.correctQuestions = correctQuestions;
            this.wrongQuestions = wrongQuestions;
            this.unAttemptedQuestions = unAttemptedQuestions;
            this.totalQuestions = totalQuestions;
        }
    }
}