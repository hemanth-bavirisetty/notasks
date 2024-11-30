package com.example.notasks;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class VoiceCommandButton extends MaterialButton {
    private static final int ANIMATION_DURATION = 300;
    private VoiceCommandManager voiceCommandManager;
    private ObjectAnimator pulseAnimator;
    private boolean isListening = false;

    public VoiceCommandButton(Context context) {
        super(context);
        init();
    }

    public VoiceCommandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoiceCommandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setIcon(AppCompatResources.getDrawable(getContext(), R.drawable.ic_mic));
        setIconGravity(MaterialButton.ICON_GRAVITY_START);
        setText("Voice Command");

        setupPulseAnimation();
        setupVoiceManager();
    }

    private void setupPulseAnimation() {
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f)
        );
        pulseAnimator.setDuration(ANIMATION_DURATION);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    private void setupVoiceManager() {
        voiceCommandManager = new VoiceCommandManager(getContext());
        voiceCommandManager.setOnVoiceCommandListener(new VoiceCommandManager.OnVoiceCommandListener() {
            @Override
            public void onTaskCreated(Task task) {
                stopListening();
                showFeedback("Task created: " + task.getTitle());
            }

            @Override
            public void onTaskUpdated(Task task) {
                stopListening();
                showFeedback("Task updated: " + task.getTitle());
            }

            @Override
            public void onTaskCompleted(long taskId) {
                stopListening();
                showFeedback("Task marked as completed");
            }

            @Override
            public void onError(String message) {
                stopListening();
                showError(message);
            }
        });

        setOnClickListener(v -> {
            if (isListening) {
                stopListening();
            } else {
                startListening();
            }
        });
    }

    private void startListening() {
        if (checkPermission()) {
            isListening = true;
            setText("Listening...");
            setIcon(AppCompatResources.getDrawable(getContext(), R.drawable.ic_mic_active));
            pulseAnimator.start();
            voiceCommandManager.startListening();
        }
    }

    private void stopListening() {
        isListening = false;
        setText("Voice Command");
        setIcon(AppCompatResources.getDrawable(getContext(), R.drawable.ic_mic));
        pulseAnimator.cancel();
        voiceCommandManager.stopListening();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                if (getContext() instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) getContext(),
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            1001);
                }
                return false;
            }
        }
        return true;
    }

    private void showFeedback(String message) {
        Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getContext().getColor(R.color.md_theme_error))
                .setTextColor(Color.WHITE)
                .show();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        voiceCommandManager.destroy();
        pulseAnimator.cancel();
    }
}


