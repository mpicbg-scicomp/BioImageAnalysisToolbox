package de.mpicbg.scf.imgtools.number.analyse.image;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * This class allows calculating moments up to any order of 3D objects stored in a label map. It furthermore contains accessors to Eigenvalues of the objects.
 * <p>
 * <p>
 * Note: An object ranging from x=0 to x=9 has its center between x=4 and x=5. thus, its centerX=4.5, not 5!
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
public class LabelMoments3D<T extends RealType<T>, U extends RealType<U>> {
    private final Img<T> labelMap;
    private Img<U> signalMap;
    /* private double averageSignal = 0; */
    private final long labelId;
    private final Interval boundingBox;
    private final double[] voxelSize;
    private final int maxOrder;

    private double[][][] moments = null;
    private double[][][] translationalInvariantMoments = null;
    private double[] eigenvalues;
    private static boolean dirtyCursorIntervalWorkaround = false;

    public LabelMoments3D(Img<T> labelMap, long labelId, Interval boundingBox, double[] voxelSize, int maxOrder) {
        this.labelMap = labelMap;
        this.labelId = labelId;
        this.boundingBox = boundingBox;
        this.voxelSize = voxelSize;
        this.maxOrder = maxOrder;
    }

    public void setSignalImage(Img<U> signalImage) {
        this.signalMap = signalImage;
        moments = null;
        translationalInvariantMoments = null;
        eigenvalues = null;
    }

    /**
     * Actual moments calculation
     *
     * @return 3D-Array containing (n+1)*(n+1)*(n+1) elements corresponding to the moments of the 3D object. n is the maximum order of moments to be calculated
     * (given in constructor)
     */
    public double[][][] getMoments() {
        if (moments == null) {
            moments = calculateMoments(0, 0, 0);
        }
        return moments;
    }

    /**
     * Actual calculation of the central moments (translation invariant moments)
     *
     * @return 3D-Array containing (n+1)*(n+1)*(n+1) elements corresponding to the moments of the 3D object. n is the maximum order of moments to be calculated
     * (given in constructor)
     */
    public double[][][] getTranslationInvariantMoments() {
        if (translationalInvariantMoments == null) {
            double[][][] moments = getMoments();
            if (moments == null) {
                return null;
            }

            double centerX = moments[1][0][0] / moments[0][0][0];
            double centerY = moments[0][1][0] / moments[0][0][0];
            double centerZ = moments[0][0][1] / moments[0][0][0];
            translationalInvariantMoments = calculateMoments(centerX, centerY, centerZ);
        }
        return translationalInvariantMoments;
    }

    /**
     * Calculate the eigen values
     *
     * @return array with 3 elements representing the eigen values of the object describing its orientation
     */
    public double[] getEigenVector() {
        if (eigenvalues == null) {
            double[][][] tim = getTranslationInvariantMoments();
            if (tim == null || tim[0][0][0] == 0) {
                return null;
            }
            double[][] covXYZ = {{tim[2][0][0] / tim[0][0][0], tim[1][1][0] / tim[0][0][0], tim[1][0][1] / tim[0][0][0]},
                    {tim[1][1][0] / tim[0][0][0], tim[0][2][0] / tim[0][0][0], tim[0][1][1] / tim[0][0][0]},
                    {tim[1][0][1] / tim[0][0][0], tim[0][1][1] / tim[0][0][0], tim[0][0][2] / tim[0][0][0]}};

            Matrix covXYZMatrix = new Matrix(covXYZ);
            EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(covXYZMatrix);
            eigenvalues = eigenvalueDecomposition.getRealEigenvalues();
        }
        return eigenvalues;
    }

    public double getAspectRatio() {
        double[] ev = getEigenVector();

        if (ev == null) {
            return 0;
        }

        double maxEv = ev[0];
        double minEv = ev[0];
        for (int j = 1; j < ev.length; j++) {
            if (minEv > ev[j]) {
                minEv = ev[j];
            }
            if (maxEv < ev[j]) {
                maxEv = ev[j];
            }
        }
        return minEv / maxEv;
    }

    /**
     * Actual calculation of the moments. If centerX=centerY=centerZ=0, then moments are calculated. If the center is given, translation invariant moments are
     * calculated.
     *
     * @param centerX position of the center on the x-axis
     * @param centerY position of the center on the y-axis
     * @param centerZ position of the center on the z-axis
     * @return
     */
    private double[][][] calculateMoments(double centerX, double centerY, double centerZ) {
        double[] center = {centerX, centerY, centerZ};
        double[][][] moments = new double[maxOrder + 1][maxOrder + 1][maxOrder + 1];
        if (boundingBox == null) {
            return null;
        }

        long[] min = new long[labelMap.numDimensions()];
        long[] max = new long[labelMap.numDimensions()];

        boundingBox.min(min);
        boundingBox.max(max);

        // -------------------------------------------------------------------
        // Dirty workaround for bounding boxes with a size of 1 (min == max)
        if (dirtyCursorIntervalWorkaround) {
            for (int d = 0; d < labelMap.numDimensions(); d++) {
                if (min[d] == max[d]) {
                    if (min[d] > labelMap.min(d)) {
                        min[d]--;
                    } else if (max[d] < labelMap.max(d)) {
                        max[d]++;
                    } else {
                        DebugHelper.print(this, "This should not happen. Only if image and interval have a size of one!");
                    }
                }
            }
        }
        // -------------------------------------------------------------------

        Cursor<T> cursor = Views.interval(labelMap, min, max).cursor();
        Cursor<U> signalCursor = null;
        if (signalMap != null) {
            signalCursor = Views.interval(signalMap, boundingBox).cursor();
        }
        int numDimensions = labelMap.numDimensions();

        double[] factor = new double[numDimensions];
        for (int d = 0; d < numDimensions; d++) {
            if (voxelSize != null && voxelSize.length < 2) {
                factor[d] = voxelSize[d];
            } else {
                factor[d] = 1;
            }
        }

        long[] position = new long[labelMap.numDimensions()];

        for (int i = 0; i <= maxOrder; i++) {
            for (int j = 0; j <= maxOrder; j++) {
                for (int k = 0; k <= maxOrder; k++) {
                    int[] order = {i, j, k};

                    cursor.reset();
                    if (signalCursor != null) {
                        signalCursor.reset();
                    }

                    while (cursor.hasNext() && (signalCursor == null || signalCursor.hasNext())) {
                        cursor.next();
                        if (signalCursor != null) {
                            signalCursor.next();
                        }

                        if ((long) cursor.get().getRealFloat() == labelId) {
                            cursor.localize(position);
                            double intermediateResult = 1;
                            if (signalCursor != null) {
                                intermediateResult = signalCursor.get().getRealFloat() /*- averageSignal*/;
                            }

                            for (int d = 0; d < numDimensions; d++) {
                                intermediateResult *= Math.pow(((double) position[d] * factor[d] - center[d]), order[d]);
                            }

                            moments[i][j][k] += intermediateResult;
                        }
                    }
                }
            }
        }
        return moments;
    }

    /**
     * TODO: Remove this, as soon as bugfix in imglib2 is available...
     *
     * @return a boolean
     */
    @Deprecated
    public static boolean isDirtyCursorIntervalWorkaround() {
        return dirtyCursorIntervalWorkaround;
    }

    /**
     * TODO: Remove this, as soon as bugfix in imglib2 is available...
     *
     * @param dirtyCursorIntervalWorkaround true or false
     */
    @Deprecated
    public static void setDirtyCursorIntervalWorkaround(boolean dirtyCursorIntervalWorkaround) {
        LabelMoments3D.dirtyCursorIntervalWorkaround = dirtyCursorIntervalWorkaround;
    }

}
