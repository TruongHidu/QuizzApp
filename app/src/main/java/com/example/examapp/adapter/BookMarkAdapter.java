package com.example.examapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.model.QuestionModel;

import java.util.List;

public class BookMarkAdapter extends RecyclerView.Adapter<BookMarkAdapter.BookMarkViewHolder> {
    private List<QuestionModel> questionList;
    public BookMarkAdapter(List<QuestionModel> questionList){
        this.questionList = questionList;
    }
    @NonNull
    @Override
    public BookMarkAdapter.BookMarkViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.answer_item_layout, viewGroup, false);
        return new BookMarkAdapter.BookMarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookMarkAdapter. BookMarkViewHolder bookMarkViewHolder, int i) {
        String question = questionList.get(i).getQuestion();
        String optionA = questionList.get(i).getOptionA();
        String optionB = questionList.get(i).getOptionB();
        String optionC = questionList.get(i).getOptionC();
        String optionD = questionList.get(i).getOptionD();
        int correctOption = questionList.get(i).getCorrectOption();

        bookMarkViewHolder.setData(i, question, optionA, optionB, optionC, optionD, correctOption);


    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }
    public class BookMarkViewHolder extends RecyclerView.ViewHolder {
        private TextView txtQuestionNo, txtQuestion, txtOptionA, txtOptionB, txtOptionC, txtOptionD, txtRessult;

        public BookMarkViewHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestionNo = itemView.findViewById(R.id.txtQuestionNo);
            txtQuestion =  itemView.findViewById(R.id.txtQuestion);
            txtOptionA =  itemView.findViewById(R.id.txtOptionA);
            txtOptionB =  itemView.findViewById(R.id.txtOptionB);
            txtOptionC =  itemView.findViewById(R.id.txtOptionC);
            txtOptionD =  itemView.findViewById(R.id.txtOptionD);
            txtRessult =  itemView.findViewById(R.id.txtResult);

        }

        private void setData(int position, String question, String optionA, String optionB, String optionC, String optionD, int correctOption){
            txtQuestionNo.setText("Question: " + String.valueOf((position + 1)));
            txtQuestion.setText(question);
            txtOptionA.setText("A. " + optionA);
            txtOptionB.setText("B. " + optionB);
            txtOptionC.setText("C. " + optionC);
            txtOptionD.setText("D. " + optionD);

            if(correctOption == 1){
                txtRessult.setText("Answer: " + optionA);
            }else if(correctOption == 2){
                txtRessult.setText("Answer: " + optionB);
            }else if(correctOption == 3){
                txtRessult.setText("Answer: " + optionC);
            }else {
                txtRessult.setText("Answer: " + optionD);
            }


        }


    }


}
