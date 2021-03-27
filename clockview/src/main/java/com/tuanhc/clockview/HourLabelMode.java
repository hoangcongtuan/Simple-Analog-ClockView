package com.tuanhc.clockview;

public enum HourLabelMode {
    Simple(0),
    Full(1);

    private int value;

    HourLabelMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
