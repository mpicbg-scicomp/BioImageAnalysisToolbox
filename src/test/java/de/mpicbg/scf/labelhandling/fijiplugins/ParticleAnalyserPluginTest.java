package de.mpicbg.scf.labelhandling.fijiplugins;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.IJ;
import ij.ImagePlus;
import java.io.File;
import java.io.IOException;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import org.junit.Test;

/**
 * Created by rhaase on 12/10/16.
 */
public class ParticleAnalyserPluginTest {
    @Test
    public void testIfParticleAnalyserIJ2WorksWithLabelImages() throws IOException {
        if (SystemUtilities.isHeadless()) {
            DebugHelper.print(this, "This test does not run in headless mode.");
            return;
        }
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        // ask the user for a file to open
        final File file = new File("src/test/resources/labelmaptest.tif");
                //ij.ui().chooseFile(null, "open");

        // load the dataset,
        System.out.println("hello1");
        final Dataset dataset = ij.scifio().datasetIO().open(file.getPath());
        System.out.println("hello2");

        // Show the image
        ij.ui().show(dataset);

        // invoke the plugins run-function for testing...
        System.out.println("hello3");
        ij.command().run(ParticleAnalyserIJ2Plugin.class, false);
        System.out.println("hello4");
    }

    @Test
    public void testIfParticleAnalyserIJ1WorksWithLabelImages()
    {
        if (SystemUtilities.isHeadless()) {
            DebugHelper.print(this, "This test does not run in headless mode.");
            return;
        }
        System.out.println("hello1");
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        System.out.println("hello2");
        imp.show();

        System.out.println("hello3");
        new ParticleAnalyserIJ1Plugin().run(null);

        System.out.println("hello4");
    }

}