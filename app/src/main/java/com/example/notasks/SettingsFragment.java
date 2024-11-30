package com.example.notasks;


import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SettingsFragment extends Fragment {
    private SharedPreferences preferences;
    private MaterialSwitch themeSwitch;
    private MaterialSwitch systemThemeSwitch;
    private MaterialSwitch notificationSwitch;
    private MaterialSwitch reminderSwitch;
    private MaterialSwitch deadlineSwitch;
    private MaterialSwitch syncSwitch;
    private TextView lastSyncText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Initialize views
        themeSwitch = view.findViewById(R.id.theme_switch);
        systemThemeSwitch = view.findViewById(R.id.system_theme_switch);
        notificationSwitch = view.findViewById(R.id.notification_switch);
        reminderSwitch = view.findViewById(R.id.reminder_switch);
        deadlineSwitch = view.findViewById(R.id.deadline_switch);
        syncSwitch = view.findViewById(R.id.sync_switch);
        lastSyncText = view.findViewById(R.id.last_sync);
        MaterialButton changePasswordButton = view.findViewById(R.id.change_password);
        MaterialButton logoutButton = view.findViewById(R.id.logout);

        // Setup switches
        setupThemeSettings();
        setupNotificationSettings();
        setupSyncSettings();
        setupAccountButtons(changePasswordButton, logoutButton);

        return view;
    }

    private void setupThemeSettings() {
        boolean isDarkTheme = preferences.getBoolean("dark_theme", false);
        boolean useSystemTheme = preferences.getBoolean("system_theme", true);

        themeSwitch.setChecked(isDarkTheme);
        systemThemeSwitch.setChecked(useSystemTheme);
        themeSwitch.setEnabled(!useSystemTheme);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("dark_theme", isChecked).apply();
            updateTheme(isChecked);
        });

        systemThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("system_theme", isChecked).apply();
            themeSwitch.setEnabled(!isChecked);
            if (isChecked) {
                updateThemeToSystem();
            }
        });
    }

    private void setupNotificationSettings() {
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", true);
        boolean remindersEnabled = preferences.getBoolean("reminders_enabled", true);
        boolean deadlinesEnabled = preferences.getBoolean("deadlines_enabled", true);

        notificationSwitch.setChecked(notificationsEnabled);
        reminderSwitch.setChecked(remindersEnabled);
        deadlineSwitch.setChecked(deadlinesEnabled);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            reminderSwitch.setEnabled(isChecked);
            deadlineSwitch.setEnabled(isChecked);
            updateNotificationSettings(isChecked);
        });

        reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("reminders_enabled", isChecked).apply();
            updateReminderSettings(isChecked);
        });

        deadlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("deadlines_enabled", isChecked).apply();
            updateDeadlineSettings(isChecked);
        });
    }

    private void setupSyncSettings() {
        boolean syncEnabled = preferences.getBoolean("sync_enabled", true);
        String lastSync = preferences.getString("last_sync", "Never");

        syncSwitch.setChecked(syncEnabled);
        lastSyncText.setText("Last synced: " + lastSync);

        syncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("sync_enabled", isChecked).apply();
            if (isChecked) {
                performSync();
            }
        });
    }

    private void setupAccountButtons(MaterialButton changePasswordButton, MaterialButton logoutButton) {
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void updateTheme(boolean isDark) {
        AppCompatDelegate.setDefaultNightMode(
            isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void updateThemeToSystem() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private void updateNotificationSettings(boolean enabled) {
        if (enabled) {
            // Request notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            new String[]{Manifest.permission.POST_NOTIFICATIONS},
            1001
        );
    }

    private void updateReminderSettings(boolean enabled) {
        // Update WorkManager for reminder tasks
        if (enabled) {
            scheduleReminders();
        } else {
            cancelReminders();
        }
    }

    private void updateDeadlineSettings(boolean enabled) {
        // Update WorkManager for deadline alerts
        if (enabled) {
            scheduleDeadlineAlerts();
        } else {
            cancelDeadlineAlerts();
        }
    }

    private void scheduleReminders() {
        // Schedule periodic reminders using WorkManager
        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
            ReminderWorker.class,
            1,
            TimeUnit.DAYS
        ).build();

        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "reminders",
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderWork
            );
    }

    private void cancelReminders() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("reminders");
    }

    private void scheduleDeadlineAlerts() {
        // Schedule deadline alerts using WorkManager
        PeriodicWorkRequest deadlineWork = new PeriodicWorkRequest.Builder(
            DeadlineWorker.class,
            1,
            TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "deadlines",
                ExistingPeriodicWorkPolicy.REPLACE,
                deadlineWork
            );
    }

    private void cancelDeadlineAlerts() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("deadlines");
    }

    private void performSync() {
        // Show sync progress
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View progressView = getLayoutInflater().inflate(R.layout.dialog_sync_progress, null);
        AlertDialog dialog = builder
            .setTitle("Syncing")
            .setView(progressView)
            .setCancelable(false)
            .show();

        // Simulate sync operation
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
            preferences.edit().putString("last_sync", currentTime).apply();
            lastSyncText.setText("Last synced: " + currentTime);
            Toast.makeText(requireContext(), "Sync completed", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText currentPassword = dialogView.findViewById(R.id.current_password);
        TextInputEditText newPassword = dialogView.findViewById(R.id.new_password);
        TextInputEditText confirmPassword = dialogView.findViewById(R.id.confirm_password);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change", (dialog, which) -> {
                String current = currentPassword.getText().toString();
                String newPass = newPassword.getText().toString();
                String confirm = confirmPassword.getText().toString();

                if (validatePasswordChange(current, newPass, confirm)) {
                    // Update password
                    updatePassword(newPass);
                    Toast.makeText(requireContext(), 
                        "Password updated successfully", 
                        Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private boolean validatePasswordChange(String current, String newPass, String confirm) {
        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), 
                "Please fill in all fields", 
                Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPass.equals(confirm)) {
            Toast.makeText(requireContext(),
                "New passwords don't match",
                Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPass.length() < 8) {
            Toast.makeText(requireContext(),
                "Password must be at least 8 characters",
                Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updatePassword(String newPassword) {
        // Here you would typically make an API call to update the password
        preferences.edit().putString("password_last_updated", 
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date())).apply();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> performLogout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void performLogout() {
        // Clear preferences
        preferences.edit().clear().apply();
        
        // Clear WorkManager tasks
        WorkManager.getInstance(requireContext()).cancelAllWork();
        
        // Navigate to login screen
        Intent intent = new Intent(requireContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}

