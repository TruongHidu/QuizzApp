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
import com.example.examapp.activities.QuestionActivity;
import com.example.examapp.activities.StartTestActivity;
import com.example.examapp.database.DbQuery;
import com.example.examapp.model.TestModel;

import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {
    private List<TestModel> testList;
    public TestAdapter(List<TestModel> testList) {
        this.testList = testList;
    }


    @NonNull
    @Override
    public TestAdapter.TestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.test_item_layout, viewGroup, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestAdapter.TestViewHolder testViewHolder, int position) {
        int progress = testList.get(position).getTopScore();
        testViewHolder.setData(position, progress);

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

        public void setData(int position, int progress) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            testNo.setText("Test No " + (position + 1));
            txtScore.setText(progress + "%");
            testProgressBar.setProgress(progress);

            itemView.setOnClickListener(view -> {
                DbQuery.g_selected_test_index = getAdapterPosition(); // Lấy vị trí hợp lệ
                Intent intent = new Intent(itemView.getContext(), StartTestActivity.class);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
