package com.example.studentmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.R;
import com.example.studentmanager.model.CourseApplication;

import java.util.List;

public class CourseApplicationAdapter extends RecyclerView.Adapter<CourseApplicationAdapter.ViewHolder> {
    private List<CourseApplication> applications;
    private OnApplicationActionListener listener;

    public interface OnApplicationActionListener {
        void onApprove(CourseApplication application);
        void onReject(CourseApplication application);
    }

    public CourseApplicationAdapter(List<CourseApplication> applications, OnApplicationActionListener listener) {
        this.applications = applications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseApplication application = applications.get(position);
        holder.tvStudentName.setText(application.getStudentName());
        holder.tvCourseName.setText(application.getCourseName());
        holder.tvApplicationTime.setText(application.getApplicationTime()+"");

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApprove(application);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(application);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public void updateApplications(List<CourseApplication> newApplications) {
        this.applications = newApplications;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName;
        TextView tvCourseName;
        TextView tvApplicationTime;
        Button btnApprove;
        Button btnReject;

        ViewHolder(View view) {
            super(view);
            tvStudentName = view.findViewById(R.id.tv_student_name);
            tvCourseName = view.findViewById(R.id.tv_course_name);
            tvApplicationTime = view.findViewById(R.id.tv_application_time);
            btnApprove = view.findViewById(R.id.btn_approve);
            btnReject = view.findViewById(R.id.btn_reject);
        }
    }
} 