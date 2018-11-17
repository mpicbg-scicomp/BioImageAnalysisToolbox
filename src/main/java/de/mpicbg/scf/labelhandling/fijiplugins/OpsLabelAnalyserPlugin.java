package de.mpicbg.scf.labelhandling.fijiplugins;

import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.VolumeManagerPlugin;
import de.mpicbg.scf.volumemanager.plugins.io.LoadFromDiscPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import java.io.IOException;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 *
 * This class is deprecated because if the JAR is in Fijis plugin folder, the OpsLabelAnalyser will be automatically
 * recognised and added to the volume manager
 */
@Deprecated
public class OpsLabelAnalyserPlugin implements PlugIn{

    @Override
    public void run(String s) {
        VolumeManagerPlugin vmp = new VolumeManagerPlugin();
        vmp.run("");

        VolumeManager volumeManager = VolumeManager.getInstance();
        volumeManager.addPlugin("Analysis", null, new de.mpicbg.scf.labelhandling.volumemanager.plugins.analysis.OpsLabelAnalyserPlugin(volumeManager));



    }

    public static void main(final String... args) throws IOException
    {
        new ij.ImageJ();

        ImagePlus imp = IJ.openImage("/Users/rhaase/Projects/Akanksha_Jain_Tomancak_Determine motion of cut cell membrane_1900/data/LAser_Cut_sample.avi");
        imp.show();

        OpsLabelAnalyserPlugin lap = new OpsLabelAnalyserPlugin();
        lap.run("");



        //new VolumeManagerPlugin().run("");

        LoadFromDiscPlugin lfdp = new LoadFromDiscPlugin(VolumeManager.getInstance());
        lfdp.load("/Users/rhaase/Projects/Akanksha_Jain_Tomancak_Determine motion of cut cell membrane_1900/data/celloutlines.zip");

    }

}
