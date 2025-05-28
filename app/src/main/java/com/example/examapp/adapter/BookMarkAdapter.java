package com.example.examapp.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.databinding.AnswerItemLayoutBinding;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.viewmodel.BookMarkViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class BookMarkAdapter extends RecyclerView.Adapter<BookMarkAdapter.BookMarkViewHolder> {
    private static final String TAG = "BookMarkAdapter";
    private final BookMarkViewModel viewModel;

    public BookMarkAdapter(BookMarkViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public BookMarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AnswerItemLayoutBinding binding = AnswerItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookMarkViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookMarkViewHolder holder, int position) {
        List<QuestionModel> questionList = viewModel.getBookmarkList().getValue();
        if (questionList != null && position < questionList.size()) {
            QuestionModel question = questionList.get(position);
            holder.bind(position, question, viewModel);
        }
    }

    @Override
    public int getItemCount() {
        List<QuestionModel> questionList = viewModel.getBookmarkList().getValue();
        return questionList != null ? questionList.size() : 0;
    }

    public class BookMarkViewHolder extends RecyclerView.ViewHolder {
        private final AnswerItemLayoutBinding binding;

        public BookMarkViewHolder(@NonNull AnswerItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position, QuestionModel question, BookMarkViewModel viewModel) {
            String categoryName = question.getCategoryName() != null ? question.getCategoryName() : "Unknown";
            String testId = question.getTestId() != null ? question.getTestId() : "Unknown";
            binding.txtQuestionNo.setText(categoryName + " - Test: " + testId);

            binding.txtQuestion.setText(question.getQuestion());
            binding.txtOptionA.setText("A. " + question.getOptionA());
            binding.txtOptionB.setText("B. " + question.getOptionB());
            binding.txtOptionC.setText("C. " + question.getOptionC());
            binding.txtOptionD.setText("D. " + question.getOptionD());

            int correctOption = question.getCorrectOption();
            String correctAnswer = "";
            switch (correctOption) {
                case 1:
                    correctAnswer = question.getOptionA();
                    break;
                case 2:
                    correctAnswer = question.getOptionB();
                    break;
                case 3:
                    correctAnswer = question.getOptionC();
                    break;
                case 4:
                    correctAnswer = question.getOptionD();
                    break;
            }
            binding.txtResult.setText("Answer: " + correctAnswer);
            binding.txtResult.setTextColor(itemView.getContext().getColor(R.color.greenLight));

            binding.btnBookmark.setImageResource(question.isBookMarked() ? R.drawable.ic_bookmark_new : R.drawable.ic_bookmark_border);
            binding.btnBookmark.setColorFilter(ContextCompat.getColor(
                    itemView.getContext(), question.isBookMarked() ? R.color.colorPrimary : R.color.gray_light));

            binding.btnBookmark.setOnClickListener(v -> {
                Log.d(TAG, "Unbookmarking question at position: " + position + ", questionId: " + question.getQuestionId());
                QuestionModel removedQuestion = new QuestionModel(
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
                );
                viewModel.updateBookmark(position, false); // Unbookmark the question
                BookMarkAdapter.this.notifyItemRemoved(position);
                BookMarkAdapter.this.notifyItemRangeChanged(position, getItemCount());

                Snackbar snackbar = Snackbar.make(
                        binding.getRoot(),
                        "Unmarked question!",
                        Snackbar.LENGTH_LONG
                );
                snackbar.setAction("Yes", view -> {
                    Log.d(TAG, "Undo unmarked for questionId: " + removedQuestion.getQuestionId());
                    viewModel.rebookmarkQuestion(removedQuestion);
                    BookMarkAdapter.this.notifyItemInserted(position);
                    BookMarkAdapter.this.notifyItemRangeChanged(position, getItemCount());
                });
                snackbar.show();
            });
        }
    }
}