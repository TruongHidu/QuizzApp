package com.example.examapp.adapter;

import static com.example.examapp.database.DbQuery.ANSWERED;
import static com.example.examapp.database.DbQuery.HIGHTLIGHTED;
import static com.example.examapp.database.DbQuery.NOT_VISITED;
import static com.example.examapp.database.DbQuery.UNANSWERED;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.examapp.R;
import com.example.examapp.activities.QuestionActivity;
import com.example.examapp.database.DbQuery;

public class QuestionGridAdapter extends BaseAdapter {
    private int numOfQuestions;
    Context context;


    public QuestionGridAdapter(Context context, int numOfQuestions) {
        this.context = context;
        this.numOfQuestions = numOfQuestions;
    }

    @Override
    public int getCount() {
        return numOfQuestions;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View myView ;
        if(view  == null){
            myView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.question_grid_item,viewGroup,false);
        }else{
            myView = view;
        }
        myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof QuestionActivity){
                    ((QuestionActivity)context).goToQuestion(i);
                }
            }
        });

        TextView questNumber = myView.findViewById(R.id.questNumber);
        questNumber.setText(String.valueOf(i+1));
        switch (DbQuery.g_questionList.get(i).getStatus()){
            case NOT_VISITED:
                questNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(myView.getContext(),R.color.gray)));
                break;
            case UNANSWERED:
                questNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(myView.getContext(),R.color.red)));
                break;
            case ANSWERED:
                questNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(myView.getContext(),R.color.green)));
                break;
            case HIGHTLIGHTED:
                questNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(myView.getContext(),R.color.pink)));
                break;
            default:
                break;


        }

        return myView;
    }
}
