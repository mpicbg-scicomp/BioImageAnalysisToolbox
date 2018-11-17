package de.mpicbg.scf.rhaase.volumemanager.plugins.manipulation;

import de.mpicbg.scf.imgtools.geometry.create.Thresholding;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.Duplicator;
import ij.plugin.RoiEnlarger;
import ij.process.ImageStatistics;
import org.scijava.plugin.Plugin;

/**
 * This plugins is hidden as it was never actively used. it used to be in menu Edit - Segmentation
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: August 2016
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Segment objects with grey value significantly above average", label="experimental", menuPath = "", priority = 1300)
public class SignificantlyAboveAverageThresholdingSegmentationPlugin extends AbstractVolumeManagerPlugin {

    public SignificantlyAboveAverageThresholdingSegmentationPlugin(){};
    public SignificantlyAboveAverageThresholdingSegmentationPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public void run() {
        GenericDialog gd = new GenericDialog(this.name);
        gd.addNumericField("Smooth image intensity (Gaussian blur sigma in pixels)", 1, 2);
        gd.addNumericField("Threshold (in times standard deviation)", 3, 2);
        gd.addNumericField("Ignore objects with radius smaller than (in pixels)", 1, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        double gaussianBlurSigma = gd.getNextNumber();
        double relativeStdThreshold = gd.getNextNumber();
        double openingDistance = gd.getNextNumber();

        PolylineSurface currentPolylineSurface = getVolumeManager().getCurrentVolume();
        if (currentPolylineSurface == null)
        {
            return;
        }

        VolumeManager volumeManager = getVolumeManager();
        volumeManager.lockManipulation();
        volumeManager.lockSwitchingToOtherImages();
        int selectedIndex = volumeManager.getSelectedIndex();

        ImagePlus imp = volumeManager.getCurrentImagePlus();


        volumeManager.addVolume(getSegmentation(currentPolylineSurface, imp, gaussianBlurSigma, relativeStdThreshold, openingDistance));

        volumeManager.unlockManipulation();
        volumeManager.unlockSwitchingToOtherImages();

        volumeManager.setSelectedIndex(selectedIndex);
    }

    static PolylineSurface getSegmentation(PolylineSurface volume, ImagePlus imp, double gaussianBlurSigma, double relativeStdThreshold, double openingDistance)
    {
        PolylineSurface newVolume = new PolylineSurface("Segmented " + volume.getTitle());
        for (int z = volume.getStartSlice(); z <= volume.getEndSlice(); z++)
        {
            DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "segment slice " + z);
            //measure background
            ShapeRoi wholeRoi = new ShapeRoi(volume.getInterpolatedRoi(z));
            ShapeRoi shrinkedRoi = new ShapeRoi(RoiEnlarger.enlarge(wholeRoi, -1));

            ShapeRoi ringRoi = wholeRoi.xor(shrinkedRoi);

            Roi temp = imp.getRoi();
            imp.setRoi(ringRoi);
            ImageStatistics stats = imp.getStatistics();
            imp.killRoi();
            wholeRoi = new ShapeRoi(volume.getInterpolatedRoi(z));

            ImagePlus dup = new Duplicator().run(imp, z, z);

            IJ.run(dup, "Gaussian Blur...", "sigma=" + gaussianBlurSigma);

            Roi thresholdedRoi = Thresholding.applyThreshold(dup, stats.mean + stats.stdDev * relativeStdThreshold, Double.POSITIVE_INFINITY);
            if (RoiUtilities.getPixelCountOfRoi(thresholdedRoi) == 0)
            {
                DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "leave 0");
                continue;
            }

            Roi newRoi = de.mpicbg.scf.volumemanager.core.RoiUtilities.fixRoi(thresholdedRoi);
            if (newRoi == null) {
                DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "leave 1");
                continue;
            }

            try {


                ShapeRoi newShapeRoi = wholeRoi.and(new ShapeRoi(newRoi));
                if (RoiUtilities.getPixelCountOfRoi(newShapeRoi) == 0) {
                    DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "leave 2");
                    continue;
                }

                Roi erodedRoi = RoiEnlarger.enlarge(newShapeRoi, -openingDistance);
                if (RoiUtilities.getPixelCountOfRoi(erodedRoi) == 0) {
                    DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "leave 3");
                    continue;
                }
                ShapeRoi erodedShapeRoi = new ShapeRoi(erodedRoi);
                if (RoiUtilities.getPixelCountOfRoi(erodedShapeRoi) == 0) {
                    DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "leave 4");
                    continue;
                }
                ShapeRoi dilatedRoi = new ShapeRoi(RoiEnlarger.enlarge(erodedShapeRoi, openingDistance));
                if (RoiUtilities.getPixelCountOfRoi(dilatedRoi) == 0) {
                    DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "leave 5");
                    continue;
                }
                newVolume.addRoi(z, dilatedRoi);
                DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "area: " + RoiUtilities.getPixelCountOfRoi(dilatedRoi));

            }
            catch (ArrayIndexOutOfBoundsException aioob)
            {
                DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "aioob " + aioob.getMessage());
                continue;
            }

            imp.setRoi(temp);

        }

        for (int z = newVolume.getStartSlice(); z <= newVolume.getEndSlice(); z++)
        {
            DebugHelper.print("SignificantlyAboveAverageThresholdingSegmentationPlugin", "" + z + " area:" + RoiUtilities.getPixelCountOfRoi(newVolume.getRoi(z)));
        }

        return newVolume;
    }
}
