package com.tuanhc.clockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;

public class ClockView extends View {
    private static final int ANGLE_STEP = 360 / (12 * 5);
    private static final int HOUR_ANGLE_STEP = 360 / 12;
    private final int[] center = new int[2];
    private final int[] startPoint = new int[2];
    private final Rect textBound = new Rect();
    private final int borderStroke;
    private final int borderColor;
    private final int backgroundColor;
    private final int indicatorColor;
    private final int indicatorStroke;
    private final int indicatorLength;
    private final int hourLabelColor;
    private final int hourLabelSize;
    private final HourLabelMode hourLabelMode;
    private final boolean showHourLabel;
    private final int hourHandleColor;
    private final int hourHandleStroke;
    private final int minuteHandleColor;
    private final int minuteHandleStroke;
    private final int secondHandleColor;
    private final int secondHandleStroke;
    private boolean showSecondHandle;
    private SecondHandleStyle secondHandleStyle;
    private Paint paint;
    private int clockSize;
    private int[] p2 = new int[2];
    private int[] p1 = new int[2];
    private ClockRunnable clockRunnable;
    private Calendar calendar;

    @Nullable
    private TimeUpdateListener timeUpdateListener;

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.ClockView);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.ClockView);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, defStyleRes);
        borderStroke = ta.getDimensionPixelSize(R.styleable.ClockView_borderStroke, 0);
        borderColor = ta.getColor(R.styleable.ClockView_borderColor, 0);

        backgroundColor = ta.getColor(R.styleable.ClockView_backgroundColor, 0);

        indicatorColor = ta.getColor(R.styleable.ClockView_indicatorColor, 0);
        indicatorLength = ta.getDimensionPixelSize(R.styleable.ClockView_indicatorLength, 0);
        indicatorStroke = ta.getDimensionPixelSize(R.styleable.ClockView_indicatorStroke, 0);

        hourLabelColor = ta.getColor(R.styleable.ClockView_hourLabelColor, 0);
        hourLabelSize = ta.getDimensionPixelSize(R.styleable.ClockView_hourLabelSize, 0);
        hourLabelMode = HourLabelMode.values()[
                ta.getInt(R.styleable.ClockView_hourLabelMode, HourLabelMode.Full.getValue())
                ];
        showHourLabel = ta.getBoolean(R.styleable.ClockView_showHourLabel, true);

        hourHandleColor = ta.getColor(R.styleable.ClockView_hourHandleColor, 0);
        hourHandleStroke = ta.getDimensionPixelSize(R.styleable.ClockView_hourHandleStroke, 0);

        minuteHandleColor = ta.getColor(R.styleable.ClockView_minuteHandleColor, 0);
        minuteHandleStroke = ta.getDimensionPixelSize(R.styleable.ClockView_minuteHandleStroke, 0);

        showSecondHandle = ta.getBoolean(R.styleable.ClockView_showSecondHandle, true);
        secondHandleColor = ta.getColor(R.styleable.ClockView_secondHandleColor, 0);
        secondHandleStroke = ta.getDimensionPixelSize(R.styleable.ClockView_secondHandleStroke, 0);
        secondHandleStyle = SecondHandleStyle.values()[
                ta.getInt(R.styleable.ClockView_secondHandleStyle, SecondHandleStyle.Tick.getValue())
                ];
        ta.recycle();
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clockRunnable = new ClockRunnable(this);
        clockRunnable.run();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        clockSize = Math.min(
                getWidth() - getPaddingStart() - getPaddingEnd(),
                getHeight() - getPaddingTop() - getPaddingBottom())
        ;
        center[0] = (getWidth() - getPaddingStart() - getPaddingEnd()) / 2 + getPaddingStart();
        center[1] = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2 + getPaddingTop();
        startPoint[0] = center[0];
        startPoint[1] = center[1] - clockSize / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backgroundColor);
        canvas.drawCircle(center[0], center[1], clockSize / 2f, paint);

        //draw border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(borderColor);
        paint.setStrokeWidth(borderStroke);
        canvas.drawCircle(center[0], center[1], clockSize / 2f, paint);

        //draw indicator, label
        int angle = 0;
        int number = 12;
        while (angle < 360) {
            //indicator line point
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(indicatorColor);

            int[] p1 = Util.rotatePoint(startPoint, angle, center);

            int _indicatorLength = (angle % HOUR_ANGLE_STEP == 0) ? (indicatorLength * 2) : indicatorLength;
            int _indicatorStroke = (angle % HOUR_ANGLE_STEP == 0) ? indicatorStroke * 2 : indicatorStroke;
            paint.setStrokeWidth(_indicatorStroke);

            p2[0] = startPoint[0];
            p2[1] = startPoint[1] + _indicatorLength;
            p2 = Util.rotatePoint(p2, angle, center);
            canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);

            //draw label
            if (angle % HOUR_ANGLE_STEP == 0 && showHourLabel) {
                int drawStep = hourLabelMode == HourLabelMode.Simple ? HOUR_ANGLE_STEP * 3 : HOUR_ANGLE_STEP;
                if (angle % drawStep == 0) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(hourLabelColor);
                    paint.setTextSize(hourLabelSize);

                    String label = String.valueOf(number);
                    paint.getTextBounds(String.valueOf(number), 0, label.length(), textBound);

                    p2[0] = startPoint[0];
                    p2[1] = startPoint[1] + _indicatorLength + hourLabelSize;
                    int[] pLabel = Util.rotatePoint(p2, angle, center);
                    canvas.drawText(String.valueOf(number), pLabel[0] - textBound.centerX(), pLabel[1] - textBound.centerY(), paint);
                }
                number = (number + 1) % 12;
            }
            angle += ANGLE_STEP;
        }

        //update time
        paint.setStyle(Paint.Style.STROKE);
        float hourAngle = calendar.get(Calendar.HOUR) * HOUR_ANGLE_STEP + calendar.get(Calendar.MINUTE) / 60f * HOUR_ANGLE_STEP;
        float minuteAngle = (calendar.get(Calendar.MINUTE) / 60f * 360) + calendar.get(Calendar.SECOND) / 60f * 360 / 60f;
        float secondAngle = calendar.get(Calendar.SECOND) / 60f * 360;

        //draw hour handle
        p2[0] = center[0];
        p2[1] = (int) (center[1] - clockSize / 2 * 0.4f);
        p2 = Util.rotatePoint(p2, hourAngle, center);

        p1[0] = center[0];
        p1[1] = (int) (center[1] + clockSize / 2 * 0.1);
        p1 = Util.rotatePoint(p1, hourAngle, center);
        paint.setColor(hourHandleColor);
        paint.setStrokeWidth(hourHandleStroke);
        canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);

        //draw minute handle
        p2[0] = center[0];
        p2[1] = (int) (center[1] - clockSize / 2 * 0.6f);
        p2 = Util.rotatePoint(p2, minuteAngle, center);
        p1[0] = center[0];
        p1[1] = (int) (center[1] + clockSize / 2 * 0.1);
        p1 = Util.rotatePoint(p1, minuteAngle, center);
        paint.setColor(minuteHandleColor);
        paint.setStrokeWidth(minuteHandleStroke);
        canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);

        if (showSecondHandle) {
            //draw second handle
            p2[0] = center[0];
            p2[1] = (int) (center[1] - clockSize / 2 * 0.7f);
            p2 = Util.rotatePoint(p2, secondAngle, center);
            p1[0] = center[0];
            p1[1] = (int) (center[1] + clockSize / 2 * 0.1);
            p1 = Util.rotatePoint(p1, secondAngle, center);
            paint.setColor(secondHandleColor);
            paint.setStrokeWidth(secondHandleStroke);
            canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);
        }
    }

    /**
     * called before invalidate
     * called by @ClockRunable
     */
    void onTimeUpdate() {
        this.calendar = Calendar.getInstance();
        if (timeUpdateListener != null)
            timeUpdateListener.onTimeUpdate(calendar);
    }

    public void setTimeUpdateListener(@Nullable TimeUpdateListener listener) {
        this.timeUpdateListener = listener;
    }
}
