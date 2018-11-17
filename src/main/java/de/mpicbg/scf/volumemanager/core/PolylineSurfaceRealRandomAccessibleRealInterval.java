package de.mpicbg.scf.volumemanager.core;

import de.mpicbg.scf.imgtools.geometry.data.Contains;
import de.mpicbg.scf.imgtools.geometry.data.ContainsRealRandomAccess;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import net.imglib2.*;
import net.imglib2.type.logic.BoolType;
import net.imglib2.util.Intervals;

/**
 * This ImgLib2 wrapper allows to use a PolylineSurface as Imglib2-ROI.
 * <p>
 * Issues: It may become obsolete if imglib2-roi becomes mature.
 * <p>
 * <p>
 * Todo: An issue with PolylineSurfaces is, their first slice is 1, not 0...
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
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
public class PolylineSurfaceRealRandomAccessibleRealInterval implements RealRandomAccessibleRealInterval<BoolType>, Contains<RealLocalizable> {
    PolylineSurface polylineSurface;
    RealInterval boundingBox;

    public PolylineSurfaceRealRandomAccessibleRealInterval(PolylineSurface polylineSurface) {
        this.polylineSurface = new PolylineSurface(polylineSurface);

        double minx = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double miny = Double.POSITIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;

        // interpolate missing slices
        // determine bounding box
        Roi[] newRois = new Roi[polylineSurface.getEndSlice() + 1];
        for (int z = polylineSurface.getStartSlice(); z <= polylineSurface.getEndSlice(); z++) {
            Roi roi = polylineSurface.getRoi(z);
            if (de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(roi)) {
                roi = polylineSurface.getInterpolatedRoi(z);
                newRois[z] = roi;
            }
            double x = roi.getBounds().x;
            double y = roi.getBounds().y;
            double w = roi.getBounds().width;
            double h = roi.getBounds().height;

            if (minx > x) {
                minx = x;
            }
            if (maxx < x + w) {
                maxx = x + w;
            }
            if (miny > y) {
                miny = y;
            }
            if (maxy < y + w) {
                maxy = y + w;
            }
        }
        for (int z = polylineSurface.getStartSlice(); z <= polylineSurface.getEndSlice(); z++) {
            if (newRois[z] != null) {
                //DebugHelper.print(this, "Adding slice " + z);
                this.polylineSurface.addRoi(z, newRois[z]);
            }

        }

        if (numDimensions() == 3) {
            boundingBox = Intervals.createMinMaxReal(new double[]{minx, miny, polylineSurface.getStartSlice(), maxx, maxy, polylineSurface.getEndSlice()});
        } else {
            boundingBox = Intervals.createMinMaxReal(new double[]{minx, miny, maxx, maxy});

        }
    }


    @Override
    public double realMin(int d) {
        return boundingBox.realMin(d);
    }

    @Override
    public void realMin(double[] doubles) {
        boundingBox.realMin(doubles);
    }

    @Override
    public void realMin(RealPositionable realPositionable) {
        boundingBox.realMin(realPositionable);
    }

    @Override
    public double realMax(int d) {
        return boundingBox.realMax(d);
    }

    @Override
    public void realMax(double[] doubles) {
        boundingBox.realMax(doubles);
    }

    @Override
    public void realMax(RealPositionable realPositionable) {
        boundingBox.realMax(realPositionable);
    }

    @Override
    public RealRandomAccess<BoolType> realRandomAccess() {
        return new ContainsRealRandomAccess(this);
    }

    @Override
    public RealRandomAccess<BoolType> realRandomAccess(RealInterval realInterval) {
        return realRandomAccess();
    }

    @Override
    public boolean contains(RealLocalizable realLocalizable) {
        if (Intervals.contains(boundingBox, realLocalizable)) {
            int z = polylineSurface.getStartSlice();
            if (realLocalizable.numDimensions() > 2) {
                z = (int) realLocalizable.getDoublePosition(2);
            }

            Roi roi = polylineSurface.getRoi(z);
            if (roi == null) {
                return false;
            }
            FloatPolygon fp = roi.getFloatPolygon();
            float[] x = fp.xpoints;
            float[] y = fp.ypoints;

            int i;
            int j;
            boolean result = false;
            for (i = 0, j = fp.npoints - 1; i < fp.npoints; j = i++) {
                final double j1 = y[j];
                final double j0 = x[j];

                final double i0 = x[i];
                final double i1 = y[i];

                final double l1 = realLocalizable.getDoublePosition(1);
                final double l0 = realLocalizable.getDoublePosition(0);

                if ((i1 > l1) != (j1 > l1) && (l0 < (j0 - i0) * (l1 - i1) / (j1 - i1) + i0)) {
                    result = !result;
                }
            }

            return result;
        }
        return false;
    }

    @Override
    public Contains<RealLocalizable> copyContains() {
        return this;
    }

    @Override
    public int numDimensions() {
        return (polylineSurface.getEndSlice() == polylineSurface.getStartSlice() && polylineSurface.getStartSlice() == 1) ? 2 : 3;
    }
}
