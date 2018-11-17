package de.mpicbg.scf.imgtools.image.neighborhood;

public class ImageConnectivity {
	// nd face connectivity : 2*d neighbor in dimension d
	// nd full connectivity : 3^d-1 in dimension d
	// nd half face connectivity
	// nd half full connectivity
	
	// special time connectivity ?
	public enum Connectivity
	{
		LEXICO_FACE,
		LEXICO_FULL,
		FACE,
		FULL
	}
	
	// position relative to center pixel
	public static long[][] getConnectivityPos( int ndim, Connectivity type )
	{
		long[][] pos;
		int npos;
		
		switch(type)
		{
		case FULL : // 8 connect in 2d, 26 connect in 3d, ...
			npos = (int)(Math.pow(3, ndim)-1);
			pos = new long[npos][ndim];
			for(int i=0; i<npos; i++)
				for(int j=0; j<ndim; j++)
					pos[i][j]=0;

			for(int i=0; i<(npos+1); i++)
			{
				int idx=i;
				int i2=i;
				if( i==(int)(npos/2) )
					continue;
				else if(i>(int)(npos/2))
					i2--;
				
				for ( int j = 0; j < ndim; j++ )
				{
					pos[ i2 ][j] = ( int ) ( idx % 3 )-1;
					idx /= 3;
				}
			}
			break;
			
		case LEXICO_FACE : // all the pixel adjacent by a face  and with index lower than the center pixel					   
			npos = ndim;
			pos = new long[npos][ndim];
			for(int i=0; i<npos; i++)
				for(int j=0; j<ndim; j++)
					pos[i][j]=0;
			for(int i=0; i<ndim; i++)
				pos[ndim-i-1][i] = -1;
			break;
		
			
		case LEXICO_FULL :
			npos = (int) ( (Math.pow(3,ndim)-1) / 2 );
			pos = new long[npos][ndim];
			for(int i=0; i<npos; i++)
				for(int j=0; j<ndim; j++)
					pos[i][j]=0;
			
			for(int i=0; i<npos; i++)
			{
				int idx=i;
				for ( int j = 0; j < ndim; j++ )
				{
					pos[ i ][j] = ( int ) ( idx % 3 )-1;
					idx /= 3;
				}
			}
			break;
			
			
		default : // FACE: all the pixel adjacent to the center pixel by a face
				  // 4 connect in 2d, 6 connect in 3d, ...
			npos = 2*ndim;
			pos = new long[npos][ndim];
			for(int i=0; i<npos; i++)
				for(int j=0; j<ndim; j++)
					pos[i][j]=0;
			for(int i=0; i<ndim; i++)
			{
				pos[ ndim-i-1 ][i] = -1;
				pos[  ndim+i  ][i] =  1;
			}
			break;
				
		}
		
		return pos;
	}
	
	// position relative to previous pixel in the list
	public static long[][] getSuccessiveMove( long[][] pos)
	{
		int npos = pos.length;
		int ndim = pos[0].length;
		long[][] posMov = new long[npos][ndim];
		for(int j=0; j<ndim; j++)
			posMov[0][j] = pos[0][j];
		for(int i=1; i<npos; i++)
			for(int j=0; j<ndim; j++)
				posMov[i][j] = pos[i][j] - pos[i-1][j];
		
		return posMov;
	}
	
	
	public static int[] getIdxOffsetToCenterPix(long[][] neigh, long[] dims)
	{
		int ndim = dims.length;
		int npos = neigh.length;
		
		long[] mul = new long[ndim];
		mul[0]=1;
		for(int j=1; j<ndim; j++)
			mul[j] = mul[j-1]*dims[j-1];
		
		int[] n_offset = new int[npos];
		for(int i = 0; i< npos; i++)
			for(int j=0; j<ndim; j++)
				n_offset[i] += neigh[i][j]*mul[j];
		
		return n_offset;
	}
	
	// index displacement relative to previous pixel in the list
	public static void getRelativeIdxMove(long[] idxMov, long[] dim )
	{
		
	}

}
