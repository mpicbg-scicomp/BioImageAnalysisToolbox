package de.mpicbg.scf.imgtools.image.projection;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
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
public class ZMapProjection {
    ImagePlus volume;
    ImagePlus zmap;

    ImagePlus projection;

    public ZMapProjection(ImagePlus volume, ImagePlus zmap) {
        this.volume = volume;
        this.zmap = zmap;
    }

    private void apply() {
        projection = NewImage.createImage("zmap projection", zmap.getWidth(), zmap.getHeight(), 1, volume.getBitDepth(), NewImage.FILL_BLACK);
        ImageProcessor projectionIp = projection.getProcessor();

        ImageProcessor zmapIp = zmap.getProcessor();


        for (int x = 0; x < zmap.getWidth(); x++) {
            for (int y = 0; y < zmap.getHeight(); y++) {
                float z = (int) zmapIp.getPixelValue(x, y);

                if (z > 0) {
                    volume.setZ((int) z);
                    ImageProcessor ip = volume.getProcessor();

                    float value = ip.getPixelValue(x, y);

                    projectionIp.setf(x, y, value);
                } else {
                    DebugHelper.print(this, "NaN!");
                }
            }
        }


    }

    public ImagePlus getProjection() {
        if (projection == null) {
            apply();
        }
        return projection;
    }
}
