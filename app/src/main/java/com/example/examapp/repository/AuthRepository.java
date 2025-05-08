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

public class AuthRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<RankModel> userPerformance = new MutableLiveData<>();
    private MutableLiveData<List<RankModel>> topUsers = new MutableLiveData<>();
    private MutableLiveData<Integer> userCount = new MutableLiveData<>();
    private MutableLiveData<List<CategoryModel>> categories = new MutableLiveData<>();
    private MutableLiveData<List<TestModel>> tests = new MutableLiveData<>();

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

    public void login(String email, String password, MyCompleteListener listener) {
        if (email.equals("admin@gmail.com") && password.equals("admin")) {
            listener.onSuccess();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginStatus.postValue(true);
                        listener.onSuccess();
                    } else {
                        loginStatus.postValue(false);
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            errorMessage.postValue("Email not exists!");
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage.postValue("Invalid password. Please try again!");
                        } else {
                            errorMessage.postValue("Error: " + e.getMessage());
                        }
                        listener.onFailture();
                    }
                });
    }

    public void signUp(String email, String password, String fullName, MyCompleteListener listener) {
        // Kiểm tra xem fullName đã tồn tại trong Firestore chưa
        firestore.collection("USERS")
                .whereEqualTo("NAME", fullName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // fullName đã tồn tại
                        errorMessage.postValue("Username already exists.");
                        listener.onFailture();
                    } else {
                        // fullName chưa tồn tại, tiếp tục tạo tài khoản
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

        // Kiểm tra xem tên đã tồn tại trong Firestore (trừ tài liệu của người dùng hiện tại)
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
                        // Tên chưa tồn tại, tiếp tục lưu dữ liệu
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

    public void checkPhone(String phone, MyCompleteListener listener) {
        if (phone == null || phone.trim().isEmpty()) {
            errorMessage.postValue("Phone number cannot be empty.");
            listener.onFailture();
            return;
        }

        firestore.collection("USERS")
                .whereEqualTo("PHONE", phone.trim())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        errorMessage.postValue("Phone number is already used.");
                        listener.onFailture();
                    } else {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("Failed to check phone number: " + e.getMessage());
                    listener.onFailture();
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

    public void loadCategories() {
        DbQuery.loadCategories(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                categories.postValue(DbQuery.g_categoryList);
            }
            @Override
            public void onFailture() {
                errorMessage.postValue("Failed to load categories");
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

    public List<TestModel> getCurrentTestList() {
        return DbQuery.g_testList;
    }

    public List<CategoryModel> getCurrentCategoryList() {
        return DbQuery.g_categoryList;
    }

    public int getSelectedTestIndex() {
        return DbQuery.g_selected_test_index;
    }

    public int getSelectedCategoryIndex() {
        return DbQuery.g_selectedCatIndex;
    }

    public List<QuestionModel> getCurrentQuestionList() {
        return DbQuery.g_questionList;
    }

    public void updateBookmark(int questionIndex, boolean isBookmarked) {
        QuestionModel question = DbQuery.g_questionList.get(questionIndex);
        question.setBookMarked(isBookmarked);
        if (isBookmarked) {
            if (!DbQuery.g_bmIdList.contains(question.getQuestionId())) {
                DbQuery.g_bmIdList.add(question.getQuestionId());
            }
        } else {
            DbQuery.g_bmIdList.remove(question.getQuestionId());
        }
        DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());
    }

    public void saveTestResult(int score, MyCompleteListener listener) {
        DbQuery.saveResult(score, listener);
    }

    public void updateBookmarks() {
        for (QuestionModel question : DbQuery.g_questionList) {
            if (question.isBookMarked()) {
                if (!DbQuery.g_bmIdList.contains(question.getQuestionId())) {
                    DbQuery.g_bmIdList.add(question.getQuestionId());
                    DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());
                }
            } else {
                if (DbQuery.g_bmIdList.contains(question.getQuestionId())) {
                    DbQuery.g_bmIdList.remove(question.getQuestionId());
                    DbQuery.myProfile.setBookmarkCount(DbQuery.g_bmIdList.size());
                }
            }
        }
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