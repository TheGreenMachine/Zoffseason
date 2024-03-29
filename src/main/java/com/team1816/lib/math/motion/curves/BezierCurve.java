package com.team1816.lib.math.motion.curves;

import com.team1816.lib.math.motion.splines.NaturalCubicSpline;
import java.util.ArrayList;

public class BezierCurve {

    public static class ControlPoint {

        public double x;
        public double y;

        public ControlPoint() {
            x = 0;
            y = 0;
        }

        public ControlPoint(double a, double b) {
            x = a;
            y = b;
        }

        public void add(ControlPoint c) {
            x += c.x;
            y += c.x;
        }

        public void multiply(double z) {
            x *= z;
            y *= z;
        }

        public double getDistance(ControlPoint c) {
            return Math.hypot(c.x - x, c.y - y);
        }

        public Double[] convertToDoubleArray() {
            return new Double[] { x, y };
        }
    }

    private ArrayList<ControlPoint> controlPoints; // defined sequentially
    private ArrayList<Double> xCoefficients; // defined such that index refers to exponent
    private ArrayList<Double> yCoefficients; // defined such that index refers to exponent
    private NaturalCubicSpline LUT;

    public BezierCurve() {
        controlPoints = new ArrayList<>();
        xCoefficients = yCoefficients = new ArrayList<>();
        LUT = new NaturalCubicSpline(new ArrayList<Double[]>());
    }

    public BezierCurve(ArrayList<ControlPoint> arr) {
        controlPoints = arr;
        generateLookUpTable(100);
    }

    public void generateLookUpTable(int resolution) {
        ArrayList<Double[]> knotPoints = new ArrayList<>();
        for (int i = 0; i <= resolution; i++) {
            double t1 = (double) i / resolution;
            double dist = getPortionLength((i + 1) * resolution, 0, t1);
            knotPoints.add(new Double[] { dist, t1 });
        }
        LUT = new NaturalCubicSpline(knotPoints);
    }

    private ControlPoint lerp(ControlPoint p1, ControlPoint p2, double t) {
        p1.multiply(1 - t);
        p2.multiply(t);
        p1.add(p2);
        return p1;
    }

    public ControlPoint getValue(double t) {
        ControlPoint val = new ControlPoint();
        for (int i = 0; i <= controlPoints.size() - 1; i++) {
            ControlPoint c = controlPoints.get(i);
            c.multiply(
                Math.pow(t, i) *
                Math.pow((1 - t), controlPoints.size() - i - 1) *
                combination(controlPoints.size() - 1, i)
            );
            val.add(c);
        }
        return val;
    }

    public ControlPoint getValueWithDistance(double d) {
        double t = Math.min(LUT.getValue(d), 1.0);
        return getValue(t);
    }

    public double combination(int n, int x) {
        double ans = 1;
        for (int i = n; i > x; i--) {
            ans *= i;
        }
        for (int i = 1; i <= x; i++) {
            ans /= i;
        }
        return ans;
    }

    public double getLength(int resolution) {
        double distance = 0;
        ControlPoint p1 = controlPoints.get(0);
        ControlPoint p2 = new ControlPoint();
        for (int i = 0; i < resolution - 1; i++) {
            p2 = getValue((double) (i + 1) / resolution);
            distance += p1.getDistance(p2);
            p1 = p2;
        }
        return distance;
    }

    public double getPortionLength(int resolution, double i, double f) {
        double distance = 0;
        ControlPoint p1 = getValue(i);
        ControlPoint p2 = new ControlPoint();
        for (double j = i; j <= f; j += 1.0 / resolution) {
            p2 = getValue(j);
            distance += p1.getDistance(p2);
            p1 = p2;
        }
        return distance;
    }
}
