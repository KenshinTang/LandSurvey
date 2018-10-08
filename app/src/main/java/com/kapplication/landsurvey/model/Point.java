package com.kapplication.landsurvey.model;

public class Point {
    private float x;
    private float y;

    Point next = null;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
