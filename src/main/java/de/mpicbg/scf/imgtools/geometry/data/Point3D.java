package de.mpicbg.scf.imgtools.geometry.data;

import ij.measure.Calibration;

/**
 * soon to be deprecated Point_N should be used instead of Point3D
 * <p>
 * This class represents a Point in 3D space with double precision of the coordinates. All coordinates are intended to be processed in pixel-coordinate system, because the object Point3D has no knowledge about a certain calibration.
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: June 2015
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
public class Point3D extends PointN {


    /**
     * Constructor
     *
     * @param x x-coordinate in pixel-unit
     * @param y y-coordinate in pixel-unit
     * @param z z-coordinate in pixel-unit
     */
    public Point3D(double x, double y, double z) {
        super(new double[]{x, y, z});

    }


    /**
     * Return the distance to another point.
     *
     * @param p     the point to which the distance should be caluclate to
     * @param calib Calibration defining the voxel size to calculate actual distance in physical units
     * @return distance in physical-units
     */
    public double getDistanceTo(Point3D p, Calibration calib) {
        return Math.sqrt(
                Math.pow((position[0] - p.position[0]) * calib.pixelWidth, 2) +
                        Math.pow((position[1] - p.position[1]) * calib.pixelHeight, 2) +
                        Math.pow((position[2] - p.position[2]) * calib.pixelDepth, 2)
        );
    }

    /**
     * compare two points
     *
     * @param p the point to compare to 'this'
     * @return true if both are equal localised, false if not.
     */
    public boolean equals(Point3D p) {
        for (int d = 0; d < nDim; d++) {
            if (position[d] != p.position[d]) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return new Double(position[0]).toString() + "\t" + new Double(position[1]).toString() + "\t" + new Double(position[2]).toString();
    }

    public double getX() {
        return position[0];
    }

    public void setX(double x) {
        this.position[0] = x;
    }

    public double getY() {
        return position[1];
    }

    public void setY(double y) {
        this.position[1] = y;
    }

    public double getZ() {
        return position[2];
    }

    public void setZ(double z) {
        this.position[2] = z;
    }

}
