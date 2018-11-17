package de.mpicbg.scf.imgtools.number.analyse.image;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * This class builds up a matrix with n times n entries, where n represents the number of labels in a given labelmap. Afterwards, the number of touching pixels
 * are counted. At the end it is possible to count the number of non-zero elements in a row or column to determine the number of neighboring labels.
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
 *
 * @param <T> Type of the label map
 */
public class ConnectedLabels<T extends RealType<T>> {
    private final Img<T> labelMap;

    int[][] neighborMatrix = null;
    int maximumCount = -1;

    public ConnectedLabels(Img<T> labelMap) {
        this.labelMap = labelMap;
    }

    private synchronized void buildNeighborMatrix() {
        if (neighborMatrix != null) {
            return;
        }

        T ftMin = labelMap.cursor().next().copy();
        T ftMax = labelMap.cursor().next().copy();
        ComputeMinMax.computeMinMax(labelMap, ftMin, ftMax);

        // Theoretically it is possible to half the memory consumption, because the matrix will be symmetric. If label A has label B as neighbor, label B also
        // has label A as neighbor.
        neighborMatrix = new int[(int) ftMax.getRealFloat()][(int) ftMax.getRealFloat()];

        Cursor<T> cursor = labelMap.cursor();
        RandomAccess<T> ra = labelMap.randomAccess();

        int numDimensions = labelMap.numDimensions();

        long[] position = new long[numDimensions];

        while (cursor.hasNext()) {
            cursor.next();

            cursor.localize(position);
            int matrixX = (int) cursor.get().getRealFloat() - 1;
            if (matrixX >= 0) {
                for (int d = 0; d < numDimensions; d++) {
                    position[d]++;
                    if (position[d] <= labelMap.max(d)) {
                        ra.setPosition(position);
                        int matrixY = (int) ra.get().getRealFloat() - 1;

                        if (matrixY >= 0 && matrixX != matrixY) {
                            neighborMatrix[matrixX][matrixY]++;
                            neighborMatrix[matrixY][matrixX]++;
                        }
                    }
                    position[d]--;
                }
            }
        }
    }

    public int[] getNeighbourCounts() {
        buildNeighborMatrix();

        int[] result = new int[neighborMatrix.length];

        maximumCount = 0;
        for (int i = 0; i < neighborMatrix.length; i++) {
            result[i] = 0;
            for (int j = 0; j < neighborMatrix.length; j++) {
                if (i != j) {
                    if (neighborMatrix[i][j] > 0) {
                        result[i]++;
                        if (result[i] > maximumCount) {
                            maximumCount = result[i];
                        }
                    }
                }
            }
        }
        return result;
    }

    public int getMaximumCount() {
        buildNeighborMatrix();
        return maximumCount;
    }
}
