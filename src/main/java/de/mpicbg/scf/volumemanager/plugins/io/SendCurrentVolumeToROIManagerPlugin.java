package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import org.scijava.plugin.Plugin;

/**
 * This plugins copies all slices of the current volume to the ROI manager
 * <p>
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

@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Send current volume to ROI manager", menuPath = "File>Export", priority = 10000)
public class SendCurrentVolumeToROIManagerPlugin extends AbstractVolumeManagerPlugin {

    public SendCurrentVolumeToROIManagerPlugin() {
    }

    ;

    public SendCurrentVolumeToROIManagerPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    public boolean silent = false;

    public boolean exportInterpolatedROIs = true;

    public boolean showDialog() {
        GenericDialog gd = new GenericDialog("Send current volume to ROI manager");
        gd.addCheckbox("Include interpolated slices", exportInterpolatedROIs);
        gd.showDialog();

        if (gd.wasCanceled()) {
            return false;
        }

        exportInterpolatedROIs = gd.getNextBoolean();
        return true;
    }

    @Override
    public void run() {
        if (!silent) {
            if (!showDialog()) {
                return;
            }
        }

        VolumeManager vm = getVolumeManager();
        PolylineSurface pls = vm.getCurrentVolumeUnsafe();

        if (pls == null) {
            return;
        }

        RoiManager rm = RoiManager.getInstance();
        if (rm == null) {
            rm = new RoiManager();
        }

        vm.lockManipulation();

        ImagePlus imp = vm.getCurrentImagePlus();

        for (int z = pls.getStartSlice(); z <= pls.getEndSlice(); z++) {
            Roi roi = pls.getRoi(z);
            if (roi == null && exportInterpolatedROIs) {
                roi = pls.getInterpolatedRoi(z);
            }
            if (roi == null) {
                continue;
            }
            roi.setPosition(imp.getChannel(), z, imp.getFrame());
            roi.setName(pls.getTitle() + " slice " + z);
            rm.addRoi(roi);
        }


        vm.unlockManipulation();
    }
}
