package de.mpicbg.scf.imgtools.image.filter;

import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.ImageJUtilities;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import java.io.IOException;
import java.util.ArrayList;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * This class is intended to provide functionalities for filtering objects in a
 * label map. The idea is to set constraints which every object in the labelmap
 * has to fulfill so that it may stay in the map. All objects which do not
 * fulfill the criteria, will be set to background ( = 0 ).
 * <p>
 * Example code can be found in the ConstraintLabelMapTest class
 * <p>
 * /**
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: September 2015
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
public class ConstraintLabelmap<I extends RealType<I>, F extends RealType<F>> {
    // mandatory input variables
    private final Img<I> labelMap;
    // optional input variables
    private boolean applyCheckOverLapWithOtherLabelMap = false;
    private Img<I> mustBeWithInLabelMap = null;
    private Img<F> signalImage = null;
    private boolean keepIds = false;

    // output variables
    private int[] dims = null;
    private boolean resultValid = false;
    private Img<I> resultingLabelMap = null;

    private int remainingCount = 0;

    private class Constraint {
        // input variables
        public final LabelAnalyser.Feature feature;
        public final double lowerThreshold;
        public final double upperThreshold;
        public int measurementDimension = 0;

        // output variables
        public long affectedLabelCount = 0;

        // constructors
        public Constraint(LabelAnalyser.Feature measurement, double lowerThreshold, double upperThreshold) {
            this.feature = measurement;
            this.lowerThreshold = lowerThreshold;
            this.upperThreshold = upperThreshold;
        }

        public Constraint(LabelAnalyser.Feature measurement, double lowerThreshold, double upperThreshold, int measurementDimension) {
            this.feature = measurement;
            this.lowerThreshold = lowerThreshold;
            this.upperThreshold = upperThreshold;
            this.measurementDimension = measurementDimension;
        }
    }

    private final ArrayList<Constraint> constraintList = new ArrayList<Constraint>();
    private final double[] voxelSize;

    /**
     * If you use this constructor, you should not call the
     * "ImagePLus getResult()" Method, because it may deliver the resulting
     * labelmap with wrong dimensions.
     *
     * @param labelMap  image with labelling information
     * @param voxelSize array with voxel size information
     */
    public ConstraintLabelmap(final Img<I> labelMap, double[] voxelSize) {
        this.labelMap = labelMap;
        this.voxelSize = voxelSize;
    }

    /**
     * internal handler, which is called as soon as a request to the processed
     * labelmap from outside comes.
     */
    private void apply() {
        if (resultValid) {
            return;
        }

        resultingLabelMap = labelMap.copy();

        if (this.applyCheckOverLapWithOtherLabelMap) {
            resultingLabelMap = ImageFilterUtilities.maskImage(resultingLabelMap, mustBeWithInLabelMap);
        }

        Cursor<I> cursor = resultingLabelMap.cursor();

        long[] histogram = LabelAnalyser.getLabelsPixelCount(resultingLabelMap);
        DebugHelper.print(this, "There were " + histogram.length + " objects before  filtering ()");
        if (histogram.length == 0) {
            return;
        }
        int[] newLabels = new int[histogram.length];

        // counter starts with 1 to skip the background
        int labelCount = 1;
        newLabels[0] = 0;

        // do measurements according to the list of constraints
        LabelAnalyser<I, F> lpa = null;
        if (constraintList.size() > 0) {
            Feature[] measurements = new Feature[constraintList.size()];
            for (int i = 0; i < measurements.length; i++) {
                measurements[i] = constraintList.get(i).feature;
            }

            lpa = new LabelAnalyser<I, F>(labelMap, voxelSize, measurements);
            if (this.signalImage != null) {
                lpa.setSignalImage(signalImage);
            }
        }

        // Go through all entries in the list of constraints and decide if the object can stay.
        for (int i = 0; i < histogram.length; i++) {
            boolean keepObject = true;

            if (lpa != null) {
                for (int c = 0; c < this.constraintList.size(); c++) {
                    Constraint constraint = constraintList.get(c);
                    double value = lpa.getFeatures(constraint.feature, constraint.measurementDimension)[i];

                    if (value < constraint.lowerThreshold || value > constraint.upperThreshold) {
                        constraint.affectedLabelCount++;
                        keepObject = false;
                    }
                }
            }

            if (keepObject) {
                newLabels[i] = labelCount;
                labelCount++;
            } else {
                newLabels[i] = 0;
            }
        }

        // actually change the (copy of the original) label map
        while (cursor.hasNext()) {
            int val = (int) cursor.next().getRealFloat();
            if (val > 0 && val - 1 < newLabels.length) {
                if (keepIds) {
                    if (newLabels[val - 1] == 0) {
                        cursor.get().setReal(0);
                    }
                } else {
                    cursor.get().setReal(newLabels[val - 1]);
                }
            }
        }

        resultValid = true;
        remainingCount = labelCount - 1;
        DebugHelper.print(this, "There were " + (remainingCount) + " objects after filtering");
    }


    /**
     * Set a (binary) mask, only pixels != 0 will be kept in the processed label map
     *
     * @param otherLabelMap other label map
     */
    public void setMustOverLapWithOtherLabelMap(ImagePlus otherLabelMap) {
        applyCheckOverLapWithOtherLabelMap = true;
        mustBeWithInLabelMap = ImageJFunctions.wrapReal(otherLabelMap);
        resultValid = false;
    }

    /**
     * Set an interval in wich the labels must be. If a pixel of a label is outside that interval, the whole label will be removed from the labelmap
     *
     * @param interval an interval
     */
    public void addConstraintToRemoveLabelsOutsideInterval(Interval interval) {
        for (int d = 0; d < labelMap.numDimensions(); d++) {
            addConstraint(Feature.BOUNDING_BOX, interval.min(d) + 1, Double.MAX_VALUE, d);
            addConstraint(Feature.BOUNDING_BOX, -Double.MAX_VALUE, labelMap.max(d) - 1, d + interval.numDimensions());
        }
        resultValid = false;
    }

    /**
     * add a constraint for filtering the label map.
     *
     * @param measurement    Which parameter/feature should be measured?
     * @param lowerThreshold What is the minimum value that is allowed to keep the label?
     * @param upperThreshold What is the maximum value that is allowed to keep the label?
     */
    public void addConstraint(LabelAnalyser.Feature measurement, double lowerThreshold, double upperThreshold) {
        Constraint c = new Constraint(measurement, lowerThreshold, upperThreshold);
        constraintList.add(c);
        resultValid = false;
    }

    /**
     * add a constraint for filtering the label map. This function allows constrainting a dimension of a parameter. E.g. if you would like to constraint the map by the average-y coordinate of the labels, hand over 1 as measuremntDimension
     *
     * @param measurement          Which parameter/feature should be measured?
     * @param lowerThreshold       What is the minimum value that is allowed to keep the label?
     * @param upperThreshold       What is the maximum value that is allowed to keep the label?
     * @param measurementDimension in which dimension should be measured
     */
    public void addConstraint(LabelAnalyser.Feature measurement, double lowerThreshold, double upperThreshold, int measurementDimension) {
        DebugHelper.print(this, "" + lowerThreshold + " <= " + measurement.toString() + "(" + measurementDimension + ") <= " + upperThreshold);
        Constraint c = new Constraint(measurement, lowerThreshold, upperThreshold, measurementDimension);
        constraintList.add(c);
        resultValid = false;
    }

    /**
     * @param signalImage image with grey values
     */
    public void setSignalImage(Img<F> signalImage) {
        this.signalImage = signalImage;
        resultValid = false;
    }

    /**
     * If this parameter is set to true, constrainting means setting deleted labels to zero and keeping the IDs of all other labels. Example:
     * <p>
     * Assume a label map contains the labels 1,2,3,4,5. Then labels 3 and 4 are then removed, because they do not fulfil a constraint. Afterwards, if keepIds == false, the result will be a labelmap with the labels 1,2,3. If keepIds = true, the result will be 1,2,5.
     * <p>
     * Assuming label 4 and 5 are kicked out
     *
     * @param b default: false
     */
    public void setKeepIDs(boolean b) {
        this.keepIds = b;
        resultValid = false;
    }

    /**
     * Deliver resulting label map and Img of given type
     *
     * @return filtered / constrained label map
     */
    public Img<I> getResult() {

        apply();

        return resultingLabelMap;
    }

    /**
     * Return the number of labels which were removed because a certain constraint was not fulfilled
     *
     * @param feature the feature (should have been given in the constructor)
     * @return number of affected labels
     */
    public long getAffectedObjectsCount(LabelAnalyser.Feature feature) {
        for (int i = 0; i < constraintList.size(); i++) {
            if (constraintList.get(i).feature == feature) {
                return constraintList.get(i).affectedLabelCount;
            }
        }
        return (long) Double.NaN;
    }

    /**
     * return the number of remaining labels after constrainting
     *
     * @return number of found labels
     */
    public int getRemainingLabelsCount() {
        this.apply();
        return remainingCount;
    }

    /**
     * For testing and development
     *
     * @param args argguments
     * @throws IOException throws exception
     */
    public static void main(final String... args) throws IOException {
        //
        new ij.ImageJ();
        IJ.open("/Users/rhaase/code/common-biis-packages_master/src/test/resources/labelmap32bit.tif");

        ImagePlus imp = IJ.getImage();

        int[] dimensions = imp.getDimensions();

        Calibration calib = imp.getCalibration();
        double[] voxelSize = new double[]{calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        Img<FloatType> wrappedImp = ImageJFunctions.wrapReal(imp);

        ConstraintLabelmap<FloatType, FloatType> lmf = new ConstraintLabelmap<FloatType, FloatType>(wrappedImp, voxelSize);
        lmf.addConstraint(Feature.AREA_VOLUME, 2000, 8000);
        ImageJUtilities.showLabelMapProperly(lmf.getResult(), "LabelMap", dimensions, calib);

    }
}
