package com.example.studentmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.R;
import com.example.studentmanager.model.Student;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private List<Student> students;
    private OnStudentActionListener listener;

    public interface OnStudentActionListener {
        void onEdit(Student student);
        void onDelete(Student student);
    }

    public StudentAdapter(List<Student> students, OnStudentActionListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = students.get(position);
        holder.tvStudentName.setText(student.getName());
        holder.tvStudentId.setText("学号：" + student.getStudentId());
        holder.tvStudentClass.setText("班级：" + student.getClassName());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(student);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(student);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void updateStudents(List<Student> newStudents) {
        this.students = newStudents;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName;
        TextView tvStudentId;
        TextView tvStudentClass;
        Button btnEdit;
        Button btnDelete;

        ViewHolder(View view) {
            super(view);
            tvStudentName = view.findViewById(R.id.tv_student_name);
            tvStudentId = view.findViewById(R.id.tv_student_id);
            tvStudentClass = view.findViewById(R.id.tv_student_class);
            btnEdit = view.findViewById(R.id.btn_edit);
            btnDelete = view.findViewById(R.id.btn_delete);
        }
    }
} 