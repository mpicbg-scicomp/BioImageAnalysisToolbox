package de.mpicbg.scf.volumemanager.core;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.FloatPolygon;
import java.awt.*;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
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
public class RoiUtilitiesTest {
    @Test
    public void testFixRoiWithRectangle() {
        Roi roiBefore = new Roi(50, 100, 200, 300);
        Roi roiAfter = RoiUtilities.fixRoi(roiBefore);

        assertTrue("rectangle roi equals ", de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.roisEqual(roiBefore, roiAfter));
    }

    @Test
    public void testFixRoiWithPolygonRectangle() {
        float[] xes = {50, 250, 250, 250, 50};
        float[] yes = {100, 100, 150, 400, 400};

        Roi roiBefore = new PolygonRoi(new FloatPolygon(xes, yes), Roi.POLYGON);
        Roi roiAfter = RoiUtilities.fixRoi(roiBefore);

        Rectangle bbBefore = roiBefore.getBounds();
        Rectangle bbAfter = roiAfter.getBounds();

        DebugHelper.print(this, "before bb h: " + bbBefore.width);
        DebugHelper.print(this, "before bb w: " + bbBefore.height);
        DebugHelper.print(this, "before bb x: " + bbBefore.x);
        DebugHelper.print(this, "before bb y: " + bbBefore.y);
        DebugHelper.print(this, "after bb h: " + bbAfter.width);
        DebugHelper.print(this, "after bb w: " + bbAfter.height);
        DebugHelper.print(this, "after bb x: " + bbAfter.x);
        DebugHelper.print(this, "after bb y: " + bbAfter.y);

        assertTrue("bounding boxes equal ", RoiUtilities.rectanglesEqual(roiBefore.getBounds(), roiAfter.getBounds()));
        assertTrue("polygonized rectangle roi equals ", de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.roisEqual(roiBefore, roiAfter));
    }

    @Test
    public void testFixRoisWithCompositeRois() {
        Roi roi1 = new Roi(50, 50, 100, 100);
        Roi roi2 = new Roi(175, 175, 50, 50);

        Roi roiBefore = new ShapeRoi(roi1).or(new ShapeRoi(roi2));
        Roi roiAfter = RoiUtilities.fixRoi(roiBefore);

        Rectangle bbBefore = roiBefore.getBounds();
        Rectangle bbAfter = roiAfter.getBounds();

        DebugHelper.print(this, "before bb h: " + bbBefore.width);
        DebugHelper.print(this, "before bb w: " + bbBefore.height);
        DebugHelper.print(this, "before bb x: " + bbBefore.x);
        DebugHelper.print(this, "before bb y: " + bbBefore.y);
        DebugHelper.print(this, "after bb h: " + bbAfter.width);
        DebugHelper.print(this, "after bb w: " + bbAfter.height);
        DebugHelper.print(this, "after bb x: " + bbAfter.x);
        DebugHelper.print(this, "after bb y: " + bbAfter.y);

        assertTrue("bounding boxes equal ", RoiUtilities.rectanglesEqual(roiBefore.getBounds(), roiAfter.getBounds()));
        assertTrue("polygonized rectangle roi equals ", de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.roisEqual(roiBefore, roiAfter));
    }


}