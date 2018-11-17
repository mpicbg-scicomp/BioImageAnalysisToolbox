package de.mpicbg.scf.rhaase.volumemanager.plugins.io;

import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import org.scijava.plugin.Plugin;

/**
 * Created by rhaase on 5/19/16.
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Create overlay image", menuPath = "File>Export", priority = 600)
public class CreateOverlayImagePlugin extends AbstractVolumeManagerPlugin {

    public CreateOverlayImagePlugin(){};
    public CreateOverlayImagePlugin(VolumeManager volumeManager)
    {
        setVolumeManager(volumeManager);
    }


    @Override
    public void run() {
        getOverlayImage().show();
    }

    public ImagePlus getOverlayImage() {
        VolumeManager sm = getVolumeManager();
        return getOverLayImage(sm.getCurrentImagePlus());
    }

    public ImagePlus getOverLayImage(ImagePlus projectonImp)
    {
        VolumeManager sm = getVolumeManager();
        //sm.lockManipulation();
        sm.lockSwitchingToOtherImages();

        SurfaceListModel surfaceData = sm.getVolumeList();

        ImagePlus imp = projectonImp;

        //boolean switchingWasAllowed = chckbxAllowSwitch.isSelected();
        //boolean showAllWasSet = chckbxShowAll.isSelected();
        //chckbxShowAll.setSelected(true);
        if ( sm.isShowingAll()) {
            sm.createNewButtonClicked();
        }

        //ImagePlus result = NewImage.createRGBImage("Overlay image", imp.getWidth(), imp.getHeight(), imp.getNSlices(), NewImage.FILL_BLACK);
        //ImageStack stack = result.getStack();
        Roi temp = imp.getRoi();
        imp.killRoi();
        ImagePlus result =(new Duplicator()).run(imp);
        imp.setRoi(temp);
        //.duplicateStack(imp, "Overlat image");

        (new StackConverter(result)).convertToRGB();

        for (int z = 0; z < result.getNSlices(); z++)
        {
            imp.setZ(z + 1);
            sm.refresh();
            result.setZ(z + 1);
            ImageProcessor ip = result.getProcessor();
            if (imp.getOverlay() != null) {
                ip.drawOverlay(imp.getOverlay());
            }
        }


        sm.unlockSwitchingToOtherImages();
        //sm.unlockManipulation();
        //chckbxShowAll.setSelected(showAllWasSet);
        return result;
    }

}
