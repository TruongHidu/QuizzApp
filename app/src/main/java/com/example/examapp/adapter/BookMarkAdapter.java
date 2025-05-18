package com.example.examapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.databinding.AnswerItemLayoutBinding;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.viewmodel.BookMarkViewModel;

import java.util.List;

public class BookMarkAdapter extends RecyclerView.Adapter<BookMarkAdapter.BookMarkViewHolder> {
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

    public static class BookMarkViewHolder extends RecyclerView.ViewHolder {
        private final AnswerItemLayoutBinding binding;

        public BookMarkViewHolder(@NonNull AnswerItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position, QuestionModel question, BookMarkViewModel viewModel) {
            binding.txtQuestionNo.setText("Question: " + (position + 1));
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

            // Handle bookmark
            binding.btnBookmark.setImageResource(R.drawable.ic_bookmark_new);
            binding.btnBookmark.setColorFilter(ContextCompat.getColor(
                    itemView.getContext(), R.color.colorPrimary));
//            binding.btnBookmark.setOnClickListener(v -> {
//                boolean newBookmarkState = !question.isBookMarked();
//                viewModel.updateBookmark(position, newBookmarkState);
//                question.setBookMarked(newBookmarkState);
//                binding.btnBookmark.setImageResource(newBookmarkState ? R.drawable.ic_bookmark_new : R.drawable.ic_bookmark_border);
//                binding.btnBookmark.setColorFilter(ContextCompat.getColor(
//                        itemView.getContext(), newBookmarkState ? R.color.colorPrimary : R.color.gray_light));
//            });
        }
    }
}