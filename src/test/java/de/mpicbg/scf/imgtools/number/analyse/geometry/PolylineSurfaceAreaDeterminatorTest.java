package de.mpicbg.scf.imgtools.number.analyse.geometry;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2017
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
public class PolylineSurfaceAreaDeterminatorTest {

    @Test
    public void measuredAreaOfSyntheticSurfacesShouldBeCorrect() {

        double accuracyThreshold = 0.00001;

        Calibration c = new Calibration();
        c.pixelWidth = 1;
        c.pixelHeight = 1;
        c.pixelDepth = 1;

        //-----------------------------------------------------------------------------
        // a single triangle
        PolylineSurface p1 = new PolylineSurface("pl");

        float[] x = {1, 6};
        float[] y = {2, 3};
        p1.addRoi(5, new PolygonRoi(x, y, Roi.POLYLINE));
        p1.addRoi(3, new PointRoi(8, 6));

        double area = new PolylineSurfaceAreaDeterminator(p1, c).getArea();
        double correctArea = 8.26135540008545;
        assertTrue("a single triangle " + area + " == " + correctArea, Math.abs(area - correctArea) < accuracyThreshold);

        //-----------------------------------------------------------------------------
        // two triangles (one point on first slice, three points on second slice)
        PolylineSurface p2 = new PolylineSurface("pl");

        float[] x1 = {1, 2, 3};
        float[] y1 = {0, 0, 0};
        p2.addRoi(2, new PolygonRoi(x1, y1, Roi.POLYLINE));
        p2.addRoi(1, new PointRoi(1, 0));

        area = new PolylineSurfaceAreaDeterminator(p2, c).getArea();
        correctArea = 1.0;
        assertTrue("two triangles (one point on first slice, three points on second slice) " + area + " == " + correctArea, Math.abs(area - correctArea) < accuracyThreshold);

        //-----------------------------------------------------------------------------
        // same but reverse z-order
        PolylineSurface p2a = new PolylineSurface("pl");
        p2a.addRoi(1, new PolygonRoi(x1, y1, Roi.POLYLINE));
        p2a.addRoi(2, new PointRoi(1, 0));

        area = new PolylineSurfaceAreaDeterminator(p2a, c).getArea();
        correctArea = 1.0;
        assertTrue("reverse z-order: two triangles (one point on first slice, three points on second slice) " + area + " == " + correctArea, Math.abs(area - correctArea) < accuracyThreshold);


        //-----------------------------------------------------------------------------
        // two triangles building a square (two points on two slices)
        PolylineSurface p3 = new PolylineSurface("pl");

        float[] x2 = {1, 2};
        float[] y2 = {0, 0};

        p3.addRoi(1, new PolygonRoi(x2, y2, Roi.POLYLINE));
        p3.addRoi(2, new PolygonRoi(x2, y2, Roi.POLYLINE));

        area = new PolylineSurfaceAreaDeterminator(p3, c).getArea();
        correctArea = 1.0;
        assertTrue("two triangles building a square (two points on two slices) " + area + " == " + correctArea, Math.abs(area - correctArea) < accuracyThreshold);

        //-----------------------------------------------------------------------------
        // 8 triangles defined by five points each on two slices
        PolylineSurface p4 = new PolylineSurface("pl");

        //more or less random numbers between 0 and 5
        float[] x3 = {0, 2, (float) 3.5, (float) 4.25, 5};
        float[] x4 = {0, (float) 0.5, (float) 0.7, 4, 5};

        float[] y3 = {0, 0, 0, 0, 0};

        p4.addRoi(1, new PolygonRoi(x3, y3, Roi.POLYLINE));
        p4.addRoi(2, new PolygonRoi(x4, y3, Roi.POLYLINE));

        area = new PolylineSurfaceAreaDeterminator(p4, c).getArea();
        correctArea = 5.0;
        assertTrue("8 triangles defined by five points each on two slices " + area + " == " + correctArea, Math.abs(area - correctArea) < accuracyThreshold);

        //-----------------------------------------------------------------------------
        // same but reverse z-order
        PolylineSurface p4a = new PolylineSurface("pl");

        p4a.addRoi(2, new PolygonRoi(x3, y3, Roi.POLYLINE));
        p4a.addRoi(1, new PolygonRoi(x4, y3, Roi.POLYLINE));

        area = new PolylineSurfaceAreaDeterminator(p4a, c).getArea();
        correctArea = 5.0;
        assertTrue("reverse z-order: 8 triangles defined by five points each on two slices " + area + " == " + correctArea, Math.abs(area - correctArea) < accuracyThreshold);

        //-----------------------------------------------------------------------------
        // 12 triangles defined by 8+6 points
        PolylineSurface p5 = new PolylineSurface("pl");

        //more or less random numbers between 0 and 5
        float[] x5 = {0, (float) 0.1, (float) 0.2, (float) 0.3, (float) 0.35, (float) 0.4, (float) 4.25, 5};
        float[] y5 = {0, 0, 0, 0, 0, 0, 0, 0};

        float[] x6 = {0, 1, 2, 3, 4, 5};
        float[] y6 = {0, 0, 0, 0, 0, 0};

        p5.addRoi(1, new PolygonRoi(x5, y5, Roi.POLYLINE));
        p5.addRoi(2, new PolygonRoi(x6, y6, Roi.POLYLINE));

        area = new PolylineSurfaceAreaDeterminator(p5, c).getArea();
        correctArea = 5.0;
        assertTrue("12 triangles defined by 8+6 points " + area + " == " + correctArea, Math.abs(area - correctArea) < accuracyThreshold);

        //-----------------------------------------------------------------------------
        //
        PolylineSurface p6 = new PolylineSurface("pl");

        //more or less random numbers between 0 and 5
        float[] x7 = {1, 2, 3, 4, 5};
        float[] y7 = {1, 2, 3, 4, 5};

        float[] x8 = {6, 9};
        float[] y8 = {1, 2};

        p6.addRoi(1, new PolygonRoi(x7, y7, Roi.POLYLINE));
        p6.addRoi(2, new PolygonRoi(x8, y8, Roi.POLYLINE));

        //-----------------------------------------------------------------------------
        //area should be equal to the prior one
        PolylineSurface p7 = new PolylineSurface("pl4 8 triangles");

        float[] x9 = {5, 4, 3, 2, 1};
        float[] y9 = {5, 4, 3, 2, 1};

        float[] x10 = {9, 6};
        float[] y10 = {2, 1};

        p7.addRoi(1, new PolygonRoi(x9, y9, Roi.POLYLINE));
        p7.addRoi(2, new PolygonRoi(x10, y10, Roi.POLYLINE));

        area = new PolylineSurfaceAreaDeterminator(p6, c).getArea();
        correctArea = new PolylineSurfaceAreaDeterminator(p7, c).getArea();
        assertTrue("2+5 points, 4 triangles, result independent from order " + area + " == " + correctArea, Math.abs(area - correctArea) < accuracyThreshold);
    }
}
