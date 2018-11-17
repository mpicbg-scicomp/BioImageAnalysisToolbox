package de.mpicbg.scf.imgtools.image.create.image;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.real.AbstractRealType;

/**
 * This class implements functions to transfrom 1-, 2-, 3- and 4-dimensional double-arrays in an ImgLib2 Img image. To actually transform the images, you need
 * to hand over an object of type T to the class, so that it can create images of that type. Afterwards, the array-data is copied to the image.
 * <p>
 * For example code, visit the corresponding test class
 * <p>
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: December 2015
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
 *
 * @param <T> Image Pixel type, e.g. FloatType, IntType, UnsignedShortType,...
 */
public class ArrayToImageConverter<T extends AbstractRealType<T> & NativeType<T>> {

    private final T t;

    public ArrayToImageConverter(T t) {
        this.t = t;
    }

    /**
     * Transform array to image
     *
     * @param array 4D-data to transform
     * @return Img image
     */
    public Img<T> getImage(double[][][][] array) {
        if (array.length == 0 || array[0].length == 0 || array[0][0].length == 0 || array[0][0][0].length == 0) {
            return null;
        }

        long[] dim = {array.length, array[0].length, array[0][0].length, array[0][0][0].length};
        Img<T> img = new ArrayImgFactory<T>().create(dim, t);

        int[] position = new int[dim.length];

        Cursor<T> cursor = img.cursor();
        while (cursor.hasNext()) {
            cursor.next();
            cursor.localize(position);

            double value = array[position[0]][position[1]][position[2]][position[3]];

            cursor.get().setReal(value);
        }

        return img;
    }

    /**
     * Transform arrat to image
     *
     * @param array 3D-data to transform
     * @return Img image
     */
    public Img<T> getImage(double[][][] array) {
        if (array.length == 0 || array[0].length == 0 || array[0][0].length == 0) {
            return null;
        }

        long[] dim = {array.length, array[0].length, array[0][0].length};
        Img<T> img = new ArrayImgFactory<T>().create(dim, t);

        int[] position = new int[dim.length];

        Cursor<T> cursor = img.cursor();
        while (cursor.hasNext()) {
            cursor.next();
            cursor.localize(position);

            double value = array[position[0]][position[1]][position[2]];

            cursor.get().setReal(value);
        }

        return img;
    }

    /**
     * Transform arrat to image
     *
     * @param array 2D-data to transform
     * @return Img image
     */
    public Img<T> getImage(double[][] array) {
        if (array.length == 0 || array[0].length == 0) {
            return null;
        }

        long[] dim = {array.length, array[0].length};
        Img<T> img = new ArrayImgFactory<T>().create(dim, t);

        int[] position = new int[dim.length];

        Cursor<T> cursor = img.cursor();
        while (cursor.hasNext()) {
            cursor.next();
            cursor.localize(position);

            double value = array[position[0]][position[1]];

            cursor.get().setReal(value);
        }

        return img;
    }

    /**
     * Transform arrat to image
     *
     * @param array 1D-data to transform
     * @return Img image
     */
    public Img<T> getImage(double[] array) {
        if (array.length == 0) {
            return null;
        }

        long[] dim = {array.length};
        Img<T> img = new ArrayImgFactory<T>().create(dim, t);

        int[] position = new int[dim.length];

        Cursor<T> cursor = img.cursor();
        while (cursor.hasNext()) {
            cursor.next();
            cursor.localize(position);

            double value = array[position[0]];

            cursor.get().setReal(value);
        }

        return img;
    }

}
