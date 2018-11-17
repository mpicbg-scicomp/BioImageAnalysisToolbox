package de.mpicbg.scf.imgtools.geometry.create;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;

/**
 * This class provides simple threshold based segmentation functions. Its members are intended to have an image as input (and some parameters, maybe) and an ROI as output.
 * <p>
 * <p>
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2015
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
public class Thresholding {

    /**
     * Apply thresholding to an image.
     *
     * @param image        image to process
     * @param minGreyValue Minimum threshold for the segmentation
     * @param maxGreyValue Maximum threshold for the segmentation
     * @return an ROI, NO binary image!
     */
    public static Roi applyThreshold(ImagePlus image, double minGreyValue, double maxGreyValue) {
        ImageProcessor imageProcessor = image.getProcessor();
        imageProcessor.setThreshold(minGreyValue, maxGreyValue, ImageProcessor.NO_LUT_UPDATE);
        try {
            return new ThresholdToSelection().convert(imageProcessor);
        } catch (ArrayIndexOutOfBoundsException e) {
            DebugHelper.print("Thresholding", "Caught ArrayIndexOutOfBoundsException: " + e.toString());
            return null;
        }

    }

}
