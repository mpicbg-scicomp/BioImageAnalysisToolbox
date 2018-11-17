package de.mpicbg.scf.imgtools.image.filter;

import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import de.mpicbg.scf.imgtools.number.analyse.array.Max;
import de.mpicbg.scf.imgtools.number.analyse.array.Min;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import java.util.Arrays;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2017
 * <p>
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class ConstraintLabelMapTest {

    /**
     * Academic example
     */
    @Test
    public void testIfAverageSignalMeasurmentWorks() {
        // Open an image, in this case a labelMap
        ImagePlus imp = IJ.openImage("src/test/resources/surface2D_labelmap.tif");
        Img<IntType> labelImg = ImageJFunctions.wrapReal(imp);

        // Get image properties
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        // Determine average signal of labels
        LabelAnalyser<IntType, IntType> lpa =
                new LabelAnalyser<IntType, IntType>(
                        labelImg,
                        voxelSize,
                        new Feature[]{Feature.MEAN}
                );

        lpa.setSignalImage(labelImg);

        System.out.println("Average signal of all labeled objects:\n" +
                Arrays.toString(lpa.getFeatures(Feature.MEAN)));
    }


    /**
     * Academic example
     */
    @Test
    public void testIfNumberOfNeigborMeasurmentWorks() {
        // Open an image, in this case a labelMap
        ImagePlus imp = IJ.openImage("src/test/resources/surface2D_labelmap.tif");
        Img<IntType> labelImg = ImageJFunctions.wrapReal(imp);

        // Get image properties
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        // Determine number of neighbors
        LabelAnalyser<IntType, IntType> lpa =
                new LabelAnalyser<IntType, IntType>(
                        labelImg,
                        voxelSize,
                        new Feature[]{Feature.NUMBER_OF_TOUCHING_NEIGHBORS}
                );

        System.out.println("Number of neighbors of all labeled objects:\n" +
                Arrays.toString(lpa.getFeatures(Feature.NUMBER_OF_TOUCHING_NEIGHBORS)));
    }

    /**
     * Academic example
     */
    @Test
    public void testIfTouchingLabelsCountBasedConstraintWorks2() {
        // Open an image, in this case a labelMap
        ImagePlus imp = IJ.openImage("src/test/resources/surface2D_labelmap.tif");
        Img<IntType> labelImg = ImageJFunctions.wrapReal(imp);

        // Get image properties
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        // kick out all labels which have less than 3 or more than 8 neighbors
        ConstraintLabelmap<IntType, FloatType> clm =
                new ConstraintLabelmap<IntType, FloatType>(
                        labelImg,
                        voxelSize
                );
        clm.addConstraint(Feature.NUMBER_OF_TOUCHING_NEIGHBORS, 3, 8);
        Img<IntType> resultingLabelImg = clm.getResult();

        resultingLabelImg.cursor();
    }

    /**
     * Academic example
     */
    @Test
    public void testIfTouchingLabelsCountBasedConstraintWorks() {
        // Open an image, in this case a labelMap
        ImagePlus imp = IJ.openImage("src/test/resources/surface2D_labelmap.tif");
        Img<IntType> img = ImageJFunctions.wrapReal(imp);

        // Get image properties
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        ConstraintLabelmap<IntType, FloatType> clm;

        // Determine number of touching neighbors  of labels
        LabelAnalyser<IntType, FloatType> lpa =
                new LabelAnalyser<IntType, FloatType>(
                        img,
                        voxelSize,
                        new Feature[]{Feature.NUMBER_OF_TOUCHING_NEIGHBORS, Feature.AREA_VOLUME}
                );

        System.out.println("Number of neighbors per label:\n" +
                Arrays.toString(lpa.getFeatures(Feature.NUMBER_OF_TOUCHING_NEIGHBORS)));
        System.out.println("Area per label:\n" +
                Arrays.toString(lpa.getFeatures(Feature.AREA_VOLUME)));

        // kick out all labels which have less than 20 pixels
        clm = new ConstraintLabelmap<IntType, FloatType>(
                img,
                voxelSize
        );
        clm.addConstraint(Feature.AREA_VOLUME, 20 * voxelSize[0] * voxelSize[1], Double.MAX_VALUE);
        img = clm.getResult();

        // kick out all labels which have less than 3 or more than 8 neighbors
        clm = new ConstraintLabelmap<IntType, FloatType>(
                img,
                voxelSize
        );
        clm.addConstraint(Feature.NUMBER_OF_TOUCHING_NEIGHBORS, 3, 8);
        img = clm.getResult();

        imp.close();
    }

    @Test
    public void testIfGenericConstraintingWorksWellIn2D() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmap_singleslice.tif");
        Img<FloatType> wrappedImp = ImageJFunctions.wrapReal(imp);
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        testIfGenericConstraintingWorksWellInND(wrappedImp, voxelSize, 0);
        testIfGenericConstraintingWorksWellInND(wrappedImp, voxelSize, 1);

        imp.close();
    }


    @Test
    public void testIfGenericConstraintingWorksWellIn3D() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmap.tif");
        Img<FloatType> wrappedImp = ImageJFunctions.wrapReal(imp);
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        testIfGenericConstraintingWorksWellInND(wrappedImp, voxelSize, 0);
        testIfGenericConstraintingWorksWellInND(wrappedImp, voxelSize, 1);
        testIfGenericConstraintingWorksWellInND(wrappedImp, voxelSize, 2);

        imp.close();
    }

    private void testIfGenericConstraintingWorksWellInND(Img<FloatType> wrappedImp, double[] voxelSize, int d) {
        // Prepare image
        if (d == 0) {
            testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.AREA_VOLUME, 0);
            testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.MEAN, 0);
            testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.STD_DEV, 0);
            testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.MIN, 0);
            testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.MAX, 0);
        }
        testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.AVERAGE_POSITION, d);
        testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.CENTER_OF_MASS, d);
        testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.BOUNDING_BOX, d);
        if (d == 0) {
            testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.SPHERICITY, 0);
        }
        testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.EIGENVALUES, d);
        if (d == 0) {
            testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.ASPECT_RATIO, 0);
            testIfGenericConstraintingWorksWell(wrappedImp, voxelSize, Feature.NUMBER_OF_TOUCHING_NEIGHBORS, 0);
        }
    }

    private void testIfGenericConstraintingWorksWell(Img<FloatType> wrappedImp, double[] voxelSize, Feature measurementToTest, int dimension) {

        // -----------------------------------------------------
        // Determine average volume of labels
        LabelAnalyser<FloatType, FloatType> lpa = new LabelAnalyser<FloatType, FloatType>(wrappedImp, voxelSize,
                new Feature[]{measurementToTest});
        double[] valuesBefore = lpa.getFeatures(measurementToTest);
        DebugHelper.print(this, "-------------------------------------------------- " + measurementToTest.toString());
        DebugHelper.print(this, "valuesBefore: " + Arrays.toString(valuesBefore));
        double averageValue = new Mean().evaluate(valuesBefore);
        DebugHelper.print(this, "==================================================");

        // constraint by average
        ConstraintLabelmap<FloatType, FloatType> clm = new ConstraintLabelmap<FloatType, FloatType>(wrappedImp, voxelSize);
        clm.addConstraint(measurementToTest, averageValue, Double.MAX_VALUE);
        Img<FloatType> resultingLabelMap = clm.getResult();

        // test if new minimum >= former average
        LabelAnalyser<FloatType, FloatType> lpa2 = new LabelAnalyser<FloatType, FloatType>(resultingLabelMap, voxelSize,
                new Feature[]{measurementToTest});
        double[] valuesAfter = lpa2.getFeatures(measurementToTest);
        double minimumValue = new Max().evaluate(valuesAfter);

        // the new minimum should be larger or equal than the old average
        assertTrue("Successfully kicking out objects which have a smaller " + measurementToTest.toString() + " than the average ", minimumValue >= averageValue);


        // -----------------------------------------------------
        // Determine average volume of labels
        lpa = new LabelAnalyser<FloatType, FloatType>(wrappedImp, voxelSize,
                new Feature[]{measurementToTest});
        valuesBefore = lpa.getFeatures(measurementToTest);
        averageValue = new Mean().evaluate(valuesBefore);

        // constraint by average
        clm = new ConstraintLabelmap<FloatType, FloatType>(wrappedImp, voxelSize);
        clm.addConstraint(measurementToTest, -Double.MAX_VALUE, averageValue);
        resultingLabelMap = clm.getResult();

        // test if new minimum >= former average
        lpa2 = new LabelAnalyser<FloatType, FloatType>(resultingLabelMap, voxelSize,
                new Feature[]{measurementToTest});
        valuesAfter = lpa2.getFeatures(measurementToTest);
        double maximumValue = new Min().evaluate(valuesAfter);

        // the new minimum should be larger or equal than the old average
        assertTrue("Successfully kicking out objects which have a larger " + measurementToTest.toString() + " than the average ", maximumValue <= averageValue);
    }

    /**
     * Academic example
     */
    @Test
    public void testIfGenericConstraintingVolumeWorksWell() {
        // Prepare image
        ImagePlus imp = IJ.openImage("src/test/resources/labelmap_singleslice.tif");
        Img<FloatType> wrappedImp = ImageJFunctions.wrapReal(imp);
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        // -----------------------------------------------------
        // Determine average volume of labels
        LabelAnalyser<FloatType, FloatType> lpa = new LabelAnalyser<FloatType, FloatType>(wrappedImp, voxelSize,
                new Feature[]{Feature.AREA_VOLUME});
        double[] volumesBefore = lpa.getFeatures(Feature.AREA_VOLUME);
        DebugHelper.print(this, "Volumes before:" + Arrays.toString(volumesBefore));
        double averageVolume = new Mean().evaluate(volumesBefore);
        DebugHelper.print(this, "Average volume:" + averageVolume);

        // -----------------------------------------------------
        // kick out all labels which are smaller than the average
        ConstraintLabelmap<FloatType, FloatType> clm = new ConstraintLabelmap<FloatType, FloatType>(wrappedImp, voxelSize);
        clm.addConstraint(Feature.AREA_VOLUME, averageVolume, Double.MAX_VALUE);
        Img<FloatType> resultingLabelMap = clm.getResult();

        // -----------------------------------------------------
        // Determine new minimum volume of any label
        LabelAnalyser<FloatType, FloatType> lpa2 = new LabelAnalyser<FloatType, FloatType>(resultingLabelMap, voxelSize,
                new Feature[]{Feature.AREA_VOLUME});
        double[] volumesAfter = lpa2.getFeatures(Feature.AREA_VOLUME);
        DebugHelper.print(this, "Volumes after:" + Arrays.toString(volumesAfter));
        double minimumVolume = new Max().evaluate(volumesAfter);
        DebugHelper.print(this, "Minimum after:" + minimumVolume);

        // the new minimum should be larger or equal than the old average
        assertTrue("Successfully kicking out objects which are smaller than the average volume", minimumVolume >= averageVolume);

        imp.close();
    }


    @Test
    public void testIfObjectsWithWrongSizeAreRemoved() {
        // new ij.ImageJ();
        ImagePlus imp = IJ.openImage("src/test/resources/labelmap_singleslice.tif");

        int[] dimensions = imp.getDimensions();
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        Img<FloatType> wrappedImp = ImageJFunctions.wrapReal(imp);

        ConstraintLabelmap<FloatType, FloatType> lmf = new ConstraintLabelmap<FloatType, FloatType>(wrappedImp, voxelSize);
        lmf.addConstraint(Feature.AREA_VOLUME, 100, 300);

        imp = ImageCreationUtilities.convertImgToImagePlus(lmf.getResult(), "Test", "", dimensions, imp.getCalibration());

        LabelAnalyser<FloatType, FloatType> la = new LabelAnalyser<FloatType, FloatType>(lmf.getResult(), voxelSize, new Feature[]{Feature.AREA_VOLUME});

        double[] volumes = la.getFeatures(Feature.AREA_VOLUME);
        double[] references = {100.0, 200.0, 300.0, 200.0, 210.0, 241.0, 199.0, 172.0, 214.0, 249.0, 167.0, 210.0, 200.0, 200.0, 200.0, 200.0, 200.0, 200.0,
                200.0

        };

        assertTrue("number of labels after constrainting is correct " + volumes.length + " == " + references.length, volumes.length == references.length);

        for (int i = 0; i < references.length; i++) {
            assertTrue("reference area " + i + " is equal ", references[i] == volumes[i]);
        }
        imp.close();
    }


    @Test
    public void testIfObjectWithWrongAverageGreyValueAreFilteredOut() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        int[] dimensions = imp.getDimensions();
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        Img<FloatType> wrappedImp = ImageJFunctions.wrapReal(imp);

        ConstraintLabelmap<FloatType, FloatType> clm = new ConstraintLabelmap<FloatType, FloatType>(wrappedImp, voxelSize);
        clm.setSignalImage(wrappedImp);
        clm.addConstraint(Feature.MEAN, 20, 25);

        imp = ImageCreationUtilities.convertImgToImagePlus(clm.getResult(), "Test", "", dimensions, imp.getCalibration());

        LabelAnalyser<FloatType, FloatType> la = new LabelAnalyser<FloatType, FloatType>(clm.getResult(), voxelSize, new Feature[]{Feature.AREA_VOLUME});

        double[] volumes = la.getFeatures(Feature.AREA_VOLUME);
        double[] references = {
                3440.0,
                4280.0,
                6240.0,
                4980.0,
                3340.0,
                4200.0
        };

        assertTrue("number of labels after constrainting is correct " + volumes.length + " == " + references.length, volumes.length == references.length);

        for (int i = 0; i < references.length; i++) {
            assertTrue("reference area " + i + " is equal ", references[i] == volumes[i]);

        }
        imp.close();
    }

    @Test
    public void testIfKeepingLabelIDsWhileConstraintingWorks() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        int[] dimensions = imp.getDimensions();
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};


        Img<FloatType> wrappedImp = ImageJFunctions.wrapReal(imp);

        ConstraintLabelmap<FloatType, FloatType> clm = new ConstraintLabelmap<FloatType, FloatType>(wrappedImp, voxelSize);
        clm.setSignalImage(wrappedImp);
        clm.addConstraint(Feature.MEAN, 20, 25);
        clm.setKeepIDs(true);

        Img<FloatType> resultImg = clm.getResult();
        LabelAnalyser<FloatType, FloatType> la = new LabelAnalyser<FloatType, FloatType>(resultImg, voxelSize, new Feature[]{Feature.MEAN});
        la.setSignalImage(resultImg);
        double[] means = la.getFeatures(Feature.MEAN);
        double[] references = {
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                20,
                21,
                22,
                23,
                24,
                25
        };

        DebugHelper.print(this, "means: " + Arrays.toString(means));

        assertTrue("number of measurements after constrainting1 is correct " + means.length + " == " + references.length, means.length == references.length);
        assertTrue("number of labels after constrainting1 is correct " + clm.getRemainingLabelsCount() + " == 6 ", clm.getRemainingLabelsCount() == 6);

        for (int i = 0; i < references.length; i++) {
            assertTrue("reference IDx " + i + " is equal: " + references[i] + " == " + means[i], references[i] == means[i] || (Double.isNaN(references[i]) && Double.isNaN(means[i])));
        }

        // invalidate result by changing an input parameter
        clm.setKeepIDs(false);


        la = new LabelAnalyser<FloatType, FloatType>(clm.getResult(), voxelSize, new Feature[]{Feature.MEAN});
        la.setSignalImage(clm.getResult());
        means = la.getFeatures(Feature.MEAN);

        references = new double[]{
                1,
                2,
                3,
                4,
                5,
                6
        };
        assertTrue("number of measurements after constrainting2 is correct " + means.length + " == " + references.length, means.length == references.length);
        assertTrue("number of labels after constrainting1 is correct " + clm.getRemainingLabelsCount() + " == 6 ", clm.getRemainingLabelsCount() == 6);

        for (int i = 0; i < references.length; i++) {
            assertTrue("reference IDx " + i + " is equal ", references[i] == means[i] || (Double.isNaN(references[i]) && Double.isNaN(means[i])));
        }

    }


}
