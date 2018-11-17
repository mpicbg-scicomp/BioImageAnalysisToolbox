package de.mpicbg.scf.imgtools.image.create.labelmap;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: May 2016
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
public class RoiArrayToLabelmapConverterTest {

    @Test
    public void testIfConversionWorks() {
        Roi roi1 = new Roi(0, 0, 100, 100);
        Roi roi2 = new Roi(100, 100, 100, 100);
        Roi roi3 = new Roi(0, 100, 100, 100);
        Roi[] rois = {roi1, roi2, roi3};

        ImagePlus imp = new RoiArrayToLabelmapConverter(rois, 200, 200).getResult();

        //check some positions in the image on correct label id
        ImageProcessor ip = imp.getProcessor();
        ;
        assertTrue("position 1 has correct label", ip.get(0, 0) == 1);
        assertTrue("position 2 has correct label", ip.get(100, 100) == 2);
        assertTrue("position 3 has correct label", ip.get(99, 99) == 1);
        assertTrue("position 4 has correct label", ip.get(199, 199) == 2);
        assertTrue("position 4 has correct label", ip.get(100, 0) == 0);

        //check average label id
        assertTrue("average labelmap value is correct", imp.getStatistics().mean == 1.5);
    }
}