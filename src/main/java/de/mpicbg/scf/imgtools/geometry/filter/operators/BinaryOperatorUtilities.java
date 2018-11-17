package de.mpicbg.scf.imgtools.geometry.filter.operators;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BoolType;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: September 2016
 * <p>
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
public class BinaryOperatorUtilities {

    public static <B1 extends BooleanType<B1>, B2 extends BooleanType<B2>> RandomAccessibleInterval<BoolType>
    intersection(RandomAccessibleInterval<B1> leftOperand, RandomAccessibleInterval<B2> rightOperand) {
        return new BinaryIntersection<B1, B2>(leftOperand, rightOperand);
    }


    public static <B1 extends BooleanType<B1>, B2 extends BooleanType<B2>> RandomAccessibleInterval<BoolType>
    and(RandomAccessibleInterval<B1> leftOperand, RandomAccessibleInterval<B2> rightOperand) {
        return intersection(leftOperand, rightOperand);
    }


    public static <B1 extends BooleanType<B1>, B2 extends BooleanType<B2>> RandomAccessibleInterval<BoolType>
    union(RandomAccessibleInterval<B1> leftOperand, RandomAccessibleInterval<B2> rightOperand) {
        return new BinaryUnion<B1, B2>(leftOperand, rightOperand);
    }


    public static <B1 extends BooleanType<B1>, B2 extends BooleanType<B2>> RandomAccessibleInterval<BoolType>
    or(RandomAccessibleInterval<B1> leftOperand, RandomAccessibleInterval<B2> rightOperand) {
        return union(leftOperand, rightOperand);
    }


    public static <B extends BooleanType<B>> RandomAccessibleInterval<BoolType>
    negation(RandomAccessibleInterval<B> leftOperand) {
        return new BinaryNegation<B>(leftOperand);
    }

    public static <B extends BooleanType<B>> RandomAccessibleInterval<BoolType>
    wrap(RandomAccessibleInterval<B> leftOperand) {
        return new BinaryWrap<B>(leftOperand);
    }

    public static <B1 extends BooleanType<B1>, B2 extends BooleanType<B2>> RandomAccessibleInterval<BoolType>
    subtraction(RandomAccessibleInterval<B1> leftOperand, RandomAccessibleInterval<B2> rightOperand) {
        return intersection(leftOperand, new BinaryNegation<B2>(rightOperand));
    }
}
