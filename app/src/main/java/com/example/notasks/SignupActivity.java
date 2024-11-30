package com.example.notasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notasks.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private MaterialButton signupButton;
    private MaterialButton loginButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        nameInput = findViewById(R.id.name);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        confirmPasswordInput = findViewById(R.id.confirm_password);
        nameLayout = findViewById(R.id.name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        signupButton = findViewById(R.id.signup_button);
        loginButton = findViewById(R.id.login_button);

        // Setup text watchers for real-time validation
        setupTextWatchers();

        // Setup click listeners
        signupButton.setOnClickListener(v -> attemptSignup());
        loginButton.setOnClickListener(v -> navigateToLogin());
    }

    private void setupTextWatchers() {
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailLayout.setError("Please enter a valid email address");
                }
            }
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordLayout.setError(null);
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && s.length() < 8) {
                    passwordLayout.setError("Password must be at least 8 characters");
                }
            }
        });

        confirmPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordLayout.setError(null);
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validatePasswords() {
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        if (!password.isEmpty() && !confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
        }
    }

    private void attemptSignup() {
        // Reset errors
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        // Get values
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        // Validate input
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            focusView = nameInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            focusView = emailInput;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            focusView = emailInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            focusView = passwordInput;
            cancel = true;
        } else if (password.length() < 8) {
            passwordLayout.setError("Password must be at least 8 characters");
            focusView = passwordInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your password");
            focusView = confirmPasswordInput;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            focusView = confirmPasswordInput;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        // Show loading state
        signupButton.setEnabled(false);
        signupButton.setText("Creating Account...");

        // Create account
        new Thread(() -> {
            // Check if email already exists
            if (isEmailTaken(email)) {
                runOnUiThread(() -> {
                    emailLayout.setError("This email is already registered");
                    emailInput.requestFocus();
                    signupButton.setEnabled(true);
                    signupButton.setText("Sign Up");
                });
                return;
            }

            // Create user
            long userId = databaseHelper.createUser(email, password, name);
            
            runOnUiThread(() -> {
                if (userId != -1) {
                    // Show success message
                    showSuccessDialog();
                } else {
                    // Show error
                    showSignupError();
                }
                // Reset button state
                signupButton.setEnabled(true);
                signupButton.setText("Sign Up");
            });
        }).start();
    }

    private boolean isEmailTaken(String email) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id"},
                "email = ?", new String[]{email},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Account Created")
            .setMessage("Your account has been created successfully. Please login to continue.")
            .setPositiveButton("Login", (dialog, which) -> {
                navigateToLogin();
                finish();
            })
            .setCancelable(false)
            .show();
    }

    private void showSignupError() {
        Snackbar.make(
            signupButton,
            "Failed to create account. Please try again.",
            Snackbar.LENGTH_LONG
        ).show();
    }

    private void navigateToLogin() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

