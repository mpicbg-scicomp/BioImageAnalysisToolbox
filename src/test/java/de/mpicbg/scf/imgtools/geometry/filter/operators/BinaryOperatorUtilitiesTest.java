package de.mpicbg.scf.imgtools.geometry.filter.operators;

import de.mpicbg.scf.imgtools.image.create.labelmap.LabelingUtilities;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.Regions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: September 2016
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
public class BinaryOperatorUtilitiesTest {
    @Test
    public void testNegation() {
        RandomAccessibleInterval<BitType> operand1 = LabelingUtilities.getBinary1DImage("0001110000");

        RandomAccessibleInterval<BoolType> negation = BinaryOperatorUtilities.negation(operand1);
        String test = LabelingUtilities.getStringFromBinary1DImage(negation);
        assertTrue("negation works !o1", test.equals("1110001111"));
    }


    @Test
    public void testSubtraction() {
        RandomAccessibleInterval<BitType> operand1 = LabelingUtilities.getBinary1DImage("0001111100");
        RandomAccessibleInterval<BitType> operand2 = LabelingUtilities.getBinary1DImage("0000011100");

        RandomAccessibleInterval<BoolType> subtraction = BinaryOperatorUtilities.subtraction(operand1, operand2);
        String test = LabelingUtilities.getStringFromBinary1DImage(subtraction);
        assertTrue("subtraction works o1\\o2", test.equals("0001100000"));
    }

    @Test
    public void testIntersection() {
        RandomAccessibleInterval<BitType> operand1 = LabelingUtilities.getBinary1DImage("0001110000");
        RandomAccessibleInterval<BitType> operand2 = LabelingUtilities.getBinary1DImage("0000111000");
        RandomAccessibleInterval<BitType> operand3 = LabelingUtilities.getBinary1DImage("0000011100");

        RandomAccessibleInterval<BoolType> intersection = BinaryOperatorUtilities.intersection(operand1, operand2);
        String test = LabelingUtilities.getStringFromBinary1DImage(intersection);
        assertTrue("intersection works o1&&o2", test.equals("0000110000"));

        intersection = BinaryOperatorUtilities.intersection(intersection, operand3);
        test = LabelingUtilities.getStringFromBinary1DImage(intersection);
        assertTrue("intersection works (o1&&o2)&&o3", test.equals("0000010000"));
    }

    @Test
    public void testUnion() {
        RandomAccessibleInterval<BitType> operand1 = LabelingUtilities.getBinary1DImage("0001110000");
        RandomAccessibleInterval<BitType> operand2 = LabelingUtilities.getBinary1DImage("0000111000");
        RandomAccessibleInterval<BitType> operand3 = LabelingUtilities.getBinary1DImage("0000011100");

        RandomAccessibleInterval<BoolType> union = BinaryOperatorUtilities.union(operand1, operand2);
        String test = LabelingUtilities.getStringFromBinary1DImage(union);
        assertTrue("union works o1||o2", test.equals("0001111000"));

        union = BinaryOperatorUtilities.union(union, operand3);
        test = LabelingUtilities.getStringFromBinary1DImage(union);
        assertTrue("union works (o1||o2)||o3", test.equals("0001111100"));
    }

    @Test
    public void testIfCursorOnIntersectionWorks() {
        RandomAccessibleInterval<BitType> operand1 = LabelingUtilities.getBinary1DImage("0001110000");
        RandomAccessibleInterval<BitType> operand2 = LabelingUtilities.getBinary1DImage("0000111000");
        Cursor<Void> cursor1 = Regions.iterable(operand1).cursor();
        cursor1.next();
        assertTrue(cursor1.getDoublePosition(0) == 3);
        cursor1.next();
        assertTrue(cursor1.getDoublePosition(0) == 4);
        cursor1.next();
        assertTrue(cursor1.getDoublePosition(0) == 5);
        assertTrue(!cursor1.hasNext());

        RandomAccessibleInterval<BoolType> intersection = BinaryOperatorUtilities.intersection(operand1, operand2);
        Cursor<Void> cursor = Regions.iterable(intersection).cursor();
        cursor.next();
        assertTrue(cursor.getDoublePosition(0) == 4);
        cursor.next();
        assertTrue(cursor.getDoublePosition(0) == 5);
        assertTrue(!cursor.hasNext());
    }


}