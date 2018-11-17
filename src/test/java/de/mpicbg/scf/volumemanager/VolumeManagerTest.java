package de.mpicbg.scf.volumemanager;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.number.analyse.image.ImageAnalysisUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.plugins.io.CreateLabelMapPlugin;
import de.mpicbg.scf.volumemanager.plugins.io.ImportLabelMapPlugin;
import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/*
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: October 2016
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
public class VolumeManagerTest {

    /**
     * Import and re-export a labelmap and see if the results are equal.
     */
    @Test
    public void testIfInputAndOutputOfLabelMapsAreNotChangingTheLabelMap3D() {
        if (SystemUtilities.isHeadless()) {
            DebugHelper.print(this, "Cancelling test, because it only runs in non-headless mode.");
            return;
        }

        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        VolumeManager volumeManager = new VolumeManager();
        volumeManager.setCurrentImage(imp);

        // Import a 3D label map image into volume manager
        ImportLabelMapPlugin ilmp = new ImportLabelMapPlugin(volumeManager);
        ilmp.setLabelMap(imp);

        // Export label map as new image
        CreateLabelMapPlugin clmp = new CreateLabelMapPlugin(volumeManager);
        ImagePlus imp2 = clmp.getLabelMap();

        // compare results
        assertTrue("Input and output labelmaps 3D are identical ", ImageAnalysisUtilities.ImagesEqual(imp, imp2));

    }

    @Test
    public void testIfInputAndOutputOfLabelMapsAreNotChangingTheLabelMa2D() {
        if (SystemUtilities.isHeadless()) {
            DebugHelper.print(this, "Cancelling test, because it only runs in non-headless mode.");
            return;
        }

        ImagePlus imp = IJ.openImage("src/test/resources/labelmap_singleslice.tif");

        VolumeManager volumeManager = new VolumeManager();
        volumeManager.setCurrentImage(imp);

        // import a 2D label map image to the volume manager
        ImportLabelMapPlugin ilmp = new ImportLabelMapPlugin(volumeManager);
        ilmp.setLabelMap(imp);

        // export label map as new image
        CreateLabelMapPlugin clmp = new CreateLabelMapPlugin(volumeManager);
        ImagePlus imp2 = clmp.getLabelMap();


        assertTrue("Input and output labelmaps 2D are identical ", ImageAnalysisUtilities.ImagesEqual(imp, imp2));

    }


}
