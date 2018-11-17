package de.mpicbg.scf.imgtools.geometry.data;

import ij.measure.Calibration;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2017
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
public class Triangle3DTest {

    @Test
    public void testAreaCalculationWorks() {
        double tolerance = 0.0001;

        Triangle3D t3d = new Triangle3D(
                new Point3D(0, 0, 0),
                new Point3D(0, 0, 1),
                new Point3D(0, 1, 0));

        double reference = 0.5;
        double value = t3d.getArea();

        assertTrue("Triangle area test", Math.abs(reference - value) < tolerance);

        Calibration calib = new Calibration();
        calib.pixelDepth = 0.5;
        calib.pixelDepth = 0.25;
        calib.pixelDepth = 0.15;
        value = t3d.getArea(calib);
        reference = 0.07500000000000008;
        assertTrue("Triangle area test", Math.abs(reference - value) < tolerance);
    }
}
