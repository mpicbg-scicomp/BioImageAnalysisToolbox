package de.mpicbg.scf.rhaase.volumemanager.plugins.manipulation;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import org.scijava.plugin.Plugin;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: August 2016
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Smooth all polygons of the current volume", label="experimental", menuPath = "Edit", priority = 1200)
public class SlicePolygonInterpolatorPlugin extends AbstractVolumeManagerPlugin {
    public SlicePolygonInterpolatorPlugin(){};
    public SlicePolygonInterpolatorPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public void run() {
        VolumeManager vm = getVolumeManager();
        PolylineSurface pls = vm.getCurrentVolume();
        if (pls == null) {
            DebugHelper.print(this, "no volume selected");
            return;
        }

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

        smoothPolylinesOfVolume(pls, stepCount, smooth);


        ImagePlus imp = vm.getCurrentImagePlus();
        if (imp != null && imp.getRoi() != null && pls.getRoi(imp.getZ()) != null)
        {
            imp.setRoi(pls.getRoi(imp.getZ()));
        }

        vm.refresh();
    }

    public static void smoothPolylinesOfVolume(PolylineSurface pls, int stepCount, boolean smooth)
    {
        for (int z = pls.getStartSlice(); z <= pls.getEndSlice(); z++)
        {
            Roi roi = pls.getRoi(z);
            if (roi != null)
            {
                FloatPolygon fp = roi.getInterpolatedPolygon();
                if (fp != null && fp.npoints > 3) {
                    fp = roi.getInterpolatedPolygon(fp.getLength(false) / stepCount, smooth);

                    roi = new PolygonRoi(fp, roi.getType());
                    pls.addRoi(z, roi);
                }
            }
        }
    }
}