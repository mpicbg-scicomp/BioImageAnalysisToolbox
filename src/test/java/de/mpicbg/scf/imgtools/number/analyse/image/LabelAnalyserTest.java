package de.mpicbg.scf.imgtools.number.analyse.image;

import de.mpicbg.scf.imgtools.image.create.labelmap.LabelingUtilities;
import de.mpicbg.scf.imgtools.number.analyse.array.Equal;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature;
import de.mpicbg.scf.imgtools.number.filter.ArrayUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test class for the label based particle analyser
 * <p>
 * TODO: Build in reference values.
 * TODO: Remove call to dirty workaround in LabelMoments3D
 * <p>
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: November 2015
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
public class LabelAnalyserTest {

    @Test
    public void academicTestExample() {
        // --------------------------------------
        // Open an image, in this case a labelMap
        ImagePlus imp = IJ.openImage("src/test/resources/surface2D_labelmap.tif");
        Img<IntType> img = ImageJFunctions.wrapReal(imp);

        // --------------------
        // Get image properties
        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};


        LabelAnalyser<IntType, IntType> lpa = new LabelAnalyser<IntType, IntType>(img, voxelSize, new Feature[]{Feature.AREA_VOLUME,
                Feature.MEAN, Feature.STD_DEV, Feature.MIN, Feature.MAX, Feature.AVERAGE_POSITION, Feature.CENTER_OF_MASS, Feature.BOUNDING_BOX,
                Feature.SPHERICITY, Feature.SURFACE_AREA, Feature.EIGENVALUES, Feature.ASPECT_RATIO, Feature.NUMBER_OF_TOUCHING_NEIGHBORS,
                Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS, Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN});


        lpa.setSignalImage(img);

        // Most features only have one dimension, e.g. for every label, there is only on average signal. These features can be accessed by
        double[] averageSignal = lpa.getFeatures(Feature.MEAN);

        DebugHelper.print(this, "Avg:\n" + Arrays.toString(averageSignal));

        // But some features, such as center of mass or average position, have a dimension like the given image: In two dimensional images the average position
        // is an X/Y position in three dimensions its X/Y/Z.
        // The bounding box feature for example has 4 features in 2D space and 6 in 3D. It's minimum and maximum coordinates sorted like minX, minY, minZ, maxX,
        // maxY, maxZ. The 'unknown' number of features may be accessed like this:

        // Determine how many dimensions a feature has.
        int numDims = lpa.getFeaturesNumDimensions(Feature.BOUNDING_BOX);

        double[][] boundingBoxes = new double[numDims][];

        for (int d = 0; d < numDims; d++) {
            boundingBoxes[d] = lpa.getFeatures(Feature.BOUNDING_BOX, d);
        }

        imp.close();
        DebugHelper.print(this, "Test finished");
    }

    @Test
    public void testImgLabelingConversionUsingBinaryMaskRegionOfInterest() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        ImgLabeling<Integer, IntType> labeling = LabelAnalyser.getIntIntImgLabellingFromLabelMapImg(ImageJFunctions.convertFloat(imp));

        Img<FloatType> img = ImageJFunctions.convertFloat(imp);

        long[] labelPixelCount = LabelAnalyser.getLabelsPixelCount(img);

        LabelRegions<Integer> regions = new LabelRegions<Integer>(labeling);

        Set<Integer> labelNames = labeling.getMapping().getLabels();

        DebugHelper.print(this, "Numer of labels: " + labelNames.size());

        for (Integer labelName : labelNames) {
            LabelRegion<Integer> lr = regions.getLabelRegion(labelName);

            IterableInterval<FloatType> ii = Regions.sample(lr, img);

            FloatType min = img.cursor().get().createVariable();
            FloatType max = img.cursor().get().createVariable();

            ComputeMinMax<FloatType> cmm = new ComputeMinMax<FloatType>(ii, min, max);
            cmm.process();
            DebugHelper.print(this, "Label " + labelName + " has a minimum of " + cmm.getMin().get() + " and a maximum of " + cmm.getMax().get());
            assertTrue("Label minimum in labelmap equals label name ", cmm.getMin().get() == labelName);
            assertTrue("Label maximum in labelmap equals label name ", cmm.getMax().get() == labelName);

            DebugHelper.print(this, "Label " + labelName + " has " + ii.size() + " elements " + labelPixelCount[labelName - 1]);
            assertTrue("Label pixel count  is correct", ii.size() == labelPixelCount[labelName - 1]);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        imp.close();
    }


    @Test
    public void testLabelsCenterOfMass() {

        DebugHelper.print(this, "Testing testLabelsCenterOfMass...");
        double tolerance = 0.001;

        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        Img<FloatType> img = ImagePlusAdapter.wrap(imp);
        float[][] coordinates = LabelAnalyser.getLabelsCenterOfMass(img);

        double[][] referenceCoordinates = {
                {
                        113.5,
                        272.5,
                        300.5,
                        256.5,
                        91.5,
                        197.5,
                        244.16667,
                        242.85,
                        226.33333,
                        37.50121,
                        61.87631,
                        103.24031,
                        135.30977,
                        145.14806,
                        125.915565,
                        91.99647,
                        56.970722,
                        111.77178,
                        147.0804,
                        158.93605,
                        144.42523,
                        103.5,
                        60.024097,
                        44.898205,
                        65.40476,
                        319.5,
                        336.5,
                        287.5,
                        310.5,
                        310.5,
                        310.5,
                        351.5
                }, {
                140.5,
                75.5,
                227.5,
                327.5,
                240.5,
                227.5,
                165.83333,
                199.5,
                226.33333,
                312.15982,
                337.97562,
                351.57056,
                336.23492,
                305.7117,
                275.56464,
                266.84628,
                282.50226,
                59.518673,
                77.487434,
                117.441864,
                158.73831,
                171.05128,
                154.30522,
                120.053894,
                81.7619,
                240.5,
                252.5,
                249.5,
                266.5,
                283.5,
                298.5,
                252.5
        }, {
                4.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5,
                9.5
        }
        };


        for (int i = 0; i < coordinates.length; i++) {
            for (int j = 0; j < coordinates[0].length; j++) {
                assertTrue("reference coordinate " + i + ", " + j + "is equal ", Math.abs(coordinates[i][j] - (float) referenceCoordinates[i][j]) < tolerance);
            }
        }

        imp.close();
    }


    @Test
    public void testIfLabelCentersMatchesToFirstMoment() {

        DebugHelper.print(this, "Testing testIfLabelCentersMatchesToFirstMoment...");
        double tolerance = 0.001;

        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        Img<FloatType> img = ImagePlusAdapter.convertFloat(imp);
        float[][] coordinates = LabelAnalyser.getLabelsCenterOfMass(img);

        for (int i = 0; i < Math.min(10, coordinates[0].length); i++) {
            LabelMoments3D<FloatType, FloatType> lm3d = new LabelMoments3D<FloatType, FloatType>(img, i + 1, img, new double[]{1, 1, 1}, 3);
            double[][][] moments = lm3d.getMoments();

            assertTrue("Center X coordinates[" + i + "] match to moments " + coordinates[0][i] + " == " + moments[1][0][0] / moments[0][0][0], Math.abs(coordinates[0][i] - moments[1][0][0] / moments[0][0][0]) < tolerance);
            assertTrue("Center Y coordinates[" + i + "] match to moments " + coordinates[1][i] + " == " + moments[0][1][0] / moments[0][0][0], Math.abs(coordinates[1][i] - moments[0][1][0] / moments[0][0][0]) < tolerance);
            assertTrue("Center Z coordinates[" + i + "] match to moments " + coordinates[2][i] + " == " + moments[0][0][1] / moments[0][0][0], Math.abs(coordinates[2][i] - moments[0][0][1] / moments[0][0][0]) < tolerance);
        }

        imp.close();
    }


    @Test
    public void testIfLabelPixelCountIsEqualToMoments() {
        DebugHelper.print(this, "Testing moments...");
        //new ij.ImageJ();
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        Img<FloatType> img = ImagePlusAdapter.wrap(imp);
        long[] counts = LabelAnalyser.getLabelsPixelCount(img);

        Interval bb = Intervals.createMinMax(new long[]{0, 0, 0, imp.getWidth() - 1, imp.getHeight() - 1, imp.getNSlices() - 1});

        for (int i = 0; i < Math.min(10, counts.length); i++) {
            LabelMoments3D<FloatType, FloatType> lm3d = new LabelMoments3D<FloatType, FloatType>(img, i + 1, bb, new double[]{1, 1, 1}, 2);

            double[][][] moments = lm3d.getMoments();

            DebugHelper.print(this, "Pixelcount[" + i + "] match to moment " + counts[i] + " == " + moments[0][0][0]);
            assertTrue("Pixelcount[" + i + "] match to moment " + counts[i] + " == " + moments[0][0][0], counts[i] == moments[0][0][0]);
        }

        imp.close();
    }


    @Test
    public void testIfLabelPixelCountIsCorrect() {
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        Img<FloatType> img = ImagePlusAdapter.wrap(imp);
        long[] counts = LabelAnalyser.getLabelsPixelCount(img);

        long[] references = {
                100 * 10,
                400 * 20,
                200 * 20,
                1600 * 20,
                316 * 20,
                4 * 20,
                300 * 20,
                200 * 20,
                210 * 20,
                413 * 20,
                574 * 20,
                645 * 20,
                481 * 20,
                385 * 20,
                379 * 20,
                566 * 20,
                444 * 20,
                241 * 20,
                199 * 20,
                172 * 20,
                214 * 20,
                312 * 20,
                249 * 20,
                167 * 20,
                210 * 20,
                200 * 20,
                200 * 20,
                200 * 20,
                200 * 20,
                200 * 20,
                200 * 20,
                200 * 20
        };

        assertTrue("Number of array elements and array entries equal", counts.length == references.length);

        for (int i = 0; i < counts.length; i++) {
            assertTrue("PixelCount " + i + " is correct " + counts[i] + " == " + references[i], counts[i] == references[i]);
        }

        imp.close();
    }


    @Test
    public void testIfFoundPositionsAreIndeedInsideTheLabels() {
        DebugHelper.print(this, "Testing testIfLabelCentersMatchesToFirstMoment...");

        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");

        Img<FloatType> img = ImagePlusAdapter.wrap(imp);
        float[][] coordinates = LabelAnalyser.getLabelsPosition(img, -1);

        for (int i = 0; i < coordinates[0].length; i++) {
            imp.setZ((int) coordinates[2][i] + 1);
            int value = imp.getProcessor().getPixel((int) coordinates[0][i], (int) coordinates[1][i]);
            assertTrue("Found points are indeed inside the labels ", value == i + 1); //plus one, because background is missing in the array
        }

        imp.close();
    }

    @Test
    public void testIfStandardDeviationIsCalculatedCorrectly() {
        ImagePlus signalMap = IJ.openImage("src/test/resources/blobs.tif");
        ImagePlus labelMap = IJ.openImage("src/test/resources/blobs_labelmap.tif");

        LabelAnalyser<FloatType, FloatType> la = new LabelAnalyser<FloatType, FloatType>(ImageJFunctions.convertFloat(labelMap), new double[]{1, 1}, new Feature[]{Feature.AREA_VOLUME, Feature.STD_DEV});
        la.setSignalImage(ImageJFunctions.convertFloat(signalMap));

        // Data comes from histogram plugin in FIJI
        double[] referenceStdDevs = {67.5, 69.2};
        double[] testStdDevs = la.getFeatures(Feature.STD_DEV);

        assertTrue("measured standard deviations equal reference (" + Arrays.toString(referenceStdDevs) + " != " + Arrays.toString(testStdDevs) + ")", new Equal(referenceStdDevs, testStdDevs, 0.1).evaluate());
    }

    @Test
    public void testIfImgBitTypeCreationWorks() {
        ImagePlus labelMapImp3D = IJ.openImage("src/test/resources/labelmaptest.tif");
        labelMapImp3D.killRoi();

        Img<FloatType> testImage = ImageJFunctions.convertFloat(labelMapImp3D);


        ImgLabeling<Integer, IntType> labeling = LabelAnalyser.getIntIntImgLabellingFromLabelMapImg(testImage);

        ArrayList<RandomAccessibleInterval<BoolType>> list = LabelingUtilities.getRegionsFromImgLabeling(labeling);
        RandomAccessibleInterval<BoolType> lr = list.get(0);

        Img<BitType> binaryImage = LabelAnalyser.convertLabelRegionToBinaryImage(lr, testImage);

        Cursor<BitType> bitCursor = binaryImage.cursor();
        Cursor<FloatType> floatCursor = testImage.cursor();

        while (bitCursor.hasNext() && floatCursor.hasNext()) {
            BitType binaryPixel = bitCursor.next();
            FloatType pixel = floatCursor.next();

            assertTrue((!binaryPixel.get()) || pixel.get() == 1);
        }
    }


    @Test
    public void testIfGetLabelPositionWorks() {
        ImagePlus labelMap = IJ.openImage("src/test/resources/labelmaptest.tif");
        Img<FloatType> labelMapImg = ImagePlusAdapter.convertFloat(labelMap);

        DebugHelper.print(this, ArrayUtilities.toString(LabelAnalyser.getLabelsPosition(labelMapImg, -1)));
    }

}
