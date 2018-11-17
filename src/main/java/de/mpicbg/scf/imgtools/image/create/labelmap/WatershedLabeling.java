package de.mpicbg.scf.imgtools.image.create.labelmap;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.neighborhood.DiamondTipsShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsAccessible;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import de.mpicbg.scf.imgtools.core.data.HierarchicalFIFO;
import de.mpicbg.scf.imgtools.image.neighborhood.ImageConnectivity;
import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;

// for debug
import de.mpicbg.scf.imgtools.image.create.labelmap.HMaximaLabeling;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import net.imglib2.type.numeric.integer.IntType;


// todo:
// - calculate the watershed in place (it will save a lot moving around  in the image) ==> done, to be tested
// - change the neighborhood strategy to be able to have 4 connectivity ==> done for the inplace version, to be tested
// - modify HIerarchical FiFo to accept float value too
// - Create an actual watershed (no seeds)




public class WatershedLabeling {

	
	
	// IFT seeded watershed
	// with hierachical_FIFO queue
	
	// 29ms on blob
	// 3.6 sec on T1_head (consistent with the size ratio with blob image)
	
	
	public enum WatershedConnectivity
	{
		FACE(ImageConnectivity.Connectivity.FACE),
		FULL(ImageConnectivity.Connectivity.FULL);
		
		ImageConnectivity.Connectivity conn;
		
		WatershedConnectivity(ImageConnectivity.Connectivity conn)
		{			
			this.conn = conn;
		}
		
		ImageConnectivity.Connectivity getConn()
		{
			return conn;
		}
	}
	
	
	@Deprecated
	public static <T extends RealType<T>, U extends RealType<U>> Img<U> watershed(Img<T> input, Img<U> seed, WatershedConnectivity connectivity)
	{
		
		Cursor<T> Cursor = input.cursor();
		float aux = Cursor.next().getRealFloat();
		float min = aux;
		float max = aux;
		while ( Cursor.hasNext() )
		{
			float val = Cursor.next().getRealFloat();
			if(val<min)       {  min = val;  }
			else if( val>max ){  max = val;  }
		}	
		
		
		// create a priority queue
		HierarchicalFIFO Q = new HierarchicalFIFO( (int)min, (int)max);
		
		int ndim = input.numDimensions();
		long[] dimensions = new long[ndim]; input.dimensions(dimensions);
		
		// create a flat iterable cursor
		long[] minInt = new long[ ndim ], maxInt = new long[ ndim ];
		for ( int d = 0; d < ndim; ++d ){   minInt[ d ] = 0 ;    maxInt[ d ] = dimensions[d] - 1 ;  }
		FinalInterval interval = new FinalInterval( minInt, maxInt );
		final Cursor< T > input_cursor = Views.flatIterable( Views.interval( input, interval)).cursor();
		final Cursor< U > seed_cursor = Views.flatIterable( Views.interval( seed, interval)).cursor();
		
		// fill the queue
		int idx=-1;
		while( input_cursor.hasNext() )
		{
			idx++;
			int val = (int)input_cursor.next().getRealFloat();
			if ( seed_cursor.next().getRealFloat()>0 )
			{
				Q.add( idx, val );
			}
		}
		
		// extend input and seeds to to deal with out of bound
		T maxT = input.firstElement().createVariable(); 
		maxT.setReal(maxT.getMaxValue());
		int outofboundval = (int)maxT.getRealFloat();
		RandomAccessible< T > input_X = Views.extendValue(input, maxT );
		RandomAccess<U> seed_RA = seed.randomAccess();
		
		// define the connectivity
		long[][] neigh = ImageConnectivity.getConnectivityPos(ndim, connectivity.getConn() );
		int[] n_offset = ImageConnectivity.getIdxOffsetToCenterPix(neigh, dimensions);
		final Shape shape;
		if (connectivity.equals(WatershedConnectivity.FULL) )
			shape = new RectangleShape( 1, true ); // defines a hyper-square of radius one
		else
			shape = new DiamondTipsShape( 1 );
		int nNeigh = n_offset.length;
		        
		RandomAccessible<Neighborhood<T>> input_XN = shape.neighborhoodsRandomAccessible(input_X);
		RandomAccessible<Neighborhood<U>> seed_N = shape.neighborhoodsRandomAccessible(seed);
		RandomAccess<Neighborhood<T>> input_XNRA = input_XN.randomAccess(interval);
		RandomAccess<Neighborhood<U>> seed_XNRA = seed_N.randomAccess(interval);
        
        int nidx, pidx;
        int pval, nval;
		long[] posCurrent = new long[ndim];
		while( Q.HasNext() )
		{ 	
			pidx = (int) Q.Next(); 
			pval = Q.getCurrent_level() + Q.getMin();
			//count++;
			// position all the cursors
			getPosFromIdx((long)pidx, posCurrent, dimensions);
			seed_RA.setPosition(posCurrent);
			U pLabel = seed_RA.get();
			
			input_XNRA.setPosition(posCurrent);
			
			Cursor< T > inputNeighC = input_XNRA.get().cursor();
			
			seed_XNRA.setPosition(posCurrent);
			Cursor< U > seedNeighC = seed_XNRA.get().cursor();
			
			// loop on neighbors			
			for( int i =0; i<nNeigh; i++)
			{
				nidx = pidx + n_offset[i];
				//IJ.log("pidx "+pidx+"  ;  n_offset " + n_offset[i]);
				//IJ.log("outofbound " + outofboundval);
				
				nval = (int)inputNeighC.next().getRealFloat();
				//IJ.log("nval "+nval+"  ;  outofbound " + outofboundval);
				
				U nLabel = seedNeighC.next(); 
				if( nval < outofboundval) // is in bound
				{ 
					if ( nLabel.getRealFloat()==0 ) // is not queued yet?
					{
						nval = Math.min(pval, nval);
						Q.add( nidx, (int)nval );
						nLabel.set(pLabel);
					}
				}
			}
		}
		return seed;
	}
	
	
	protected static void getPosFromIdx(long idx, long[] position, long[] dimensions)
	{
		for ( int i = 0; i < dimensions.length; i++ )
		{
			position[ i ] = ( int ) ( idx % dimensions[ i ] );
			idx /= dimensions[ i ];
		}
	}
	
	
	// process only the pixel strictly superior to tresh
	@Deprecated
	public static <T extends RealType<T>, U extends RealType<U>> Img<U> watershed(Img<T> input, Img<U> seed, float thresh, WatershedConnectivity connectivity )
	{
		
		float min = thresh;
		float max = Float.MIN_VALUE;
		Cursor<T> Cursor = input.cursor();
		while ( Cursor.hasNext() )
		{
			float val = Cursor.next().getRealFloat();
			if( val>max )
				max = val;
		}	
		
		
		// create a priority queue
		HierarchicalFIFO Q = new HierarchicalFIFO( (int)min, (int)max);
		
		int ndim = input.numDimensions();
		long[] dimensions = new long[ndim]; input.dimensions(dimensions);
		
		// create a flat iterable cursor
		long[] minInt = new long[ ndim ], maxInt = new long[ ndim ];
		for ( int d = 0; d < ndim; ++d ){   minInt[ d ] = 0 ;    maxInt[ d ] = dimensions[d] - 1 ;  }
		FinalInterval interval = new FinalInterval( minInt, maxInt );
		final Cursor< T > input_cursor = Views.flatIterable( Views.interval( input, interval)).cursor();
		final Cursor< U > seed_cursor = Views.flatIterable( Views.interval( seed, interval)).cursor();
		
		// fill the queue
		int idx=-1;
		while( input_cursor.hasNext() )
		{
			idx++;
			int val = (int)input_cursor.next().getRealFloat();
			if ( seed_cursor.next().getRealFloat()>0 & val>min )
			{
				Q.add( idx, val );
			}
		}
		
		// extend input and seeds to to deal with out of bound
		T minT = input.firstElement().createVariable(); 
		minT.setReal(min);
		int outofboundval = (int)min;
		RandomAccessible< T > input_X = Views.extendValue(input, minT );
		RandomAccess<U> seed_RA = seed.randomAccess();
		
		
		
		// define the connectivity
		long[][] neigh = ImageConnectivity.getConnectivityPos(ndim, connectivity.getConn() );
		int[] n_offset = ImageConnectivity.getIdxOffsetToCenterPix(neigh, dimensions);
		final RectangleShape shape = new RectangleShape( 1, true ); // defines a hyper-square of radius one 
		int nNeigh = n_offset.length;
		        
		NeighborhoodsAccessible<T> input_XN = shape.neighborhoodsRandomAccessible(input_X);
		NeighborhoodsAccessible<U> seed_N = shape.neighborhoodsRandomAccessible(seed);
		RandomAccess<Neighborhood<T>> input_XNRA = input_XN.randomAccess(interval);
		RandomAccess<Neighborhood<U>> seed_XNRA = seed_N.randomAccess(interval);
        
        int nidx, pidx;
        int pval, nval;
		long[] posCurrent = new long[ndim];
		while( Q.HasNext() )
		{ 	
			pidx = (int) Q.Next(); 
			pval = Q.getCurrent_level() + Q.getMin();
			//count++;
			// position all the cursors
			getPosFromIdx((long)pidx, posCurrent, dimensions);
			seed_RA.setPosition(posCurrent);
			U pLabel = seed_RA.get();
			
			input_XNRA.setPosition(posCurrent);
			Cursor< T > inputNeighC = input_XNRA.get().cursor();
			
			seed_XNRA.setPosition(posCurrent);
			Cursor< U > seedNeighC = seed_XNRA.get().cursor();
			
			// loop on neighbors			
			for( int i =0; i<nNeigh; i++)
			{
				nidx = pidx + n_offset[i];
				
				nval = (int)inputNeighC.next().getRealFloat();
				
				U nLabel = seedNeighC.next(); 
				if( nval > outofboundval) // is in bound
				{ 
					if ( nLabel.getRealFloat()==0 ) // is not queued yet?
					{
						nval = Math.min(pval, nval);
						Q.add( nidx, (int)nval );
						nLabel.set(pLabel);
					}
				}
			}
		}


		return seed;
	}
	
	@Deprecated
	public static <T extends RealType<T>, U extends RealType<U>> Img<U> watershed(Img<T> input, Img<U> seed)
	{
		return watershed(input, seed, WatershedConnectivity.FULL);
	}
	
	@Deprecated
	public static <T extends RealType<T>, U extends RealType<U>> Img<U> watershed(Img<T> input, Img<U> seed, float thresh)
	{
		return watershed(input, seed, thresh, WatershedConnectivity.FULL);
	}
	
	
	
	
	
	
	
	// 2016-06-02 (thresh=100, seed=Hmaximax  with h=5)
	// algorithm				| t1-head	| blobs		|
	// full, current 			| 13.1s		| 42ms		|
	// face, in place			| 1.0s		| 25ms		|
	// full, in place			| 2.5s		| 33ms		|
	// face, in place, tresh	| 0.8s		| 19ms		|
	
	public static <T extends RealType<T>, U extends RealType<U>> Img<T> watershedInPlace(Img<T> input, Img<U> seed, float thresh, WatershedConnectivity connectivity)
	{
		
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		Cursor<T> Cursor = input.cursor();
		while ( Cursor.hasNext() )
		{
			float val = Cursor.next().getRealFloat();
			if(val<min)
				min=val;
			else if( val>max )
				max = val;
		}	
		
		min = Math.max(min, thresh);
		
		// create a priority queue
		HierarchicalFIFO Q = new HierarchicalFIFO( (int)min, (int)max );
		
		int ndim = input.numDimensions();
		long[] dimensions = new long[ndim]; input.dimensions(dimensions);
		
		// create a flat iterable cursor
		long[] minInt = new long[ ndim ], maxInt = new long[ ndim ];
		for ( int d = 0; d < ndim; ++d ){   minInt[ d ] = 0 ;    maxInt[ d ] = dimensions[d] - 1 ;  }
		FinalInterval interval = new FinalInterval( minInt, maxInt );
		final Cursor< T > input_cursor = Views.flatIterable( Views.interval( input, interval)).cursor();
		final Cursor< U > seed_cursor = Views.flatIterable( Views.interval( seed, interval)).cursor();
		
		// fill the queue
		int idx=-1;
		while( input_cursor.hasNext() )
		{
			idx++;
			T pInput = input_cursor.next();
			float pVal = pInput.getRealFloat();
			float valSeed = seed_cursor.next().getRealFloat(); 
			if ( pVal>=min)
			{
				if ( valSeed>0)
				{
					Q.add( idx, (int)pVal );
					pInput.setReal(min-1-valSeed);
				}
			}
			else
			{
				pInput.setReal(min-1);
			}
		}
		
		// extend input and seeds to to deal with out of bound
		T outOfBoundT = input.firstElement().createVariable(); 
		outOfBoundT.setReal(min-1);
		RandomAccess< T > input_XRA = Views.extendValue(input, outOfBoundT ).randomAccess();
		
		// define the connectivity
		long[][] neigh = ImageConnectivity.getConnectivityPos(ndim, connectivity.getConn() );
		int[] n_offset = ImageConnectivity.getIdxOffsetToCenterPix(neigh, dimensions);
		long[][] dPosList = ImageConnectivity.getSuccessiveMove(neigh);
		int nNeigh = n_offset.length;
        
        while( Q.HasNext() )
		{ 	
			final int pIdx = (int) Q.Next(); 
			final int pVal = Q.getCurrent_level() + Q.getMin();
			
			final long[] posCurrent = new long[ndim];
			getPosFromIdx((long)pIdx, posCurrent, dimensions);
			input_XRA.setPosition(posCurrent);
			float pLabel = input_XRA.get().getRealFloat();
			
			// loop on neighbors			
			for( int i =0; i<nNeigh; i++)
			{
				final int nIdx = pIdx + n_offset[i];
				
				input_XRA.move(dPosList[i]);
				final T n = input_XRA.get();
				final float nVal = n.getRealFloat();
				if ( nVal>=min ) // is not queued yet and is in bound?
				{
					Q.add( nIdx, (int)Math.min(pVal, nVal) );
					n.setReal(pLabel);
				}
			}
		}
        
        final T minT = input.firstElement().createVariable();
        minT.setReal(min-1);
        final T minusOneT=input.firstElement().createVariable();
        minusOneT.setReal(-1);
        Cursor<T> input_cursor2 = input.cursor();
        while( input_cursor2.hasNext() )
		{
        	T p = input_cursor2.next();
        	if (p.getRealFloat()>=(min-1) )
        	{
        		p.setReal(0);
        	}
        	else
        	{
        		p.sub(minT);
            	p.mul(minusOneT);
        	}
		}
        
		return input;
	}
	
	public static <T extends RealType<T>, U extends RealType<U>> Img<T> watershedInPlace(Img<T> input, Img<U> seed)
	{
		float threshold = Float.NEGATIVE_INFINITY; 
		return watershedInPlace(input, seed, threshold, WatershedConnectivity.FULL);
	}
	
	public static <T extends RealType<T>, U extends RealType<U>> Img<T> watershedInPlace(Img<T> input, Img<U> seed, float threshold)
	{
		return watershedInPlace(input, seed, threshold, WatershedConnectivity.FULL);
	}
	
	
	
	
	
	
	public static void main(final String... args)
	{
		// image to flood
		new ij.ImageJ();
		IJ.open("F:\\project_data\\blobs.tif");
		ImagePlus imp = IJ.getImage();
		Img<FloatType> input = ImagePlusAdapter.convertFloat(imp);
		
		//seeds
		//IJ.open("F:\\project_data\\binary2d_seeds.tif");
		//ImagePlus impSeed = IJ.getImage();
		//Img<IntType> seed = ImagePlusAdapter.wrap(impSeed);

		HMaximaLabeling maxDetector = new HMaximaLabeling();
		Img<IntType> seed = maxDetector.HMaxima(input,5);
		//ImagePlus impSeed = ImageCreationUtilities.convertImgToImagePlus(seed, "seeds", lut, imp.getDimensions(), imp.getCalibration());
		//impSeed.show() ;
		//IJ.run(impSeed, "Enhance Contrast", "saturated=0.35");
		
		IJ.run(imp, "Gaussian Blur...", "sigma=2 stack");
		
		int nIter=1;	
		boolean show = true;
		String lut = "3-3-2 RGB";
		
		
		DebugHelper.trackDeltaTime(null);
		System.out.println("\n====== c8 old  ==========");
		for (int i=0; i<nIter; i++)
		{
			Img<IntType> ws1 = watershed( input, seed.copy());
			if(show)
			{
				ImagePlus imp1 = ImageCreationUtilities.convertImgToImagePlus(ws1, "c8 auto", lut, imp.getDimensions(), imp.getCalibration());
				imp1.show() ;
				IJ.run(imp1, "Enhance Contrast", "saturated=0.35");
			}
			DebugHelper.trackDeltaTime(null);
		}
		
		float thresh;
		System.out.println("\n====== c8 in place ==========");
		for (int i=0; i<nIter; i++)
		{
			thresh = Float.NEGATIVE_INFINITY;;
			Img<FloatType> ws2 = watershedInPlace( input.copy(), seed,thresh, WatershedConnectivity.FULL);
			if(show)
			{
				ImagePlus imp2 = ImageCreationUtilities.convertImgToImagePlus(ws2, "c8 in place", lut, imp.getDimensions(), imp.getCalibration());
				imp2.show() ;
				IJ.run(imp2, "Enhance Contrast", "saturated=0.35");
			}
			DebugHelper.trackDeltaTime(null);
		}
		
		System.out.println("\n====== c4 in place ==========");
		for (int i=0; i<nIter; i++)
		{
			thresh = Float.NEGATIVE_INFINITY;
			Img<FloatType> ws3 = watershedInPlace( input.copy(), seed, thresh, WatershedConnectivity.FACE);
			if(show)
			{
				ImagePlus imp3 = ImageCreationUtilities.convertImgToImagePlus(ws3, "c4 in place", lut, imp.getDimensions(), imp.getCalibration());
				imp3.show() ;
				IJ.run(imp3, "Enhance Contrast", "saturated=0.35");
			}
			DebugHelper.trackDeltaTime(null);
		}
		
		
		System.out.println("\n====== c4 in place, thresh ==========");
		for (int i=0; i<nIter; i++)
		{
			thresh = 100;
			Img<FloatType> ws4 = watershedInPlace( input.copy(), seed, thresh, WatershedConnectivity.FACE);
			if(show)
			{
				ImagePlus imp4 = ImageCreationUtilities.convertImgToImagePlus(ws4, "c4 in place, thresh", lut, imp.getDimensions(), imp.getCalibration());
				imp4.show() ;
				IJ.run(imp4, "Enhance Contrast", "saturated=0.35");
			}
			DebugHelper.trackDeltaTime(null);
		}
		
		System.out.println("\n====== c8 in place, thresh ==========");
		for (int i=0; i<nIter; i++)
		{
			thresh = 100;
			Img<FloatType> ws5 = watershedInPlace( input.copy(), seed, thresh, WatershedConnectivity.FULL);
			if(show)
			{
				ImagePlus imp5 = ImageCreationUtilities.convertImgToImagePlus(ws5, "c8 in place, thresh", lut, imp.getDimensions(), imp.getCalibration());
				imp5.show() ;
				IJ.run(imp5, "Enhance Contrast", "saturated=0.35");
			}
			DebugHelper.trackDeltaTime(null);
		}
		/*
		System.out.println("\n====== c4 in place, imp-20 ==========");
		for (int i=0; i<nIter; i++)
		{
			thresh = Float.MIN_VALUE;
			Img<FloatType> input4 = ImagePlusAdapter.convertFloat(imp.duplicate());
			Img<FloatType> ws3 = watershedInPlace( input4, seed, thresh, WatershedConnectivity.FACE);
			if(show)
			{
				ImagePlus imp3 = ImageCreationUtilities.convertImgToImagePlus(ws3, "c4 in place, imp-20","Spectrum", imp.getDimensions(), imp.getCalibration());
				imp3.show() ;
				IJ.run(imp3, "Enhance Contrast", "saturated=0.35");
			}
			DebugHelper.trackDeltaTime(null);
		}
		
		Img<IntType> ws2 = watershed( input, seed.copy(), WatershedConnectivity.FULL);
		ImagePlus imp2 = ImageCreationUtilities.convertImgToImagePlus(ws2, "c8","Spectrum", imp.getDimensions(), imp.getCalibration());
		imp2.show() ;
		IJ.run(imp2, "Enhance Contrast", "saturated=0.35");
		
		Img<IntType> ws3 = watershed( input, seed.copy(), WatershedConnectivity.FACE);
		ImagePlus imp3 = ImageCreationUtilities.convertImgToImagePlus(ws3, "c4","Spectrum", imp.getDimensions(), imp.getCalibration());
		imp3.show();
		IJ.run(imp3, "Enhance Contrast", "saturated=0.35");
		
		
		
		float thresh = 100;
		
		Img<IntType> ws4 = watershed( input, seed.copy(), thresh);
		ImagePlus imp4 = ImageCreationUtilities.convertImgToImagePlus(ws4, "c8 auto thresh","Spectrum", imp.getDimensions(), imp.getCalibration());
		imp4.show() ;
		IJ.run(imp4, "Enhance Contrast", "saturated=0.35");
		
		Img<IntType> ws5 = watershed( input, seed.copy(), thresh, WatershedConnectivity.FULL);
		ImagePlus imp5 = ImageCreationUtilities.convertImgToImagePlus(ws5, "c8 thresh","Spectrum", imp.getDimensions(), imp.getCalibration());
		imp5.show() ;
		IJ.run(imp5, "Enhance Contrast", "saturated=0.35");
		
		Img<IntType> ws6 = watershed( input, seed.copy(), thresh, WatershedConnectivity.FACE);
		ImagePlus imp6 = ImageCreationUtilities.convertImgToImagePlus(ws6, "c4 thresh","Spectrum", imp.getDimensions(), imp.getCalibration());
		imp6.show() ;
		IJ.run(imp6, "Enhance Contrast", "saturated=0.35");
		*/
		
	}
	

}
