package de.mpicbg.scf.rhaase.volumemanager;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.rhaase.volumemanager.plugins.io.ImportOutlineImagePlugin;
import de.mpicbg.scf.volumemanager.VolumeManager;
import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, 
 *         rhaase@mpi-cbg.de
 * Date: March 2017
 * 
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics, 
 *                Dresden, Germany
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice, 
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in the 
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its 
 *      contributors may be used to endorse or promote products derived from 
 *      this software without specific prior written permission.
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
 *
 */ 
public class VolumeManagerTest {

    /**
     * Test if reading in outline images works using an example image.
     */
    @Ignore
    @Test
    public void testReadOutline() {

        if (SystemUtilities.isHeadless())
        {
            DebugHelper.print(this, "Cancelling test, because it only runs in non-headless mode.");
            return;
        }

        ImagePlus imp = IJ.openImage("src/test/resources/binarymesh.tif");

        VolumeManager suMa = new VolumeManager();
        suMa.lockManipulation();
        suMa.lockSwitchingToOtherImages();

        ImportOutlineImagePlugin iolp = new ImportOutlineImagePlugin(suMa);
        iolp.readOutlineImage(imp, (float)0.6);

        int referenceAreaMeasurements[] = {

						/* 45*31  1395 */  1247,
						/* 28*19, 532  */  442,
						/* 16*19, 304  */  238
        };

        assertTrue("number of imported surfaces is correct ", suMa.length() == 3);

        for (int i = 0; i < suMa.length(); i++)
        {
            PolylineSurface surface = suMa.getVolume(i);

            assertTrue("starting slice is correct of object " + i, surface.getStartSlice() == 1);
            assertTrue("ending slice is correct of object " + i, surface.getEndSlice() == 2);

            int area = RoiUtilities.getPixelCountOfRoi((surface.getRoi(1)));
            System.out.println("area   " + area);
            assertTrue("area is correct of object " + i + " " + area + " == " + referenceAreaMeasurements[i], area == referenceAreaMeasurements[i]);

        }


        suMa.unlockManipulation();
        suMa.unlockSwitchingToOtherImages();
    }

}
