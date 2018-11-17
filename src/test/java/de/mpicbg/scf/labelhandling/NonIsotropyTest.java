package de.mpicbg.scf.labelhandling;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.labelhandling.data.Feature;
import de.mpicbg.scf.labelhandling.data.Utilities;
import de.mpicbg.scf.volumemanager.core.PolylineSurfaceRealRandomAccessibleRealInterval;
import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.NewImage;
import ij.measure.Calibration;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
public class NonIsotropyTest {

    double relativeTolerance = 0.02; //2% error allowed

    @Test
    public void testCircle()
    {
        // Generate test circle
        double circleXinPixels = 500;
        double circleYinPixels = 500;
        double circleRadiusinPixels = 250;
        double halfsize = circleRadiusinPixels / Math.sqrt(2);
        EllipseRoi ellipseRoi = new EllipseRoi(circleXinPixels - halfsize, circleYinPixels - halfsize, circleXinPixels + halfsize, circleYinPixels + halfsize, 1);

        // generate test PolylineSurface
        PolylineSurface polylineSurface = new PolylineSurface("");
        polylineSurface.addRoi(1, ellipseRoi);

        // Generate Test Image
        Calibration calib = new Calibration();
        calib.pixelWidth = 0.5;
        calib.pixelHeight = 0.5;
        calib.pixelDepth = 1;
        calib.xOrigin = 476;
        calib.yOrigin = 2342.5;
        calib.zOrigin = -433;

        ImagePlus imp = NewImage.createByteImage("", 100,100,1,NewImage.FILL_BLACK);
        imp.setCalibration(calib);

        // Conversions
        double[] origin = Utilities.getOrigin(imp);
        double[] voxelSize = Utilities.getVoxelSize(imp);

        PolylineSurfaceRealRandomAccessibleRealInterval psrrari = new PolylineSurfaceRealRandomAccessibleRealInterval(polylineSurface);
        RandomAccessibleInterval<BoolType> rai = Utilities.raster(psrrari);




        ArrayList<RandomAccessibleInterval<BoolType>> regions = new ArrayList<>();
        regions.add(rai);


        // Actually do the test

        // setup analyser
        OpsLabelAnalyser<FloatType, BoolType> ola = new OpsLabelAnalyser<FloatType, BoolType>(regions, new Feature[]{Feature.AREA, Feature.CENTROID_2D});
        ola.setVoxelSize(voxelSize);
        ola.setOrigin(origin);

        // measure area
        double calculatedArea = Math.PI * Math.pow(circleRadiusinPixels * calib.pixelWidth ,2);
        double measuredArea = ola.getFeatures(Feature.AREA)[0];

        DebugHelper.print(this, "voxelSize: " + Arrays.toString(voxelSize));
        DebugHelper.print(this, "origin: " + Arrays.toString(origin));

        DebugHelper.print(this, "calculatedArea: " + calculatedArea);
        DebugHelper.print(this, "measuredArea: " + measuredArea);

        assertTrue("Circle area determined correctly ", Math.abs(calculatedArea / measuredArea - 1.0) < relativeTolerance);

        // measure position

        double calculatedX = circleXinPixels * calib.pixelWidth + calib.xOrigin;
        double calculatedY = circleYinPixels * calib.pixelHeight + calib.yOrigin;
        double measuredX = ola.getFeatures(Feature.CENTROID_2D, 0)[0];
        double measuredY = ola.getFeatures(Feature.CENTROID_2D, 1)[0];

        DebugHelper.print(this, "calculatedX: " + calculatedX);
        DebugHelper.print(this, "measuredX: " + measuredX);
        DebugHelper.print(this, "calculatedY: " + calculatedY);
        DebugHelper.print(this, "measuredY: " + measuredY);


        assertTrue("Circle X determined correctly ", Math.abs(calculatedX / measuredX - 1.0) < relativeTolerance);
        assertTrue("Circle Y determined correctly ", Math.abs(calculatedY / measuredY - 1.0) < relativeTolerance);
    }


    @Test
    public void testCylinder()
    {
        // Generate test circle
        double circleXinPixels = 50;
        double circleYinPixels = 50;
        double circleRadiusinPixels = 25;
        double sizeZ = 10;
        double halfsize = circleRadiusinPixels / Math.sqrt(2);
        EllipseRoi ellipseRoi = new EllipseRoi(circleXinPixels - halfsize, circleYinPixels - halfsize, circleXinPixels + halfsize, circleYinPixels + halfsize, 1);

        // generate test PolylineSurface
        PolylineSurface polylineSurface = new PolylineSurface("");
        polylineSurface.addRoi(1, ellipseRoi);
        polylineSurface.addRoi((int)sizeZ, ellipseRoi);

        // Generate Test Image
        Calibration calib = new Calibration();
        calib.pixelWidth = 0.5;
        calib.pixelHeight = calib.pixelWidth;
        calib.pixelDepth = 2;
        calib.xOrigin = 476;
        calib.yOrigin = 2342.5;
        calib.zOrigin = -433;

        ImagePlus imp = NewImage.createByteImage("", 100,100,100,NewImage.FILL_BLACK);
        imp.setCalibration(calib);

        // Conversions
        double[] origin = Utilities.getOrigin(imp);
        double[] voxelSize = Utilities.getVoxelSize(imp);

        PolylineSurfaceRealRandomAccessibleRealInterval psrrari = new PolylineSurfaceRealRandomAccessibleRealInterval(polylineSurface);
        RandomAccessibleInterval<BoolType> rai = Utilities.raster(psrrari);

        //ArrayList<RandomAccessibleInterval<BoolType>> regions = new ArrayList<>();

        //IterableRegion<BoolType> reg = Regions.iterable(rai);
        //regions.add(reg);


        RandomAccessibleInterval<BitType> bitImg = Utilities.convertBoolTypeImgToBitType(rai);
        //ImageJFunctions.show(bitImg);
        //new WaitForUserDialog("blaa", "bl").show();


        // setup analyser
        OpsLabelAnalyser<FloatType, BitType> ola = new OpsLabelAnalyser<FloatType, BitType>(bitImg, new Feature[]{Feature.VOLUME/*, Feature.CENTROID_3D*/});
        ola.setVoxelSize(voxelSize);
        ola.setOrigin(origin);

        // measure area
        double calculatedVolume = Math.PI * Math.pow(circleRadiusinPixels * calib.pixelWidth ,2) * sizeZ * calib.pixelDepth;
        double measuredVolume = ola.getFeatures(Feature.VOLUME)[0];

        DebugHelper.print(this, "voxelSize: " + Arrays.toString(voxelSize));
        DebugHelper.print(this, "origin: " + Arrays.toString(origin));

        DebugHelper.print(this, "calculatedVolume: " + calculatedVolume);
        DebugHelper.print(this, "measuredVolume: " + measuredVolume);

        assertTrue("Cylinder volume determined correctly ", Math.abs(calculatedVolume / measuredVolume - 1.0) < relativeTolerance);

        // measure position
        double calculatedX = circleXinPixels * calib.pixelWidth + calib.xOrigin;
        double calculatedY = circleYinPixels * calib.pixelHeight + calib.yOrigin;
        double calculatedZ = ((polylineSurface.getStartSlice() + polylineSurface.getEndSlice()) / 2.0 - 1) * calib.pixelDepth + calib.zOrigin;
//        double measuredX = ola.getFeatures(Feature.CENTROID_3D, 0)[0];
//        double measuredY = ola.getFeatures(Feature.CENTROID_3D, 1)[0];
//        double measuredZ = ola.getFeatures(Feature.CENTROID_3D, 2)[0];

        DebugHelper.print(this, "calculatedX: " + calculatedX);
        //DebugHelper.print(this, "measuredX: " + measuredX);
        DebugHelper.print(this, "calculatedY: " + calculatedY);
        //DebugHelper.print(this, "measuredY: " + measuredY);
        DebugHelper.print(this, "calculatedZ: " + calculatedZ);
        //DebugHelper.print(this, "measuredZ: " + measuredZ);

        //Todo: run this after ops fix
        //assertTrue("Circle X determined correctly ", Math.abs(calculatedX / measuredX - 1.0) < relativeTolerance);
        //assertTrue("Circle Y determined correctly ", Math.abs(calculatedY / measuredY - 1.0) < relativeTolerance);
        //ssertTrue("Circle Z determined correctly ", Math.abs(calculatedZ / measuredZ - 1.0) < relativeTolerance);
    }
}
