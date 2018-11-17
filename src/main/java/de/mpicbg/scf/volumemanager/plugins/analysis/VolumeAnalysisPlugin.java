package de.mpicbg.scf.volumemanager.plugins.analysis;

import de.mpicbg.scf.fijiplugins.ui.measurement.LabelAnalyserPlugin;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import de.mpicbg.scf.volumemanager.plugins.io.CreateLabelMapPlugin;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import org.scijava.plugin.Plugin;

/**
 * The Volume Analysis plugin allows measuring several properties of a volume
 * such as volume, signal intensity, shape, ...
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

@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Analyse Volumes", menuPath = "Analysis", priority = 2100)
public class VolumeAnalysisPlugin extends AbstractVolumeManagerAnalysisPlugin {

    public VolumeAnalysisPlugin() {
    }

    public VolumeAnalysisPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public ResultsTable getResults() {

        VolumeManager vm = this.getVolumeManager();
        vm.lockManipulation();
        vm.lockSwitchingToOtherImages();

        CreateLabelMapPlugin clmp = new CreateLabelMapPlugin(vm);

        ImagePlus imp = vm.getCurrentImagePlus();
        ImagePlus labelMap = clmp.getLabelMap();
        labelMap.show();

        LabelAnalyserPlugin lpap = new LabelAnalyserPlugin();
        lpap.processWithConfigDialog(labelMap, imp, new LabelAnalyser.Feature[]{LabelAnalyser.Feature.AREA_VOLUME}, 5, 10, false, false);

        ResultsTable rt = lpap.getResultsTable();

        labelMap.hide();
        vm.unlockSwitchingToOtherImages();
        vm.unlockManipulation();

        return rt;
    }
}
