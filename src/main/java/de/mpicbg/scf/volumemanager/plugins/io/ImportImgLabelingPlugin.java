package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import java.util.ArrayList;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Plugin;

/**
 * This plugin allows importing ImgLabeling data structures into the volume manager
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: June 2016
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
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Import labeling (not useful from menu)", menuPath = "")
public class ImportImgLabelingPlugin extends AbstractVolumeManagerPlugin {

    //input
    ImgLabeling<Integer, IntType> labeling;

    public ImportImgLabelingPlugin() {
    }

    public ImportImgLabelingPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public void run() {
        if (labeling == null) {
            return;
        }

        LabelRegions<Integer> labelRegions = new LabelRegions<Integer>(labeling);

        ArrayList<RandomAccessibleInterval<BoolType>> regions = new ArrayList<RandomAccessibleInterval<BoolType>>();

        if (labelRegions == null) {
            return;
        }

        Object[] regionsArr = labelRegions.getExistingLabels().toArray();
        for (int i = 0; i < labelRegions.getExistingLabels().size(); i++) {
            LabelRegion<Integer> lr = labelRegions.getLabelRegion((Integer) regionsArr[i]);
            regions.add(lr);
        }

        ImportRegionArrayListPlugin iralp = new ImportRegionArrayListPlugin(getVolumeManager());
        iralp.setRegions(regions);
        iralp.run();

    }

    public void setImgLabeling(ImgLabeling<Integer, IntType> labeling) {
        this.labeling = labeling;
    }


}
