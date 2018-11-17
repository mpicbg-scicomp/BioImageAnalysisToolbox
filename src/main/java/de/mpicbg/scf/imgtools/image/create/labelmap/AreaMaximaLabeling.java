package de.mpicbg.scf.imgtools.image.create.labelmap;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.IntAccess;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Fraction;
import net.imglib2.view.Views;
import de.mpicbg.scf.imgtools.image.neighborhood.ImageConnectivity;

public class AreaMaximaLabeling {

	int[] parent;
	boolean[] is_ActivePeak;
	int[] criteria;
	
	private int numberOfFoundObjects = 0;
	
	public <T extends RealType<T> > Img<IntType> AreaMaxima(Img<T> input, final int AreaThresh, float threshold)
	{
		//////////////////////////////////////////////////////////////////////////////////////
		// build an ordered list of the pixel ////////////////////////////////////////////////
		
		// get image min and max
		Cursor<T> in_cursor = input.cursor();
		float aux = in_cursor.next().getRealFloat();
		float min = aux;
		float max = aux;
		while ( in_cursor.hasNext() )
		{
			float val = in_cursor.next().getRealFloat();
			if(val<min)       {  min = val;  }
			else if( val>max ){  max = val;  }
		}
		
		min = Math.max(min, threshold+1);
		
		// get image histogram
		int nlevel = (int) (max - min + 1);
        int[] histo_Int = new int[ nlevel ];
        
        // create a flat iterable cursor
        int ndim = input.numDimensions();
 		long[] dimensions = new long[ndim]; input.dimensions(dimensions);
 		long[] minindim = new long[ ndim ], maxindim = new long[ ndim ];
        for ( int d = 0; d < ndim; ++d ){   minindim[ d ] = 0 ;    maxindim[ d ] = dimensions[d] - 1 ;  }
        FinalInterval interval = new FinalInterval( minindim, maxindim );
        
        final Cursor< T > in_flatcursor = Views.flatIterable( Views.interval( input, interval)).cursor();
        while ( in_flatcursor.hasNext() )
		{
        	int level = (int)(in_flatcursor.next().getRealFloat() - min);
        	if(level>=0)
        	{
        		histo_Int[ level ]++;
        	}
		}

        // get each level start to build the sorted list
        int[] posInLevel = new int[ nlevel ];
        posInLevel[nlevel-1] = 0;
        for (int i = nlevel - 1; i > 0; i--)
            posInLevel[ i-1 ] = posInLevel[ i ] + histo_Int[ i ];

        // build a sorted list // higher level are put first // for each level pix are in lexicographic order
        int[] Sorted_Pix = new int[(int) input.size()];
        in_flatcursor.reset(); 
        int idx = 0; 
        while ( in_flatcursor.hasNext() )
        {
        	int level = (int)(in_flatcursor.next().getRealFloat() - min);
        	if(level>=0)
        	{
        		Sorted_Pix[posInLevel[level]] = idx;
        		posInLevel[level]++;
        	}
        	idx++;
        }
            
		//////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////

		
		// define image dealing with out of bound
 		T outOfBoundValue = input.firstElement();
 		final OutOfBoundsFactory< T, RandomAccessibleInterval< T >> oobImageFactory =  new OutOfBoundsConstantValueFactory< T, RandomAccessibleInterval< T >>( outOfBoundValue );
 		final OutOfBounds< T > input_X = oobImageFactory.create( input );
 		RandomAccess<T> input_RA = input.randomAccess();
 				
 		// define the connectivity
 		long[][] neigh = ImageConnectivity.getConnectivityPos(ndim, ImageConnectivity.Connectivity.FULL);
 		int[] n_offset = ImageConnectivity.getIdxOffsetToCenterPix(neigh, dimensions);
 		long[][] neighMov = ImageConnectivity.getSuccessiveMove(neigh);
 		int nNeigh = neigh.length;
 		
 		// first pass in sorted order of the pixel
 		parent = new int[(int)input.size()];
 		int pval, nval, rval;
 		int nidx, ridx;
 		long[] position = new long[ndim];
 		
 		is_ActivePeak = new boolean[(int)input.size()];
 		for( int i = 0; i < is_ActivePeak.length; i++){  is_ActivePeak[i] = false;}
 		criteria = new int[(int) input.size()];
 		
		for(int pidx: Sorted_Pix)
		{
			getPosFromIdx( pidx, dimensions, position);
			input_X.setPosition(position);
			pval = (int)input_X.get().getRealFloat();
			
			if(pval<=threshold){ break; }
			
			parent[pidx]=pidx;
			is_ActivePeak[pidx]=true;
			criteria[pidx] = 1;
			
			//boolean is_ActiveAux = true;
			
			// for each neighbor
			for(int i=0; i<nNeigh; i++)
			{
				input_X.move(neighMov[i]);
				if( input_X.isOutOfBounds() ){  continue;  }
				nval = (int)input_X.get().getRealFloat();
				nidx = pidx + n_offset[i];
				
				if ( (pval < nval)  |  ((pval == nval) & (nidx < pidx)) ) // test if n was already processed processed
				{	
					ridx = FindRoot(nidx);
					if( ridx == pidx) { continue; }
					if ( ! is_ActivePeak[ridx])
					{
						is_ActivePeak[pidx] = false;
						continue;
					}
					
					getPosFromIdx( ridx, dimensions, position);
					input_RA.setPosition(position);
					rval = (int)input_RA.get().getRealFloat();
					
					union(ridx, pidx, rval, pval, AreaThresh);
					
					
				}
			}
			
		}
		
		// i would like to label all the peaks whose area is at least Area 
		
		// label the image
		int current_label=1;
		for (int i = Sorted_Pix.length - 1; i >= 0; i--)
        {
            idx = Sorted_Pix[i];
            if ( parent[idx] != idx )
                parent[idx] = parent[parent[idx]];
            else
            { 
            	if( is_ActivePeak[idx] && criteria[idx]>=AreaThresh)
            	{
            		//parent_area[idx] = crit_area[idx]; // to color with the peak volume
            		parent[idx] = current_label; // to color with label
            		current_label++;
            	}
            	else
            	{
            		parent[idx] = 0;
            	}
            	
            }
        }
		numberOfFoundObjects = current_label-1;
		
		// create an output image from the label array
		final IntAccess access = new IntArray( parent );
		final Fraction frac = new Fraction(1,1);
		final ArrayImg<IntType, IntAccess> array =	new ArrayImg<IntType, IntAccess>( access, dimensions, frac );// create a Type that is linked to the container
		final IntType linkedType = new IntType( array );
		// pass it to the DirectAccessContainer
		array.setLinkedType( linkedType );	
		
		return array;
	}
	
	
	private int FindRoot(int n)
    {
        if (parent[n] != n)
        {
            parent[n] = FindRoot(parent[n]);
            return parent[n];
        }
        else { return n; }
    }
	
	
	private void union(int r, int p, int valr, int valp, int AreaThresh)
	{
		if (  ( valr == valp )  |  ( criteria[r]<AreaThresh )  )
		{
			criteria[p] = criteria[p] + criteria[r];
			criteria[r]=0;
			parent[r] = p;
			is_ActivePeak[r] = false;
		}
		else // without this else. a new region are restarted indefinitely when previous one reaches the max Area 
		  is_ActivePeak[p]=false;  
		
		return;
	}

	
	protected static void getPosFromIdx(int idx, long[] dimensions, long[] position)
	{
		int ndim = dimensions.length;
		for( int d=0; d<ndim; d++)
		{
			position[d] = idx % dimensions[d];
			idx /=  dimensions[d] ;
		}
	}
	
	
	
	public <T extends RealType<T> > Img<IntType> AreaMaxima(Img<T> input, final int AreaThresh)
	{
		float threshold = (float)input.firstElement().createVariable().getMinValue();
		return AreaMaxima(input, AreaThresh, threshold);
	}


	public int getNumberOfFoundObjects() {
		return numberOfFoundObjects;
	}
	
	
	
}
