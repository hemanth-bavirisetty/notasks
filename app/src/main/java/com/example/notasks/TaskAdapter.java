package com.example.notasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.google.android.material.checkbox.MaterialCheckBox;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onCompleteClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.titleTextView.setText(task.getTitle());
        holder.descriptionTextView.setText(task.getDescription());
        holder.deadlineTextView.setText(task.getDeadline().toString());
        holder.priorityTextView.setText("bujji");
        //holder.statusTextView.setText("bujji");

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });

        holder.checkboxStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCompleteClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView deadlineTextView;
        TextView priorityTextView;
        TextView statusTextView;
        MaterialCheckBox checkboxStatus;
        TaskViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.text_view_title);
            descriptionTextView = view.findViewById(R.id.text_view_description);
            deadlineTextView = view.findViewById(R.id.text_view_deadline);
            priorityTextView = view.findViewById(R.id.text_view_priority);
           // statusTextView = view.findViewById(R.id.text_view_status);
            checkboxStatus = view.findViewById(R.id.checkbox_status);
        }
    }
}

