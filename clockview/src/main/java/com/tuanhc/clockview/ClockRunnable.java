package com.tuanhc.clockview;

import android.view.View;

import androidx.annotation.NonNull;

public class ClockRunnable implements Runnable {
    private final View view;

    ClockRunnable(@NonNull View view) {
        this.view = view;
    }

    @Override
    public void run() {
        view.postDelayed(this, 1000L - System.currentTimeMillis() % 1000L);
        view.invalidate();
    }
}
