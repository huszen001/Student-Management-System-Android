package com.example.studentmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.R;
import com.example.studentmanager.model.Grade;

import java.util.List;

public class StudentGradeAdapter extends RecyclerView.Adapter<StudentGradeAdapter.ViewHolder> {
    private List<Grade> grades;

    public StudentGradeAdapter(List<Grade> grades) {
        this.grades = grades;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_grade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Grade grade = grades.get(position);
        holder.tvCourseName.setText("课程：" + grade.getCourseName());

        if (grade.getScore() < 0) {
            holder.tvGrade.setText("成绩：在修");
            holder.tvGrade.setTextColor(holder.tvGrade.getContext().getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            holder.tvGrade.setText(String.format("成绩：%.1f", grade.getScore()));

            // ================== 挂科背景变红 ==================
            if (grade.getScore() < 60) {
                holder.tvGrade.setBackgroundColor(holder.tvGrade.getContext().getResources().getColor(android.R.color.holo_red_light));
                holder.tvGrade.setTextColor(holder.tvGrade.getContext().getResources().getColor(android.R.color.white));
            } else {
                holder.tvGrade.setBackgroundColor(0); // 透明
                holder.tvGrade.setTextColor(holder.tvGrade.getContext().getResources().getColor(android.R.color.black));
            }
            // ===================================================
        }
    }

    @Override
    public int getItemCount() {
        return grades.size();
    }

    public void updateGrades(List<Grade> newGrades) {
        this.grades = newGrades;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvGrade;

        ViewHolder(View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvGrade = itemView.findViewById(R.id.tv_grade);
        }
    }
} 