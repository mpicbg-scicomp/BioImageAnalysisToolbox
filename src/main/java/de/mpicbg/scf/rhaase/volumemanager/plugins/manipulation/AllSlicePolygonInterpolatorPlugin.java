package de.mpicbg.scf.rhaase.volumemanager.plugins.manipulation;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import org.scijava.plugin.Plugin;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: September 2016
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Smooth all polygons of all volumes", label="experimental", menuPath = "Edit", priority = 1201)
public class AllSlicePolygonInterpolatorPlugin  extends AbstractVolumeManagerPlugin {
    public AllSlicePolygonInterpolatorPlugin(){};
    public AllSlicePolygonInterpolatorPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public void run() {
        VolumeManager vm = getVolumeManager();
        /*PolylineSurface pls = vm.getCurrentVolume();
        if (pls == null) {
            DebugHelper.print(this, "no volume selected");
            return;
        }*/

        GenericDialog gd = new GenericDialog(this.name);
        gd.addNumericField("Step_count", 100, 0);
        gd.addCheckbox("Smooth", false);

        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        int stepCount = (int) gd.getNextNumber();
        boolean smooth = gd.getNextBoolean();

        for (int i = 0; i < vm.length(); i++) {
            PolylineSurface pls = vm.getVolume(i);
            SlicePolygonInterpolatorPlugin.smoothPolylinesOfVolume(pls, stepCount, smooth);
        }

        PolylineSurface pls = vm.getCurrentVolume();

        if (pls != null) {
            ImagePlus imp = vm.getCurrentImagePlus();
            if (imp != null && imp.getRoi() != null && pls.getRoi(imp.getZ()) != null) {
                imp.setRoi(pls.getRoi(imp.getZ()));
            }
        }

        vm.refresh();
    }
}
