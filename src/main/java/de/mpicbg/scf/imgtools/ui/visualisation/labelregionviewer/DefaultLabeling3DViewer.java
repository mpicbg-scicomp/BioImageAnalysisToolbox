package de.mpicbg.scf.imgtools.ui.visualisation.labelregionviewer;


import de.mpicbg.scf.imgtools.ui.visualisation.labelregionviewer.labelingcolorings.LabelingRGB;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij3d.ImageJ3DViewer;
import java.awt.*;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.BooleanType;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: September 2016
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
public class DefaultLabeling3DViewer<B extends BooleanType<B>> implements Labeling3DViewer<B> {

    LabelingRGB<B> labelingRGB;
    Calibration calibration;

    public DefaultLabeling3DViewer() {
        labelingRGB = new LabelingRGB<B>();
        calibration = new Calibration();
        calibration.pixelWidth = 1;
        calibration.pixelHeight = 1;
        calibration.pixelDepth = 1;
    }


    @Override
    public void addLabel(RandomAccessibleInterval<B> label, Color color) {
        labelingRGB.addRoi(label, color);
    }

    @Override
    public void setVoxelSize(double w, double h, double d) {
        calibration.pixelWidth = w;
        calibration.pixelHeight = h;
        calibration.pixelDepth = d;
    }

    @Override
    public void show() {

        long[] dims = new long[4];
        labelingRGB.dimensions(dims);

        ImagePlus imp2 = ImageJFunctions.wrapFloat(labelingRGB, "RGB test");
        final ImagePlus imp = new Duplicator().run(imp2);

        imp.setDimensions((int) dims[2], (int) dims[3], 1);
        imp.setOpenAsHyperStack(true);

        for (int c = 1; c <= 3; c++) {
            imp.setC(c);
            imp.setDisplayRange(0, 255);
        }

        imp.setCalibration(calibration);
        imp.setTitle("RGB " + getUniqueIdentifier());
        imp.show();

        IJ.run(imp, "Make Composite", "display=Composite");
        IJ.run(imp, "RGB Color", "slices");

        try {
            new ImageJ3DViewer().run("");

        } catch (Exception e) {
            System.out.println("Ex: " + e.toString());
        }
        imp.hide();
        imp.close();
    }


    private String getUniqueIdentifier() {
        return "" + System.currentTimeMillis();
    }
}
