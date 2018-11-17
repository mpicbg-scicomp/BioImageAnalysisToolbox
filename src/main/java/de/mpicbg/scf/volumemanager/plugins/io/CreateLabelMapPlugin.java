package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import org.scijava.plugin.Plugin;

/**
 * The CreateLabelMapPlugin allows to export all entries in the Volume Manager
 * as a label map ImagePlus
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
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Create labelmap", menuPath = "File>Export", priority = 500)
public class CreateLabelMapPlugin extends AbstractVolumeManagerPlugin {

    public CreateLabelMapPlugin() {
    }

    ;

    public CreateLabelMapPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public void run() {
        ImagePlus map = getLabelMap();
        if (map == null) {
            DebugHelper.print(this, "Failed to export label map");
            return;
        }
        map.show();
    }

    public ImagePlus getLabelMap() {
        VolumeManager sm = getVolumeManager();
        sm.lockManipulation();
        sm.lockSwitchingToOtherImages();

        SurfaceListModel surfaceData = sm.getVolumeList();
        ImagePlus imp = sm.getCurrentImagePlus();
        if (imp == null) {
            return null;
        }

        sm.lockSwitchingToOtherImages();

        ImagePlus map = NewImage.createFloatImage("Labelmap " + getUniqueIdentifier(), imp.getWidth(), imp.getHeight(), imp.getNSlices(), NewImage.FILL_BLACK);

        for (int z = 0; z < imp.getNSlices(); z++) {
            map.setZ(z + 1);
            ImageProcessor ip = map.getProcessor();

            for (int s = 0; s < surfaceData.size(); s++) {
                PolylineSurface pls = surfaceData.getSurface(s);
                Roi roi = pls.getRoi(z + 1);

                if (roi == null) {
                    roi = pls.getInterpolatedRoi(z + 1, false);
                }
                if (roi != null) {
                    ip.setColor(s + 1);
                    ip.fill(roi);
                }
            }
        }
        sm.unlockSwitchingToOtherImages();
        map.setCalibration(imp.getCalibration().copy());

        sm.unlockSwitchingToOtherImages();
        sm.unlockManipulation();
        return map;

    }

    private String getUniqueIdentifier() {
        return "" + System.currentTimeMillis();
    }
}
