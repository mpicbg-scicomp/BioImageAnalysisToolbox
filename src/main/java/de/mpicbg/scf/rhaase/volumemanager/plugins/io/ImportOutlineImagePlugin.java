package de.mpicbg.scf.rhaase.volumemanager.plugins.io;

import de.mpicbg.scf.fijiplugins.ui.roi.LabelMapToRoiManagerPlugin;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.geometry.filter.GeometryFilterUtilities;
import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import de.mpicbg.scf.imgtools.image.create.labelmap.ThresholdLabeling;
import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.RoiEnlarger;
import ij.plugin.frame.ModRoiManager;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Plugin;

/**
 * Created by rhaase on 5/19/16.
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Import outline image", label="experimental", menuPath = "File>Import", priority = 400)
public class ImportOutlineImagePlugin extends AbstractVolumeManagerPlugin {
    public ImportOutlineImagePlugin(){};
    public ImportOutlineImagePlugin(VolumeManager volumeManager)
    {
        setVolumeManager(volumeManager);
    }

    public int erodeRoisByPixels = 1;

    @Override
    public void run() {
        readOutlineImage(IJ.getImage());
    }


    public void readOutlineImage(ImagePlus imp) {
        GenericDialogPlus gdp = new GenericDialogPlus("Import outline image");
        gdp.addNumericField("Match areas in subsequent planes which overlap by more than [Jaccard Index in %, between 0 and 100]", 60, 0);
        gdp.showDialog();
        if (gdp.wasCanceled()) {
            return;
        }
        float minimumJaccard = (float) (gdp.getNextNumber() / 100.0);
        readOutlineImage(imp, minimumJaccard);
    }

    public void readOutlineImage(ImagePlus imp, float minimumJaccard)
    {
        VolumeManager sm = getVolumeManager();
        SurfaceListModel surfaceData = sm.getVolumeList();

        //ImagePlus imp = sm.getCurrentImagePlus();

        sm.lockSwitchingToOtherImages();

        // ProgressDialog.reset();
        // ProgressDialog.setStatusText("Reading outline image...");

        // Img<IntType> detectedMaxima_FormerSlice = null;
        for (int i = 0; i < imp.getNSlices(); i++) {
            DebugHelper.print(this, "processing slice " + i + " of " + imp.getNSlices());
            // ProgressDialog.setProgress((double) i / imp.getNSlices());
            // if (ProgressDialog.wasCancelled())
            // {
            // break;
            // }
            imp.killRoi();
            ImagePlus sliceImp = new Duplicator().run(imp, i + 1, i + 1);
            sliceImp.show();

            if (sliceImp.getNSlices() > 0 || sliceImp.getNChannels() > 0 || sliceImp.getNFrames() > 0)
            {
                IJ.run(sliceImp, "Invert", "stack");
            }
            else
            {
                IJ.run(sliceImp, "Invert", "");
            }
            //ImageUtilities.invertImage(sliceImp);

            // Create label map
            Img<FloatType> localHotSpotsMap = ImagePlusAdapter.convertFloat(sliceImp);
            ThresholdLabeling filter = new ThresholdLabeling();
            DebugHelper.print(this, "Labeling thresh = " + 128);
            Img<IntType> detectedMaxima = filter.Labeling(localHotSpotsMap, (float) 128);
            // ImgLib2Utils.showLabelMapProperly(detectedMaxima, "Label map from threshold ", imp.getDimensions(), imp.getCalibration());

            ImagePlus labelMapImp = ImageCreationUtilities.convertImgToImagePlus(detectedMaxima, "Label map from threshold ", "", imp.getDimensions(),
                    imp.getCalibration());

            // Open ROI Manager
            ModRoiManager mrm = ModRoiManager.getInstance();
			/*
			 * RoiManager mrm = RoiManager.getInstance(); if (mrm == null) { mrm = ModRoiManager.getInstance(); }
			 */

            // Transfer all labels to the ROI manager
            LabelMapToRoiManagerPlugin.apply(labelMapImp);
            sliceImp.hide();
            // IJ.getImage().hide();
            //
            if (i == 0) {
                // read in all ROI as new surface objects
                for (int s = 1; s < mrm.getCount(); s++) {
                    Roi roiToSave = mrm.getRoi(s);
                    roiToSave = RoiEnlarger.enlarge(roiToSave, -erodeRoisByPixels);
                    // DebugHelper.print(this, "RoiToSave: " + roiToSave);
                    // DebugHelper.print(this, "RoiToSave type: " + roiToSave.getType());
                    PolylineSurface pls = new PolylineSurface("S" + (s + 1));
                    pls.addRoi(i + 1, roiToSave);
                    sm.addVolume(pls);
                    // DebugHelper.print(this, "init surface " + s + " with pixelcount " + RoiUtilities.getPixelCountOfRoi(roiToSave));
                }
            } else {
                for (int s = 1; s < mrm.getCount(); s++) {
                    Roi roi = mrm.getRoi(s);
                    roi = RoiEnlarger.enlarge(roi, -erodeRoisByPixels);
                    // DebugHelper.print(this, "current roi: " + roi);
                    PolylineSurface maximumJaccardPls = null;
                    float maximumJaccard = 0;

                    for (int xs = 0; xs < surfaceData.size(); xs++) {
                        PolylineSurface xPls = surfaceData.getSurface(xs);
                        // DebugHelper.print(this, "Start slice: " + xPls.getStartSlice());
                        // DebugHelper.print(this, "End slice: " + xPls.getEndSlice());
                        Roi xRoi = xPls.getInterpolatedRoi(i);
                        // DebugHelper.print(this, "existing roi: " + xRoi);

                        Roi interSectionRoi = GeometryFilterUtilities.intersect(roi, xRoi);
                        Roi unionRoi = GeometryFilterUtilities.unite(roi, xRoi);

                        float jaccardIndex = (float) RoiUtilities.getPixelCountOfRoi(interSectionRoi) / (float) RoiUtilities.getPixelCountOfRoi(unionRoi);
                        // DebugHelper.print(this, "jacc = " + jaccardIndex);
                        if (maximumJaccard < jaccardIndex) {
                            maximumJaccard = jaccardIndex;
                            maximumJaccardPls = xPls;
                        }
                    }

                    if (maximumJaccardPls != null && maximumJaccard > minimumJaccard) {
                        maximumJaccardPls.addRoi(i + 1, roi);
                        DebugHelper.print(this, "edit surface " + s + " with pixelcount " + RoiUtilities.getPixelCountOfRoi(roi));
                    }
                }
            }
            mrm.deselect();
            mrm.run("Delete");
            mrm.close();

            // detectedMaxima_FormerSlice = detectedMaxima;

			/*
			 * if ( i == 1) { break; }
			 */
        }
        // ProgressDialog.finish();
        DebugHelper.print(this, "Reading outline image done.");
		/*
		 * for (int xs = 0; xs < surfaceData.size(); xs ++) { PolylineSurface xPls = surfaceData.getVolume(xs); if (xPls.getStartSlice() > 1 ||
		 * xPls.getEndSlice() < imp.getNSlices()) { DebugHelper.print(this, "Adding * to the title of pls " + xs); surfaceData.renameButtonClicked(xPls.getTitle() + "*",
		 * xs); //xPls.setTitle(xPls.getTitle() + "*"); } }
		 */

        imp.show();
        sm.unlockSwitchingToOtherImages();

    }
}
