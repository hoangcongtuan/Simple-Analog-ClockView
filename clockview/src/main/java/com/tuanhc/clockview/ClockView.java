package com.tuanhc.clockview;

import android.animation.ValueAnimator;
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
    private static final int INDICATOR_STEP_DEGREE = 360 / (12 * 5);
    private static final int HOUR_STEP_DEGREE = 360 / 12;
    private final float[] center = new float[2];
    private final float[] startPoint = new float[2];
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
    private final boolean showSecondHandle;
    private SecondHandleStyle secondHandleStyle;
    private Paint paint;
    private int clockSize;
    private float[] p2 = new float[2];
    private float[] p1 = new float[2];
    private ClockRunnable clockRunnable;
    private Calendar calendar;
    private final ValueAnimator secondDegreeValueAnimator = new ValueAnimator();
    private float smoothSecondDegreeOffset = 0f;

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
        secondDegreeValueAnimator.setInterpolator(null);
        secondDegreeValueAnimator.setFloatValues(360 / 60f);
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
        center[0] = (getWidth() - getPaddingStart() - getPaddingEnd()) / 2f + getPaddingStart();
        center[1] = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f + getPaddingTop();
        startPoint[0] = center[0];
        startPoint[1] = center[1] - clockSize / 2f;
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

            p1 = Util.rotatePoint(startPoint, angle, center);

            float _indicatorLength = (angle % HOUR_STEP_DEGREE == 0) ? (indicatorLength * 2) : indicatorLength;
            float _indicatorStroke = (angle % HOUR_STEP_DEGREE == 0) ? indicatorStroke * 2 : indicatorStroke;
            paint.setStrokeWidth(_indicatorStroke);

            p2[0] = startPoint[0];
            p2[1] = startPoint[1] + _indicatorLength;
            p2 = Util.rotatePoint(p2, angle, center);
            canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);

            //draw label
            if (angle % HOUR_STEP_DEGREE == 0 && showHourLabel) {
                int drawStep = hourLabelMode == HourLabelMode.Simple ? HOUR_STEP_DEGREE * 3 : HOUR_STEP_DEGREE;
                if (angle % drawStep == 0) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(hourLabelColor);
                    paint.setTextSize(hourLabelSize);

                    String label = String.valueOf(number);
                    paint.getTextBounds(String.valueOf(number), 0, label.length(), textBound);

                    p2[0] = startPoint[0];
                    p2[1] = startPoint[1] + _indicatorLength + hourLabelSize;
                    float[] pLabel = Util.rotatePoint(p2, angle, center);
                    canvas.drawText(String.valueOf(number), pLabel[0] - textBound.centerX(), pLabel[1] - textBound.centerY(), paint);
                }
                number = (number + 1) % 12;
            }
            angle += INDICATOR_STEP_DEGREE;
        }

        //update time
        paint.setStyle(Paint.Style.STROKE);
        float hourAngle = calendar.get(Calendar.HOUR) * HOUR_STEP_DEGREE + calendar.get(Calendar.MINUTE) / 60f * HOUR_STEP_DEGREE;
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
        p2[1] = center[1] - clockSize / 2f * 0.6f;
        p2 = Util.rotatePoint(p2, minuteAngle, center);
        p1[0] = center[0];
        p1[1] = center[1] + clockSize / 2f * 0.1f;
        p1 = Util.rotatePoint(p1, minuteAngle, center);
        paint.setColor(minuteHandleColor);
        paint.setStrokeWidth(minuteHandleStroke);
        canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);

        if (showSecondHandle) {
            if (secondHandleStyle == SecondHandleStyle.Smooth) {
                p2[0] = center[0];
                p2[1] = center[1] - clockSize / 2f * 0.9f;
                p2 = Util.rotatePoint(p2, secondAngle + smoothSecondDegreeOffset, center);
                p1[0] = center[0];
                p1[1] = center[1] + clockSize / 2f * 0.1f;
                p1 = Util.rotatePoint(p1, secondAngle + smoothSecondDegreeOffset, center);
                paint.setColor(secondHandleColor);
                paint.setStrokeWidth(secondHandleStroke);
                canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);
            } else {
                //draw second handle
                p2[0] = center[0];
                p2[1] = center[1] - clockSize / 2f * 0.7f;
                p2 = Util.rotatePoint(p2, secondAngle, center);
                p1[0] = center[0];
                p1[1] = center[1] + clockSize / 2f * 0.1f;
                p1 = Util.rotatePoint(p1, secondAngle, center);
                paint.setColor(secondHandleColor);
                paint.setStrokeWidth(secondHandleStroke);
                canvas.drawLine(p1[0], p1[1], p2[0], p2[1], paint);
            }
        }
    }

    /**
     * called by @ClockRunnable
     */
    void onTimeUpdate(long duration) {
        this.calendar = Calendar.getInstance();
        if (timeUpdateListener != null)
            timeUpdateListener.onTimeUpdate(calendar);
        if (secondHandleStyle == SecondHandleStyle.Smooth) {
            doAnimation(duration);
        } else
            invalidate();
    }

    private void doAnimation(long duration) {
        secondDegreeValueAnimator.cancel();
        secondDegreeValueAnimator.removeAllUpdateListeners();
        secondDegreeValueAnimator.setDuration(duration);
        secondDegreeValueAnimator.start();
        secondDegreeValueAnimator.addUpdateListener(animation -> {
            smoothSecondDegreeOffset = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    public void setTimeUpdateListener(@Nullable TimeUpdateListener listener) {
        this.timeUpdateListener = listener;
    }
}
