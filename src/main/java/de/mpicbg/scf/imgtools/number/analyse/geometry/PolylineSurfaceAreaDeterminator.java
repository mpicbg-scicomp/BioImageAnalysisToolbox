package de.mpicbg.scf.imgtools.number.analyse.geometry;

import de.mpicbg.scf.imgtools.geometry.create.TriangleStripCreator;
import de.mpicbg.scf.imgtools.geometry.data.Point3D;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.geometry.data.Triangle3D;
import ij.measure.Calibration;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class allows calculation the area of a surface defined by polylines (class PolylineSurface).
 * <p>
 * It goes through pairs for neighbouring polylines (e.g. one one slice 1 and the next one on slice 9),
 * searches for triangles connecting the points of the polylines and calculates and sums up the
 * area of these triangles.
 * <p>
 * <b>Note:</b> While the class expects a PolylineSurface managing pixel-coordinates, it returns the area
 * in physical units. To measure the area in pixel-units, hand over a Calibration with pixelWidth = pixelHeight = pixelDepth = 1.
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
public class PolylineSurfaceAreaDeterminator {
    private static final boolean verbose = false;

    private final PolylineSurface pls;
    private final Calibration calib;

    /**
     * Constructor
     *
     * @param pls a PolylineSurface containing at least two polylines on different slices.
     * @param c   Calibration of the image where the polylines were drawn on. This is used to calculate the area in physical units.
     */
    public PolylineSurfaceAreaDeterminator(PolylineSurface pls, Calibration c) {
        this.pls = pls;
        this.calib = c;
    }

    /**
     * Return the area of the surface.
     * TODO: To calculate areas between closes polylines (circles, rectangles, polygons), handling must be changed a bit. Probably it's enough, to add the first point of the polyline to its end.
     *
     * @return area
     */
    public double getArea() {
        if (pls == null || calib == null || !pls.isInitialized()) {

            return 0;
        }

        int minSlice = pls.getStartSlice();
        int maxSlice = pls.getEndSlice();

        ArrayList<Point3D> previousPl = null;
        int previousSlice = 0;
        float area = 0;

        for (int i = minSlice; i <= maxSlice; i++) {
            ArrayList<Point3D> pl = pls.getPointList(i);


            if (pl != null && pl.size() > 0) {
                if (previousPl != null) {
                    if (verbose) {
                        System.out.println("Found two polylines on slices " + (new Integer(previousSlice).toString()) + " and " + (new Integer(i).toString()));
                    }
                    area += getAreaBetweenPolylines(previousPl, pl);
                }
                previousPl = pl;
                previousSlice = i;
            }
        }

        return area;
    }

    /**
     * This function is intended to return the area between two polylines. It asks getTriangleList to
     * divide the area between the polylines into triangles and afterwards sums up the area of them.
     *
     * @param pl1 First Polyline as an array of Point3Ds containing voxel coordinates
     * @param pl2 First Polyline as an array of Point3Ds containing voxel coordinates
     * @return area in square physical units
     */
    private double getAreaBetweenPolylines(ArrayList<Point3D> pl1, ArrayList<Point3D> pl2) {
        ArrayList<Triangle3D> triangleList = TriangleStripCreator.getTriangleList(pl1, pl2, calib);
        if (verbose) {
            System.out.println("Found number of triangles: " + new Integer(triangleList.size()).toString());
        }
        float area = 0;

        //calculate area between last and current polyline
        for (int i = 0; i < triangleList.size(); i++) {
            Triangle3D triangle = triangleList.get(i);

            if (verbose) {
                System.out.println("Triangle " + new Integer(i).toString() + "X:\t" + String.format("%.2g", triangle.getA().getX()) + "\t" + String.format("%.2g", triangle.getB().getX()) + "\t" + String.format("%.2g", triangle.getC().getX()) + "\t");
                System.out.println("Triangle " + new Integer(i).toString() + "Y:\t" + String.format("%.2g", triangle.getA().getY()) + "\t" + String.format("%.2g", triangle.getB().getY()) + "\t" + String.format("%.2g", triangle.getC().getY()) + "\t");
                System.out.println("Triangle " + new Integer(i).toString() + "Z:\t" + String.format("%.2g", triangle.getA().getZ()) + "\t" + String.format("%.2g", triangle.getB().getZ()) + "\t" + String.format("%.2g", triangle.getC().getZ()) + "\t" + String.format("%.2g", triangle.getArea()) + "\t" + String.format("%.2g", triangle.getArea(calib)));
            }
            area += triangle.getArea(calib);
        }
        return area;
    }


    /**
     * public main for testing.
     *
     * @param args argumens
     * @throws IOException throws an exception
     */
    public static void main(final String... args) throws IOException {


    }

}
