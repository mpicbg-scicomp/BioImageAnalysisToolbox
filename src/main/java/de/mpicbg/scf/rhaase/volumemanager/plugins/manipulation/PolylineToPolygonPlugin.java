package de.mpicbg.scf.rhaase.volumemanager.plugins.manipulation;

import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import org.scijava.plugin.Plugin;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
 */

@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Polyline to Polygon", label="experimente", menuPath = "Edit", priority = 1201)
public class PolylineToPolygonPlugin extends AbstractVolumeManagerPlugin {
    public PolylineToPolygonPlugin(){}

    public PolylineToPolygonPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }


    @Override
    public void run() {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            return;
        }
        Roi roi = imp.getRoi();
        if (roi != null && roi.getType() == Roi.POLYLINE) {
            roi = new PolygonRoi(roi.getFloatPolygon(), Roi.POLYGON);
            imp.setRoi(roi);

            VolumeManager vm = VolumeManager.getInstance();
            vm.refresh();
        }
    }

}
