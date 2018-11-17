package de.mpicbg.scf.imgtools.number.analyse.image;

import de.mpicbg.scf.imgtools.geometry.filter.operators.BinaryOperatorUtilities;
import de.mpicbg.scf.imgtools.image.create.labelmap.LabelingUtilities;
import java.util.ArrayList;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
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
public class LabelOverlapCalculatorTest {
    @Test
    public void simpleTest() {
        RandomAccessibleInterval<BitType> example1 = LabelingUtilities.getBinary1DImage("0001111100");
        RandomAccessibleInterval<BitType> example2 = LabelingUtilities.getBinary1DImage("0000011100");

        ArrayList<RandomAccessibleInterval<BitType>> list1 = new ArrayList<RandomAccessibleInterval<BitType>>();
        ArrayList<RandomAccessibleInterval<BoolType>> list2 = new ArrayList<RandomAccessibleInterval<BoolType>>();

        list1.add(example1);
        list2.add(BinaryOperatorUtilities.wrap(example2));

        LabelOverlapCalculator<BitType, BoolType> loc = new LabelOverlapCalculator<BitType, BoolType>(list1, list2);
        assertEquals(loc.getOverlapOf(0, 0), 0.6, 0.01);
        assertEquals(loc.getMaximumOverlapIndexOfColumn(0), 0);
    }
}