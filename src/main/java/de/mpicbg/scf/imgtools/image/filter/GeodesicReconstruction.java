package de.mpicbg.scf.imgtools.image.filter;


import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsAccessible;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import de.mpicbg.scf.imgtools.core.data.HierarchicalFIFO;
import de.mpicbg.scf.imgtools.image.create.labelmap.LocalMaximaLabeling;
import de.mpicbg.scf.imgtools.image.neighborhood.ImageConnectivity;


// compare reconstruction 1 and 2 and keep only the fastest

public class GeodesicReconstruction {	
	
	// 1 - reconstruction queuing all pixel of the image
	// for each p in Q
	//     p = Q.pop()
	//     for n in neigh(p)
	//         if marker(n)<marker(p)
	//             dequeue(n,marker(n)); // optional
	//             marker(n) = min(mask(n),marker(p))
	//             enqueue(n, marker(n))
	//         end 
	//     end 
	// end 
	
	
	public <T extends RealType<T>> Img<T> reconstruction1(Img<T> MaskImg, Img<T> MarkerImg )
	{
		
		
		// detect the local maxima in the image
		LocalMaximaLabeling maxfilter = new LocalMaximaLabeling();
		Img<IntType> LocalMaxImg = maxfilter.LocalMaxima(MarkerImg);
		Cursor<IntType> maxCursor = LocalMaxImg.cursor();
		
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		Cursor<T> Cursor = MarkerImg.cursor();
		while ( Cursor.hasNext() )
		{
			float val = Cursor.next().getRealFloat();
			if(val<min)
				min=val;
			else if( val>max )
				max = val;
		}	
		
		// create a priority queue
		HierarchicalFIFO Q = new HierarchicalFIFO( (int)min, (int)max);
		
		//PriorityQueue< Pixel > Q = new PriorityQueue< Pixel >((int) MarkerImg.size());
		
		// fill the queue
		int ndim = MarkerImg.numDimensions();
		long[] dimensions = new long[ndim]; MarkerImg.dimensions(dimensions);
		
		long[] minInt = new long[ ndim ], maxInt = new long[ ndim ];
		for ( int d = 0; d < ndim; ++d ){   minInt[ d ] = 0 ;    maxInt[ d ] = dimensions[d] - 1 ;  }
		FinalInterval interval = new FinalInterval( minInt, maxInt );
		final Cursor< T > marker_cursor = Views.flatIterable( Views.interval( MarkerImg, interval)).cursor();
		
		long[] pos=new long[ndim];
		long idx = 0;
		while( marker_cursor.hasNext() )
		{
			marker_cursor.fwd();
			if (maxCursor.next().getRealFloat()>0)
			{
				marker_cursor.localize(pos);
				float val = marker_cursor.get().getRealFloat();
				Q.add( idx, (int)val );
			}
			idx++;
		}
		
		// extend marker to deal with out of bound
		T outOfBoundValue = MarkerImg.firstElement();
		final OutOfBoundsFactory< T, RandomAccessibleInterval< T >> oobImageFactory =  new OutOfBoundsConstantValueFactory< T, RandomAccessibleInterval< T >>( outOfBoundValue );
		final OutOfBounds< T > Marker_X = oobImageFactory.create( MarkerImg );
		RandomAccess<T> Mask_X = MaskImg.randomAccess();
		
		// define the connectivity
		long[][] neigh = ImageConnectivity.getConnectivityPos(ndim, ImageConnectivity.Connectivity.FULL);
		int[] n_offset = ImageConnectivity.getIdxOffsetToCenterPix(neigh, dimensions);
		long[][] neighMov = ImageConnectivity.getSuccessiveMove(neigh);
		int nNeigh = neigh.length;
		
		boolean[] is_processed = new boolean[(int)MaskImg.size()];
        boolean[] is_requeued = new boolean[(int)MaskImg.size()];
        for( int i=0; i<is_processed.length; i++)
        {
        	is_processed[i] = false;
        	is_requeued[i] = false;
        	
        }
        int pidx, nidx;
		float pfloatVal, nfloatVal, nfloatValMask;
		long[] posCurrent = new long[ndim]; //, posNeigh = new long[ndim];
		while(  Q.HasNext() )
		{
			pidx = (int)Q.Next(); // retrieve head of the queue and remove it from the queue;
			
			if( is_processed[(int)pidx]){ continue; }
			is_processed[(int)pidx]=true;
			is_requeued[pidx]=true;
			
			getPosFromIdx(pidx, posCurrent, dimensions);
			Marker_X.setPosition( posCurrent );
			pfloatVal = Marker_X.get().getRealFloat();
			Mask_X.setPosition( posCurrent );

			// loop on neighbors
			for( int i =0; i<nNeigh; i++)
			{
				nidx = pidx + n_offset[i];
				Marker_X.move(neighMov[i]);
				Mask_X.move(neighMov[i]);
				
				if ( Marker_X.isOutOfBounds() ) { continue; } // already re-queued or out of bound
				if ( is_requeued[(int)nidx] ){ continue; }// already processed
				
				//if ( nfloatVal>=pfloatVal  |  Marker_X.isOutOfBounds() ) { continue; }
				
				// update neighbor value (nfloatVal)
				nfloatValMask = Mask_X.get().getRealFloat(); 
				nfloatVal = Math.min(pfloatVal, nfloatValMask);
				Marker_X.get().setReal(nfloatVal);
				
				// queue n
				Q.add( nidx , (int)nfloatVal );
				is_requeued[nidx]=true;
				//insertTime++;
			}
		}
		
		return MarkerImg;
	}
	

	protected static long getIdxFromPos(long[] position, long[] dimensions){
		long index = position[0];
		long mul=dimensions[0];
		for( int i = 1; i< dimensions.length; i++ )
		{
			index += mul* position[i];
			mul *= dimensions[i];
		}
		return index;
	}
	
	
	
	
	
	// after could test the hierarchical heap
	// test using the home made hierachical queue
	// test using hierarchical_fifo slightly slower with blobs but 3 time faster with t1-head
	public <T extends RealType<T>> Img<T> reconstruction2(Img<T> MaskImg, Img<T> MarkerImg )
	{
		
		// detect the local maxima in the marker image
		LocalMaximaLabeling maxfilter = new LocalMaximaLabeling();
		Img<IntType> LocalMaxImg = maxfilter.LocalMaxima(MarkerImg);
		
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		Cursor<T> Cursor = MarkerImg.cursor();
		while ( Cursor.hasNext() )
		{
			float val = Cursor.next().getRealFloat();
			if(val<min)
				min=val;
			else if( val>max )
				max = val;
		}	
		
		
		// create a priority queue
		HierarchicalFIFO Q = new HierarchicalFIFO( (int)min, (int)max );
		
		int ndim = MarkerImg.numDimensions();
		long[] dimensions = new long[ndim]; MarkerImg.dimensions(dimensions);
		
		// create a flat iterable cursor
		long[] minInt = new long[ ndim ], maxInt = new long[ ndim ];
		for ( int d = 0; d < ndim; ++d ){   minInt[ d ] = 0 ;    maxInt[ d ] = dimensions[d] - 1 ;  }
		FinalInterval interval = new FinalInterval( minInt, maxInt );
		final Cursor< T > marker_cursor = Views.flatIterable( Views.interval( MarkerImg, interval)).cursor();
		final Cursor< IntType > max_cursor = Views.flatIterable( Views.interval( LocalMaxImg, interval)).cursor();
		
		// fill the queue
		int idx=-1;
		while( marker_cursor.hasNext() )
		{
			idx++;
			int val = (int)marker_cursor.next().getRealFloat();
			if (max_cursor.next().getRealFloat()>0 & val>min)
			{
				Q.add( idx, val );
			}
		}
		
		// extend marker and mask to deal with out of bound
		T maxT = MarkerImg.firstElement().createVariable(); 
		maxT.setReal(maxT.getMaxValue()); //ideally set min(Marker) -1
		RandomAccessible< T > Mask_X = Views.extendValue(MaskImg, maxT );
		RandomAccessible< T > Marker_X = Views.extendValue(MarkerImg, maxT );
		RandomAccess<T> Marker_XRA = Marker_X.randomAccess();
		
		// define the connectivity
		long[][] neigh = ImageConnectivity.getConnectivityPos(ndim, ImageConnectivity.Connectivity.FULL);
		int[] n_offset = ImageConnectivity.getIdxOffsetToCenterPix(neigh, dimensions);
		final RectangleShape shape1 = new RectangleShape( 1, true ); // defines a hyper-square of radius one 
		final RectangleShape shape2 = new RectangleShape( 1, true ); // defines a hyper-square of radius one 
        int nNeigh = n_offset.length;
		        
        NeighborhoodsAccessible<T> Mask_XN = shape1.neighborhoodsRandomAccessible(Views.interval( Mask_X, interval));
        NeighborhoodsAccessible<T> Marker_XN = shape2.neighborhoodsRandomAccessible(Views.interval( Marker_X, interval));
        RandomAccess<Neighborhood<T>> Mask_XNRA = Mask_XN.randomAccess();
        RandomAccess<Neighborhood<T>> Marker_XNRA = Marker_XN.randomAccess();
        
        long nidx, pidx;
        float pfloatVal, nfloatVal, nfloatValMask;
		long[] posCurrent = new long[ndim];
		
		boolean[] is_processed = new boolean[(int)MaskImg.size()];
		boolean[] is_requeued = new boolean[(int)MaskImg.size()];
        for( int i=0; i<is_processed.length; i++)
        {
        	is_processed[i] = false;
        	is_requeued[i] = false;	
        }
        
		while( Q.HasNext() )
		{ 	
			pidx = Q.Next(); // retrieve head of the queue and remove it from the queue;
			if ( is_processed[(int) pidx] ) { continue; }
			is_processed[(int)pidx]=true;

			// position all the cursors
			getPosFromIdx((long)pidx, posCurrent, dimensions);
			Marker_XRA.setPosition( posCurrent );
			Mask_XNRA.setPosition(posCurrent);
			Marker_XNRA.setPosition(posCurrent);
			
			pfloatVal = Marker_XRA.get().getRealFloat();

			// loop on neighbors			
			Cursor< T >  MaskNeighC = Mask_XNRA.get().cursor();
			Cursor< T >  MarkerNeighC = Marker_XNRA.get().cursor();
			for( int i =0; i<nNeigh; i++)
			{
				nidx = pidx + n_offset[i];
				
				nfloatVal = MarkerNeighC.next().getRealFloat();
				MaskNeighC.fwd(); 
				
				if ( nfloatVal >= pfloatVal ) { continue; }
				if ( is_requeued[(int)nidx] ){ continue; }
				nfloatValMask = MaskNeighC.get().getRealFloat(); 

				// update neighbor value (nfloatVal)
				nfloatVal = Math.min(pfloatVal, nfloatValMask);
				MarkerNeighC.get().setReal(nfloatVal);
				
				// queue n
				Q.add( nidx, (int)nfloatVal );
				is_requeued[(int)nidx]=true;
			}
			
		}
		
		return MarkerImg;
	}
	
	
	protected static void getPosFromIdx(long idx, long[] position, long[] dimensions)
	{
		for ( int i = 0; i < dimensions.length; i++ )
		{
			position[ i ] = ( int ) ( idx % dimensions[ i ] );
			idx /= dimensions[ i ];
		}
	}
	
	
	
	
}