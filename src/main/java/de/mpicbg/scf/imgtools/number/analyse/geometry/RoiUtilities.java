package de.mpicbg.scf.imgtools.number.analyse.geometry;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.ImageStatistics;
import java.awt.*;

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
public class RoiUtilities {

    /**
     * Return the area of the ROI in pixels
     *
     * @param roi ROI to analyse
     * @return area in pixels
     */
    public static int getPixelCountOfRoi(Roi roi) {
        return getPixelCountOfRoi(null, roi);
    }

    /**
     * Return the area in pixels of an ROI using an underlying imageplus. This function is minimally more efficient compared to getAreaOfRoi(Roi roi)
     *
     * @param imp an image which will not be changed, the current ROI of that image is maintained as well
     * @param roi ROI to analyse
     * @return area in pixels
     */
    public static int getPixelCountOfRoi(ImagePlus imp, Roi roi) {
        if (roi == null) {
            return 0;
        }

        if (imp == null) {
            Rectangle r = roi.getBounds();
            imp = NewImage.createImage("temp", r.x + r.width, r.y + r.height, 1, 8, 0);
        }

        Roi temp = imp.getRoi();

        imp.setRoi(roi);
        ImageStatistics stats = imp.getStatistics();
        int area = stats.pixelCount;
        if (area == imp.getWidth() * imp.getHeight()) {
            area = 0;
        }
        imp.setRoi(temp);
        return area;
    }

    /**
     * Returns if an ROI contains no pixels
     *
     * @param imp if null it may be a bit less performant
     * @param roi ROI to analyse
     * @return true if the ROI is empty
     */
    public static boolean isEmptyRoi(ImagePlus imp, Roi roi) {
        if (roi == null) {
            return true;
        }

        int area = getPixelCountOfRoi(imp, roi);
        return (area == 0);
    }

    /**
     * Returns if an ROI contains no pixels
     *
     * @param roi ROI to analyse
     * @return true if the ROI is empty
     */
    public static boolean isEmptyRoi(Roi roi) {
        return isEmptyRoi(null, roi);
    }

    /**
     * Determine, if two Rois are equal
     *
     * @param roi1 First ROI
     * @param roi2 SEcond ROI
     * @return true, if both ROIs are equal.
     */
    public static boolean roisEqual(Roi roi1, Roi roi2) {
        if (roi1 == roi2) {
            return true;
        }
        if (roi1 == null || roi2 == null) {
            return false;
        }
        ShapeRoi sr1 = new ShapeRoi(roi1);
        ShapeRoi sr2 = new ShapeRoi(roi2);

        int areaRoi1 = getPixelCountOfRoi(roi1);
        int areaRoi2 = getPixelCountOfRoi(roi2);
        if (areaRoi1 != areaRoi2) {
            return false;
        }

        ShapeRoi andRoi = new ShapeRoi(sr1);
        andRoi = andRoi.and(sr2);

        ShapeRoi xorRoi = new ShapeRoi(sr1);
        xorRoi = xorRoi.xor(sr2);

        return getPixelCountOfRoi(andRoi) == areaRoi1 && (isEmptyRoi(xorRoi));

    }

}
