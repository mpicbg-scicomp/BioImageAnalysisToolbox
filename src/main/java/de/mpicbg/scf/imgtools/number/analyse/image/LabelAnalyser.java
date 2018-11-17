package de.mpicbg.scf.imgtools.number.analyse.image;

import de.mpicbg.scf.imgtools.image.create.labelmap.LabelingUtilities;
import de.mpicbg.scf.imgtools.number.analyse.geometry.NeighborPointsAnalyser;
import de.mpicbg.scf.imgtools.number.filter.ArrayUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import java.util.Arrays;
import java.util.EnumSet;
import net.imglib2.*;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegionCursor;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * This class allows to measure features (volume/area, aspect ratio, average signal value, ...) of labels in a label map in a generic way. Only features which
 * have been handed over to the constructor will be determined.
 * <p>
 * Example code can be found in LabelParticleAnalyserTest
 * <p>
 * <p>
 * TODO: - Implement features Surface area
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: March 2016
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
 *
 * @param <I> Type of the LabelMap
 * @param <F> Type of the image where signal measures are performed on.
 */
public class LabelAnalyser<I extends RealType<I>, F extends RealType<F>> {
    public enum Feature {
        AREA_VOLUME("Area / volume"),
        MEAN("Mean average signal"),
        STD_DEV("Standard deviation of signal"),
        MIN("Minimum signal"),
        MAX("Maximum signal"),
        AVERAGE_POSITION("Mean average position"),
        CENTER_OF_MASS("Center of mass"),
        BOUNDING_BOX("Bounding box"),
        SPHERICITY("Sphericity"),
        SURFACE_AREA("Circumference / surface area"),
        EIGENVALUES("Eigenvalues"),
        ASPECT_RATIO("Aspect ratio"),
        NUMBER_OF_TOUCHING_NEIGHBORS("Number of touching neighbors"),
        AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS("Average distance of n closes neighbors"),
        NUMBER_OF_NEIGHBORS_CLOSER_THAN("Number of neighbors closer than distance d");

        private final String name;

        Feature(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

    }

    // Input:
    private EnumSet<Feature> whatToMeasure = null;
    private int numberNOfClosestNeighbors = 5;
    private double closeNeighborDistanceD = 100;

    private final Img<I> labelMap;
    private Img<F> signalMap;

    private final double[] voxelSize;

    // State:
    private boolean resultsValid = false;

    // Output:
    private int numLabels = 0;

    private double[] volumes = null;
    private double[] averages = null;
    private double[] standardDeviations = null;
    private double[] minima = null;
    private double[] maxima = null;
    private double[][] centerofMassPositions = null;
    private double[][] boundingBoxPosition = null;

    private double[][] averagePositions = null;
    private double[][] eigenValues = null;
    private double[] aspectRatios = null;
    private double[] sphericities = null;
    private int[] numberOfTouchingNeighbors = null;
    private double[] averageDistanceOfNClosestNeighbors = null;
    private long[] numberOfNeighborsCloserThanDistanceD = null;

    public LabelAnalyser(Img<I> labelMap, double[] voxelSize, Feature[] featuresToExtract) {
        this.labelMap = labelMap;
        this.whatToMeasure = EnumSet.copyOf(Arrays.asList(featuresToExtract));
        this.voxelSize = voxelSize;
    }

    public LabelAnalyser(Img<I> labelMap, double[] voxelSize, EnumSet<Feature> featuresToExtract) {
        this.labelMap = labelMap;
        this.whatToMeasure = featuresToExtract;
        this.voxelSize = voxelSize;
    }

    public void setSignalImage(Img<F> signalImage) {
        this.signalMap = signalImage;
        resultsValid = false;
    }

    public void addMeasurement(Feature feature) {
        whatToMeasure.add(feature);
        resultsValid = false;
    }

    public void setNumberNOfClosestNeighbors(int numberNOfClosestNeighbors) {
        this.numberNOfClosestNeighbors = numberNOfClosestNeighbors;
        resultsValid = false;
    }

    public void setCloseNeighborDistanceD(double closeNeighborDistanceD) {
        this.closeNeighborDistanceD = closeNeighborDistanceD;
        resultsValid = false;
    }

    private void doFeatureExtaction() {
        if (resultsValid) {
            return;
        }

        // ------------------------
        // reset

        volumes = null;
        averages = null;
        standardDeviations = null;
        minima = null;
        maxima = null;
        centerofMassPositions = null;
        boundingBoxPosition = null;

        averagePositions = null;
        eigenValues = null;
        aspectRatios = null;
        sphericities = null;
        numberOfTouchingNeighbors = null;
        averageDistanceOfNClosestNeighbors = null;
        numberOfNeighborsCloserThanDistanceD = null;

        // ------------------------------------------------------------------------------------
        // Prepare: Get label map and signal image in ImgLib2 format, read out sizes, dimensions, number of labels
        LabelRegions<Integer> regions = null;

        Interval[] boundingIntervals = LabelAnalyser.getLabelsBoundingIntervals(labelMap);

        int numDimensions = labelMap.numDimensions();
        DebugHelper.print(this, "numDimensions " + numDimensions);
        DebugHelper.print(this, "numLabels " + numLabels);

        numLabels = boundingIntervals.length;

        // -------------------------------------------------------------
        // prepare: Create memory for all deserved parameters
        if (whatToMeasure.contains(Feature.AREA_VOLUME)) {
            volumes = new double[numLabels];
        }
        if (whatToMeasure.contains(Feature.MEAN)) {
            averages = new double[numLabels];
        }
        if (whatToMeasure.contains(Feature.STD_DEV)) {
            standardDeviations = new double[numLabels];
        }
        if (whatToMeasure.contains(Feature.MIN) || whatToMeasure.contains(Feature.MAX)) {
            if (whatToMeasure.contains(Feature.MIN)) {
                minima = new double[numLabels];
            }
            if (whatToMeasure.contains(Feature.MAX)) {
                maxima = new double[numLabels];
            }
            ImgLabeling<Integer, IntType> labeling = LabelAnalyser.getIntIntImgLabellingFromLabelMapImg(labelMap);

            regions = new LabelRegions<Integer>(labeling);
        }

        if (whatToMeasure.contains(Feature.AVERAGE_POSITION)) {
            averagePositions = new double[numDimensions][numLabels];
        }
        if (whatToMeasure.contains(Feature.CENTER_OF_MASS)) {
            centerofMassPositions = new double[numDimensions][numLabels];
        }
        if (whatToMeasure.contains(Feature.BOUNDING_BOX)) {
            boundingBoxPosition = new double[numDimensions * 2][numLabels];
        }
        if (whatToMeasure.contains(Feature.EIGENVALUES)) {
            eigenValues = new double[3/* imp.getNDimensions() */][numLabels];
        }
        if (whatToMeasure.contains(Feature.ASPECT_RATIO)) {
            aspectRatios = new double[numLabels];
        }
        if (whatToMeasure.contains(Feature.SPHERICITY)) {
            sphericities = new double[numLabels];
        }
        if (whatToMeasure.contains(Feature.NUMBER_OF_TOUCHING_NEIGHBORS)) {
            ConnectedLabels<I> lnc = new ConnectedLabels<I>(labelMap);
            numberOfTouchingNeighbors = lnc.getNeighbourCounts();
        }

        if (whatToMeasure.contains(Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS) || whatToMeasure.contains(Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN)) {
            DebugHelper.print(this, "Neighbor analysis");
            float[][] points = LabelAnalyser.getLabelsCenterOfMass(labelMap);

            NeighborPointsAnalyser pla = new NeighborPointsAnalyser(points, false);

            if (whatToMeasure.contains(Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS)) {
                averageDistanceOfNClosestNeighbors = ArrayUtilities.typeConvertToDouble(pla.getAverageDistanceOfNClosestPoints(numberNOfClosestNeighbors));
            }
            if (whatToMeasure.contains(Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN)) {
                numberOfNeighborsCloserThanDistanceD = pla.getNumberOfPointsNearerAs(closeNeighborDistanceD);
            }
        }

        if (signalMap == null) {
            if (averages != null || standardDeviations != null || centerofMassPositions != null) {
                DebugHelper.print(this, "Warning: Grey value measure requested, but no signal image given. Use LabelAnalyser.setSignalImage(img)!");
            }
        }
        // ---------------------------------------------------------------------------------------
        // Go through all labels and determine parameters (which were not determined so far)
        for (int i = 0; i < numLabels; i++) {
            LabelMoments3D<I, F> lm3d = new LabelMoments3D<I, F>(labelMap, i + 1, boundingIntervals[i], voxelSize, 2);

            if (averagePositions != null) {
                double[][][] m = lm3d.getMoments();
                averagePositions[0][i] = m[1][0][0] / m[0][0][0];
                averagePositions[1][i] = m[0][1][0] / m[0][0][0];
                if (labelMap.numDimensions() > 2) {
                    averagePositions[2][i] = m[0][0][1] / m[0][0][0];
                }
            }

            if (boundingBoxPosition != null) {
                for (int d = 0; d < numDimensions; d++) {
                    boundingBoxPosition[d][i] = boundingIntervals[i].min(d);
                    boundingBoxPosition[d + numDimensions][i] = boundingIntervals[i].max(d);
                }
            }

            if (eigenValues != null) {
                double[] ev = lm3d.getEigenVector();
                if (ev != null) {
                    for (int j = 0; j < Math.min(ev.length, eigenValues.length); j++) {
                        eigenValues[j][i] = ev[j];
                    }
                }
            }

            if (aspectRatios != null) {
                aspectRatios[i] = lm3d.getAspectRatio();
            }

            if (volumes != null) {
                volumes[i] = lm3d.getMoments()[0][0][0];
            }

            if (sphericities != null) {
                LabelSphericityDeterminator<I> lsd = new LabelSphericityDeterminator<I>(labelMap, i + 1, boundingIntervals[i], voxelSize);
                sphericities[i] = lsd.getSphericity();
            }

            if (signalMap != null) {
                if (averages != null || standardDeviations != null || centerofMassPositions != null) {

                    if (averages != null) {
                        double volume = lm3d.getMoments()[0][0][0];
                        lm3d.setSignalImage(signalMap);
                        averages[i] = lm3d.getMoments()[0][0][0] / volume;
                    } else {
                        lm3d.setSignalImage(signalMap);
                    }

                    if (centerofMassPositions != null) {
                        double[][][] m = lm3d.getMoments();
                        centerofMassPositions[0][i] = m[1][0][0] / m[0][0][0];
                        centerofMassPositions[1][i] = m[0][1][0] / m[0][0][0];
                        if (labelMap.numDimensions() > 2) {
                            centerofMassPositions[2][i] = m[0][0][1] / m[0][0][0];
                        }
                    }

                    if (standardDeviations != null) {
                        lm3d.setSignalImage(null);
                        double volume = lm3d.getMoments()[0][0][0];
                        lm3d.setSignalImage(signalMap);
                        double average = lm3d.getMoments()[0][0][0] / volume;

                        Cursor<F> signalCursor = Views.interval(signalMap, boundingIntervals[i]).cursor();
                        Cursor<I> labelCursor = Views.interval(labelMap, boundingIntervals[i]).cursor();

                        double sum = 0;
                        long count = 0;
                        while (signalCursor.hasNext() && labelCursor.hasNext()) {
                            signalCursor.next();
                            labelCursor.next();

                            if (((int) labelCursor.get().getRealDouble()) == (i + 1)) {
                                sum += Math.pow(signalCursor.get().getRealDouble() - average, 2);
                                count++;
                            }
                        }
                        standardDeviations[i] = Math.sqrt(sum / (count - 1));
                    }
                }

                if (regions != null) {
                    LabelRegion<Integer> lr = regions.getLabelRegion(i + 1);
                    IterableInterval<F> ii = Regions.sample(lr, signalMap);

                    F min = signalMap.cursor().next().copy();
                    F max = signalMap.cursor().next().copy();

                    ComputeMinMax<F> cmm = new ComputeMinMax<F>(ii, min, max);
                    cmm.process();

                    if (minima != null) {
                        minima[i] = cmm.getMin().getRealFloat();
                    }
                    if (maxima != null) {
                        maxima[i] = cmm.getMax().getRealFloat();
                    }
                }
            }
        }
        resultsValid = true;
    }

    public double[] getFeatures(Feature measurement) {
        return getFeatures(measurement, 0);
    }

    public int getFeaturesNumDimensions(Feature measurement) {
        doFeatureExtaction();

        switch (measurement) {
            case AREA_VOLUME:
                return 1;
            case MEAN:
                return 1;
            case STD_DEV:
                return 1;
            case MIN:
                return 1;
            case MAX:
                return 1;
            case AVERAGE_POSITION:
                if (averagePositions != null) {
                    return averagePositions.length;
                } else {
                    return 0;
                }
            case CENTER_OF_MASS:
                if (centerofMassPositions != null) {
                    return centerofMassPositions.length;
                } else {
                    return 0;
                }
            case BOUNDING_BOX:
                if (boundingBoxPosition != null) {
                    return boundingBoxPosition.length;
                } else {
                    return 0;
                }
            case SPHERICITY:
                return 1;
            case SURFACE_AREA:
                return 1;
            case EIGENVALUES:
                if (eigenValues != null) {
                    return eigenValues.length;
                } else {
                    return 0;
                }
            case ASPECT_RATIO:
                return 1;
            case NUMBER_OF_TOUCHING_NEIGHBORS:
                return 1;
            case AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS:
                return 1;
            case NUMBER_OF_NEIGHBORS_CLOSER_THAN:
                return 1;
            default:
                return 0;
        }
    }

    public double[] getFeatures(Feature measurement, int dimension) {
        doFeatureExtaction();

        switch (measurement) {
            case AREA_VOLUME:
                return volumes;
            case MEAN:
                return averages;
            case STD_DEV:
                return standardDeviations;
            case MIN:
                return minima;
            case MAX:
                return maxima;
            case AVERAGE_POSITION:
                return averagePositions[dimension];
            case CENTER_OF_MASS:
                return centerofMassPositions[dimension];
            case BOUNDING_BOX:
                return boundingBoxPosition[dimension];
            case SPHERICITY:
                return sphericities;
            case SURFACE_AREA:
                return null; // TODO !!! Easy! But: Differentiate between 2D and 3D!
            case EIGENVALUES:
                return eigenValues[dimension];
            case ASPECT_RATIO:
                return aspectRatios;
            case NUMBER_OF_TOUCHING_NEIGHBORS:
                return ArrayUtilities.typeConvertToDouble(numberOfTouchingNeighbors);
            case AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS:
                return averageDistanceOfNClosestNeighbors;
            case NUMBER_OF_NEIGHBORS_CLOSER_THAN:
                return ArrayUtilities.typeConvertToDouble(numberOfNeighborsCloserThanDistanceD);
            default:
                return null;
        }
    }

    public int getNumLabels() {
        doFeatureExtaction();

        return numLabels;
    }

    /**
     * Returns a histogram of all pixels in the image. In fact, the indexes of
     * the histogram are the rounded (rather floored) pixel signal values.
     *
     * @param img ImgLib2 Img to be processed.
     * @param <T> pixel type of the image
     * @return returns an array containing (int)max grey value elements.
     */
    public static <T extends RealType<T>> long[] getLabelsPixelCount(Img<T> img) {

        Cursor<T> cursor = img.cursor();
        int max = 0;
        while (cursor.hasNext()) {
            int val = (int) cursor.next().getRealFloat();
            if (val > max) {
                max = val;
            }
        }

        long[] volumes = new long[max];
        cursor.reset();
        while (cursor.hasNext()) {
            int val = (int) cursor.next().getRealFloat();
            if (val > 0) {
                volumes[val - 1]++;
            }
        }

        return volumes;
    }

    /**
     * give the center of mass of each labeled region the input image
     *
     * @param img a labeled image (e.g. pixel belonging to the same region have
     *            the same integer value)
     * @param <T> pixel type of the image
     * @return an array of the center of mass of each labeled region, the
     * coordinate have 0 value if the region with a particular index is
     * empty
     */
    public static <T extends RealType<T>> float[][] getLabelsCenterOfMass(Img<T> img) {
        Cursor<T> cursor = img.cursor();

        // get the value of the maximum label
        int max = 0;
        while (cursor.hasNext()) {
            int val = (int) cursor.next().getRealFloat();
            if (val > max) {
                max = val;
            }
        }

        // sum the pixels per coordinates per label, also count the number of
        // pixels per label
        int nD = img.numDimensions();
        float[][] posList = new float[nD][];
        for (int i = 0; i < nD; i++) {
            posList[i] = new float[max];
        }
        float[] volumes = new float[max];
        cursor.reset();
        while (cursor.hasNext()) {
            int val = (int) cursor.next().getRealFloat();
            if (val > 0) {
                for (int i = 0; i < nD; i++) {
                    posList[i][val - 1] += cursor.getFloatPosition(i);
                }
                volumes[val - 1] += 1;
            }
        }

        // average the pixel position per label
        for (int i = 0; i < nD; i++) {
            for (int j = 0; j < max; j++) {
                posList[i][j] /= volumes[j];
            }
        }

        return posList;
    }


    /**
     * Return positions of labels in a label image. The background is excluded.
     * Thus, information about label 1 is stored in array entry 0; If givenSlice
     * is larger than -1, the resulting array only contains information from one
     * certain slice. Labels which are not present at that slice, have
     * position[dimension] = -1
     * <p>
     * <p>
     * Assuming a three dimensional image is given, the result should be
     * interpreted like this:
     * <p>
     * <pre>
     * float[][] result ImgLib2Utils.getLabelsPostion(image);
     *
     * //First object in the list:
     * float posX = result[0][0];
     * float posY = result[1][0];
     * float posZ = result[2][0];
     *
     * //Last object in the list:
     * float posX = result[0][result[0].length - 1];
     * float posY = result[1][result[0].length - 1];
     * float posZ = result[2][result[0].length - 1];
     * </pre>
     *
     * @param img        an image where the grey value represents the class to which a
     *                   pixel belongs to
     * @param givenSlice the slice which should be analysed. If -1, the whole stack
     *                   will be analysed.
     * @param <T>        pixel type of image
     * @return array of coordinates.
     */
    public static <T extends RealType<T>> float[][] getLabelsPosition(Img<T> img, int givenSlice) {
        int nD = img.numDimensions();

        Cursor<T> cursor = img.cursor();
        // get the value of the maximum label
        int max = 0;
        while (cursor.hasNext()) {
            int val = (int) cursor.next().getRealFloat();
            if (val > max) {
                max = val;
            }
        }

        if (givenSlice >= 0) {

            long[] startPos = new long[nD];
            long[] endPos = new long[nD];
            for (int i = 0; i < nD; i++) {
                startPos[i] = img.min(i);
                endPos[i] = img.max(i);
            }

            startPos[2] = givenSlice;
            endPos[2] = givenSlice;
            IntervalView<T> intervalView = new IntervalView<T>(img, startPos, endPos);
            cursor = intervalView.cursor();
        }

        // sum the pixels per coordinates per label, also count the number of
        // pixels per label
        float[][] posList = new float[nD][max];

        if (givenSlice >= 0) {
            for (int i = 0; i < nD; i++) {
                for (int j = 0; j < max; j++) {
                    posList[i][j] = -1;
                }
            }
        }

        cursor.reset();
        while (cursor.hasNext()) {
            int val = (int) cursor.next().getRealFloat();
            if (val > 0) {
                for (int i = 0; i < nD; i++) {
                    posList[i][val - 1] = cursor.getFloatPosition(i);
                }
            }
        }

        return posList;
    }

    public static <T extends RealType<T>> long[][] getLabelsBoundingBoxes(Img<T> labelMap) {
        Cursor<T> cursor = labelMap.cursor();
        int max = 0;
        while (cursor.hasNext()) {
            int val = (int) cursor.next().getRealFloat();
            if (val > max) {
                max = val;
            }
        }

        int numDimensions = labelMap.numDimensions();

        long[][] minmaxintervals = new long[max][numDimensions * 2];
        cursor.reset();

        boolean[] intervalInitialized = new boolean[max];

        while (cursor.hasNext()) {
            int idx = (int) cursor.next().getRealFloat() - 1;
            if (idx >= 0) {
                if (!intervalInitialized[idx]) {
                    for (int d = 0; d < numDimensions; d++) {
                        long position = cursor.getLongPosition(d);
                        // min
                        minmaxintervals[idx][d] = position;
                        // max
                        minmaxintervals[idx][d + numDimensions] = position;
                    }
                    intervalInitialized[idx] = true;
                }

                for (int d = 0; d < numDimensions; d++) {
                    long position = cursor.getLongPosition(d);
                    // min
                    if (minmaxintervals[idx][d] > position) {
                        minmaxintervals[idx][d] = position;
                    }
                    // max
                    if (minmaxintervals[idx][d + numDimensions] < position) {
                        minmaxintervals[idx][d + numDimensions] = position;
                    }
                }
            }
        }

        return minmaxintervals;
    }

    public static <T extends RealType<T>> Interval[] getLabelsBoundingIntervals(Img<T> labelMap) {

        long[][] boundingBoxes = LabelAnalyser.getLabelsBoundingBoxes(labelMap);
        Interval[] intervals = new Interval[boundingBoxes.length];

        for (int i = 0; i < intervals.length; i++) {
            intervals[i] = Intervals.createMinMax(boundingBoxes[i]);
        }
        return intervals;
    }

    /**
     * Return a binary image (Img&lt;BitType$gt;) expressing the membership of pixels
     * to a given region.
     *
     * @param region Region to binarize
     * @param img    This image is only used to determine the dimensions of the
     *               binary image to create
     * @param <T>    pixel type of the image
     * @param <F>    pixel type of the image
     * @return return a binary image with the same size as the given img. In
     * this binary image, all pixels beloging to the given region are
     * true, the remaining ones are false.
     */
    public static <T extends BooleanType<T>, F extends RealType<F>> Img<BitType> convertLabelRegionToBinaryImage(LabelRegion<T> region, Img<F> img) {
        final long[] pos = new long[img.numDimensions()];
        final long[] dims = new long[img.numDimensions()];
        img.dimensions(dims);

        final Img<BitType> copy = ArrayImgs.bits(dims);

        RandomAccess<BitType> imageRA = copy.randomAccess();
        LabelRegionCursor regionCursor = region.cursor();
        int count = 0;
        while (regionCursor.hasNext()) {
            regionCursor.next();
            regionCursor.localize(pos);

            imageRA.setPosition(pos);
            BitType imageElement = imageRA.get();

            count++;
            imageElement.set(true);
        }
        return copy;
    }


    /**
     * Return a binary image (Img&lt;BitType$gt;) expressing the membership of pixels
     * to a given region.
     *
     * @param region Region to binarize
     * @param img    This image is only used to determine the dimensions of the
     *               binary image to create
     * @param <T>    pixel type of the image
     * @param <F>    pixel type of the image
     * @return return a binary image with the same size as the given img. In
     * this binary image, all pixels beloging to the given region are
     * true, the remaining ones are false.
     */
    public static <T extends BooleanType<T>, F extends RealType<F>> Img<BitType> convertLabelRegionToBinaryImage(RandomAccessibleInterval<T> region, Img<F> img) {
        final long[] pos = new long[img.numDimensions()];
        final long[] dims = new long[img.numDimensions()];
        img.dimensions(dims);

        final Img<BitType> copy = ArrayImgs.bits(dims);

        RandomAccess<BitType> imageRA = copy.randomAccess();
        Cursor<Void> regionCursor = Regions.iterable(region).cursor();
        int count = 0;
        while (regionCursor.hasNext()) {
            regionCursor.next();
            regionCursor.localize(pos);

            imageRA.setPosition(pos);
            BitType imageElement = imageRA.get();

            count++;
            imageElement.set(true);
        }
        return copy;
    }

    /**
     * Deprecated: use LabelingUtilities instead!
     */
    @Deprecated
    public static <T extends RealType<T>> ImgLabeling<Integer, IntType> getIntIntImgLabellingFromLabelMapImg(Img<T> labelMap) {
        return LabelingUtilities.getIntIntImgLabellingFromLabelMapImg(labelMap);
    }

}