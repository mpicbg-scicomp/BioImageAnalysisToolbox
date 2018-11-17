package de.mpicbg.scf.rhaase.volumemanager;

import de.mpicbg.scf.fijiplugins.ui.roi.BrushPluginTool;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.VolumeManagerPluginService;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * This class corresponds to the menu entry in ImageJ2 to run the Volume manager from scratch.
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: April 2017
 */

@Plugin(type = Command.class, menuPath = "SCF>Experimental>Segmentation>ROI sequence manager (2D/2D+t)")
public class RoiSequenceManagerIJ2Plugin implements Command {

    @Parameter
    private LogService log;

    @Parameter(required = false)
    private ImagePlus imp;

    @Parameter
    private ImageJ ij;

    @Override
    public void run() {
        DebugHelper.print(this, "Context... " + log.getContext());

        VolumeManager.context = ij.getContext();

        VolumeManagerPluginService vmps = ij.get(VolumeManagerPluginService.class);
        DebugHelper.print(this, "available vm plugins: " +  vmps.getPluginNames().size());

        VolumeManager volumeManager = VolumeManager.getInstance();
        volumeManager.initializeAllPlugins("ROI sequence manager");
        volumeManager.setTitle("ROI sequence manager (experimental)");


        if (imp != null) {
            volumeManager.imageUpdated(imp);
        }
        volumeManager.setVisible(true);

        BrushPluginTool bpt = new BrushPluginTool();
        bpt.run("");
        IJ.setTool("polygon");
    }
}
