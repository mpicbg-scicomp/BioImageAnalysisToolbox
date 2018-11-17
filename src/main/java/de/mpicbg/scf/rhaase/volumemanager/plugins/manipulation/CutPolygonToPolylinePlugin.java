package de.mpicbg.scf.rhaase.volumemanager.plugins.manipulation;

import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Cut polygon to become a polyline", label="experimental", menuPath = "Edit", priority = 1202)
public class CutPolygonToPolylinePlugin  extends AbstractVolumeManagerPlugin implements ImageListener, MouseListener {
    public CutPolygonToPolylinePlugin(){};
    public CutPolygonToPolylinePlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    ImagePlus imp;

    @Override
    public void run() {
        addListeners();

    }

    private void addListeners(){
        ImagePlus.addImageListener(this);
        imp = IJ.getImage();
        IJ.showStatus("Please click at the point where you want to cut the polygon!");
        imp.getCanvas().addMouseListener(this);
    }

    private void removeListeners()
    {
        imp.getCanvas().removeMouseListener(this);
        ImagePlus.removeImageListener(this);
    }


    @Override
    public void imageOpened(ImagePlus imagePlus) {

        removeListeners();
    }

    @Override
    public void imageClosed(ImagePlus imagePlus) {
        removeListeners();
    }

    @Override
    public void imageUpdated(ImagePlus imagePlus) {
        removeListeners();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (IJ.getImage() == imp) {
            Roi roi = imp.getRoi();
            if (roi != null && roi.getType() == Roi.POLYGON) {
                Point p = imp.getCanvas().getCursorLoc();
                FloatPolygon fp = roi.getFloatPolygon();
                double closestDistance = Double.POSITIVE_INFINITY;
                int closestDistanceIdx = 0;
                for (int i = 0; i < fp.npoints; i++) {
                    double distance = Math.sqrt(Math.pow(fp.xpoints[i] - p.x, 2 ) + Math.pow(fp.ypoints[i] - p.y, 2 ));
                    if (distance < closestDistance)
                    {
                        closestDistance = distance;
                        closestDistanceIdx = i;
                    }
                }
                FloatPolygon fpOut = new FloatPolygon();
                for (int i = closestDistanceIdx; i < fp.npoints; i++) {
                    fpOut.addPoint(fp.xpoints[i], fp.ypoints[i]);
                }
                for (int i = 0; i <= closestDistanceIdx; i++) {
                    fpOut.addPoint(fp.xpoints[i], fp.ypoints[i]);
                }
                imp.setRoi(new PolygonRoi(fpOut, Roi.POLYLINE));
                VolumeManager vm = VolumeManager.getInstance();
                vm.refresh();
            }
        }
        removeListeners();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
