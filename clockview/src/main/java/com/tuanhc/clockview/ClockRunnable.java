package com.tuanhc.clockview;

import android.view.View;

import androidx.annotation.NonNull;

public class ClockRunnable implements Runnable {
    private boolean needSynchronizeSecond;
    private final long synchronizedAmount;
    private final View view;

    ClockRunnable(@NonNull View view, long millis) {
        this.view = view;
        needSynchronizeSecond = millis != 0;
        this.synchronizedAmount = 1000L - millis;
    }

    @Override
    public void run() {
        if (needSynchronizeSecond) {
            view.postDelayed(this, synchronizedAmount);
            needSynchronizeSecond = false;
        } else
            view.postDelayed(this, 1000L);
        view.invalidate();
    }
}
