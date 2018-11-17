package de.mpicbg.scf.imgtools.image.create.image;

import de.mpicbg.scf.imgtools.image.create.labelmap.ThresholdLabeling;
import de.mpicbg.scf.imgtools.number.analyse.image.ImageAnalysisUtilities;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: MAy 2016
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
public class ImagePlusImgConverterTest {


    @Test
    public void testIfBackAndForthFloatTypeConversionWorks() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        ImagePlusImgConverter converter = new ImagePlusImgConverter(imp);
        Img<FloatType> img = converter.getImgFloatType();

        // ... do fancy image processing

        ImagePlus impResult = converter.getImagePlus(img);

        assertTrue("ImagePlus before and after conversion to FloatType are identical", ImageAnalysisUtilities.ImagesEqual(imp, impResult));
    }


    @Test
    public void testIfBackAndForthRealTypeConversionWorks() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        ImagePlusImgConverter converter = new ImagePlusImgConverter(imp);
        Img<RealType> img = converter.getImgRealType();

        // ... do fancy image processing

        ImagePlus impResult = converter.getImagePlus(img);

        assertTrue("ImagePlus before and after conversion to RealType are identical", ImageAnalysisUtilities.ImagesEqual(imp, impResult));
    }

    @Test
    public void testIfLabelmapConversionWorks() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        ImagePlusImgConverter converter = new ImagePlusImgConverter(imp);
        Img<FloatType> img = converter.getImgFloatType();

        ThresholdLabeling tl = new ThresholdLabeling();
        Img<IntType> labelMapImg = tl.Labeling(img, 16);

        ImagePlus labelMapImp = converter.getImagePlus(labelMapImg);


        assertTrue("ImagePlus and corresponding labelmap have equal dimensions", ImageAnalysisUtilities.ImagesDimensionsEqual(imp, labelMapImp));
        assertTrue("ImagePlus and corresponding labelmap have equal calibration", ImageAnalysisUtilities.ImagesCalibrationsEqual(imp, labelMapImp));
    }

}
