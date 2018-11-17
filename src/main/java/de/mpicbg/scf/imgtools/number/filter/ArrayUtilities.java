package de.mpicbg.scf.imgtools.number.filter;

import java.util.Arrays;
/**
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 *         rhaase@mpi-cbg.de
 * Date: July 2017
 *
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 *                Dresden, Germany
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
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
 */
public class ArrayUtilities {

	/**
	 * Allows to insert an element to an array and move all following elements one step behind. The last element will be lost
	 * 
	 * @param array
	 *            Array to manipulate
	 * @param indexWhereToInsert
	 *            index, where the new element should be inserted
	 * @param elementToInsert
	 *            new element to insert.
	 */
	public static void insertAndShiftBehind(float[] array, int indexWhereToInsert, float elementToInsert) {
		for (int i = array.length - 1; i > indexWhereToInsert; i--) {
			array[i] = array[i - 1];
		}
		array[indexWhereToInsert] = elementToInsert;
	}

	/**
	 * Allows to change the structure of an array like:
	 * 
	 * <pre>
	 * { { 0, 1, 2 }, { 3, 4, 5 }
	 * 
	 * }
	 * </pre>
	 * 
	 * to
	 * 
	 * <pre>
	 * { { 0, 3 }, { 1, 4 }, { 2, 5 } }
	 * </pre>
	 * 
	 * @param array an array of floats
	 * @return return a new reordered array
	 */
	public static float[][] transpose(float[][] array) {
		if (array == null) {
			return null;
		}
		float[][] newPointList = new float[array[0].length][array.length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				newPointList[i][j] = array[j][i];
			}
		}
		return newPointList;
	}


	public static String toString(float[][] arr) {
		if (arr == null) {
			return null;
		}
		String result = "";
		for (float[] anArr : arr) {
			result = result.concat(Arrays.toString(anArr) + "\n");
		}
		return result;
	}

	public static String toString(double[][] arr) {
		if (arr == null) {
			return null;
		}
		String result = "";
		for (double[] anArr : arr) {
			result = result.concat(Arrays.toString(anArr) + "\n");
		}
		return result;
	}

	public static String toString(double[][][] arr) {
		if (arr == null) {
			return null;
		}
		String result = "";
		for (int x = 0; x < arr.length; x++) {
			result = result.concat(toString(arr[x]) + "\n\n");
		}
		return result;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static double[] typeConvertToDouble(float[] arr) {
		if (arr == null) {
			return null;
		}
		double[] intArr = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (double) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static double[] typeConvertToDouble(int[] arr) {
		if (arr == null) {
			return null;
		}
		double[] intArr = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (double) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static double[] typeConvertToDouble(long[] arr) {
		if (arr == null) {
			return null;
		}
		double[] intArr = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (double) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static double[] typeConvertToDouble(Object[] arr) {
		if (arr == null) {
			return null;
		}
		double[] intArr = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = Double.parseDouble(arr[i].toString());
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static float[] typeConvertToFloat(double[] arr) {
		if (arr == null) {
			return null;
		}
		float[] intArr = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (float) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static float[] typeConvertToFloat(int[] arr) {
		if (arr == null) {
			return null;
		}
		float[] intArr = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (float) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static float[] typeConvertToFloat(long[] arr) {
		if (arr == null) {
			return null;
		}
		float[] intArr = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (float) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static int[] typeConvertToInt(double[] arr) {
		if (arr == null) {
			return null;
		}
		int[] intArr = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (int) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static int[] typeConvertToInt(float[] arr) {
		if (arr == null) {
			return null;
		}
		int[] intArr = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (int) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static int[] typeConvertToInt(long[] arr) {
		if (arr == null) {
			return null;
		}
		int[] intArr = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (int) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static long[] typeConvertToLong(double[] arr) {
		if (arr == null) {
			return null;
		}
		long[] intArr = new long[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (long) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static long[] typeConvertToLong(float[] arr) {
		if (arr == null) {
			return null;
		}
		long[] intArr = new long[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (long) arr[i];
		}
		return intArr;
	}

	/**
	 * Type conversion as name and parameters suggests. TODO: In Java 1.8, there is a generic way to do this.
	 * 
	 * @param arr
	 *            Array to convert
	 * @return Same Array with elements in new type
	 */
	public static long[] typeConvertToLong(int[] arr) {
		if (arr == null) {
			return null;
		}
		long[] intArr = new long[arr.length];
		for (int i = 0; i < arr.length; i++) {
			intArr[i] = (long) arr[i];
		}
		return intArr;
	}

	/**
	 * Calculate a unit vector with length=1 from a given vector.
	 * 
	 * @param vector vector to be shortened
	 * @return vector with length = 1
	 */
	public static double[] makeUnitVector(double[] vector)
	{
		if (vector == null)
		{
			return null;
		}
		double sum = 0;
		for (int i = 0; i < vector.length; i++)
		{
			sum += Math.pow(vector[i], 2.0);
		}
		double length = Math.sqrt(sum);
		
		double result[] = new double[vector.length];
		for (int i = 0; i < vector.length; i++)
		{
			result[i] = vector[i]/length;
		}
		return result;
	}

}
