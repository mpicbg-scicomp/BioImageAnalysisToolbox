package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.gui.Roi;
import ij.plugin.frame.ModRoiManager;
import ij.plugin.frame.RoiManager;
import java.awt.*;
import java.util.ArrayList;
import org.scijava.plugin.Plugin;

/**
 * This Plugin allows loading the content of the volume manager from the hard disc
 * <p>
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: March 2017
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
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Load from disc...", menuPath = "File", priority = 100)
public class LoadFromDiscPlugin extends AbstractVolumeManagerPlugin {

    public LoadFromDiscPlugin() {
    }

    ;

    public LoadFromDiscPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    Roi[] rois = null;

    private void saveRoiManagerState() {
        rois = null;
        RoiManager rm = RoiManager.getInstance();
        if (rm != null) {
            rois = rm.getRoisAsArray();
            rm.close();
        }
    }

    private void resetRoiManagerState() {
        RoiManager rm = RoiManager.getInstance();
        if (rm != null) {
            rm.close();
        }
        if (rois != null) {
            rm = new RoiManager();
            for (int i = 0; i < rois.length; i++) {
                rm.addRoi(rois[i]);
            }
        }
    }

    @Override
    public void run() {
        saveRoiManagerState();

        RoiManager rm = new RoiManager();

        rm.setVisible(false);
        rm.runCommand("Open", "");

        parseRoiManager(rm);

        rm.setVisible(false);
        rm.close();

        resetRoiManagerState();
    }

    public void load(String filename) {
        saveRoiManagerState();

        ModRoiManager rm = new ModRoiManager();

        DebugHelper.print(this, "vm count before saving: " + getVolumeManager().length());
        DebugHelper.print(this, "rm count before saving: " + rm.getCount());

        rm.open(filename);

        parseRoiManager(rm);

        rm.setVisible(false);
        rm.close();

        resetRoiManagerState();
    }

    class MyPolylineSurface extends PolylineSurface {

        public MyPolylineSurface(String title) {
            super(title);
        }

        @Override
        public void addRoi(int slice, Roi roi) {
            super.addRoi(slice, roi);
        }
    }

    // I cannot believe it, but I had to program this, because of an ROI file, which was not able to parse. It appeared
    // in project redmine #2002
    private String[] splitString(String text, String splitter) {
        ArrayList<String> result = new ArrayList<>();

        String temp = "";

        for (int i = 0; i < text.length(); i++) {
            String potentialDelimiter = text.substring(i, i + splitter.length());
            if (potentialDelimiter.equals(splitter)) {
                i += splitter.length() - 1;
                result.add(temp);
                temp = "";
            } else {
                temp = temp + text.substring(i, i + 1);
            }
        }
        result.add(temp);

        String[] resultArray = new String[result.size()];
        result.toArray(resultArray);

        return resultArray;
    }

    public void parseRoiManager(RoiManager rm) {

        VolumeManager sm = getVolumeManager();
        sm.deactivateImageInteraction();
        sm.lockManipulation();

        int formerSi = -1;
        PolylineSurface plSurface = null;
        String formerName = "";

        DebugHelper.print(this, "creating new pls (init)");
        plSurface = new MyPolylineSurface(""); //sm.createVolume("");
        for (int i = 0; i < rm.getCount(); i++) {
            Roi roi = rm.getRoi(i);
            String[] name = splitString(roi.getName(), "|");

            // read out surface-index
            int si = Integer.parseInt(name[0]);

            // if surface-index is different, create a new data structure
            if ((si != formerSi && i > 0) || i == rm.getCount() - 1) {


                //DebugHelper.print(this, "saving surface");
                plSurface.setTitle(formerName);
                sm.addVolume(plSurface);
                if (si != formerSi && i > 0) {
                    plSurface = new MyPolylineSurface("");
                }
            }

            // read out slice index within surface\
            int ri = Integer.parseInt(name[1]);

            name[0] = "";
            name[1] = "";

            if (name.length >= 9) {
                Color lineColor = null;
                if (Integer.parseInt(name[3]) >= 0 &&
                        Integer.parseInt(name[4]) >= 0 &&
                        Integer.parseInt(name[5]) >= 0) {

                    lineColor = new Color(Integer.parseInt(name[3]), Integer.parseInt(name[4]), Integer.parseInt(name[5]));
                }

                Color fillColor = null;
                if (Integer.parseInt(name[6]) >= 0 &&
                        Integer.parseInt(name[7]) >= 0 &&
                        Integer.parseInt(name[8]) >= 0) {
                    fillColor = new Color(Integer.parseInt(name[6]), Integer.parseInt(name[7]), Integer.parseInt(name[8]));
                }

                for (int j = 3; j < 9; j++) {
                    name[j] = "";
                }
                plSurface.lineColor = lineColor;
                plSurface.fillColor = fillColor;
            }
            if (name.length >= 12) {
                plSurface.setLineThickness(Double.parseDouble(name[9]));
                plSurface.setTransparency(Double.parseDouble(name[10]));
                plSurface.viewInterpolatedLinesDotted = name[11].equals("1");

                for (int j = 9; j < 12; j++) {
                    name[j] = "";
                }
            }

            formerName = "";
            for (String namePart : name) {
                formerName += namePart;
            }
            formerSi = si;

            int cnt = RoiUtilities.getPixelCountOfRoi(roi);

            plSurface.addRoi(ri == 0 ? 1 : ri, roi);
        }

        sm.unlockManipulation();
        sm.activateImageInteraction();
        sm.refreshCurrentVolumeFromList();
        sm.refresh();
    }

}
