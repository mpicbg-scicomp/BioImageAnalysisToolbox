package de.mpicbg.scf.imgtools.number.analyse.image;

import de.mpicbg.scf.imgtools.geometry.filter.operators.BinaryOperatorUtilities;
import de.mpicbg.scf.imgtools.image.create.labelmap.LabelingUtilities;
import java.util.List;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: November 2016
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
public class LabelOverlapCalculator<B1 extends BooleanType<B1>, B2 extends BooleanType<B2>> {

    double[][] overlapMatrix;

    public LabelOverlapCalculator(List<RandomAccessibleInterval<B1>> listB1, List<RandomAccessibleInterval<B2>> listB2) {
        overlapMatrix = new double[listB1.size()][listB2.size()];
        int x = 0;
        for (RandomAccessibleInterval<B1> b1 : listB1) {
            int y = 0;
            for (RandomAccessibleInterval<B2> b2 : listB2) {
                overlapMatrix[x][y] = (double) LabelingUtilities.count(BinaryOperatorUtilities.intersection(b1, b2)) /
                        (double) LabelingUtilities.count(BinaryOperatorUtilities.union(b1, b2));
                y++;
            }
            x++;
        }
    }

    public double getOverlapOf(int index1, int index2) {
        return overlapMatrix[index1][index2];
    }

    public double getMaximumOverlapOfRow(int row) {
        double maximum = 0;
        for (int x = 0; x < overlapMatrix.length; x++) {
            if (maximum < overlapMatrix[x][row]) {
                maximum = overlapMatrix[x][row];
            }
        }
        return maximum;
    }

    public double getMaximumOverlapOfColumn(int column) {
        double maximum = 0;
        for (int y = 0; y < overlapMatrix[column].length; y++) {
            if (maximum < overlapMatrix[column][y]) {
                maximum = overlapMatrix[column][y];
            }
        }
        return maximum;
    }

    public int getMaximumOverlapIndexOfRow(int row) {
        double maximum = 0;
        int index = -1;
        for (int x = 0; x < overlapMatrix.length; x++) {
            if (maximum < overlapMatrix[x][row]) {
                maximum = overlapMatrix[x][row];
                index = x;
            }
        }
        return index;
    }

    public int getMaximumOverlapIndexOfColumn(int column) {
        double maximum = 0;
        int index = -1;
        for (int y = 0; y < overlapMatrix[column].length; y++) {
            if (maximum < overlapMatrix[column][y]) {
                maximum = overlapMatrix[column][y];
                index = y;
            }
        }
        return index;
    }
}
