package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.volumemanager.VolumeManager;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
public class ImportLabelMapPluginTest {

    @Test
    public void testIfCompositeLabelMapsAreReadCorrectly() {
        ImagePlus imp = IJ.openImage("src/test/resources/compositelabel.tif");
        if (!SystemUtilities.isHeadless()) {
            imp.killRoi();

            new ImageJ();
            imp.show();
        }
        VolumeManager volumeManager = new VolumeManager();
        volumeManager.setCurrentImage(imp);

        ImportLabelMapPlugin ilmp = new ImportLabelMapPlugin(volumeManager);
        ilmp.setLabelMap(imp);

        imp.setRoi(volumeManager.getVolume(0).getInterpolatedRoi(1));

        assertTrue("average inside == 1 ", imp.getStatistics().mean == 1.0);

        if (!SystemUtilities.isHeadless()) {
            IJ.run(imp, "Make Inverse", "");
            assertTrue("average outside == 0 ", imp.getStatistics().mean == 0.0);
        }
    }

    @Test
    public void testIfEmptyImageSliceIsReadCorrectly() {
        ImagePlus imp = NewImage.createByteImage("", 100, 100, 2, NewImage.FILL_BLACK);
        imp.getProcessor().set(3, 3, 1);
        imp.getProcessor().set(3, 4, 1);
        imp.getProcessor().set(4, 3, 1);
        imp.getProcessor().set(4, 4, 1);
        imp.killRoi();

        if (!SystemUtilities.isHeadless()) {
            new ImageJ();
            imp.show();
        }

        VolumeManager volumeManager = new VolumeManager();
        volumeManager.setCurrentImage(imp);

        ImportLabelMapPlugin ilmp = new ImportLabelMapPlugin(volumeManager);
        ilmp.setLabelMap(imp);

        assertTrue("roi == null ", RoiUtilities.getPixelCountOfRoi(volumeManager.getVolume(0).getRoi(1)) == 4);
        assertTrue("roi == null ", volumeManager.getVolume(0).getRoi(2) == null);
    }
}
