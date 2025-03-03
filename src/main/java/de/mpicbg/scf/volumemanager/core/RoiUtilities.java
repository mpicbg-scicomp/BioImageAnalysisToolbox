package de.mpicbg.scf.volumemanager.core;

import de.mpicbg.scf.imgtools.geometry.create.Thresholding;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.FloatPolygon;
import java.awt.*;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * This class contains convencience functions ROI in ImageJ1/Image2/Imglib2 handling.
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: June 2016
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
public class RoiUtilities {

    public static Roi fixRoi(Roi roi) {
        if (roi == null) {
            return null;
        }

        if (roi.getType() == Roi.POLYLINE) {
            return roi;
        }

        Roi[] roiArr = new ShapeRoi(roi).getRois();
        if (roiArr.length == 1) {
            if (roiArr[0].getBounds().x != roi.getBounds().x || roiArr[0].getBounds().y != roi.getBounds().y) {
                FloatPolygon fp = roiArr[0].getFloatPolygon();

                for (int i = 0; i < fp.npoints; i++) {
                    fp.xpoints[i] += roi.getBounds().x;
                    fp.ypoints[i] += roi.getBounds().y;
                }

                return new PolygonRoi(fp, Roi.POLYGON);
            }
            return roiArr[0];

        }
        if (roiArr.length == 0) {
            return roi;
        }

        FloatPolygon firstPolygon = roiArr[0].getFloatPolygon();

        for (int i = 1; i < roiArr.length; i++) {
            FloatPolygon secondPolygon = roiArr[i].getFloatPolygon();

            double minimumSquaredDistance = Double.MAX_VALUE;

            int firstShortestIndex = 0;
            int secondShortestIndex = 0;

            // find closest points
            for (int firstCount = 0; firstCount < firstPolygon.npoints; firstCount++) {
                for (int secondCount = 0; secondCount < secondPolygon.npoints; secondCount++) {
                    double distance = Math.pow(firstPolygon.xpoints[firstCount] - secondPolygon.xpoints[secondCount], 2) +
                            Math.pow(firstPolygon.ypoints[firstCount] - secondPolygon.ypoints[secondCount], 2);

                    if (distance < minimumSquaredDistance) {
                        minimumSquaredDistance = distance;
                        firstShortestIndex = firstCount;
                        secondShortestIndex = secondCount;
                    }
                }
            }

            FloatPolygon summedPolygon = new FloatPolygon();


            for (int j = 0; j <= firstShortestIndex; j++) {
                summedPolygon.addPoint(firstPolygon.xpoints[j], firstPolygon.ypoints[j]);
            }

            for (int j = secondShortestIndex; j < secondPolygon.npoints; j++) {
                summedPolygon.addPoint(secondPolygon.xpoints[j], secondPolygon.ypoints[j]);
            }


            for (int j = 0; j <= secondShortestIndex; j++) {
                summedPolygon.addPoint(secondPolygon.xpoints[j], secondPolygon.ypoints[j]);
            }


            for (int j = firstShortestIndex; j < firstPolygon.npoints; j++) {
                summedPolygon.addPoint(firstPolygon.xpoints[j], firstPolygon.ypoints[j]);
            }

            firstPolygon = summedPolygon;
        }

        PolygonRoi pr = new PolygonRoi(firstPolygon, Roi.POLYGON);
        if (pr.getBounds().x != roi.getBounds().x || pr.getBounds().y != roi.getBounds().y) {
            pr.getBounds().x = roi.getBounds().x;
            pr.getBounds().y = roi.getBounds().y;
        }
        return pr;
    }

    public static boolean rectanglesEqual(Rectangle r1, Rectangle r2) {
        return
                r1.x == r2.x &&
                        r1.y == r2.y &&
                        r1.width == r2.width &&
                        r1.height == r2.height;
    }


    public static RandomAccessibleInterval<BoolType> raster(RealRandomAccessibleRealInterval<BoolType> cr) {
        int n = cr.numDimensions();

        long[] minmax = new long[n * 2];
        for (int d = 0; d < n; d++) {
            minmax[d] = (long) Math.floor(cr.realMin(d));
            minmax[d + n] = (long) Math.ceil(cr.realMax(d));
        }

        Interval interval = Intervals.createMinMax(minmax);
        return Views.interval(Views.raster(cr), interval);
    }


    public static <B extends BooleanType<B>> Roi getRoiFromRAISlice(RandomAccessibleInterval<B> lr, Interval interval) {
        IterableRegion<B> iterable = Regions.iterable(lr);

        net.imglib2.Cursor<Void> cursor = iterable.inside().cursor();

        Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(new long[]{interval.max(0), interval.max(1)});
        RandomAccess<UnsignedByteType> ira = img.randomAccess();

        long[] position3 = new long[3];
        long[] position2 = new long[2];

        int countPixels = 0;
        while (cursor.hasNext()) {
            cursor.next();
            cursor.localize(position3);

            if (position3[0] >= interval.min(0) &&
                    position3[1] >= interval.min(1) &&
                    position3[2] >= interval.min(2) &&
                    position3[0] <= interval.max(0) &&
                    position3[1] <= interval.max(1) &&
                    position3[2] <= interval.max(2)) {


                position2[0] = position3[0];
                position2[1] = position3[1];

                ira.setPosition(position3);
                ira.get().set((byte) 255);

                countPixels++;
            }
        }

        if (countPixels > 0) {
            ImagePlus maskImage = ImageJFunctions.wrapUnsignedByte(img, "");
            Roi roi = Thresholding.applyThreshold(maskImage, 128, 256);
            return roi;
        } else {
            return null;
        }
    }

}
