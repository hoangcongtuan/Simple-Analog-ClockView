package com.tuanhc.clockview;

public class Util {

    /**
     * rotate point
     *
     * @param point  point
     * @param angle  rotate angle in degree
     * @param origin rotate origin point
     * @return new point
     */
    public static float[] rotatePoint(float[] point, float angle, float[] origin) {
        float dx = point[0] - origin[0];
        float dy = point[1] - origin[1];

        double inRadian = Math.toRadians(angle);
        double cos = Math.cos(inRadian);
        double sin = Math.sin(inRadian);

        float newX = (float) (dx * cos - dy * sin + origin[0]);
        float newY = (float) (dx * sin + dy * cos + origin[1]);
        return new float[]{
                newX, newY
        };
    }
}
