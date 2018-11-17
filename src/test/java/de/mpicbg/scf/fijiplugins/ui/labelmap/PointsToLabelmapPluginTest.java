package de.mpicbg.scf.fijiplugins.ui.labelmap;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.WaitForUserDialog;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rhaase on 3/3/16.
 */
public class PointsToLabelmapPluginTest {


    @Test
    public void testTransferPointsToLabelmap()
    {
        ImagePlus imp = IJ.openImage("src/test/resources/blobs.tif");
        //imp.show();

        PointRoi pr = new PointRoi(new int[]{10,20,30,40,50}, new int[]{15,25,35,45,55},5);
        imp.setRoi(pr);

        //imp.show();
        //new WaitForUserDialog("", "").show();


        ImagePlus labelMap = new PointsToLabelmapPlugin().apply(imp);

        ImageStatistics stats = labelMap.getStatistics();

        assertTrue("after transfering 5 points to a labelmap, its maximum is 5", stats.max == 5);

        ImageProcessor ip = labelMap.getProcessor();
        assertTrue("point 1 is labelled correctly " + ip.getPixelValue(10,15), ip.getPixelValue(10,15) == 1);
        assertTrue("point 2 is labelled correctly " + ip.getPixelValue(20,25), ip.getPixelValue(20,25) == 2);
        assertTrue("point 3 is labelled correctly " + ip.getPixelValue(30,35), ip.getPixelValue(30,35) == 3);
        assertTrue("point 4 is labelled correctly " + ip.getPixelValue(40,45), ip.getPixelValue(40,45) == 4);
        assertTrue("point 5 is labelled correctly " + ip.getPixelValue(50,55), ip.getPixelValue(50,55) == 5);
    }

    /**
     * Beginning of march 2016, the tool crashed when the second point of a list of points had smaller x or y coordinates compared to the first point... As that bug appeared again after fixing, this test case was written.
     */
    @Test
    public void testIfItCrashesIfSecondPointIsToLeftOfFirstPoint()
    {
        ImagePlus imp = IJ.openImage("src/test/resources/blobs.tif");

        PointRoi pr = new PointRoi(new int[]{10,20}, new int[]{5,5}, 2);
        imp.setRoi(pr);
        //imp.show();

        ImagePlus labelMap = new PointsToLabelmapPlugin().apply(imp);
        //labelMap.show();

       // new WaitForUserDialog("","").show();
    }
}