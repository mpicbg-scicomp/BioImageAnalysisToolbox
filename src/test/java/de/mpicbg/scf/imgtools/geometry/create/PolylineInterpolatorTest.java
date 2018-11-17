package de.mpicbg.scf.imgtools.geometry.create;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.FloatPolygon;
import java.util.Arrays;
import org.junit.Test;

import static de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.roisEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
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
public class PolylineInterpolatorTest {
    @Test
    public void simpleTest() {
        PolylineInterpolator pi = new PolylineInterpolator();

        PolylineSurface pls = new PolylineSurface("temp");
        pls.addRoi(1, new Roi(10, 10, 100, 100));
        pls.addRoi(5, new Roi(30, 30, 100, 100));

        Roi roi2 = pi.getInterpolatedRoi(2, pls, false);
        DebugHelper.print(this, "" + Arrays.toString(roi2.getFloatPolygon().xpoints));
        DebugHelper.print(this, "" + Arrays.toString(roi2.getFloatPolygon().ypoints));


        Roi roi = pi.getInterpolatedRoi(3, pls, false);
        DebugHelper.print(this, "" + Arrays.toString(roi.getFloatPolygon().xpoints));
        DebugHelper.print(this, "" + Arrays.toString(roi.getFloatPolygon().ypoints));

        assertTrue(roisEqual(roi, new Roi(20, 20, 100, 100)));
    }


    @Test
    public void testInterpolationOfPolylines() {
        PolylineInterpolator pi = new PolylineInterpolator();

        PolylineSurface pls = new PolylineSurface("temp");
        FloatPolygon fp1 = new FloatPolygon();
        fp1.addPoint(0, 0);
        fp1.addPoint(0, 10);
        fp1.addPoint(10, 10);
        fp1.addPoint(10, 0);
        pls.addRoi(1, new PolygonRoi(fp1, Roi.POLYLINE));


        FloatPolygon fp2 = new FloatPolygon();
        fp2.addPoint(0, 10);
        fp2.addPoint(10, 10);
        fp2.addPoint(10, 0);
        fp2.addPoint(0, 0);
        pls.addRoi(5, new PolygonRoi(fp2, Roi.POLYLINE));

        Roi roi = pi.getInterpolatedRoi(3, pls, false);
        assertTrue("The interpolation of two polylines is a polyline", roi.getType() == Roi.POLYLINE);

        FloatPolygon fp3 = roi.getFloatPolygon();

        DebugHelper.print(this, "x: " + Arrays.toString(fp3.xpoints));
        DebugHelper.print(this, "y: " + Arrays.toString(fp3.ypoints));

        // this should be something like
        Arrays.equals(fp3.xpoints, new float[]{0.0f, 0.0f, 5.0f, 10.0f, 10.0f, 10.0f, 5.0f});
        Arrays.equals(fp3.ypoints, new float[]{5.0f, 10.0f, 10.0f, 10.0f, 5.0f, 0.0f, 0.0f});
    }


    @Test
    public void testComplicatedInterpolationOfPolylines() {
        PolylineInterpolator pi = new PolylineInterpolator();

        PolylineSurface pls = new PolylineSurface("temp");
        FloatPolygon fp1 = new FloatPolygon();
        fp1.addPoint(0, 5);
        fp1.addPoint(0, 10);
        fp1.addPoint(10, 10);
        fp1.addPoint(10, 0);
        fp1.addPoint(0, 0);
        fp1.addPoint(0, 2.5);
        pls.addRoi(1, new PolygonRoi(fp1, Roi.POLYLINE));


        FloatPolygon fp2 = new FloatPolygon();
        fp2.addPoint(0, 10);
        fp2.addPoint(10, 10);
        fp2.addPoint(10, 0);
        fp2.addPoint(0, 0);
        fp2.addPoint(0, 7.5);
        pls.addRoi(5, new PolygonRoi(fp2, Roi.POLYLINE));

        Roi roi = pi.getInterpolatedRoi(3, pls, false);

        assertTrue("The interpolation of two polylines is a polyline", roi.getType() == Roi.POLYLINE);

        FloatPolygon fp3 = roi.getFloatPolygon();

        DebugHelper.print(this, "x: " + Arrays.toString(fp3.xpoints));
        DebugHelper.print(this, "y: " + Arrays.toString(fp3.ypoints));

        // this should be something like
        Arrays.equals(fp3.xpoints, new float[]{0.0f, 0.0f, 5.0f, 10.0f, 10.0f, 10.0f, 5.0f, 0, 0, 0});
        Arrays.equals(fp3.ypoints, new float[]{7.5f, 10.0f, 10.0f, 10.0f, 5.0f, 0.0f, 0.0f, 0, 1.25f, 5});

    }


    @Test
    public void testRealisticInterpolationOfPolylines() {
        PolylineInterpolator pi = new PolylineInterpolator();

        PolylineSurface pls = new PolylineSurface("temp");

        Roi roi5 = RoiDecoder.open("src/test/resources/interpol1.roi");

        pls.addRoi(5, roi5);

        Roi roi1 = RoiDecoder.open("src/test/resources/interpol2.roi");

        pls.addRoi(1, roi1);

        Roi roi = pi.getInterpolatedRoi(3, pls, false);

        assertTrue("The interpolation of two polylines is a polyline", roi.getType() == Roi.POLYLINE);

        FloatPolygon fp3 = roi.getFloatPolygon();

        DebugHelper.print(this, "x: " + Arrays.toString(fp3.xpoints));
        DebugHelper.print(this, "y: " + Arrays.toString(fp3.ypoints));

        DebugHelper.print(this, "l1 = " + roi1.getFloatPolygon().getLength(true));
        DebugHelper.print(this, "l5 = " + roi5.getFloatPolygon().getLength(true));
        DebugHelper.print(this, "l3 = " + fp3.getLength(true));
        DebugHelper.print(this, "l3expected = " + (0.5 * (roi1.getFloatPolygon().getLength(true) + roi5.getFloatPolygon().getLength(true))));

        double tolerance = 0.0001;

        assertEquals("Interpolated polyline has approximately the average length of both input polylines",
                (0.5 * (roi1.getFloatPolygon().getLength(true) + roi5.getFloatPolygon().getLength(true))),
                fp3.getLength(true), tolerance);

    }
}