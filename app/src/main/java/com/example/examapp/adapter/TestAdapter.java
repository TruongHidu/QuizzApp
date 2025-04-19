package com.example.examapp.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examapp.R;
import com.example.examapp.activities.StartTestActivity;
import com.example.examapp.database.DbQuery;
import com.example.examapp.model.TestModel;

import java.util.ArrayList;
import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {
    private List<TestModel> testList;

    public TestAdapter(List<TestModel> testList) {
        this.testList = new ArrayList<>(testList);
    }

    public void updateData(List<TestModel> newTestList) {
        this.testList.clear();
        this.testList.addAll(newTestList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.test_item_layout, viewGroup, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        holder.setData(position, testList.get(position));
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    public static class TestViewHolder extends RecyclerView.ViewHolder {
        private TextView testNo;
        private ProgressBar testProgressBar;
        private TextView txtScore;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            testNo = itemView.findViewById(R.id.txtTestName);
            testProgressBar = itemView.findViewById(R.id.testProgressBar);
            txtScore = itemView.findViewById(R.id.txtScore);
        }

        public void setData(int position, TestModel testModel) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            testNo.setText("Test " + testModel.getTestId());
            txtScore.setText(testModel.getTopScore() + "%");
            testProgressBar.setProgress(testModel.getTopScore());

            itemView.setOnClickListener(view -> {
                DbQuery.g_selected_test_index = getAdapterPosition();
                Intent intent = new Intent(itemView.getContext(), StartTestActivity.class);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}