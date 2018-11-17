package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import java.io.File;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
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
public class SaveAndLoadPluginTest {
    @Test
    public void testIfSavingAndLoadingWorks()
    {
        if (SystemUtilities.isHeadless()) {
            return;
        }

        String tempFilename = "temp2.zip";

        ImagePlus imp = NewImage.createByteImage("test", 100,100,100, NewImage.FILL_BLACK);
        VolumeManager vm = VolumeManager.getInstance();
        vm.dispose();
        vm = VolumeManager.getInstance();
        vm.setCurrentImage(imp);

        PolylineSurface pls = vm.getCurrentVolume();
        pls.addRoi(1, new Roi(0,0, 20, 20));
        pls.addRoi(3, new Roi(10,10, 20, 20));
        pls.setTransparency(0.67);
        pls.setLineThickness(4);
        pls.viewInterpolatedLinesDotted = false;

        File tempFile = new File(tempFilename);
        if (tempFile.exists()) {
            tempFile.delete();
        }


        SaveToDiscPlugin stdp = new SaveToDiscPlugin(vm);
        stdp.setFilename(tempFilename);
        stdp.run();

        vm.dispose();

        vm = VolumeManager.getInstance();
        assertTrue(vm.getVolumeList().size() == 0);

        LoadFromDiscPlugin lfdp = new LoadFromDiscPlugin(vm);
        lfdp.load(tempFilename);

        PolylineSurface plsFromDisc = vm.getVolume(0);
        assertTrue(pls.getTransparency() == 0.67);
        assertTrue(pls.getLineThickness() == 4);
        assertTrue(pls.viewInterpolatedLinesDotted == false);
        DebugHelper.print(this, "Startslice: " + pls.getStartSlice());
        DebugHelper.print(this, "getEndSlice: " + pls.getEndSlice());
        assertTrue( pls.getStartSlice() == 1);
        assertTrue( pls.getEndSlice() == 3);
    }
}