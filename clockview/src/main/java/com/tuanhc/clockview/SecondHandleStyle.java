package com.tuanhc.clockview;

public enum SecondHandleStyle {
    Tick(0),
    Smooth(1);

    private int value;

    SecondHandleStyle(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
