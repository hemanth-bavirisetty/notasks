package com.example.notasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SharedTaskAdapter extends RecyclerView.Adapter<SharedTaskAdapter.SharedTaskViewHolder> {

    private List<SharedTask> sharedTasks;

    public SharedTaskAdapter(List<SharedTask> sharedTasks) {
        this.sharedTasks = sharedTasks;
    }

    @NonNull
    @Override
    public SharedTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shared_task, parent, false);
        return new SharedTaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedTaskViewHolder holder, int position) {
        SharedTask sharedTask = sharedTasks.get(position);
        holder.titleTextView.setText(sharedTask.getTitle());
        holder.descriptionTextView.setText(sharedTask.getDescription());
        holder.sharedByTextView.setText("Shared by: " + sharedTask.getSharedBy());
        holder.sharedWithTextView.setText("Shared with: " + sharedTask.getSharedWith());
    }

    @Override
    public int getItemCount() {
        return sharedTasks.size();
    }

    static class SharedTaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView sharedByTextView;
        TextView sharedWithTextView;

        SharedTaskViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.text_view_shared_task_title);
            descriptionTextView = view.findViewById(R.id.text_view_shared_task_description);
            sharedByTextView = view.findViewById(R.id.text_view_shared_by);
            sharedWithTextView = view.findViewById(R.id.text_view_shared_with);
        }
    }
}

