package de.mpicbg.scf.volumemanager.plugins.analysis;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.io.LoadFromDiscPlugin;
import de.mpicbg.scf.volumemanager.plugins.io.SurfaceViewerPlugin;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
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
public class SurfaceAnalysisPluginTest {
    @Test
    public void testIfAreaMeasurementIsConsistentWithVisualisedArea() {
        if (SystemUtilities.isHeadless()) {
            DebugHelper.print(this, "This test does not work headless.");
            return;
        }

        ImagePlus imp = NewImage.createByteImage("temp", 100, 100, 100, NewImage.FILL_BLACK);
        imp.getProcessor().set(1, 1, 1);

        imp.getProcessor().set(1, 1, 1);
        imp.getCalibration().pixelDepth = 10;

        if (!SystemUtilities.isHeadless()) {
            imp.show();
        }
        VolumeManager vm = VolumeManager.getInstance();
        vm.setCurrentImage(imp);
        vm.initializeAllPlugins();

        LoadFromDiscPlugin lfdp = new LoadFromDiscPlugin(vm);
        lfdp.load("src/test/resources/test_surface.zip");

        SurfaceViewerPlugin svp = new SurfaceViewerPlugin(vm);
        DebugHelper.print("SVP", "hello a ");
        DebugHelper.trackDeltaTime("main");
        svp.run();
        DebugHelper.print("SVP", "hello b ");
        DebugHelper.trackDeltaTime("main");

        SurfaceAnalysisPlugin sap = new SurfaceAnalysisPlugin(vm);
        DebugHelper.print("main", "area: " + sap.getResults().getStringValue(1, 0));
        DebugHelper.print("main", "area: " + svp.getLastVisualisedSurfaceArea());
        assertEquals("visualised and measured area equal ", sap.getResults().getValueAsDouble(1, 0), svp.getLastVisualisedSurfaceArea(), 0.1);

    }

    @Test
    public void testIfSurfaceAreaMeasurementsAreReasonable() {
        if (SystemUtilities.isHeadless()) {
            DebugHelper.print(this, "This test does not work headless.");
            return;
        }

        PolylineSurface pls = new PolylineSurface("test");
        pls.addRoi(5, new Roi(10, 10, 10, 10));
        pls.addRoi(15, new Roi(10, 10, 10, 10));

        VolumeManager vm = new VolumeManager();
        vm.setCurrentImage(NewImage.createByteImage("temp", 100, 100, 100, NewImage.FILL_BLACK));
        vm.addVolume(pls);

        SurfaceAnalysisPlugin sap = new SurfaceAnalysisPlugin(vm);
        double surfaceArea = sap.getResults().getValueAsDouble(1, 0);

        // its 400 and not 600, because the box has not bottom and top closing
        assertEquals("area measurements of a simple box like volume is correct", surfaceArea, 400, 0.1);


    }
}