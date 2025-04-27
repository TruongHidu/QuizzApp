package com.example.examapp.adapter;

import static com.example.examapp.database.DbQuery.ANSWERED;
import static com.example.examapp.database.DbQuery.UNANSWERED;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.databinding.QuestionItemLayoutBinding;
import com.example.examapp.model.QuestionModel;

import java.util.List;
import java.util.Locale;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
    private final List<QuestionModel> questionList;
    private final OnOptionSelectedListener optionSelectedListener;

    public interface OnOptionSelectedListener {
        void onOptionSelected(int position, int selectedOption);
        void onOptionCleared(int position);
    }

    public QuestionAdapter(List<QuestionModel> questionList, OnOptionSelectedListener listener) {
        this.questionList = questionList;
        this.optionSelectedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        QuestionItemLayoutBinding binding = QuestionItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(questionList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final QuestionItemLayoutBinding binding;

        public ViewHolder(@NonNull QuestionItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void setData(QuestionModel question, int position) {
            binding.txtQuestion.setText((position + 1) +". " + question.getQuestion().toUpperCase());
            binding.optionA.setText(question.getOptionA().toLowerCase());
            binding.optionB.setText(question.getOptionB().toLowerCase());
            binding.optionC.setText(question.getOptionC().toLowerCase());
            binding.optionD.setText(question.getOptionD().toLowerCase());

            resetButtonColors();

            if (question.getSelectedOption() != -1) {
                highlightSelectedOption(question.getSelectedOption());
            }

            View.OnClickListener optionClickListener = view -> {
                selectedOption((Button) view, question, position);
            };

            binding.optionA.setOnClickListener(optionClickListener);
            binding.optionB.setOnClickListener(optionClickListener);
            binding.optionC.setOnClickListener(optionClickListener);
            binding.optionD.setOnClickListener(optionClickListener);
        }

        private void resetButtonColors() {
            int defaultColor = binding.getRoot().getContext().getColor(R.color.blueQuestion);
            int unselectedBg = R.drawable.unselected_button;

            binding.optionA.setBackgroundResource(unselectedBg);
            binding.optionB.setBackgroundResource(unselectedBg);
            binding.optionC.setBackgroundResource(unselectedBg);
            binding.optionD.setBackgroundResource(unselectedBg);

            binding.optionA.setTextColor(defaultColor);
            binding.optionB.setTextColor(defaultColor);
            binding.optionC.setTextColor(defaultColor);
            binding.optionD.setTextColor(defaultColor);
        }


        private void highlightSelectedOption(int selectedOption) {
            int selectedColor = binding.getRoot().getContext().getColor(R.color.white);
            switch (selectedOption) {
                case 1:
                    binding.optionA.setBackgroundResource(R.drawable.selectd_button);
                    binding.optionA.setTextColor(selectedColor);
                    break;
                case 2:
                    binding.optionB.setBackgroundResource(R.drawable.selectd_button);
                    binding.optionB.setTextColor(selectedColor);
                    break;
                case 3:
                    binding.optionC.setBackgroundResource(R.drawable.selectd_button);
                    binding.optionC.setTextColor(selectedColor);
                    break;
                case 4:
                    binding.optionD.setBackgroundResource(R.drawable.selectd_button);
                    binding.optionD.setTextColor(selectedColor);
                    break;
            }
        }

        private void selectedOption(Button selectedBtn, QuestionModel question, int position) {
            if (question.getSelectedOption() != -1 && selectedBtn == getSelectedButton(question.getSelectedOption())) {
                resetButtonColors();
                optionSelectedListener.onOptionCleared(position);
                return;
            }

            resetButtonColors();
            selectedBtn.setBackgroundResource(R.drawable.selectd_button);

            int selectedOption = -1;
            if (selectedBtn == binding.optionA) {
                selectedOption = 1;
            } else if (selectedBtn == binding.optionB) {
                selectedOption = 2;
            } else if (selectedBtn == binding.optionC) {
                selectedOption = 3;
            } else if (selectedBtn == binding.optionD) {
                selectedOption = 4;
            }
            optionSelectedListener.onOptionSelected(position, selectedOption);
        }

        private Button getSelectedButton(int selectedOption) {
            switch (selectedOption) {
                case 1: return binding.optionA;
                case 2: return binding.optionB;
                case 3: return binding.optionC;
                case 4: return binding.optionD;
                default: return null;
            }
        }
    }
}