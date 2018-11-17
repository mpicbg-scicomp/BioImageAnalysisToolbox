package de.mpicbg.scf.imgtools.geometry.create;

import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
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
public class ThresholdingTest {

    @Test
    public void testIfThresholdingWorks() {
        ImagePlus imp = IJ.openImage("src/test/resources/leaf.jpg");
        new ImageConverter(imp).convertToGray8();

        Roi testRoi = Thresholding.applyThreshold(imp, 50, 100);
        System.out.println(de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.getPixelCountOfRoi(testRoi));

        Roi referenceRoi = null;
        referenceRoi = RoiDecoder.open("src/test/resources/referenceLeafThresholdingResult.roi");

        assertTrue("Check if test and Reference ROI on Leaf are equal", de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.roisEqual(testRoi, referenceRoi));
        imp.close();
    }


    @Test
    public void test() {
        ImagePlus labelMap = NewImage.createByteImage("temp", 100, 100, 1, NewImage.FILL_BLACK);

        ImageProcessor ip = labelMap.getProcessor();

        Roi roi1 = new Roi(10, 10, 20, 20);
        ip.setRoi(roi1);
        ip.add(1);

        Roi roi2 = new Roi(10, 40, 20, 20);
        ip.setRoi(roi2);
        ip.add(2);

        ImagePlus tempImp = new ImagePlus("test ", ip);

        tempImp.killRoi();
        Roi r1 = Thresholding.applyThreshold(tempImp, 1, 1);
        Roi r2 = Thresholding.applyThreshold(tempImp, 2, 2);


        assertTrue("Area 1 matches reference ", RoiUtilities.roisEqual(r1, roi1));
        assertFalse("Area 1 does not match reference 2 ", RoiUtilities.roisEqual(r1, roi2));
        assertTrue("Area 2 matches reference ", RoiUtilities.roisEqual(r2, roi2));
    }

}
