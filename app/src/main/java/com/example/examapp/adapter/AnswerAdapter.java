package com.example.examapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.databinding.AnswerItemLayoutBinding;
import com.example.examapp.model.QuestionModel;
import com.example.examapp.viewmodel.TestViewModel;

import java.util.List;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder> {
    private final List<QuestionModel> questionList;
    private final TestViewModel viewModel;

    public AnswerAdapter(List<QuestionModel> questionList, TestViewModel viewModel) {
        this.questionList = questionList;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AnswerItemLayoutBinding binding = AnswerItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AnswerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        QuestionModel question = questionList.get(position);
        holder.bind(position, question, viewModel);
    }

    @Override
    public int getItemCount() {
        return questionList != null ? questionList.size() : 0;
    }

    public static class AnswerViewHolder extends RecyclerView.ViewHolder {
        private final AnswerItemLayoutBinding binding;

        public AnswerViewHolder(@NonNull AnswerItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position, QuestionModel question, TestViewModel viewModel) {
            binding.txtQuestionNo.setText("Question: " + (position + 1));
            binding.txtQuestion.setText(question.getQuestion());
            binding.txtOptionA.setText("A. " + question.getOptionA());
            binding.txtOptionB.setText("B. " + question.getOptionB());
            binding.txtOptionC.setText("C. " + question.getOptionC());
            binding.txtOptionD.setText("D. " + question.getOptionD());

            // Handle result and option colors
            int selected = question.getSelectedOption();
            int correctOption = question.getCorrectOption();
            if (selected == -1) {
                binding.txtResult.setText("Not answered");
                binding.txtResult.setTextColor(itemView.getContext().getColor(R.color.gray_dark));
                setOptionColor(selected, R.color.gray_dark);
            } else if (selected == correctOption) {
                binding.txtResult.setText("Correct");
                binding.txtResult.setTextColor(itemView.getContext().getColor(R.color.green));
                setOptionColor(selected, R.color.green);
            } else {
                binding.txtResult.setText("Wrong");
                binding.txtResult.setTextColor(itemView.getContext().getColor(R.color.red));
                setOptionColor(selected, R.color.red);
            }

            // Handle bookmark
            binding.btnBookmark.setImageResource(question.isBookMarked() ? R.drawable.ic_bookmark_new : R.drawable.ic_bookmark_border);
            binding.btnBookmark.setColorFilter(ContextCompat.getColor(
                    itemView.getContext(), question.isBookMarked() ? R.color.colorPrimary : R.color.gray_light));
            binding.btnBookmark.setOnClickListener(v -> {
                boolean newBookmarkState = !question.isBookMarked();
                viewModel.updateBookmark(position, newBookmarkState);
                question.setBookMarked(newBookmarkState);
                binding.btnBookmark.setImageResource(newBookmarkState ? R.drawable.ic_bookmark_new : R.drawable.ic_bookmark_border);
                binding.btnBookmark.setColorFilter(ContextCompat.getColor(
                        itemView.getContext(), newBookmarkState ? R.color.colorPrimary : R.color.gray_light));
                // Hiển thị Toast
                Toast.makeText(
                        itemView.getContext(),
                        newBookmarkState ? "Saved question!" : "Unsaved question!",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        private void setOptionColor(int selected, int color) {
            int defaultColor = itemView.getContext().getColor(R.color.gray_dark);
            binding.txtOptionA.setTextColor(selected == 1 ? itemView.getContext().getColor(color) : defaultColor);
            binding.txtOptionB.setTextColor(selected == 2 ? itemView.getContext().getColor(color) : defaultColor);
            binding.txtOptionC.setTextColor(selected == 3 ? itemView.getContext().getColor(color) : defaultColor);
            binding.txtOptionD.setTextColor(selected == 4 ? itemView.getContext().getColor(color) : defaultColor);
        }
    }
}