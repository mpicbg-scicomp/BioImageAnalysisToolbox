package de.mpicbg.scf.imgtools.image.create.labelmap;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
//import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
//import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.IntAccess;
import net.imglib2.img.basictypeaccess.array.IntArray;
//import net.imglib2.outofbounds.OutOfBounds;
//import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
//import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Fraction;
import net.imglib2.view.Views;
import de.mpicbg.scf.imgtools.image.neighborhood.ImageConnectivity;



public class LocalMaximaLabeling
{
	
	int[] parent;
	boolean[] ismaxroot;
	int numberOfFoundObjects = 0;
	
	
	// speed : 10ms at the best of 10 successive filtering of the blob image
	// 1.2 sec on t1-head 
	
	public <T extends RealType<T> > Img<IntType> LocalMaxima(Img<T> input)
	{
		
		int ndim = input.numDimensions();
		long[] dims = new long[ndim]; input.dimensions(dims);
		parent = new int[(int) input.size()];
		for( int i=0; i<parent.length; i++)
			parent[i]=-1;
		
		// extend the input
		T minT = input.firstElement().createVariable();
		minT.setReal(minT.getMinValue());
		float minT_float = minT.getRealFloat();
		RandomAccessible< T > input_X = Views.extendValue(input, minT );
		
		
		// create a flat iterable cursor
		long[] min = new long[ ndim ], max = new long[ ndim ];
        for ( int d = 0; d < ndim; ++d ){   min[ d ] = 0 ;    max[ d ] = dims[d] - 1 ;  }
        FinalInterval interval = new FinalInterval( min, max );
        final Cursor< T > ipix = Views.flatIterable( Views.interval( input_X, interval)).cursor();
        		
		
		// define the connectivity
		long[][] neigh = ImageConnectivity.getConnectivityPos(ndim, ImageConnectivity.Connectivity.LEXICO_FULL);
		int[] n_offset = ImageConnectivity.getIdxOffsetToCenterPix(neigh, dims);
        final RectangleShape shape = new RectangleShape( 1, true ); // defines a hyper-square of radius one 
        int nNeigh = (int)((Math.pow(3, ndim)-1)/2);
		
        
		// first path, go through all the pixel and check already visited neighbor for existing tree
        int p = -1, n, r;
        float pval, nval;
        
        ismaxroot = new boolean[(int)input.size()];
        for( int i=0; i<ismaxroot.length; i++)
        	ismaxroot[i] = false; 
        
        for ( final Neighborhood< T > neighborhood : shape.neighborhoods( Views.interval( input_X, interval)) )
        {
        	p++;
        	pval = ipix.next().getRealFloat();
			if (pval<=minT_float){ continue;}
        	
			parent[p]=p;
			ismaxroot[p]= true;
			
			// loop on neighbor
			Cursor<T> nCursor = neighborhood.cursor();
			for( int i = 0; i<nNeigh; i++)
			{
				// if n is in bounds
				nval = nCursor.next().getRealFloat(); 
				if(nval<=minT_float){ continue; }
				
				n=p+n_offset[i];
				if( pval >= nval) 
				{
					// union of n and p
					r = find_root(n);
					if( r == p) { continue; }
					if (pval==nval)
					{	parent[r]=p;
						ismaxroot[p] = ismaxroot[p] & ismaxroot[r];
					}
					ismaxroot[r]=false;
					
				}
				else // if ( pval < nval ) then p status is changed to non-maximum root
				{	
					ismaxroot[p] = false;
				}
			}
			
		}
		
        
		// second path to label the tree
        int current_label = 0;
        for(int i = parent.length-1 ; i>-1 ; i--)
		{
			if(parent[i]>=0)
			{
				if(parent[i]==i) // if i is root of a flat zone
				{
					if( ismaxroot[i] ) // if i is root of a maxima create a new label
					{
						current_label++;
						parent[i] = current_label;
					}
					else // if i is not a maxima set its intensity to zero
						parent[i] = 0;
				}
				else 
					parent[i] = parent[ parent[i] ];
			}
		}
		numberOfFoundObjects = current_label;
        
		
        // create an output image from the label array
		final IntAccess access = new IntArray( parent );
		final Fraction frac = new Fraction(1,1);
		final ArrayImg<IntType, IntAccess> array =	new ArrayImg<IntType, IntAccess>( access, dims, frac );// create a Type that is linked to the container
		//final ArrayImg<IntType, IntAccess> array = new ArrayImg<IntType, IntAccess>( access, dims, 1 );// create a Type that is linked to the container
		final IntType linkedType = new IntType( array );
		// pass it to the DirectAccessContainer
		array.setLinkedType( linkedType );	
		
		return array;
	}
	
	
	
	private int find_root(int p)
	{
		if( parent[p]!=p)
		{
			parent[p] = find_root(parent[p]);
			return parent[p];
		}
			
		return p;
	}
	
	
	
	
	
	
	
	
	public <T extends RealType<T> > Img<IntType> LocalMaxima(Img<T> input, float threshold)
	{
		
		int ndim = input.numDimensions();
		long[] dims = new long[ndim]; input.dimensions(dims);
		parent = new int[(int) input.size()];
		for( int i=0; i<parent.length; i++)
			parent[i]=-1;
		
		// extend the input
		T minT = input.firstElement().createVariable();
		minT.setReal(minT.getMinValue());
		float minT_float = minT.getRealFloat();
		threshold = threshold<minT_float?minT_float:threshold;
		//minT.setReal(Float.NEGATIVE_INFINITY);
		RandomAccessible< T > input_X = Views.extendValue(input, minT );
		
		
		// create a flat iterable cursor
		long[] min = new long[ ndim ], max = new long[ ndim ];
        for ( int d = 0; d < ndim; ++d ){   min[ d ] = 0 ;    max[ d ] = dims[d] - 1 ;  }
        FinalInterval interval = new FinalInterval( min, max );
        final Cursor< T > ipix = Views.flatIterable( Views.interval( input_X, interval)).cursor();
        		
		
		// define the connectivity
		long[][] neigh = ImageConnectivity.getConnectivityPos(ndim, ImageConnectivity.Connectivity.LEXICO_FULL);
		int[] n_offset = ImageConnectivity.getIdxOffsetToCenterPix(neigh, dims);
        final RectangleShape shape = new RectangleShape( 1, true ); // defines a hyper-square of radius one 
        int nNeigh = (int)((Math.pow(3, ndim)-1)/2);
		
        
		// first path, go through all the pixel and check already visited neighbor for existing tree
        int p = -1, n, r;
        float pval, nval;
        
        ismaxroot = new boolean[(int)input.size()];
        for( int i=0; i<ismaxroot.length; i++)
        	ismaxroot[i] = false; 
        
        for ( final Neighborhood< T > neighborhood : shape.neighborhoods( Views.interval( input_X, interval)) )
        {
        	p++;
        	pval = ipix.next().getRealFloat();
			if (pval>threshold)
			{
        	
				parent[p]=p;
				ismaxroot[p]= true;
				
				// loop on neighbor
				Cursor<T> nCursor = neighborhood.cursor();
				for( int i = 0; i<nNeigh; i++)
				{
					// if n is in bounds
					nval = nCursor.next().getRealFloat(); 
					if(nval>threshold)
					{
					
						n=p+n_offset[i];
						if( pval >= nval) 
						{
							// union of n and p
							r = find_root(n);
							if( r == p) { continue; }
							if (pval==nval)
							{	parent[r]=p;
								ismaxroot[p] = ismaxroot[p] & ismaxroot[r];
							}
							ismaxroot[r]=false;
							
						}
						else // if ( pval < nval ) then p status is changed to non-maximum root
						{	
							ismaxroot[p] = false;
						}
						
					}
				}
				
			}
			
		}
		
        
		// second path to label the tree
        int current_label = 0;
        for(int i = parent.length-1 ; i>-1 ; i--)
		{
			if(parent[i]>=0)
			{
				if(parent[i]==i) // if i is root of a flat zone
				{
					if( ismaxroot[i] ) // if i is root of a maxima create a new label
					{
						current_label++;
						parent[i] = current_label;
					}
					else // if i is not a maxima set its intensity to zero
						parent[i] = 0;
				}
				else 
					parent[i] = parent[ parent[i] ];
			}
		}
        
		
        // create an output image from the label array
		final IntAccess access = new IntArray( parent );
		final Fraction frac = new Fraction(1,1);
		final ArrayImg<IntType, IntAccess> array =	new ArrayImg<IntType, IntAccess>( access, dims, frac );// create a Type that is linked to the container
		//final ArrayImg<IntType, IntAccess> array = new ArrayImg<IntType, IntAccess>( access, dims, 1 );// create a Type that is linked to the container
		final IntType linkedType = new IntType( array );
		// pass it to the DirectAccessContainer
		array.setLinkedType( linkedType );	
		
		return array;
	}
	
	
	

	public int getNumberOfFoundObjects() {
		return numberOfFoundObjects;
	}
	
	
	
	
}
