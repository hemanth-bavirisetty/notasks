package com.example.notasks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Login extends AppCompatActivity {
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private MaterialButton loginButton;
    private MaterialButton forgotPasswordButton;
    private MaterialButton signupButton;
    private DatabaseHelper databaseHelper;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if user is already logged in
        if (isLoggedIn()) {
            navigateToMain();
            return;
        }

        // Initialize views
        emailInput = findViewById(R.id.email_edit_text);
        passwordInput = findViewById(R.id.password_edit_text);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordButton = findViewById(R.id.forgot_password);
        signupButton = findViewById(R.id.register_button);

        // Setup click listeners
        loginButton.setOnClickListener(v -> attemptLogin());
        forgotPasswordButton.setOnClickListener(v -> showForgotPasswordDialog());
        signupButton.setOnClickListener(v -> navigateToSignup());
    }

    private boolean isLoggedIn() {
        return preferences.getLong("user_id", -1) != -1;
    }

    private void attemptLogin() {
        // Reset errors
        emailLayout.setError(null);
        passwordLayout.setError(null);

        // Get values
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Authenticate user
        new Thread(() -> {
            User user = databaseHelper.authenticateUser(email, password);

            runOnUiThread(() -> {
                if (user != null) {
                    // Save user session
                    saveUserSession(user);
                    // Navigate to main activity
                    navigateToMain();
                } else {
                    // Show error
                    showLoginError();
                }
                // Reset button state
                loginButton.setEnabled(true);
                loginButton.setText("Login");
            });
        }).start();
    }

    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("user_id", user.getId());
        editor.putString("user_email", user.getEmail());
        editor.putString("user_name", user.getName());
        editor.putString("user_profile_image", user.getProfileImage());
        editor.apply();
    }

    private void showLoginError() {
        Snackbar.make(
                loginButton,
                "Invalid email or password",
                Snackbar.LENGTH_LONG
        ).show();
    }

    private void showForgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText emailInput = dialogView.findViewById(R.id.email_reset);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Reset Password")
                .setView(dialogView)
                .setPositiveButton("Send Reset Link", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        // TODO: Implement password reset functionality
                        Toast.makeText(this,
                                "Password reset link sent to your email",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Please enter a valid email address",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void navigateToSignup() {
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

