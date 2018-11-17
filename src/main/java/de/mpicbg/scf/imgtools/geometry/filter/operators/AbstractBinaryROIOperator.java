package de.mpicbg.scf.imgtools.geometry.filter.operators;

import de.mpicbg.scf.imgtools.geometry.data.Contains;
import de.mpicbg.scf.imgtools.geometry.data.ContainsRandomAccess;
import net.imglib2.*;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BoolType;

/**
 * This is an abstract class as base for all binary operators which allow combining discree regions of interest
 * like in the theory of sets.
 * Example: intersection of two ROIs.
 * <p>
 * <p>
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
public abstract class AbstractBinaryROIOperator<B1 extends BooleanType<B1>, B2 extends BooleanType<B2>> extends AbstractInterval implements RandomAccessibleInterval<BoolType>, Contains<RealLocalizable> {

    protected RandomAccessibleInterval<B1> leftOperand;
    protected RandomAccessibleInterval<B2> rightOperand;

    public AbstractBinaryROIOperator(RandomAccessibleInterval<B1> leftOperand, RandomAccessibleInterval<B2> rightOperand) {
        super(leftOperand.numDimensions());
        for (int d = 0; d < n; ++d)
            this.max[d] = leftOperand.max(d);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;

    }


    @Override
    public RandomAccess<BoolType> randomAccess() {
        return new ContainsRandomAccess(this);
    }

    @Override
    public RandomAccess<BoolType> randomAccess(Interval interval) {

        return randomAccess();
    }

    @Override
    public abstract boolean contains(RealLocalizable l);

    @Override
    public Contains<RealLocalizable> copyContains() {
        return this;
    }


    @Override
    public long min(final int d) {
        return Math.min(leftOperand.min(d), rightOperand.min(d));
    }

    @Override
    public void min(final long[] min) {
        for (int d = 0; d < n; ++d)
            min[d] = min(d);
    }

    @Override
    public void min(final Positionable min) {
        for (int d = 0; d < n; ++d)
            min.setPosition(min(d), d);
    }

    @Override
    public long max(final int d) {

        return Math.max(leftOperand.max(d), rightOperand.max(d));
    }

    @Override
    public void max(final long[] max) {
        for (int d = 0; d < n; ++d)
            max[d] = max(d);
    }

    @Override
    public void max(final Positionable max) {
        for (int d = 0; d < n; ++d)
            max.setPosition(max(d), d);
    }
}
