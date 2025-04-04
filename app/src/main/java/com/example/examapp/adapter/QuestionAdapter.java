package com.example.examapp.adapter;

import static com.example.examapp.database.DbQuery.ANSWERED;
import static com.example.examapp.database.DbQuery.HIGHTLIGHTED;
import static com.example.examapp.database.DbQuery.UNANSWERED;
import static com.example.examapp.database.DbQuery.g_questionList;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.database.DbQuery;
import com.example.examapp.databinding.QuestionItemLayoutBinding;
import com.example.examapp.model.QuestionModel;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
    private List<QuestionModel> questionList;
    private Button btnPreSelected = null;

    public QuestionAdapter(List<QuestionModel> questionList) {
        this.questionList = questionList;
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
        holder.setData(questionList.get(position));
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

        private void setData(QuestionModel question) {
            binding.txtQuestion.setText(question.getQuestion());
            binding.optionA.setText(question.getOptionA());
            binding.optionB.setText(question.getOptionB());
            binding.optionC.setText(question.getOptionC());
            binding.optionD.setText(question.getOptionD());

            // Reset màu của tất cả nút trước khi đặt lại lựa chọn đã chọn
            resetButtonColors();

            // Nếu đã có lựa chọn trước đó, hiển thị lại
            if (question.getSelectedOption() != -1) {
                highlightSelectedOption(question.getSelectedOption());
            }

            // Xử lý sự kiện chọn đáp án
            View.OnClickListener optionClickListener = view -> {
                selectedOption((Button) view, question);
            };

            binding.optionA.setOnClickListener(optionClickListener);
            binding.optionB.setOnClickListener(optionClickListener);
            binding.optionC.setOnClickListener(optionClickListener);
            binding.optionD.setOnClickListener(optionClickListener);
        }

        // Hàm reset màu tất cả các nút về mặc định
        private void resetButtonColors() {
            binding.optionA.setBackgroundResource(R.drawable.unselected_button);
            binding.optionB.setBackgroundResource(R.drawable.unselected_button);
            binding.optionC.setBackgroundResource(R.drawable.unselected_button);
            binding.optionD.setBackgroundResource(R.drawable.unselected_button);
        }

        // Hàm hiển thị lại lựa chọn đã chọn
        private void highlightSelectedOption(int selectedOption) {
            switch (selectedOption) {
                case 1:
                    binding.optionA.setBackgroundResource(R.drawable.selectd_button);
                    break;
                case 2:
                    binding.optionB.setBackgroundResource(R.drawable.selectd_button);
                    break;
                case 3:
                    binding.optionC.setBackgroundResource(R.drawable.selectd_button);
                    break;
                case 4:
                    binding.optionD.setBackgroundResource(R.drawable.selectd_button);
                    break;
            }
        }

        private void selectedOption(Button selectedBtn, QuestionModel question) {
            // Nếu người dùng nhấn lại vào nút đã chọn trước đó -> Bỏ chọn
            if (question.getSelectedOption() != -1 && selectedBtn == getSelectedButton(question.getSelectedOption())) {
                resetButtonColors();
                question.setSelectedOption(-1);
                question.setStatus(UNANSWERED);
                return;
            }

            // Nếu chọn một đáp án mới, cập nhật đáp án
            resetButtonColors();
            selectedBtn.setBackgroundResource(R.drawable.selectd_button);

            if (selectedBtn == binding.optionA) {
                question.setSelectedOption(1);
            } else if (selectedBtn == binding.optionB) {
                question.setSelectedOption(2);
            } else if (selectedBtn == binding.optionC) {
                question.setSelectedOption(3);
            } else if (selectedBtn == binding.optionD) {
                question.setSelectedOption(4);
            }
            question.setStatus(ANSWERED);
        }



        // Hàm lấy nút đã chọn dựa trên `selectedOption`
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
