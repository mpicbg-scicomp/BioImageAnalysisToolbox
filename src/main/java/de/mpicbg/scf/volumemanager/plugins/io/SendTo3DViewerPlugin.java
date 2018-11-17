package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.visualisation.labelregionviewer.DefaultLabeling3DViewer;
import de.mpicbg.scf.imgtools.ui.visualisation.labelregionviewer.Labeling3DViewer;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.PolylineSurfaceRealRandomAccessibleRealInterval;
import de.mpicbg.scf.volumemanager.core.RoiUtilities;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.Calibration;
import java.awt.*;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BoolType;
import org.scijava.plugin.Plugin;

/**
 * This class sends all volumes to the good old ImageJ 3D Viewer
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
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Send to 3D Viewer", menuPath = "File>Export", priority = 10000)
public class SendTo3DViewerPlugin extends AbstractVolumeManagerPlugin {

    public SendTo3DViewerPlugin() {
    }

    ;

    public SendTo3DViewerPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }


    @Override
    public void run() {
        VolumeManager vm = getVolumeManager();
        ImagePlus imp = vm.getCurrentImagePlus();

        vm.lockSwitchingToOtherImages();

        Labeling3DViewer l3dv = new DefaultLabeling3DViewer();


        for (int i = 0; i < vm.length(); i++) {

            PolylineSurface pls = vm.getVolume(i);

            DebugHelper.print(this, "step 1");
            PolylineSurfaceRealRandomAccessibleRealInterval plsrrari = new PolylineSurfaceRealRandomAccessibleRealInterval(pls);

            DebugHelper.print(this, "step 2");
            RandomAccessibleInterval<BoolType> rai = RoiUtilities.raster(plsrrari);

            Color color = pls.fillColor;
            if (color == null) {
                color = pls.lineColor;
            }
            if (color == null) {
                color = Color.white;
            }

            l3dv.addLabel(rai, color);
        }

        vm.unlockSwitchingToOtherImages();
        vm.setCurrentImage(imp);

        Calibration calib = imp.getCalibration();
        l3dv.setVoxelSize(calib.pixelWidth, calib.pixelHeight, calib.pixelDepth);
        l3dv.show();

    }

    public static void main(String... args) {
        new ImageJ();
        ImagePlus imp = NewImage.createByteImage("temp", 100, 100, 100, NewImage.FILL_BLACK);
        imp.setRoi(new Roi(10, 10, 10, 10));
        imp.show();

        PolylineSurface pls = new PolylineSurface("test");
        pls.addRoi(5, new Roi(10, 10, 10, 10));
        pls.addRoi(15, new Roi(10, 10, 10, 10));

        VolumeManager vm = new VolumeManager();
        vm.setCurrentImage(NewImage.createByteImage("temp", 100, 100, 100, NewImage.FILL_BLACK));
        vm.addVolume(pls);

        SendTo3DViewerPlugin st3dvp = new SendTo3DViewerPlugin(vm);
        st3dvp.run();
    }
}
