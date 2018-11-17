package de.mpicbg.scf.rhaase.volumemanager.plugins.manipulation;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.gui.Roi;
import org.scijava.plugin.Plugin;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: October 2016
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Make independent volumes out of annotated slices", label="experimental", menuPath = "Edit", priority = 1500)
public class SplitAnnotatedSlicesIntoSeparateVolumesPlugin extends AbstractVolumeManagerPlugin {
    public SplitAnnotatedSlicesIntoSeparateVolumesPlugin(){};
    public SplitAnnotatedSlicesIntoSeparateVolumesPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }


    @Override
    public void run() {
        VolumeManager vm = getVolumeManager();

        PolylineSurface pls = vm.getCurrentVolumeUnsafe();
        if (pls == null)
        {
            return;
        }

        for (int z = pls.getStartSlice(); z <= pls.getEndSlice(); z++)
        {
            Roi roi = pls.getRoi(z);
            if (roi != null)
            {
                PolylineSurface newVolume = vm.createVolume("");
                newVolume.addRoi(z, roi);
            }
        }

        vm.refresh();
    }


}
