package com.example.notasks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private RecyclerView recentActivityList;
    private ActivityAdapter activityAdapter;
    private List<ActivityItem> activities = new ArrayList<>();
    private ShapeableImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView taskCompletionRateTextView;
    private TextView averageRatingTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        recentActivityList = view.findViewById(R.id.recent_activity_list);
        FloatingActionButton editProfileFab = view.findViewById(R.id.edit_profile_fab);
        profileImageView = view.findViewById(R.id.image_view_profile);
        nameTextView = view.findViewById(R.id.text_view_name);
        emailTextView = view.findViewById(R.id.text_view_email);
        taskCompletionRateTextView = view.findViewById(R.id.text_view_task_completion_rate);
        averageRatingTextView = view.findViewById(R.id.text_view_average_rating);


        setupRecentActivity();
        setupEditProfileButton(editProfileFab);
        updateUI();

        return view;
    }

    private void setupRecentActivity() {
        // Add sample activities
        activities.add(new ActivityItem(
            "Completed task: Project Proposal",
            "2 hours ago",
            ActivityItem.Type.COMPLETION
        ));
        activities.add(new ActivityItem(
            "Added new task: Team Meeting",
            "5 hours ago",
            ActivityItem.Type.CREATION
        ));
        activities.add(new ActivityItem(
            "Updated task: Client Presentation",
            "1 day ago",
            ActivityItem.Type.UPDATE
        ));

        activityAdapter = new ActivityAdapter(activities);
        recentActivityList.setLayoutManager(new LinearLayoutManager(getContext()));
        recentActivityList.setAdapter(activityAdapter);
    }

    private void setupEditProfileButton(FloatingActionButton fab) {
        fab.setOnClickListener(v -> showEditProfileDialog());
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.edit_name);
        ShapeableImageView profileImage = dialogView.findViewById(R.id.edit_profile_image);

        // Pre-fill current values
        nameInput.setText("John Doe");

        profileImage.setOnClickListener(v -> {
            // Launch image picker
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1000);
        });

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String newName = nameInput.getText().toString();
                if (!newName.isEmpty()) {
                    // Update profile name
                    nameTextView.setText(newName);
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void updateUI() {
        // TODO: Replace with actual user data
        nameTextView.setText("John Doe");
        emailTextView.setText("john.doe@example.com");
        taskCompletionRateTextView.setText("Task Completion Rate: 85%");
        averageRatingTextView.setText("Average Rating: 4.5");
    }

    private static class ActivityItem {
        String description;
        String timestamp;
        Type type;

        enum Type {
            COMPLETION,
            CREATION,
            UPDATE
        }

        ActivityItem(String description, String timestamp, Type type) {
            this.description = description;
            this.timestamp = timestamp;
            this.type = type;
        }
    }

    private static class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
        private List<ActivityItem> activities;

        ActivityAdapter(List<ActivityItem> activities) {
            this.activities = activities;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActivityItem activity = activities.get(position);
            holder.bind(activity);
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView descriptionView;
            private final TextView timestampView;
            private final ImageView iconView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                descriptionView = itemView.findViewById(R.id.activity_description);
                timestampView = itemView.findViewById(R.id.activity_timestamp);
                iconView = itemView.findViewById(R.id.activity_icon);
            }

            void bind(ActivityItem activity) {
                descriptionView.setText(activity.description);
                timestampView.setText(activity.timestamp);

                // Set icon based on activity type
                int iconRes;
                switch (activity.type) {
                    case COMPLETION:
                        iconRes = R.drawable.ic_check_circle;
                        break;
                    case CREATION:
                        iconRes = R.drawable.ic_add_circle;
                        break;
                    default:
                        iconRes = R.drawable.ic_edit;
                }
                iconView.setImageResource(iconRes);
            }
        }
    }
}

