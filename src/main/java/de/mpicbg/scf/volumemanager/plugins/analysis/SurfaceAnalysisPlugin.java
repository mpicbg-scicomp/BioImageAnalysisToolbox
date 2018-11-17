package de.mpicbg.scf.volumemanager.plugins.analysis;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.number.analyse.geometry.PolylineSurfaceAreaDeterminator;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import org.scijava.plugin.Plugin;

/**
 * The surface analysis plugin allows measuring the surface area of volumes
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: MAy 2016
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


@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Analyse surfaces", menuPath = "Analysis", priority = 2200)
public class SurfaceAnalysisPlugin extends AbstractVolumeManagerAnalysisPlugin {

    public SurfaceAnalysisPlugin() {
    }

    public SurfaceAnalysisPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public ResultsTable getResults() {
        VolumeManager sm = getVolumeManager();
        sm.lockManipulation();

        ResultsTable rt = new ResultsTable();
        rt.reset();

        Calibration pixelCalib = new Calibration();
        pixelCalib.pixelWidth = 1;
        pixelCalib.pixelHeight = 1;
        pixelCalib.pixelDepth = 1;

        Calibration calib = null;
        if (sm.getCurrentImagePlus() != null) {
            calib = sm.getCurrentImagePlus().getCalibration();
        }

        SurfaceListModel surfaceData = sm.getVolumeList();

        for (int i = 0; i < surfaceData.size(); i++) {
            PolylineSurface pls = surfaceData.getSurface(i);
            rt.incrementCounter();
            rt.addValue("Surface", pls.getTitle());
            rt.addValue("Area / pixels^2", new PolylineSurfaceAreaDeterminator(pls, pixelCalib).getArea());
            if (calib != null) {
                rt.addValue("Area / " + calib.getUnit() + "^2)", new PolylineSurfaceAreaDeterminator(pls, calib).getArea());
            }
        }

        sm.unlockManipulation();
        return rt;
    }
}
