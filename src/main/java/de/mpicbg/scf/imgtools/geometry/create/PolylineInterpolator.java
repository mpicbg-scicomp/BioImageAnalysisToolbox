package de.mpicbg.scf.imgtools.geometry.create;

import de.mpicbg.scf.imgtools.geometry.data.Point3D;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.geometry.data.Triangle3D;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.FloatPolygon;
import java.util.ArrayList;

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
public class PolylineInterpolator {

    private final double eqTolerance = 0.1;

    private ArrayList<Triangle3D> cachedTriangleList = null;
    private ArrayList<Point3D> cacheSource1 = null;
    private ArrayList<Point3D> cacheSource2 = null;
    private int cacheSourcePos1;
    private int cacheSourcePos2;

    public PolylineInterpolator() {

    }

    public void invalidateCache() {
        cachedTriangleList = null;
    }

    private void setSource(ArrayList<Point3D> pl1, ArrayList<Point3D> pl2, int z1, int z2) {
        cacheSourcePos1 = z1;
        cacheSourcePos2 = z2;
        if (pl1 == cacheSource1 && pl2 == cacheSource2) {
            return;
        }
        cacheSource1 = pl1;
        cacheSource2 = pl2;
        invalidateCache();
    }

    public Roi getInterpolatedRoi(int z, PolylineSurface pls, boolean extrapolationAllowed) {
        if (pls.getRoi(z) != null) {
            return pls.getRoi(z);
        }

        if (pls.getStartSlice() > z) {
            if (extrapolationAllowed) {
                return pls.getRoi(pls.getStartSlice());
            } else {
                return null;
            }
        }

        if (pls.getEndSlice() < z) {
            if (extrapolationAllowed) {
                return pls.getRoi(pls.getEndSlice());
            } else {
                return null;
            }
        }


        // initialisation
        int previousPolylineSlide = 0;
        int nextPolylineSlide = 0;
        boolean previousFound = false;
        boolean nextFound = false;

        //find polylines above/below current slice
        for (int s = pls.getStartSlice(); s <= pls.getEndSlice(); s++) {
            if (pls.getRoi(s) != null) {
                if (s < z) {
                    if (previousPolylineSlide < s || !previousFound) {
                        previousPolylineSlide = s;
                    }
                    previousFound = true;
                }

                if (s > z) {
                    if (nextPolylineSlide > s || !nextFound) {
                        nextPolylineSlide = s;
                    }
                    nextFound = true;
                }
            }
        }

        //If both found: interpolate between them
        if (nextFound && previousFound) {
            setSource(pls.getPointList(previousPolylineSlide), pls.getPointList(nextPolylineSlide), previousPolylineSlide, nextPolylineSlide);
        } else {
            return null;
        }

        double factor = Math.abs((z - cacheSourcePos1) / (double) (cacheSourcePos1 - cacheSourcePos2));

        if (pls.getRoi(previousPolylineSlide).getFloatPolygon().npoints == 1 && pls.getRoi(nextPolylineSlide).getFloatPolygon().npoints == 1) {
            FloatPolygon fpNext = pls.getRoi(previousPolylineSlide).getFloatPolygon();
            FloatPolygon fpPrev = pls.getRoi(nextPolylineSlide).getFloatPolygon();

            double newX = fpPrev.xpoints[0] * factor + fpNext.xpoints[0] * (1.0 - factor);
            double newY = fpPrev.ypoints[0] * factor + fpNext.ypoints[0] * (1.0 - factor);

            return new PointRoi(newX, newY);
        }

        if (cachedTriangleList == null) {
            cachedTriangleList = TriangleStripCreator.getTriangleList(cacheSource1, cacheSource2, new Calibration());
        }
        if (cachedTriangleList == null) {
            return null;
        }

        ArrayList<Double> x = new ArrayList<Double>();
        ArrayList<Double> y = new ArrayList<Double>();

        Triangle3D triangle1 = cachedTriangleList.get(cachedTriangleList.size() - 1);
        Triangle3D triangle2 = cachedTriangleList.get(0);

        Edge formerEdge = Edge.findCommonEdge(triangle1, triangle2);
        if (formerEdge == null) {
            Edge temp = Edge.findCommonEdge(cachedTriangleList.get(0), cachedTriangleList.get(1));
            formerEdge = Edge.findEdgeCandidate(cachedTriangleList.get(0), temp);
        }
        if (formerEdge == null) {
            return null;
        }
        processLine(formerEdge.a, formerEdge.b, factor, x, y, z);

        for (Triangle3D triangle : cachedTriangleList) {
            formerEdge = Edge.findEdgeCandidate(triangle, formerEdge);

            processLine(formerEdge.a, formerEdge.b, factor, x, y, z);
        }

        if (Math.abs(x.get(0) - x.get(x.size() - 1)) < eqTolerance && Math.abs(y.get(0) - y.get(y.size() - 1)) < eqTolerance) {
            x.remove(x.size() - 1);
            y.remove(y.size() - 1);
        }

        FloatPolygon fp = new FloatPolygon();
        for (int i = 0; i < x.size(); i++) {
            fp.addPoint(x.get(i), y.get(i));
        }

        if (pls.getRoi(previousPolylineSlide).getType() == Roi.POLYLINE && pls.getRoi(nextPolylineSlide).getType() == Roi.POLYLINE) {
            return new PolygonRoi(fp, Roi.POLYLINE);
        } else if (pls.getRoi(previousPolylineSlide).getType() == Roi.POLYLINE) {
            FloatPolygon fp2 = pls.getRoi(previousPolylineSlide).getFloatPolygon();
            return breakPolygonIntoPolyline(fp, fp2.xpoints[0], fp2.ypoints[0]);
        } else if (pls.getRoi(nextPolylineSlide).getType() == Roi.POLYLINE) {
            FloatPolygon fp2 = pls.getRoi(nextPolylineSlide).getFloatPolygon();
            return breakPolygonIntoPolyline(fp, fp2.xpoints[0], fp2.ypoints[0]);
        } else {
            return new PolygonRoi(fp, Roi.POLYGON);
        }
    }

    private PolygonRoi breakPolygonIntoPolyline(FloatPolygon fp, float x, float y) {
        double minimumDistance = Double.POSITIVE_INFINITY;
        int minimumDistanceIndex = -1;
        for (int i = 0; i < fp.npoints; i++) {
            double distance = Math.sqrt(Math.pow(fp.xpoints[i] - x, 2) + Math.pow(fp.ypoints[i] - y, 2));
            if (distance < minimumDistance) {
                minimumDistance = distance;
                minimumDistanceIndex = i;
            }
        }

        FloatPolygon outFp = new FloatPolygon();
        for (int i = minimumDistanceIndex; i < fp.npoints; i++) {
            outFp.addPoint(fp.xpoints[i], fp.ypoints[i]);
        }
        for (int i = 0; i < minimumDistanceIndex; i++) {
            outFp.addPoint(fp.xpoints[i], fp.ypoints[i]);
        }

        return new PolygonRoi(outFp, Roi.POLYLINE);
    }


    private void processLine(Point3D a, Point3D b, double factor, ArrayList<Double> x, ArrayList<Double> y, double z) {
        if (Math.abs(a.getZ() - b.getZ()) < eqTolerance) {
            // ignore lines which lie on the same slice
            return;
        }
        if (a.getZ() > b.getZ()) {
            Point3D temp = a;
            a = b;
            b = temp;
        }

        x.add(b.getX() * factor + a.getX() * (1.0 - factor));
        y.add(b.getY() * factor + a.getY() * (1.0 - factor));
    }


}
