package com.kapplication.landsurvey.model;

public class Polygon {
    private Point head = null;

    public Polygon(Point head) {
        this.head = head;
        head.next = head;
    }

    public Polygon(float x, float y) {
        head = new Point(x, y);
        head.next = head;
    }

    public void addPoint(float x, float y) {
        addPoint(new Point(x, y));
    }

    public void addPoint(Point p) {
        if (head.next == head) {
            head.next = p;
            p.next = head;
        } else {
            Point temp = head;
            while (temp.next != head) {
                temp = temp.next;
            }
            temp.next = p;
            p.next = head;
        }
    }

    public double getArea() {
        return 0d;
    }

    public double getPerimeter() {
        return 0d;
    }
}
