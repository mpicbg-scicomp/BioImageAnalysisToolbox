package de.mpicbg.scf.imgtools.image.filter;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RealRandomAccess;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class Interpolation {

	public static enum Interpolator {
		NearestNeighbor, Linear, Lanczos
	}

	/**
	 * this function down-sample the input image and return an image which size
	 * is input.dimension(i)*ratio[i]) Prior to down-sampling the input is
	 * blurred with a gaussian with standard deviation DownSampleFactor to avoid
	 * aliasing
	 * 
	 * @param input
	 *            an image to down-sample
	 * @param DownSampleFactor
	 *            the down-sampling factor for each dimension (value should be
	 *            superior to 1 for down-sampling)
	 * @param <T> type of the image
	 * @return a down-sampled version of input
	 */
	public static <T extends RealType<T> & NativeType<T>> Img<T> downsample(Img<T> input, double[] DownSampleFactor) {
	
		int nDim = input.numDimensions();
		double[] sig = new double[nDim];
		for (int i = 0; i < nDim; i++) {
			sig[i] = DownSampleFactor[i];
		}
	
		long[] input_range = new long[2 * nDim];
		for (int i = 0; i < nDim; i++) {
			input_range[i] = 0;
			input_range[i + nDim] = input.dimension(i);
		}
		Interval interval = Intervals.createMinSize(input_range);
		try {
			Gauss3.gauss(sig, Views.extendMirrorDouble(input), Views.interval(input, interval));
		} catch (IncompatibleTypeException e) {
			e.printStackTrace();
		}
	
		return decimate(input, DownSampleFactor);
	}

	/**
	 * this function down-sample the input image and return an image which size
	 * is input.dimension(i)*ratio[i])
	 * 
	 * @param input
	 *            an image to down-sample
	 * @param DownSampleFactor
	 *            the down-sampling factor for each dimension (value should be
	 *            superior to 1 for down-sampling)
	 * @param <T> type of the image
	 * @return a down-sampled version of input
	 */
	public static <T extends RealType<T> & NumericType<T> & NativeType<T>> Img<T> decimate(Img<T> input, double[] DownSampleFactor) {
		// create a new image
		int nDim = input.numDimensions();
		long[] out_dims = new long[nDim];
		for (int i = 0; i < nDim; i++) {
			out_dims[i] = (int) (input.dimension(i) / DownSampleFactor[i]);
		}
		Img<T> output = input.factory().create(out_dims, input.firstElement().createVariable());
	
		// iterate through the image
		RandomAccess<T> input_RA = input.randomAccess();// Views.extendBorder(
														// input
														// ).randomAccess();
		Cursor<T> out_cursor = output.localizingCursor();
		int[] out_pos = new int[nDim];
		int[] in_pos = new int[nDim];
		while (out_cursor.hasNext()) {
			out_cursor.fwd();
			out_cursor.localize(out_pos);
			for (int i = 0; i < nDim; i++) {
				in_pos[i] = (int) (out_pos[i] * DownSampleFactor[i]);
			}
			input_RA.setPosition(in_pos);
			out_cursor.get().setReal(input_RA.get().getRealFloat());
		}
	
		return output;
	}

	public static <T extends RealType<T> & NativeType<T>> Img<T> upsample(Img<T> input, long[] out_size, Interpolation.Interpolator interpType) {
		int nDim = input.numDimensions();
		if (nDim != out_size.length) {
			// print("upsampling error: the new size and input have a different number of dimension");
			return input;
		}
		long[] in_size = new long[nDim];
		input.dimensions(in_size);
		float[] upfactor = new float[nDim];
		for (int i = 0; i < nDim; i++) {
			upfactor[i] = (float) out_size[i] / in_size[i];
		}
	
		RealRandomAccess<T> interpolant;
		switch (interpType) {
		case Linear:
			NLinearInterpolatorFactory<T> NLinterp_factory = new NLinearInterpolatorFactory<T>();
			interpolant = Views.interpolate(Views.extendBorder(input), NLinterp_factory).realRandomAccess();
			break;
	
		case Lanczos:
			LanczosInterpolatorFactory<T> LanczosInterp_factory = new LanczosInterpolatorFactory<T>();
			interpolant = Views.interpolate(Views.extendBorder(input), LanczosInterp_factory).realRandomAccess();
			break;
	
		default: // NearestNeighbor:
			NearestNeighborInterpolatorFactory<T> NNInterp_factory = new NearestNeighborInterpolatorFactory<T>();
			interpolant = Views.interpolate(Views.extendBorder(input), NNInterp_factory).realRandomAccess();
			break;
		}
	
		final ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	
		final Img<T> output = imgFactory.create(out_size, input.firstElement().createVariable());
		Cursor<T> out_cursor = output.localizingCursor();
	
		float[] tmp = new float[nDim];
		while (out_cursor.hasNext()) {
			out_cursor.fwd();
			for (int d = 0; d < nDim; ++d)
				tmp[d] = out_cursor.getFloatPosition(d) / upfactor[d];
			interpolant.setPosition(tmp);
			out_cursor.get().setReal(Math.round(interpolant.get().getRealFloat()));
		}
	
		return output;
	}

	public static <T extends RealType<T> & NativeType<T>> Img<T> upsample(Img<T> input, double[] upsampling_factor, Interpolation.Interpolator interpType) {
		int dimension = input.numDimensions();
		//input.dimension(dimension);
		long[] dims = new long[dimension];
		input.dimensions(dims);
	
		long[] out_size = new long[dimension];
		for (int i = 0; i < dimension; i++)
			out_size[i] = (long) ((double) dims[i] * upsampling_factor[i]);
	
		return upsample(input, out_size, interpType);
	}

}
