package de.mpicbg.scf.imgtools.ui.visualisation;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
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
public class GeometryVisualisationUtilities {

    /**
     * Return a 'Random' Color. In fact it returns every time another color, but not really random ones.
     *
     * @param seed ith color
     * @return Return a 'Random' Color. In fact it returns every time another color, but not really random ones.
     */
    public static java.awt.Color getRandomColor(int seed) {
        switch ((int) (seed % 30)) {
            case 0:
                return new java.awt.Color(255, 128, 0);
            case 1:
                return new java.awt.Color(255, 0, 128);
            case 2:
                return new java.awt.Color(128, 255, 0);
            case 3:
                return new java.awt.Color(128, 0, 255);
            case 4:
                return new java.awt.Color(255, 128, 0);
            case 5:
                return new java.awt.Color(128, 255, 0);

            case 6:
                return new java.awt.Color(192, 255, 255);
            case 7:
                return new java.awt.Color(255, 192, 255);
            case 8:
                return new java.awt.Color(255, 255, 192);
            case 9:
                return new java.awt.Color(192, 192, 255);
            case 10:
                return new java.awt.Color(255, 192, 192);
            case 11:
                return new java.awt.Color(192, 255, 192);

            case 12:
                return new java.awt.Color(64, 255, 255);
            case 13:
                return new java.awt.Color(255, 64, 255);
            case 14:
                return new java.awt.Color(255, 255, 64);
            case 15:
                return new java.awt.Color(64, 64, 255);
            case 16:
                return new java.awt.Color(255, 64, 64);
            case 17:
                return new java.awt.Color(64, 255, 64);

            case 18:
                return new java.awt.Color(0, 255, 255);
            case 19:
                return new java.awt.Color(255, 0, 255);
            case 20:
                return new java.awt.Color(255, 255, 0);
            case 21:
                return new java.awt.Color(0, 0, 255);
            case 22:
                return new java.awt.Color(255, 0, 0);
            case 23:
                return new java.awt.Color(0, 255, 0);

            case 24:
                return new java.awt.Color(0, 128, 255);
            case 25:
                return new java.awt.Color(0, 255, 128);
            case 26:
                return new java.awt.Color(128, 0, 255);
            case 27:
                return new java.awt.Color(0, 128, 255);
            case 28:
                return new java.awt.Color(255, 0, 128);
            default:
                return new java.awt.Color(0, 255, 128);
        }
    }

    /**
     * To put an ROI (r) to an Overlay of an image (imp) in a specific color. The current ROI of the image is maintained.
     *
     * @param r   ROI to put
     * @param imp target image
     * @param c   target color
     */
    public static void fixRoiAsOverlay(Roi r, ImagePlus imp, Color c) {
        Roi temp = imp.getRoi();
        Color origColor = Roi.getColor();
        if (c != null) {
            Roi.setColor(c);
        }

        if (r != null) {
            imp.setRoi(r);

            IJ.run(imp, "Add Selection...", "");

        }
        Roi.setColor(origColor);
        imp.setRoi(temp);
    }

    public static void setRoiDotted(Roi roi) {
        BasicStroke stroke = roi.getStroke();

        float lineWidth = 1;
        if (stroke != null) {
            lineWidth = stroke.getLineWidth();
        }
        BasicStroke newStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{10, 10}, 0);
        roi.setStroke(newStroke);
    }

    public static void showRoi(Roi roi, String title) {
        ImagePlus imp = NewImage.createByteImage(title, roi.getBounds().x + roi.getBounds().width + 1, roi.getBounds().y + roi.getBounds().height + 1, 1, NewImage.FILL_BLACK);
        imp.setRoi(roi);
        imp.show();
    }

}
