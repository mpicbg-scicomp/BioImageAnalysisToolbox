package de.mpicbg.scf.imgtools.number.analyse.image;

import de.mpicbg.scf.imgtools.number.analyse.array.Equal;
import de.mpicbg.scf.imgtools.number.filter.ArrayUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.IJ;
import ij.ImagePlus;
import java.util.Arrays;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
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
public class ConnectedLabelsTest {

    @Test
    public void testLabelNeighborCountingIsCorrect() {

        ImagePlus labelMap = IJ.openImage("src/test/resources/touchinglabels.tif");

        Img<FloatType> labelMapImg = ImagePlusAdapter.convertFloat(labelMap);
        ConnectedLabels<FloatType> lnc = new ConnectedLabels<FloatType>(labelMapImg);
        double[] neighborCounts = ArrayUtilities.typeConvertToDouble(lnc.getNeighbourCounts());

        double[] referenceNeighborCounts = {2.0, 4.0, 4.0, 6.0, 3.0, 4.0, 6.0, 4.0, 4.0, 6.0, 3.0, 4.0, 5.0, 6.0, 4.0, 4.0, 6.0, 4.0, 4.0, 6.0, 4.0, 3.0, 6.0, 5.0, 3.0, 5.0, 5.0, 3, 4, 4, 3, 3, 2, 1, 0, 0};

        DebugHelper.print(this, Arrays.toString(neighborCounts));
        assertTrue("Refrence and test neighborCounts equal", new Equal(neighborCounts, referenceNeighborCounts, 0).evaluate());

        labelMap.close();
    }
}
