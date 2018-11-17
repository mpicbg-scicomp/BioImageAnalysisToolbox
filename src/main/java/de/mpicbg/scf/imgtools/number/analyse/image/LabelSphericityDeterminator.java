package de.mpicbg.scf.imgtools.number.analyse.image;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * This class allows to analyse 3D-objects represented in a label map. The surface
 * area and the volume of these objects are determined as well as the number of surface voxels.
 * <p>
 * From these parameters, several definitions of sphericity are feasible to implement. However,
 * one standard way is implemented in the getSphericity() function.
 * <p>
 * Alternatively, pow(getSurfaceArea())/getVolume() may be calculated as approximation.
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: November 2015
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
public class LabelSphericityDeterminator<T extends RealType<T>> {
    private final Img<T> labelMap;
    private final long labelId;
    private final Interval boundingInterval;
    private final double[] voxelSize;
    private boolean validAnalysisResults = false;

    private long[] numberOfSurfaceVoxels = null;
    private long numberOfVolumeVoxels = 0;
    private double surfaceArea;
    private double volume;
    private double sphericity;

    public LabelSphericityDeterminator(Img<T> labelMap, long labelId, Interval boundingInterval, double[] voxelSize) {
        this.labelMap = labelMap;
        this.labelId = labelId;
        this.boundingInterval = boundingInterval;
        this.voxelSize = voxelSize;
    }

    /**
     * internal function is called, whenever the accessor wants to know anything from this object.
     */
    private void analyse() {
        if (validAnalysisResults) {
            return;
        }
        validAnalysisResults = true;

        int numDimensions = labelMap.numDimensions();
        long[] minPosition = new long[numDimensions];
        labelMap.min(minPosition);
        long[] maxPosition = new long[numDimensions];
        labelMap.max(maxPosition);
        long[] position = new long[numDimensions];

        numberOfSurfaceVoxels = new long[numDimensions];

        RandomAccess<T> ra = labelMap.randomAccess();
        Cursor<T> cursor = Views.interval(labelMap, boundingInterval).cursor();

        cursor.reset();

        while (cursor.hasNext()) {
            cursor.next();
            if ((long) cursor.get().getRealFloat() == labelId) {
                numberOfVolumeVoxels++;

                cursor.localize(position);
                for (int d = 0; d < numDimensions; d++) {
                    position[d]++;
                    if (position[d] > maxPosition[d]) {
                        numberOfSurfaceVoxels[d]++;
                    } else {
                        ra.setPosition(position);
                        if ((long) ra.get().getRealFloat() != labelId) {
                            numberOfSurfaceVoxels[d]++;
                        }
                    }
                    position[d] -= 2;
                    if (position[d] < minPosition[d]) {
                        numberOfSurfaceVoxels[d]++;
                    } else {
                        ra.setPosition(position);
                        if ((long) ra.get().getRealFloat() != labelId) {
                            numberOfSurfaceVoxels[d]++;
                        }
                    }
                    position[d]++;
                }
            }
        }

        surfaceArea = 0;

        double voxelVolume = 1;
        for (int e = 0; e < numDimensions; e++) {

            double area = 1;
            for (int d = 0; d < numDimensions; d++) {
                if (d != e) {
                    area = area * voxelSize[d];
                }
            }

            voxelVolume = voxelVolume * voxelSize[e];
            surfaceArea += area * numberOfSurfaceVoxels[e];
        }
        volume = voxelVolume * numberOfVolumeVoxels;
        sphericity = Math.cbrt(Math.PI * 36 * Math.pow(volume, 2.0)) / surfaceArea;
    }

    /**
     * Returns an array of surface voxel counts corresponding to the elements in the supplied RoiListModel
     *
     * @return array with elements representing the counts for all objects in the RoiListModel
     */
    public long[] getNumberOfSurfaceVoxels() {
        analyse();

        return numberOfSurfaceVoxels;
    }

    /**
     * Returns an array of voxel counts corresponding to the elements in the supplied RoiListModel
     *
     * @return array with elements representing the counts for all objects in the RoiListModel
     */
    public long getNumberOfVolumeVoxels() {
        analyse();

        return numberOfVolumeVoxels;
    }

    /**
     * Returns an array of surface measurements corresponding to the elements in the supplied RoiListModel
     *
     * @return array with elements representing the surface area. This value may be useless, if the calibration is not given.
     */
    public double getSurfaceArea() {
        analyse();

        return surfaceArea;
    }

    /**
     * Returns an array of volume measurements corresponding to the elements in the supplied RoiListModel
     *
     * @return array with elements of volume in physical units. If no calibration was given, the volumes equal the number of positive voxels.
     */
    public double getVolume() {
        analyse();

        return volume;
    }

    /**
     * Returns an array of sphericity measurements corresponding to the elements in the supplied RoiListModel
     *
     * @return array with elements between 0 and 1
     */
    public double getSphericity() {
        analyse();

        return sphericity;
    }
}
