package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import org.scijava.plugin.Plugin;

/**
 * This class allows to export the content of the volume manager to the hard disc.
 * <p>
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
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Save to disc...", menuPath = "File", priority = 200)
public class SaveToDiscPlugin extends AbstractVolumeManagerPlugin {

    public SaveToDiscPlugin() {
    }

    ;

    public SaveToDiscPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    private String filename = ""; // optional, if empty, the user will have to pick one

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void run() {
        VolumeManager sm = getVolumeManager();
        sm.lockManipulation();
        sm.lockSwitchingToOtherImages();

        SurfaceListModel surfaceData = sm.getVolumeList();

        RoiManager rm = RoiManager.getInstance();
        if (rm == null) {
            rm = new RoiManager();
        }
        if (rm.getCount() > 0) {
            rm.runCommand("Unselect");
            rm.runCommand("Delete");
        }

        for (int si = 0; si < surfaceData.size(); si++) {
            PolylineSurface s = surfaceData.getSurface(si);

            for (int ri = s.getStartSlice(); ri <= s.getEndSlice(); ri++) {
                // add all rois with specific names...
                Roi roi = s.getRoi(ri);
                if (roi != null) {
                    int lineColorRed = -1;
                    int lineColorGreen = -1;
                    int lineColorBlue = -1;
                    if (s.lineColor != null) {
                        lineColorRed = s.lineColor.getRed();
                        lineColorGreen = s.lineColor.getGreen();
                        lineColorBlue = s.lineColor.getBlue();
                    }

                    int fillColorRed = -1;
                    int fillColorGreen = -1;
                    int fillColorBlue = -1;
                    if (s.fillColor != null) {
                        fillColorRed = s.fillColor.getRed();
                        fillColorGreen = s.fillColor.getGreen();
                        fillColorBlue = s.fillColor.getBlue();
                    }


                    roi.setName(
                            new Integer(si).toString() + "|" +
                                    new Integer(ri).toString() + "|" +
                                    s.getTitle() + "|" +
                                    new Integer(lineColorRed).toString() + "|" +
                                    new Integer(lineColorGreen).toString() + "|" +
                                    new Integer(lineColorBlue).toString() + "|" +
                                    new Integer(fillColorRed).toString() + "|" +
                                    new Integer(fillColorGreen).toString() + "|" +
                                    new Integer(fillColorBlue).toString() + "|" +
                                    new Integer((int) s.getLineThickness()) + "|" +
                                    new Double(s.getTransparency()) + "|" +
                                    (s.viewInterpolatedLinesDotted ? "1" : "0")

                    );
                    rm.add(sm.getCurrentImagePlus(), roi, -1);
                }
            }
        }
        rm.deselect();
        rm.runCommand("Save", filename);
        rm.setVisible(false);
        rm.close();

        sm.unlockSwitchingToOtherImages();
        sm.unlockManipulation();
    }
}
