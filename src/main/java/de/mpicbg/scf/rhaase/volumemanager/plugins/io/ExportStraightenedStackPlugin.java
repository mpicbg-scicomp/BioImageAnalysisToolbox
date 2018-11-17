package de.mpicbg.scf.rhaase.volumemanager.plugins.io;


import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.Straightener;
import ij.process.FloatPolygon;
import org.scijava.plugin.Plugin;

/**
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: October 2016
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Export straightened stack", label="experimental", menuPath = "File>Export", priority = 100000)
public class ExportStraightenedStackPlugin extends AbstractVolumeManagerPlugin {

    public ExportStraightenedStackPlugin()
    {
    }
    public ExportStraightenedStackPlugin(VolumeManager volumeManager)
    {
        setVolumeManager(volumeManager);
    }


    @Override
    public void run() {

        int straightenWidth = 20;
        GenericDialog gd = new GenericDialog(this.getMenuItem().getText());
        gd.addNumericField("Line width (in pixels)", straightenWidth, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        straightenWidth = (int)gd.getNextNumber();


        VolumeManager vm = getVolumeManager();
        vm.lockSwitchingToOtherImages();

        PolylineSurface pls = vm.getCurrentVolume();

        ImagePlus imp = vm.getCurrentImagePlus();

        if (pls == null || imp == null)
        {
            DebugHelper.print(this, "No image or no volume found.");
            return;
        }



        int count = 0;
        int countMax = imp.getNFrames() * ( pls.getEndSlice()-  pls.getStartSlice() + 1 ) * imp.getNChannels();
        ImagePlus[] imps = new ImagePlus[countMax];
        for (int t = 1; t <= imp.getNFrames(); t++) {
            //imp.setT(t);
            for (int z = pls.getStartSlice(); z <= pls.getEndSlice(); z++) {

                for (int c = 1; c <= imp.getNChannels(); c++) {
                    //imp.setC(c);
                    count++;
                    IJ.showProgress(count, countMax);

                    //imp.setRoi(pls.getInterpolatedRoi(z));
                    Roi temp = imp.getRoi();
                    imp.killRoi();
                    ImagePlus sliceImp = new Duplicator().run(imp, c, c, z, z, t, t);
                    imp.setRoi(temp);

                    //run("Straighten...", "title=MAX_1_TL_7,2sec-1.tif line=20");

                    Roi roi = pls.getInterpolatedRoi(z);
                    if (roi.getType() != Roi.POLYLINE && roi.getType() != Roi.LINE) {
                        FloatPolygon fp = roi.getFloatPolygon();
                        roi = new PolygonRoi(fp, Roi.POLYLINE);
                        DebugHelper.print(this, "Found ROI type which is not allowed. I'm interpreting it as polyline.");
                    }


                    sliceImp.setRoi(roi);
                    //sliceImp.show();
                    //new WaitForUserDialog("bla").show();
                    imps[count - 1] = new ImagePlus("temp " + z, new Straightener().straighten(sliceImp, roi, straightenWidth));
                }
            }
        }

        ImagePlus result = new Concatenator().concatenate(imps, false);
        ImagePlus hyperStack = HyperStackConverter.toHyperStack(result, imp.getNChannels(), pls.getEndSlice() - pls.getStartSlice() + 1, imp.getNFrames());

        hyperStack.setTitle("Straigthened " + imp.getTitle());
        hyperStack.show();
        hyperStack.setDisplayRange(imp.getDisplayRangeMin(), imp.getDisplayRangeMax());

        //vm.unlockSwitchingToOtherImages();
        vm.refresh();
    }
}
