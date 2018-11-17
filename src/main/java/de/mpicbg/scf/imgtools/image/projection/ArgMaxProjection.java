package de.mpicbg.scf.imgtools.image.projection;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;

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
public class ArgMaxProjection {

    private ImagePlus imp;
    private ImagePlus maxImp;
    private ImagePlus argMaxImp;

    public ArgMaxProjection(ImagePlus imp) {
        this.imp = imp;
    }

    private void apply() {
        if (imp == null) {
            imp = IJ.getImage();
            if (imp == null) {
                return;
            }
        }
        maxImp = NewImage.createImage("max projection", imp.getWidth(), imp.getHeight(), 1, imp.getBitDepth(), NewImage.FILL_BLACK);
        ImageProcessor maxIp = maxImp.getProcessor();
        argMaxImp = NewImage.createShortImage("ArgMax projection", imp.getWidth(), imp.getHeight(), 1, NewImage.FILL_BLACK);
        ImageProcessor argMaxIp = argMaxImp.getProcessor();

        for (int z = 1; z <= imp.getNSlices(); z++) {
            imp.setZ(z);
            ImageProcessor ip = imp.getProcessor();

            for (int x = 0; x < imp.getWidth(); x++) {
                for (int y = 0; y < imp.getHeight(); y++) {
                    float value = ip.getPixelValue(x, y);

                    if (value > maxIp.getPixelValue(x, y) || z == 1) {
                        maxIp.setf(x, y, value);
                        argMaxIp.set(x, y, z);
                    }
                }
            }
        }

        argMaxImp.show();
        maxImp.show();
    }

    public void setImagePlus(ImagePlus imp) {
        this.imp = imp;
        argMaxImp = null;
        maxImp = null;
    }

    public ImagePlus getArgMaxImp() {
        if (argMaxImp == null) {
            apply();
        }
        return argMaxImp;
    }


    public ImagePlus getMaxImp() {
        if (maxImp == null) {
            apply();
        }
        return maxImp;
    }
}
