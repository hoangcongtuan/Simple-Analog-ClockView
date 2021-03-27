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
    public static int[] rotatePoint(int[] point, float angle, int[] origin) {
        int dx = point[0] - origin[0];
        int dy = point[1] - origin[1];

        double inRadian = Math.toRadians(angle);
        double cos = Math.cos(inRadian);
        double sin = Math.sin(inRadian);

        int newX = (int) (dx * cos - dy * sin + origin[0]);
        int newY = (int) (dx * sin + dy * cos + origin[1]);
        return new int[]{
                newX, newY
        };
    }
}
