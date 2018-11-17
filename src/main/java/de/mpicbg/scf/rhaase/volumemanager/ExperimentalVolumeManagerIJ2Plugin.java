package de.mpicbg.scf.rhaase.volumemanager;

import de.mpicbg.scf.fijiplugins.ui.roi.BrushPluginTool;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.VolumeManagerPluginService;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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

@Plugin(type = Command.class, menuPath = "SCF>Experimental>Segmentation>Experimental volume manager (3D)")
public class ExperimentalVolumeManagerIJ2Plugin implements Command {

    @Parameter
    private LogService log;

    @Parameter(required = false)
    private ImagePlus imp;

    @Parameter
    private ImageJ ij;

    @Override
    public void run() {
        DebugHelper.print(this, "Context... " + log.getContext());

        VolumeManager.context = ij.getContext();

        VolumeManagerPluginService vmps = ij.get(VolumeManagerPluginService.class);
        DebugHelper.print(this, "available vm plugins: " +  vmps.getPluginNames().size());

        VolumeManager volumeManager = VolumeManager.getInstance();
        volumeManager.initializeAllPlugins("");
        volumeManager.setTitle("Volume manager (experimental)");

        if (imp != null) {
            volumeManager.imageUpdated(imp);
        }
        volumeManager.setVisible(true);

        BrushPluginTool bpt = new BrushPluginTool();
        bpt.run("");
        IJ.setTool("polygon");
    }
}
