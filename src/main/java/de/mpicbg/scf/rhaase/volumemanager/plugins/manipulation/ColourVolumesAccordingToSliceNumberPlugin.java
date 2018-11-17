package de.mpicbg.scf.rhaase.volumemanager.plugins.manipulation;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ColorProcessor;
import org.scijava.plugin.Plugin;

import java.awt.*;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: September 2016
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Set colors corresponding to slice number", label="experimental", menuPath = "Edit>Colors", priority = 1101)
public class ColourVolumesAccordingToSliceNumberPlugin  extends AbstractVolumeManagerPlugin {
    public ColourVolumesAccordingToSliceNumberPlugin(){};
    public ColourVolumesAccordingToSliceNumberPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public void run() {
        DebugHelper.print(this, "hello 1");
        VolumeManager vm = getVolumeManager();
        DebugHelper.print(this, "hello 2");

        vm.lockSwitchingToOtherImages();
        DebugHelper.print(this, "hello 3");
        vm.lockManipulation();
        DebugHelper.print(this, "hello 4");

        GenericDialogPlus gd = new GenericDialogPlus("Choose LUT");
        gd.addChoice("Lookup table", new String[] {
                "Fire",
                "Grays",
                "Ice",
                "Spectrum",
                "3-3-2 RGB",
                "Red",
                "Green",
                "Blue",
                "Cyan",
                "Magenta",
                "Yellow",
                "Red/Green",
                "16 colors",
                "5 ramps",
                "6 shaded",
                "blue orange icb",
                "brgbcmyw",
                "cool",
                "Cyan Hot",
                "edges",
                "glasbey",
                "glasbey inverted",
                "glow",
                "Green Fire Blue"}, "Fire");
        gd.addCheckbox("Show scale", false);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        String colorTable = gd.getNextChoice();
        boolean showScale = gd.getNextBoolean();


        DebugHelper.print(this, "hello 5");

        ImagePlus scale = NewImage.createByteImage("Scale", getVolumeManager().getCurrentImagePlus().getNSlices(), 30, 1, NewImage.FILL_RAMP);
        DebugHelper.print(this, "hello 6");
        if (showScale) {
            scale.show();
        }
        IJ.run(scale, colorTable, "");

        DebugHelper.print(this, "hello 7");

        //new WaitForUserDialog("Please choose a Lookup table for your scale and click ok afterwards.").show();
        DebugHelper.print(this, "hello 8");

        IJ.run(scale, "RGB Color", "");

        ColorProcessor colorProcessor = (ColorProcessor)scale.getProcessor();


        for (int i = 0; i < vm.length(); i++) {
            PolylineSurface pls = vm.getVolume(i);
            //Color color = GeometryVisualisationUtilities.getRandomColor(i);

            int centerZ = (pls.getEndSlice() + pls.getStartSlice()) / 2;
            Color color = colorProcessor.getColor(centerZ - 1, 1);
            DebugHelper.print(this, "color " + color.getRed() + "/" + color.getGreen() + "/" + color.getBlue() + "/" + color.getAlpha());

            pls.lineColor = color;
            pls.fillColor = color;
        }
        if (!showScale) {
            vm.unlockSwitchingToOtherImages();
        }
        vm.unlockManipulation();
        vm.refresh();
    }
}
