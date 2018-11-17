package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.geometry.create.Thresholding;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.visualisation.ProgressDialog;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.RoiUtilities;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Plugin;

/**
 * This class allows importing ImagePlus labelmaps as volumes into the
 * Volume Manager.
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
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Import labelmap", menuPath = "File>Import", priority = 300)
public class ImportLabelMapPlugin extends AbstractVolumeManagerPlugin {
    public ImportLabelMapPlugin() {
    }

    ;

    public ImportLabelMapPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public void run() {
        setLabelMap(IJ.getImage());
    }

    public void setLabelMap(ImagePlus labelMap) {
        VolumeManager sm = getVolumeManager();
        SurfaceListModel surfaceData = sm.getVolumeList();

        int numberOfVolumesBeforeImport = surfaceData.size();

        labelMap.killRoi();

        int count = 0;
        for (int slice = 0; slice < labelMap.getNSlices(); slice++) {
            labelMap.setZ(slice);
            ImageStatistics stats = labelMap.getStatistics();

            if (count < stats.max) {
                count = (int) stats.max;
            }
        }

        for (int i = 0; i < count; i++) {
            surfaceData.addElement("Label " + (numberOfVolumesBeforeImport + i + 1), new PolylineSurface("Label " + (numberOfVolumesBeforeImport + i + 1)));
        }


        labelMap.killRoi();
        long imagePixelCount = labelMap.getWidth() * labelMap.getWidth();

        long startTimeAll = System.currentTimeMillis();
        long lastTimeStamp = startTimeAll;

        Img<FloatType> labelMapImg = ImagePlusAdapter.convertFloat(labelMap);
        DebugHelper.print(this, "labelMap " + labelMap);
        DebugHelper.print(this, "labelMapImg " + labelMapImg);

        ProgressDialog.reset();
        ProgressDialog.setStatusText("Creating ROIs from LabelMap");

        for (int slice = 0; slice < labelMap.getNSlices(); slice++) {
            ProgressDialog.setProgress((double) slice / labelMap.getNSlices());
            if (ProgressDialog.wasCancelled()) {
                break;
            }

            labelMap.setZ(slice + 1);
            float[][] labelPositions;
            if (labelMap.getNSlices() == 1) {
                labelPositions = LabelAnalyser.getLabelsPosition(labelMapImg, -1);
            } else {
                labelPositions = LabelAnalyser.getLabelsPosition(labelMapImg, slice);
            }
            ImageProcessor ip = labelMap.getProcessor();

            for (int t = 1; t <= count; t++) {
                if (ProgressDialog.wasCancelled()) {
                    break;
                }

                //trace output because this may take a while
                long timeDiff = System.currentTimeMillis() - lastTimeStamp;
                if (timeDiff > 5000) {
                    DebugHelper.print(this, "Processing z = " + slice + " of " + labelMap.getNSlices() + ", object " + t + " of " + count);
                    lastTimeStamp = System.currentTimeMillis();
                }

                //Determine the outline of the object in the current slice
                Roi r = RoiUtilities.fixRoi(Thresholding.applyThreshold(labelMap, t, t));

                if (!de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.isEmptyRoi(r)) {
                    labelMap.setRoi(r);
                    ImageStatistics roiStats = labelMap.getStatistics();
                    //if the roi has a different size than the image
                    if (roiStats.pixelCount != imagePixelCount) {
                        //Store the ROI of the current slice
                        surfaceData.getSurface(numberOfVolumesBeforeImport + t - 1).addRoi(slice + 1, r);
                    }
                }
            }
        }
        labelMap.killRoi();
        DebugHelper.print(this, "Quickly transforming all slices took " + (System.currentTimeMillis() - startTimeAll) + " ms");

        ProgressDialog.finish();

        DebugHelper.print(this, "quickConvertLabelMapToRoiList finished");
    }
}
