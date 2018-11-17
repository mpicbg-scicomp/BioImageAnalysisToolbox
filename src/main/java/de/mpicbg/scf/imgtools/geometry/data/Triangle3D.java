package de.mpicbg.scf.imgtools.geometry.data;

import ij.measure.Calibration;

/**
 * This class is the data structure corresponding a triangle in 3D-space.
 * <p>
 * <b>Note:</b>The three points of the triangle are reordered internally to make it (computationally) easier to compare triangles.
 * <p>
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: June 2015
 * <p>
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class Triangle3D {
    private Point3D a;
    private Point3D b;
    private Point3D c;

    /**
     * Constructor retrieving three points
     * TODO: (nice to have) Check if the points are located on a line. In this case, it's not a triangle.
     *
     * @param a first point,
     * @param b second point and
     * @param c third point of the triangle
     */
    public Triangle3D(Point3D a, Point3D b, Point3D c) {
        this.a = a;
        this.b = b;
        this.c = c;

        orderPoints();
    }

    /**
     * internal function to order the points A,B and C by ascending x, y and z
     */
    private void orderPoints() {
        if (b.getX() > c.getX() || (b.getX() == c.getX() && b.getY() > c.getY()) || (b.getX() == c.getX() && b.getY() == c.getY() && b.getZ() > c.getZ())) {
            Point3D temp = b;
            b = c;
            c = temp;
        }

        if (a.getX() > b.getX() || (a.getX() == b.getX() && a.getY() > b.getY()) || (a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() > b.getZ())) {
            Point3D temp = a;
            a = b;
            b = temp;
        }
        if (b.getX() > c.getX() || (b.getX() == c.getX() && b.getY() > c.getY()) || (b.getX() == c.getX() && b.getY() == c.getY() && b.getZ() > c.getZ())) {
            Point3D temp = b;
            b = c;
            c = temp;
        }
    }

    /**
     * Compare two triangles.
     *
     * @param t the triangle to compare "this" to
     * @return true if coordinates of the triangles are equal. Because of point reordering this is independent from the given order of points.
     */
    public boolean equals(Triangle3D t) {
        return (t.a.equals(a) && t.b.equals(b) && t.c.equals(c));
    }

    /**
     * Calculates and returns the area of the triangle according to Herons Formula
     * https://en.wikipedia.org/wiki/Heron%27s_formula
     * <p>
     * <pre>
     *
     *      a
     *      /\
     *  lc /   \  lb
     *    /      \
     *   /         \
     *  b-----------c
     *        la
     *
     * s =  ((la + lb + lc) / 2.0)
     * A = sqrt(s * (s - la) * (s - lb) * (s - lc))
     *
     * </pre>
     *
     * @return area of the triangle in pixels^2
     */
    public double getArea() {
        double la = c.getDistanceTo(b);
        double lb = a.getDistanceTo(c);
        double lc = b.getDistanceTo(a);

        double s = ((la + lb + lc) / 2.0);

        return Math.sqrt(s * (s - la) * (s - lb) * (s - lc));
    }

    /**
     * Calculates the area of the triangle in physical units as defined by the given calibration.
     *
     * @param calibration the pixel size is stored in this data structure
     * @return area of the triangle in physical units ^ 2
     */
    public double getArea(Calibration calibration) {
        double la = c.getDistanceTo(b, calibration);
        double lb = a.getDistanceTo(c, calibration);
        double lc = b.getDistanceTo(a, calibration);

        double s = ((la + lb + lc) / 2.0);

        return Math.sqrt(s * (s - la) * (s - lb) * (s - lc));
    }

    public String toString() {
        return a.toString() + "\t-\t" + b.toString() + "\t-\t" + c.toString() + ", area=" + new Double(getArea()).toString();
    }

    /**
     * Return the first point of the triangle, it is the point with the smallest x or  y (if x is equal) or z (if x and z were equal) value.
     *
     * @return First point of the triangle
     */
    public Point3D getA() {
        return a;
    }

    /**
     * Return the second point of the triangle
     *
     * @return Second point of the triangle
     */
    public Point3D getB() {
        return b;
    }

    /**
     * Return the third point of the triangle, it is the point with the highest x or  y (if x is equal) or z (if x and z were equal) value.
     *
     * @return Third point of the triangle
     */
    public Point3D getC() {
        return c;
    }
}
