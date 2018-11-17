package de.mpicbg.scf.labelhandling.data;

import de.mpicbg.scf.labelhandling.data.Feature;
import java.util.Arrays;

public class Measurement {
	public Measurement(Feature feature, int imageDimensions, int numberOfExpectedResults)
	{
		this.feature = feature;
		this.columnCount = feature.getSubParameterCount(imageDimensions);
		this.rowCount = numberOfExpectedResults;
		values = new double[columnCount][numberOfExpectedResults];
	}

	private int columnCount;
	private int rowCount;
	
	private Feature feature;
	private double[][] values;

	public long timeTakenInMilliseconds = 0;
	/*TBD: should I save the corresponding regions here?*/
	
	public double[][] getValues()
	{
		if (values == null)
		{
			return null;
		}
		
		double[][] resultData = new double[values.length][values[0].length];
		for (int i = 0; i < resultData.length; i++)
		{
			System.arraycopy(values[i], 0, resultData[i], 0, values[i].length);
		}
		return resultData;
	}
	
	public double[] getValues(int column)
	{
		if (values == null || column >= values.length)
		{
			return null;
		}
		
		double[] resultData = new double[values[column].length];

		System.arraycopy(values[column], 0, resultData, 0, values[column].length);
		
		return resultData;
	}
	
	public void setValue(double value, int column, int row)
	{
		//System.arraycopy(values, 0, this.values[column], 0, values.length);
		values[column][row] = value;
	}
	

	public double getValue(int column, int row)
	{
		//DebugHelper.print(this, "getting (" + column + "/" + row + ") from (" + values + "/" +  + ")");
		return values[column][row];
	}
	
	public int getColumnCount()
	{
		return columnCount;
	}
	
	public int getRowCount()
	{
		return rowCount;
	}

	public Feature getFeature() {
		return feature;
	}

	@Override
	public String toString()
	{
		String result = "(cols=" + columnCount + ",rows=" + rowCount + ")[";

		for (int i = 0; i < columnCount; i++) {
			result = result.concat(Arrays.toString(values[i]));
		}
		return result + "]";
	}

}