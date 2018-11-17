package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.RoiUtilities;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.ImagePlus;
import ij.gui.Roi;
import java.util.ArrayList;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Plugin;

/**
 * This class allows access (Read only) to all volumes in the Volume Manager
 * as list of imglib2 regions
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: June 2016
 * <p>
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
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Import ArrayList of regions (not useful in menu)", menuPath = "")
public class ImportRegionArrayListPlugin<B extends BooleanType<B>> extends AbstractVolumeManagerPlugin {

    ArrayList<RandomAccessibleInterval<B>> regions;

    public ImportRegionArrayListPlugin() {
    }

    ;

    public ImportRegionArrayListPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }


    public void setRegions(ArrayList<RandomAccessibleInterval<B>> regions) {
        this.regions = regions;
    }


    @Override
    public void run() {

        if (regions == null || regions.size() == 0) {
            return;
        }

        VolumeManager volumeManager = this.getVolumeManager();
        SurfaceListModel surfaceData = volumeManager.getVolumeList();

        ImagePlus imp = volumeManager.getCurrentImagePlus();

        for (int r = 0; r < regions.size(); r++) {

            RandomAccessibleInterval<B> lr = regions.get(r);

            PolylineSurface pls = new PolylineSurface("Label " + (r + 1));


            int maxSlice = imp.getNSlices();
            for (int z = 1; z <= maxSlice; z++) {

                Interval interval = Intervals.createMinMax(0, 0, z - 1, imp.getWidth(), imp.getHeight(), z - 1);


                Roi roi = binaryImageToRoi(lr, interval);
                if (roi != null) {
                    roi = RoiUtilities.fixRoi(roi);

                    pls.addRoi(z, roi);
                }
            }
            surfaceData.addElement(pls.getTitle(), pls);
        }
    }


    private Roi binaryImageToRoi(RandomAccessibleInterval<B> lr, Interval interval) {
        return RoiUtilities.getRoiFromRAISlice(lr, interval);
    }

}
