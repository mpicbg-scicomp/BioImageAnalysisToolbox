package de.mpicbg.scf.volumemanager.core;

import ij.ImagePlus;
import ij.gui.*;
import java.awt.*;

/**
 * Utilities class for visualisation tools (especially ROIs)
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: June 2016
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
    @Deprecated //moved to imgtools
    public static void setRoiDotted(Roi roi) {
        BasicStroke stroke = roi.getStroke();

        float lineWidth = 1;
        if (stroke != null) {
            lineWidth = stroke.getLineWidth();
        }
        BasicStroke newStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{3, 3}, 0);
        roi.setStroke(newStroke);
    }

    @Deprecated //moved to imgtools
    public static void showRoi(Roi roi, String title) {
        ImagePlus imp = NewImage.createByteImage(title, roi.getBounds().x + roi.getBounds().width + 1, roi.getBounds().y + roi.getBounds().height + 1, 1, NewImage.FILL_BLACK);
        imp.setRoi(roi);
        imp.show();
    }

    /**
     * To put an ROI (r) to an Overlay of an image (imp) in a specific color. The current ROI of the image is maintained.
     *
     * @param r   ROI to put
     * @param imp target image
     * @param c   target color
     */
    @Deprecated
    public static synchronized void fixRoiAsOverlay(Roi r, ImagePlus imp, Color c) {
        if (c == null) {
            fixRoiAsOverlay(r, imp);
        }
        if (!(r instanceof TextRoi)) {
            r = new ShapeRoi(r);
        }
        Roi temp = imp.getRoi();
        Color origColor = Roi.getColor();
        if (c != null) {
            Roi.setColor(c);
        }

        if (r != null) {

            if (imp.getOverlay() == null) {
                imp.setOverlay(new Overlay());
            }
            imp.getOverlay().add(r);

        }
        Roi.setColor(origColor);
        imp.setRoi(temp);
    }

    public static synchronized void fixRoiAsOverlay(Roi r, ImagePlus imp) {
        if (imp.getOverlay() == null) {
            imp.setOverlay(new Overlay());
        }
        if (r.getType() != Roi.POINT && r.getType() != Roi.POLYLINE && (!(r instanceof TextRoi))) {
            Roi oldRoi = r;
            r = new ShapeRoi(r);
            r.setStrokeColor(oldRoi.getStrokeColor());
            r.setStrokeWidth(oldRoi.getStrokeWidth());
            r.setFillColor(oldRoi.getFillColor());
            r.setStroke(oldRoi.getStroke());

        }
        imp.getOverlay().add(r);
    }
}