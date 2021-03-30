package com.tuanhc.clockview;

import android.view.View;

import androidx.annotation.NonNull;

public class ClockRunnable implements Runnable {
    private final ClockView view;

    ClockRunnable(@NonNull ClockView view) {
        this.view = view;
    }

    @Override
    public void run() {
        view.postDelayed(this, 1000L - System.currentTimeMillis() % 1000L);
        view.onTimeUpdate(1000L - System.currentTimeMillis() % 1000L);
    }
}
