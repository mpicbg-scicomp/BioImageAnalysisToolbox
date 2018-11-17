package de.mpicbg.scf.imgtools.geometry.filter.operators;

import de.mpicbg.scf.imgtools.geometry.data.Contains;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.type.BooleanType;

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
public class BinaryUnion<B1 extends BooleanType<B1>, B2 extends BooleanType<B2>> extends AbstractBinaryROIOperator<B1, B2> implements Contains<RealLocalizable> {
    public BinaryUnion(RandomAccessibleInterval leftOperand,
                       RandomAccessibleInterval rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public boolean contains(RealLocalizable l) {
        RandomAccess<B1> leftRa = leftOperand.randomAccess();
        RandomAccess<B2> rightRa = rightOperand.randomAccess();
        long[] pos = new long[l.numDimensions()];
        for (int d = 0; d < l.numDimensions(); d++) {
            pos[d] = (long) l.getDoublePosition(d);
        }
        leftRa.setPosition(pos);
        rightRa.setPosition(pos);

        return leftRa.get().get() || rightRa.get().get();
    }
}
