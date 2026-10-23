package com.example.studentmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.MyCoursesActivity;
import com.example.studentmanager.R;
import com.example.studentmanager.model.Course;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> courses;
    private boolean isAdmin;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;
    private OnApplyClickListener applyListener;
    private List<Integer> appliedCourseIds = new ArrayList<>();
    private boolean showApplyButton = true;

    // 控制退选按钮
    private boolean showDropButton = false;

    public interface OnItemClickListener {
        void onItemClick(Course course, int position);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(Course course, int position);
    }
    public interface OnApplyClickListener {
        void onApplyClick(Course course, int position);
    }

    public CourseAdapter(List<Course> courses, boolean isAdmin) {
        this.courses = courses;
        this.isAdmin = isAdmin;
    }

    // 开关退选按钮
    public void setShowDropButton(boolean show) {
        this.showDropButton = show;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }
    public void setOnApplyClickListener(OnApplyClickListener listener) {
        this.applyListener = listener;
    }
    public void setAppliedCourseIds(List<Integer> ids) {
        this.appliedCourseIds = ids;
    }
    public void setShowApplyButton(boolean showApplyButton) {
        this.showApplyButton = showApplyButton;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.tvName.setText(course.getName());
        holder.tvTeacher.setText("教师：" + course.getTeacher());
        holder.tvTime.setText("时间：" + course.getTime());
        holder.tvLocation.setText("地点：" + course.getLocation());
        holder.tvCapacity.setText(String.format("容量：%d/%d", course.getSelected(), course.getCapacity()));
        holder.tvCourseCode.setText("课程号：" + course.getCourseCode());
        holder.tvCredit.setText("学分：" + course.getCredit());
        // ==============================================
        // 退选按钮逻辑（只在这里写一次！）
        // ==============================================
        // 只有 我的课程 页面才显示退选按钮
        if (holder.itemView.getContext() instanceof MyCoursesActivity) {
            holder.btnDropCourse.setVisibility(View.VISIBLE);
            holder.btnDropCourse.setOnClickListener(v -> {
                ((MyCoursesActivity) holder.itemView.getContext()).dropCourse(course.getId());
            });
        } else {
            holder.btnDropCourse.setVisibility(View.GONE);
        }

        // 删除按钮（管理员）
        holder.btnDelete.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(course, position);
            }
        });

        // 申请按钮
        if (!isAdmin && holder.btnApply != null && showApplyButton) {
            holder.btnApply.setVisibility(View.VISIBLE);
            boolean isApplied = appliedCourseIds != null && appliedCourseIds.contains(course.getId());
            boolean isFull = course.getSelected() >= course.getCapacity();

            if (isApplied) {
                holder.btnApply.setText("已申请");
                holder.btnApply.setEnabled(false);
            } else if (isFull) {
                holder.btnApply.setText("已满员");
                holder.btnApply.setEnabled(false);
            } else {
                holder.btnApply.setText("申请");
                holder.btnApply.setEnabled(true);
                holder.btnApply.setOnClickListener(v -> {
                    if (applyListener != null) {
                        applyListener.onApplyClick(course, position);
                    }
                });
            }
        } else if (holder.btnApply != null) {
            holder.btnApply.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(course, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTeacher, tvTime, tvLocation, tvCapacity;
        ImageButton btnDelete;
        MaterialButton btnApply;
        Button btnDropCourse;
        TextView tvCourseCode;
        TextView tvCredit;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_course_name);
            tvTeacher = itemView.findViewById(R.id.tv_course_teacher);
            tvTime = itemView.findViewById(R.id.tv_course_time);
            tvLocation = itemView.findViewById(R.id.tv_course_location);
            tvCapacity = itemView.findViewById(R.id.tv_course_capacity);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnApply = itemView.findViewById(R.id.btn_apply);
            btnDropCourse = itemView.findViewById(R.id.btn_drop_course);

            tvCourseCode = itemView.findViewById(R.id.tv_course_code);
            tvCredit = itemView.findViewById(R.id.tv_credit);
        }
    }
}