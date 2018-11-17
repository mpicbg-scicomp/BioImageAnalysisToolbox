package de.mpicbg.scf.imgtools.geometry.filter;

import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.RoiEnlarger;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: March 2017
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
public class GeometryFilterUtilities {

    /**
     * @param imp 0
     * @param roi 0
     * @return 0
     * @deprecated Use splitRoi(Roi roi) instead
     */
    @Deprecated
    public static Roi[] splitRoi(ImagePlus imp, Roi roi) {
        return splitRoi(roi);
    }

    /**
     * Split ROIs in separate connected components, if possible
     *
     * @param roi Roi to split
     * @return array of splitted rois.
     */
    public static Roi[] splitRoi(Roi roi) {
        if (de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(roi)) {
            return new Roi[]{};
        }
        return (new ShapeRoi(roi)).getRois();
    }

    /**
     * Connect ROIs to a big one. Overlapping rois is ok, but not mandatory.
     *
     * @param rois Array of ROIs to join
     * @return One single ROI containing all pixels the ROIs contained before.
     */
    public static Roi joinRois(Roi[] rois) {
        if (rois.length == 0) {
            return null;
        }
        ShapeRoi result = new ShapeRoi(rois[0]);
        for (int i = 1; i < rois.length; i++) {
            result = result.or(new ShapeRoi(rois[i]));
        }
        return result;
    }

    /**
     * Return the intersection area of two ROIs
     *
     * @param rA First ROI to intersect
     * @param rB Second ROI to intersect
     * @return Intersection area of  both ROIs
     */
    public static Roi intersect(Roi rA, Roi rB) {
        if (de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(rA) || de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(rB)) {
            return null;
        }
        ShapeRoi srA = new ShapeRoi(rA);
        ShapeRoi srB = new ShapeRoi(rB);

        return srA.and(srB);
    }

    /**
     * Return the union area of two ROIs
     *
     * @param rA First ROI to unite
     * @param rB Second ROI to unite
     * @return Union area of  both ROIs
     */
    public static Roi unite(Roi rA, Roi rB) {
        if (de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(rA) || de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(rB)) {
            return null;
        }
        ShapeRoi srA = new ShapeRoi(rA);
        ShapeRoi srB = new ShapeRoi(rB);

        return srA.or(srB);
    }

    /**
     * Return the subtract area of two ROIs
     *
     * @param rA First ROI to subtract the second from
     * @param rB Second ROI to subtract from the first
     * @return Remaining area after Subtraction
     */
    public static Roi subtract(Roi rA, Roi rB) {
        if (de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(rA) || de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(rB)) {
            return null;
        }
        ShapeRoi srA = new ShapeRoi(rA);
        ShapeRoi srB = new ShapeRoi(rB);

        return srA.not(srB);
    }

    /**
     * Return the exclusive OR area (union minus intersection) of two ROIs
     *
     * @param rA First ROI
     * @param rB Second ROI
     * @return XOR area of  both ROIs
     */
    public static Roi xor(Roi rA, Roi rB) {
        if (de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(rA) || de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(rB)) {
            return null;
        }
        ShapeRoi srA = new ShapeRoi(rA);
        ShapeRoi srB = new ShapeRoi(rB);

        return srA.xor(srB);
    }


    public static Roi applyRoiClosing(Roi roi, double marginInPixels) {
        Roi sroi = new ShapeRoi(roi);
        if (RoiUtilities.getPixelCountOfRoi(sroi) > 0) {
            DebugHelper.print("GeometricFilterUtilities", "pixels in roi to close: " + RoiUtilities.getPixelCountOfRoi(sroi));
            sroi = RoiEnlarger.enlarge(sroi, marginInPixels);
            if (RoiUtilities.getPixelCountOfRoi(sroi) > 0) {
                sroi = RoiEnlarger.enlarge(sroi, -marginInPixels);
            }
        }
        return sroi;
    }

}
