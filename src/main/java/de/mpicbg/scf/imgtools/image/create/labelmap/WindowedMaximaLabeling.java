package de.mpicbg.scf.imgtools.image.create.labelmap;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.LocalExtrema;
import net.imglib2.algorithm.localextrema.LocalExtrema.LocalNeighborhoodCheck;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class WindowedMaximaLabeling {

	public enum ExtremaType{
		MINIMA,
		MAXIMA;
	}
	
	public enum NeighborhoodType{
		SQUARE,
		SPHERE;
	}
	
	public static < T extends RealType<T> & NumericType< T > & NativeType< T > > 
						List<Point> getExtrema( RandomAccessibleInterval<T> img, double threshold, int neighborhoodRadius, ExtremaType extremaType)
	{
		return  getExtrema( img, threshold, neighborhoodRadius, extremaType, NeighborhoodType.SQUARE );
	}
	
	public static < T extends RealType<T> & NumericType< T > & NativeType< T > > 
						List<Point> getExtrema( RandomAccessibleInterval<T> img, double threshold, int neighborhoodRadius, ExtremaType extremaType, NeighborhoodType neighType)
	{
		RandomAccessible< T > imgX = Views.extendBorder(img);
		Interval interval = Intervals.expand(img, 0);
		
		
		LocalNeighborhoodCheck< Point, T > localNeighborhoodCheck;
		T val = Util.getTypeFromInterval(img).createVariable();
		val.setReal(threshold);
		switch(extremaType)
		{
			case MINIMA:	
				localNeighborhoodCheck = new LocalExtrema.MinimumCheck< T >( val );
				break;
			default: // case MAXIMA:
				localNeighborhoodCheck  = new LocalExtrema.MaximumCheck< T >( val );
				break;
		}
		
		Shape shape;
		switch(neighType)
		{
			case SQUARE:	
				boolean skipCenter = true; //implementation of the extrema checked is such that we don't care of equal values
				shape = new RectangleShape( neighborhoodRadius, skipCenter );
				break;
			default: // case SPHERE:
				shape = new HyperSphereShape( neighborhoodRadius);
				break;
		}
		
		final Cursor< T > center = Views.flatIterable( img ).cursor();
		
		List<Point> extrema = new ArrayList<Point>();
		
		for ( final Neighborhood< T > neighborhood : shape.neighborhoods( Views.interval( imgX, interval)) ) 
		{
			center.fwd();
			Point p = localNeighborhoodCheck.check( center, neighborhood );
			if ( p != null )
				extrema.add( p );
		}
		
		return extrema;
	}
	
	public static < T extends RealType<T> & NumericType< T > & NativeType< T > > 
	Img<FloatType> getExtremaLabelImage( RandomAccessibleInterval<T> img, double threshold, int neighborhoodRadius, ExtremaType extremaType, NeighborhoodType neighType)
	{
		List<Point> points = getExtrema( img, threshold, neighborhoodRadius, extremaType, neighType );
		long[] dims = new long[img.numDimensions()];
		img.dimensions(dims);
		Img<FloatType> labelImage = LabelingUtilities.convertPointsToLabelmap(dims, points);
		
		return labelImage;		
	}
	
	
	// not working, I might not initialize properly the service. the detection are stuck next to the first line of the image  
	/*	private List<Point> getExtremaCandidate( Img<T> img)
	{
		// dog pyramid, level in the dog pyramid
		T minVal = input.firstElement().createVariable();
		minVal.setReal(0);
		LocalNeighborhoodCheck< Point, T > localNeighborhoodCheck = new LocalExtrema.MaximumCheck< T >( minVal );
		int numThreads = 1;//Runtime.getRuntime().availableProcessors();
		
		ExecutorService service = Executors.newFixedThreadPool( numThreads );
		
		List< Point > peaks = LocalExtrema.findLocalExtrema( img, localNeighborhoodCheck, service );
		
		return peaks;
	}*/
	
	
	


	
	
	
}
