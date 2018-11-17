package de.mpicbg.scf.imgtools.geometry.create;

import de.mpicbg.scf.imgtools.geometry.data.Point3D;
import de.mpicbg.scf.imgtools.geometry.data.Triangle3D;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.measure.Calibration;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
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
public class TriangleStripCreator {
    public static boolean verbose = false;

    /**
     * Splits the given lists of points into two list. Both lists start with the points being closest together between the polylines.
     * <p>
     * If this function should calculate a closed polygon, the first and the last coordinates of the lists should be identical.
     * <p>
     * <pre>
     *                  +
     *  $lt;-pl1Decreasing + pl1Increasing -$gt;
     *        (A)       +    (A,B,C)
     *                  +
     * -----------------A-------B-----C------- pl1 (A,B,C)
     *                 /|\      /    /
     *                / | \    |   /
     *               /  |  \   | /
     *              /   |   \ //
     * ------------D----E----F---------------- pl2 (D,E,F)
     *                  +
     *  $lt;-pl2Decreasing + pl2Increasing -$gt;
     *       (D,E)      +     (E,F)
     *                  +
     * </pre>
     * <p>
     * Afterwards, triangles are found one the left and right side separately using {@link #getCandidateTriangleList}
     * <p>
     * The whole procedure is done two times. The first time as described, the second time using second point list in reverse order. Afterwards, two triangle
     * lists result and the one with the smaller area is returned.
     *
     * @param pl1   First Polyline as an array of Point3Ds containing voxel coordinates
     * @param pl2   First Polyline as an array of Point3Ds containing voxel coordinates
     * @param calib The Calibration to describt the voxel size of the current image space.
     * @return List of Triangles describing the whole area between both polylines
     */
    public static ArrayList<Triangle3D> getTriangleList(ArrayList<Point3D> pl1, ArrayList<Point3D> pl2, Calibration calib) {
        if (verbose)
            DebugHelper.print("PointListToTriangleConverter", "pl1 first and last equal : " + pl1.get(0).toString() + " == " + pl1.get(pl1.size() - 1));
        if (pl1.get(0).equals(pl1.get(pl1.size() - 1)) && pl1.size() > 1) // then pl1 is a polygon, because it starts and ends at the same point
        {
            if (verbose) DebugHelper.print("PointListToTriangleConverter", "they equal");
            if (!pl2.get(0).equals(pl2.get(pl2.size() - 1))) // then pl2 is no polygon and also no single point
            {
                if (verbose) DebugHelper.print("PointListToTriangleConverter", "reorder");
                reorderPolygon(pl1, pl2, calib);
            }
        } else {
            if (verbose)
                DebugHelper.print("PointListToTriangleConverter", "pl2 first and last equal : " + pl2.get(0).toString() + " == " + pl2.get(pl2.size() - 1));

            if (pl2.get(0).equals(pl2.get(pl2.size() - 1)) && pl2.size() > 1) // then pl2 is a polygon, because it starts and ends at the same point
            {
                if (verbose) DebugHelper.print("PointListToTriangleConverter", "they equal");
                if (!pl1.get(0).equals(pl1.get(pl1.size() - 1))) // then pl1 is no polygon and also no single point
                {
                    if (verbose) DebugHelper.print("PointListToTriangleConverter", "reorder");
                    reorderPolygon(pl2, pl1, calib);
                }
            }
        }

        if (pl1.size() + pl2.size() < 3 || pl1.size() < 1 || pl2.size() < 1) {
            return null;
        }

        // ---------------------------------------------------------------------------------------
        // Reorganize point lists; make them start with the two closes points
        double minDistance = pl1.get(0).getDistanceTo(pl2.get(0), calib);
        int idxPl1 = 0;
        int idxPl2 = 0;

        if (verbose) DebugHelper.print("PointListToTriangleConverter", "pl1 elements: " + (pl1.size()));
        if (verbose) DebugHelper.print("PointListToTriangleConverter", "pl2 elements: " + (pl2.size()));

        double minArea = Double.POSITIVE_INFINITY;
        ArrayList<Triangle3D> result = null;

        Point3D p1start = pl1.get(0);
        Point3D p1end = pl1.get(pl1.size() - 1);

        Point3D p2start = pl2.get(0);
        Point3D p2end = pl2.get(pl2.size() - 1);

        double tolerance = 0.001;

        if ((
                Math.abs(p1start.getX() - p1end.getX()) > tolerance ||
                        Math.abs(p1start.getY() - p1end.getY()) > tolerance
        ) && (
                Math.abs(p2start.getX() - p2end.getX()) > tolerance ||
                        Math.abs(p2start.getY() - p2end.getY()) > tolerance
        )) {
            // special case: both are polylines
            if (verbose) DebugHelper.print("TriangleStripGenerator", "special case: both are polylines!");

            ArrayList<Point3D> pl1increasingIdx = new ArrayList<Point3D>();
            pl1increasingIdx.addAll(pl1.subList(0, pl1.size()));

            ArrayList<Point3D> pl1decreasingIdx = new ArrayList<Point3D>();
            pl1decreasingIdx.addAll(pl1.subList(0, pl1.size()));
            Collections.reverse(pl1decreasingIdx);

            ArrayList<Point3D> pl2increasingIdx = new ArrayList<Point3D>();
            pl2increasingIdx.addAll(pl2.subList(0, pl2.size()));

            ArrayList<Triangle3D> candidatesA = new ArrayList<Triangle3D>();
            candidatesA.addAll(getCandidateTriangleList(pl1increasingIdx, pl2increasingIdx, calib));

            ArrayList<Triangle3D> candidatesB = new ArrayList<Triangle3D>();
            candidatesB.addAll(getCandidateTriangleList(pl1decreasingIdx, pl2increasingIdx, calib));


            double areaA = 0;
            double areaB = 0;

            for (int k = 0; k < candidatesA.size(); k++) {
                areaA += candidatesA.get(k).getArea();
            }
            for (int k = 0; k < candidatesB.size(); k++) {
                areaB += candidatesB.get(k).getArea();
            }

            if (verbose) DebugHelper.print("PointListToTriangleConverter", "Comparing A and B solution of triangle lists: " + areaA + " < " + areaB);
            if (areaA < areaB) {
                minArea = areaA;
                result = candidatesA;
            } else {
                minArea = areaB;
                result = candidatesB;
            }
        } else {

            for (int i = 0; i < pl1.size(); i++) {
                for (int j = 0; j < pl2.size(); j++) {

                    idxPl1 = i;
                    idxPl2 = j;

                    // ---------------------------------------------------------------------------------------
                    // build two sub-lists for each direction:
                    ArrayList<Point3D> pl1increasingIdx = new ArrayList<Point3D>();
                    pl1increasingIdx.addAll(pl1.subList(idxPl1, pl1.size()));

                    ArrayList<Point3D> pl1decreasingIdx = new ArrayList<Point3D>();
                    pl1decreasingIdx.addAll(pl1.subList(0, idxPl1 + 1));
                    Collections.reverse(pl1decreasingIdx);

                    ArrayList<Point3D> pl2increasingIdx = new ArrayList<Point3D>();
                    pl2increasingIdx.addAll(pl2.subList(idxPl2, pl2.size()));

                    ArrayList<Point3D> pl2decreasingIdx = new ArrayList<Point3D>();
                    pl2decreasingIdx.addAll(pl2.subList(0, idxPl2 + 1));
                    Collections.reverse(pl2decreasingIdx);

                    // ---------------------------------------------------------------------------------------
                    // Go in both directions from the starting point(s) and build triangles.

                    ArrayList<Triangle3D> candidatesA = new ArrayList<Triangle3D>();
                    candidatesA.addAll(getCandidateTriangleList(pl1increasingIdx, pl2increasingIdx, calib));

                    ArrayList<Triangle3D> temp = getCandidateTriangleList(pl1decreasingIdx, pl2decreasingIdx, calib);

                    if (candidatesA.size() > 0 && temp.size() > 0 && Edge.findCommonEdge(temp.get(0), candidatesA.get(candidatesA.size() - 1)) == null) {
                        Collections.reverse(temp);
                    }
                    candidatesA.addAll(temp);

                    ArrayList<Triangle3D> candidatesB = new ArrayList<Triangle3D>();
                    candidatesB.addAll(getCandidateTriangleList(pl1increasingIdx, pl2decreasingIdx, calib));

                    temp = getCandidateTriangleList(pl1decreasingIdx, pl2increasingIdx, calib);

                    if (candidatesB.size() > 0 && temp.size() > 0 && Edge.findCommonEdge(temp.get(0), candidatesB.get(candidatesB.size() - 1)) == null) {
                        Collections.reverse(temp);
                    }

                    candidatesB.addAll(temp);

                    // determine which candidate is shorter
                    double areaA = 0;
                    double areaB = 0;

                    for (int k = 0; k < candidatesA.size(); k++) {
                        areaA += candidatesA.get(k).getArea();
                    }
                    for (int k = 0; k < candidatesB.size(); k++) {
                        areaB += candidatesB.get(k).getArea();
                    }

                    if (areaA < minArea) {
                        minArea = areaA;
                        result = candidatesA;
                    }

                    if (areaB < minArea) {
                        minArea = areaB;
                        result = candidatesB;
                    }
                }
            }
        }
        if (verbose) DebugHelper.print("PointListToTriangleConverter", "minarea: " + minArea);
        return result;
    }

    /**
     * Find the smallest possible triangles starting at a line between pl1[0] and pl2[0]. A triangle always contains the line from the last triangle and the
     * closes point to this line. Rather:
     * <p>
     * <pre>
     * -A----C----------F-----
     *
     * -B--------D--E---------
     * </pre>
     * <p>
     * In this scenario, it starts with the line AB and the closest point C to build triangle ABC In the next step, the line BC is taken together with the point
     * D to build triangle BCD Afterwards, triangles CDE and CEF are defined. Finally, all points are connected by triangles.
     *
     * @param pl1 First Polyline as an array of Point3Ds containing voxel coordinates
     * @param pl2 First Polyline as an array of Point3Ds containing voxel coordinates
     * @return a list of
     */
    private static ArrayList<Triangle3D> getCandidateTriangleList(ArrayList<Point3D> pl1, ArrayList<Point3D> pl2, Calibration calib) {
        ArrayList<Triangle3D> candidates = new ArrayList<Triangle3D>();

        if (pl1.size() + pl2.size() < 3 || pl1.size() < 1 || pl2.size() < 1) {
            if (verbose) {
                System.out.println("Leaving because " + pl1.size() + " " + pl2.size());
            }
            return candidates;
        }

        int currentIdx1 = 0;
        int currentIdx2 = 0;

        if (verbose) {
            System.out.println("Looking for triangle candidates.");
        }

        while (currentIdx1 < pl1.size() - 1 || currentIdx2 < pl2.size() - 1) {

            Point3D A = pl1.get(currentIdx1);
            Point3D B = pl2.get(currentIdx2);
            if (verbose) {
                DebugHelper.print("PointListToTriangleConverter", currentIdx1 + "  " + currentIdx2 + ": " + A.getDistanceTo(B, calib));
            }
            Point3D candidateCa = null;
            Point3D candidateCb = null;
            Point3D C = null;

            if (currentIdx1 < pl1.size() - 1) {
                candidateCa = pl1.get(currentIdx1 + 1);
            }
            if (currentIdx2 < pl2.size() - 1) {
                candidateCb = pl2.get(currentIdx2 + 1);
            }

            if (candidateCa == null && candidateCb != null) {
                C = candidateCb;
                currentIdx2++;
            } else if (candidateCa != null && candidateCb == null) {
                C = candidateCa;
                currentIdx1++;
            } else if (candidateCa != null && candidateCb != null) {
                double distanceACb = A.getDistanceTo(candidateCb, calib);
                double distanceBCa = B.getDistanceTo(candidateCa, calib);
                if (distanceACb < distanceBCa) {
                    C = candidateCb;
                    currentIdx2++;
                } else {
                    C = candidateCa;
                    currentIdx1++;
                }
            } else {
                if (verbose) {
                    DebugHelper.print("PointListToTriangleConverter", "This should never happen!");
                }
            }
            candidates.add(new Triangle3D(A, B, C));
        }

        return candidates;
    }

    private static void reorderPolygon(ArrayList<Point3D> pl1, ArrayList<Point3D> pl2, Calibration calib) {

        // look for the shortest Point to begin and start
        int idxPl1 = 0;
        int idxPl2 = 0;

        if (verbose) DebugHelper.print("PointListToTriangleConverter", "pl1 elements: " + (pl1.size()));
        if (verbose) DebugHelper.print("PointListToTriangleConverter", "pl2 elements: " + (pl2.size()));

        double minDistance = pl1.get(0).getDistanceTo(pl2.get(0), calib);
        for (int i = 0; i < pl1.size(); i++) {
            for (int j = 0; j < pl2.size(); j += pl1.size() - 1) // only look at neighboring polyline start and end
            {
                double distance = pl1.get(i).getDistanceTo(pl2.get(j), calib);
                if (distance < minDistance) {
                    minDistance = distance;
                    idxPl1 = i;
                    idxPl2 = j;
                }
            }
        }

        if (idxPl1 == 0 || idxPl1 == pl1.size() - 1) {
            return;
        }

        pl1.remove(pl1.size() - 1);
        for (int i = 0; i < idxPl1; i++) {
            pl1.add(pl1.get(0));
            pl1.remove(0);
        }
        pl1.add(pl1.get(0));

    }
}
