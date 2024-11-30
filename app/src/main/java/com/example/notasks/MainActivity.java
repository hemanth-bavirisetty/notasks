package com.example.notasks;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    //private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private GoogleCalendarHelper googleCalendarHelper;

    private List<Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        //tasksRecyclerView = findViewById(R.id.content_frame);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        // Setup toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Setup navigation drawer
        setupNavigationDrawer();

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new TasksFragment())
                .commit();
        }

        // Setup FAB
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        // Initialize Google Calendar Helper with the current user's account
        // You'll need to replace "user@example.com" with the actual user's email
        googleCalendarHelper = new GoogleCalendarHelper(this, "user@example.com");
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            
            if (id == R.id.nav_tasks) {
                fragment = new TasksFragment();
            } else if (id == R.id.nav_analytics) {
                fragment = new AnalyticsFragment();
            } else if (id == R.id.nav_calendar) {
                fragment = new CalendarFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });
    }

    private void addSampleTasks() {
        tasks.add(new Task(
            1,
            "Complete Project Proposal",
            "Write and submit the project proposal for the new client",
            new Date(),
            Task.Priority.HIGH,
            Task.Status.IN_PROGRESS,
            "john.doe@example.com"
        ));

        tasks.add(new Task(
            2,
            "Team Meeting",
            "Weekly team sync-up meeting",
            new Date(),
            Task.Priority.MEDIUM,
            Task.Status.YET_TO_START,
            "john.doe@example.com"
        ));

        taskAdapter.notifyDataSetChanged();
    }

    private void showAddTaskDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        
        // Get references to views using the correct IDs from dialog_add_task.xml
        TextInputEditText titleInput = dialogView.findViewById(R.id.input_title);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.input_description);
        DatePicker deadlinePicker = dialogView.findViewById(R.id.date_picker_deadline);
        Spinner prioritySpinner = dialogView.findViewById(R.id.spinner_priority);
        
        // Setup priority spinner
        ArrayAdapter<Task.Priority> priorityAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            Task.Priority.values()
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        builder.setView(dialogView)
            .setTitle("Add New Task")
            .setPositiveButton("Add", (dialog, which) -> {
                String title = titleInput.getText().toString();
                String description = descriptionInput.getText().toString();
                
                // Get date from DatePicker
                Calendar calendar = Calendar.getInstance();
                calendar.set(deadlinePicker.getYear(), deadlinePicker.getMonth(), deadlinePicker.getDayOfMonth());
                Date deadline = calendar.getTime();
                
                Task.Priority priority = (Task.Priority) prioritySpinner.getSelectedItem();

                if (!title.isEmpty()) {
                    Task newTask = new Task(
                        tasks.size() + 1,
                        title,
                        description,
                        deadline,
                        priority,
                        Task.Status.YET_TO_START,
                        "john.doe@example.com"
                    );
                    tasks.add(0, newTask);
                    taskAdapter.notifyItemInserted(0);
                    //tasksRecyclerView.smoothScrollToPosition(0);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public GoogleCalendarHelper getGoogleCalendarHelper() {
        return googleCalendarHelper;
    }
}

