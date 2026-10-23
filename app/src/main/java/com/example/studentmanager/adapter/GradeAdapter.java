package com.example.studentmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.R;
import com.example.studentmanager.model.Grade;

import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.ViewHolder> {
    private List<Grade> grades;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnEditClickListener {
        void onEditClick(Grade grade, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Grade grade, int position);
    }

    public GradeAdapter(List<Grade> grades) {
        this.grades = grades;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Grade grade = grades.get(position);
        holder.tvStudentName.setText(grade.getStudentName());
        holder.tvCourseName.setText("课程：" + grade.getCourseName());
        holder.tvGrade.setText(String.format("成绩：%.1f", grade.getScore()));

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(grade, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(grade, position);
            }
        });
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
        TextView tvStudentName, tvCourseName, tvGrade;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvGrade = itemView.findViewById(R.id.tv_grade);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
} 