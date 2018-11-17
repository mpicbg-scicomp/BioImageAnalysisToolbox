package de.mpicbg.scf.imgtools.geometry.data;

//import ij.measure.Calibration;

import java.util.HashMap;

/**
 * N-dimensional point to which arbitrary attributes can be attached by mean of a dictionary  
 * @author Lombardot Benoit, Scientific Computing, MPI-CBG
 *
 */
public class PointN {

		protected int nDim;
		protected double[] position;
		private HashMap<String,Double> features;
		
		/**
		 * @param nDim number of point dimension
		 */
		public PointN(int nDim)
		{
			this.nDim = nDim;
			double[] position = new double[nDim];
			for(int i=0; i<nDim; i++){ position[i] = 0; }
			this.position = position;
			this.features = new HashMap<String,Double>();
		}
		
		/**
		 * 
		 * @param position an array with n values indicating the position of an n dimensionnal point
		 */
		public PointN(double[] position)
		{
			this.nDim = position.length;
			this.position = position;
			this.features = new HashMap<String,Double>();
		}
		
		/**
		 * 
		 * @param position an array with n values indicating the position of an n dimensionnal point
		 * @param features a dictionnary associating a string attributes to double values
		 */
		public PointN(double[] position, HashMap<String, Double> features)
		{
			this.nDim = position.length;
			this.position = position;
			this.features = features;

		}
		
		
		/**
		 * @return Return the dimenionnality of the point
		 */
		public int getnDim() 			{ return nDim; }
		
		public double[] getPosition() 	{ return position; }

		public double getPosition(int d){ return position[d]; }
		
		public HashMap<String,Double> getFeatures(){ return features; }
		
		public double getFeature(String feat){ return features.get(feat); }
		
		public void   setFeature(String feat, double value){ features.put(feat, value); }
		
		public double getDistanceTo(PointN p)
		{
			double sum = 0;
			for (int d = 0; d < nDim; d ++)
			{
				sum += Math.pow((position[d] - p.position[d]),2);
			}
			return Math.sqrt(sum);
		}
	
	/*
	public static class PointNS extends PointN
	{
		double scale;
		double value;
		
		PointNS(int nDim)
		{
			super(nDim);
			this.scale = 1;
			this.value = 0;
			
		}
		
		PointNS(double[] position, double scale)
		{
			super(position);
			this.scale = scale;
			this.value = 0;
		}
		
		PointNS(double[] position, double scale, double value)
		{
			super(position);
			this.scale = scale;
			this.value = value;
		}
				
		public double getScale() 		{ return scale; }
		public double getValue() 		{ return value; }
	}
	*/
	
	
	
}
