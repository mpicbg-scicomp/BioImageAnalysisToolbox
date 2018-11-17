package de.mpicbg.scf.fijiplugins.ui.labelmap;

import de.mpicbg.scf.imgtools.image.create.labelmap.RoiArrayToLabelmapConverter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.MessageDialog;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: May 2016
 *
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
public class RoiManagerToLabelMapPlugin  implements PlugInFilter {
    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_8G + DOES_16 + DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        ImagePlus imp = IJ.getImage();

        RoiManager rm = RoiManager.getInstance();
        if (rm == null)
        {
            new MessageDialog(null, "Error", "Roi Manager is not open.").setVisible(true);
            return;
        }

        Roi[] rois = rm.getRoisAsArray();

        ImagePlus labelMap = new RoiArrayToLabelmapConverter(rois, imp.getWidth(), imp.getHeight()).getResult();

        labelMap.setDisplayRange(0, rois.length);

        labelMap.show();

        IJ.run(labelMap, "glasbey", "");



    }
}

