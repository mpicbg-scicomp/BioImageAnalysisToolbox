package de.mpicbg.scf.imgtools.number.analyse.image;

import ij.ImagePlus;
import ij.gui.NewImage;
import java.util.Arrays;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
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
public class LabelMoments3DTest {
    @Test
    public void testIfVolumesAreMeasuredCorrectly() {
        // First test: using a native ImgLib2 image
        Img<FloatType> img1 = ArrayImgs.floats(new long[]{10, 10, 10});
        testIfBoundingBoxCursorCreationWorks(img1); // passes

        // Second test: using a wrapped/converted ImagePlus
        ImagePlus imp = NewImage.createImage("test", 10, 10, 10, 32, NewImage.FILL_BLACK);
        Img<FloatType> img2 = ImageJFunctions.convertFloat(imp);
        testIfBoundingBoxCursorCreationWorks(img2); // fails
    }

    private void testIfBoundingBoxCursorCreationWorks(Img<FloatType> img) {
        // Create bounding box with width = height = depth = 1
        Interval boundingBox = Intervals.createMinMax(new long[]{5, 5, 5, 5, 5, 5});

        Cursor<FloatType> cursor = Views.interval(img, boundingBox).cursor();
        cursor.next();

        long[] pos = new long[img.numDimensions()];
        cursor.localize(pos);
        System.out.println("initial cursor position:" + Arrays.toString(pos));

        assertTrue(pos[0] == 5);
        assertTrue(pos[1] == 5);
        assertTrue(pos[2] == 5);
    }

}
