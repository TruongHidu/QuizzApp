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
import com.example.examapp.model.QuestionModel;

import java.util.List;

public class QuestionGridAdapter extends BaseAdapter {
    private final Context context;
    private final List<QuestionModel> questionList;
    private final OnQuestionClickListener clickListener;

    public interface OnQuestionClickListener {
        void onQuestionClicked(int position);
    }

    public QuestionGridAdapter(Context context, List<QuestionModel> questionList, OnQuestionClickListener clickListener) {
        this.context = context;
        this.questionList = questionList;
        this.clickListener = clickListener;
    }

    @Override
    public int getCount() {
        return questionList.size();
    }

    @Override
    public Object getItem(int position) {
        return questionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.question_grid_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bind(position);

        return convertView;
    }

    private class ViewHolder {
        private final TextView questNumber;
        private final View itemView;

        ViewHolder(View itemView) {
            this.itemView = itemView;
            questNumber = itemView.findViewById(R.id.questNumber);
        }

        void bind(int position) {
            questNumber.setText(String.valueOf(position + 1));
            int status = questionList.get(position).getStatus();
            int colorRes;
            switch (status) {
                case NOT_VISITED:
                    colorRes = R.color.gray;
                    break;
                case UNANSWERED:
                    colorRes = R.color.red;
                    break;
                case ANSWERED:
                    colorRes = R.color.green;
                    break;
                case HIGHTLIGHTED:
                    colorRes = R.color.colorPrimary; // Đồng bộ với question_list_layout.xml
                    break;
                default:
                    colorRes = R.color.gray;
            }
            questNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));

            itemView.setOnClickListener(v -> clickListener.onQuestionClicked(position));
        }
    }
}