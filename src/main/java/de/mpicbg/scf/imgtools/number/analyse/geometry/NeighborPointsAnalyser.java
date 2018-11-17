package de.mpicbg.scf.imgtools.number.analyse.geometry;

import de.mpicbg.scf.imgtools.geometry.data.PointN;
import de.mpicbg.scf.imgtools.number.filter.ArrayUtilities;

/**
 * The pointlist analyser is thought to deliver statistical information from point lists. Theoretically, Pearson coefficients are thinkable,
 * but for the moment, just the distance between the points is analysed. Add whatever tools come to your mind.
 * <p>
 * <p>
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
public class NeighborPointsAnalyser {

    private PointN[] pointlist = null;
    private float[][] distanceMatrix = null;

    /**
     * Constructor using an array in the form
     * <pre>
     * {
     * {point1_x, point1_y, point1_z, ...},
     * {point2_x, point2_y, point2_z, ...},
     * ...
     * }
     * </pre>
     *
     * @param pointlist list of points
     */
    public NeighborPointsAnalyser(float[][] pointlist) {
        initialize(pointlist, true);
    }

    /**
     * Constructor. If pointlistFirstDimensionsLast == true, then the constructor expects an array in the form
     * <pre>
     * {
     * {point1_x, point1_y, point1_z, ...},
     * {point2_x, point2_y, point2_z, ...},
     * ...
     * }
     * </pre>
     * <p>
     * If pointlistFirstDimensionsLast == false, the form of the array should be:
     * <p>
     * <pre>
     * {
     * {point1_x, point2_x, point3_x, ...}
     * {point1_y, point2_y, point3_y, ...}
     * {point1_z, point2_z, point3_z, ...}
     * ...
     * }
     * </pre>
     *
     * @param pointlist                    list of points
     * @param pointlistFirstDimensionsLast true if point coordinates should be reordered
     */
    public NeighborPointsAnalyser(float[][] pointlist, boolean pointlistFirstDimensionsLast) {
        initialize(pointlist, pointlistFirstDimensionsLast);
    }

    /**
     * Internal initialisation; handling point arrays
     *
     * @param pointlist                    list of points
     * @param pointlistFirstDimensionsLast true if point coordinates should be reordered
     */
    private void initialize(float[][] pointlist, boolean pointlistFirstDimensionsLast) {
        if (pointlist.length == 0) {
            //Init failed: got an empty list :(
            return;
        }

        int numPoints;
        int dimensions;
        if (pointlistFirstDimensionsLast) {
            numPoints = pointlist.length;
            dimensions = pointlist[0].length;
        } else {
            numPoints = pointlist[0].length;
            dimensions = pointlist.length;
        }


        this.pointlist = new PointN[numPoints];
        for (int i = 0; i < numPoints; i++) {
            if (pointlistFirstDimensionsLast) {
                this.pointlist[i] = new PointN(ArrayUtilities.typeConvertToDouble(pointlist[i]));
            } else {
                double[] temp = new double[dimensions];
                for (int j = 0; j < dimensions; j++) {
                    temp[j] = pointlist[j][i];
                }
                this.pointlist[i] = new PointN(temp);
            }
        }
    }

    /**
     * Internally built up a n*n matrix storing the distance between all of the points to each other. Of course, the diagonal in this matrix will be equal to zero.
     */
    private void buildDistanceMatrix() {
        if (distanceMatrix == null) {
            distanceMatrix = new float[pointlist.length][pointlist.length];
            for (int i = 0; i < pointlist.length; i++) {
                for (int j = i; j < pointlist.length; j++) {
                    if (i == j) {
                        distanceMatrix[i][j] = 0;
                    } else {
                        distanceMatrix[i][j] = (float) pointlist[i].getDistanceTo(pointlist[j]);

                        distanceMatrix[j][i] = distanceMatrix[i][j];
                    }
                }
            }
        }
    }

    /**
     * calculate the average distance between each point and his n closest neighbors.
     *
     * @param n number of closest neighbors to be taken into account
     * @return average distance to these neighbors
     */
    public float[] getAverageDistanceOfNClosestPoints(int n) {
        buildDistanceMatrix();

        float[] result = new float[pointlist.length];
        float[] closestDistances = new float[n];


        for (int i = 0; i < pointlist.length; i++) {
            for (int k = 0; k < n; k++) {
                closestDistances[k] = Float.MAX_VALUE;
            }
            for (int j = 0; j < pointlist.length; j++) {
                if (i != j) {
                    for (int k = 0; k < n; k++) {
                        if (closestDistances[k] > distanceMatrix[i][j]) {
                            ArrayUtilities.insertAndShiftBehind(closestDistances, k, distanceMatrix[i][j]);
                            break;
                        }
                    }
                }
            }
            float sum = 0;
            for (int k = 0; k < n; k++) {
                sum += closestDistances[k];
            }
            result[i] = sum / n;
        }
        return result;
    }

    /**
     * Returns an array which contains the number of points which are nearer than maximumDistance for every point given in the constructor.
     *
     * @param maximumDistance maximum distance
     * @return a long array containing point counts
     */
    public long[] getNumberOfPointsNearerAs(double maximumDistance) {
        buildDistanceMatrix();

        long[] result = new long[pointlist.length];

        for (int i = 0; i < pointlist.length; i++) {
            result[i] = 0;
            for (int j = 0; j < pointlist.length; j++) {
                if (i != j) {
                    if (distanceMatrix[i][j] <= maximumDistance) {
                        result[i]++;
                    }
                }
            }
        }
        return result;
    }
}
