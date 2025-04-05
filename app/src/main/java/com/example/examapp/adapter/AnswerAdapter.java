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

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder> {
    private List<QuestionModel> questionList;
    public AnswerAdapter(List<QuestionModel> questionList){
        this.questionList = questionList;
    }
    @NonNull
    @Override
    public AnswerAdapter.AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.answer_item_layout, viewGroup, false);
        return new AnswerAdapter.AnswerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerAdapter.AnswerViewHolder answerViewHolder, int i) {
        String question = questionList.get(i).getQuestion();
        String optionA = questionList.get(i).getOptionA();
        String optionB = questionList.get(i).getOptionB();
        String optionC = questionList.get(i).getOptionC();
        String optionD = questionList.get(i).getOptionD();
        int selected = questionList.get(i).getSelectedOption();
        int correctOption = questionList.get(i).getCorrectOption();

        answerViewHolder.setData(i, question, optionA, optionB, optionC, optionD, selected, correctOption);


    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }
    public class AnswerViewHolder extends RecyclerView.ViewHolder {
        private TextView txtQuestionNo, txtQuestion, txtOptionA, txtOptionB, txtOptionC, txtOptionD, txtRessult;

        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestionNo = itemView.findViewById(R.id.txtQuestionNo);
            txtQuestion =  itemView.findViewById(R.id.txtQuestion);
            txtOptionA =  itemView.findViewById(R.id.txtOptionA);
            txtOptionB =  itemView.findViewById(R.id.txtOptionB);
            txtOptionC =  itemView.findViewById(R.id.txtOptionC);
            txtOptionD =  itemView.findViewById(R.id.txtOptionD);
            txtRessult =  itemView.findViewById(R.id.txtResult);

        }

        private void setData(int position, String question, String optionA, String optionB, String optionC, String optionD,int selected, int correctOption){
            txtQuestionNo.setText("Question: " + String.valueOf((position + 1)));
            txtQuestion.setText(question);
            txtOptionA.setText("A. " + optionA);
            txtOptionB.setText("B. " + optionB);
            txtOptionC.setText("C. " + optionC);
            txtOptionD.setText("D. " + optionD);

           if(selected == -1){
               txtRessult.setText("Not answered");
               txtRessult.setTextColor(itemView.getContext().getColor(R.color.black));
               setOptionColorf(selected, R.color.textNormal);
           }else if(selected == correctOption){
               txtRessult.setText("Correct");
               txtRessult.setTextColor(itemView.getContext().getColor(R.color.green));
               setOptionColorf(selected, R.color.green);
           }else{
               txtRessult.setText("Wrong");
               txtRessult.setTextColor(itemView.getContext().getColor(R.color.red));
               setOptionColorf(selected, R.color.red);
           }

        }

        private void setOptionColorf(int selected, int color) {
            if(selected == 1){
                txtOptionA.setTextColor(itemView.getContext().getColor(color));
            } else{
                txtOptionA.setTextColor(itemView.getContext().getColor(R.color.textNormal));
            }
            if(selected == 2){
                txtOptionB.setTextColor(itemView.getContext().getColor(color));
            } else{
                txtOptionB.setTextColor(itemView.getContext().getColor(R.color.textNormal));
            }
            if(selected == 3){
                txtOptionC.setTextColor(itemView.getContext().getColor(color));
            } else{
                txtOptionC.setTextColor(itemView.getContext().getColor(R.color.textNormal));
            }
            if(selected == 4){
                txtOptionD.setTextColor(itemView.getContext().getColor(color ));
            } else{
                txtOptionD.setTextColor(itemView.getContext().getColor(R.color.textNormal));
            }

        }
    }


}
