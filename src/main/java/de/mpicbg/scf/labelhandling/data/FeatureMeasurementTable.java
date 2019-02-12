package de.mpicbg.scf.labelhandling.data;


import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.table.ResultsImg;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.table.AbstractTable;
import org.scijava.table.DoubleColumn;

import java.util.Hashtable;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
public class FeatureMeasurementTable extends AbstractTable<DoubleColumn, Double> {
    Hashtable<Feature, Measurement> table;

    public FeatureMeasurementTable(Hashtable<Feature, Measurement> table)
    {
        super(0,0);
        this.table = table;

        int columnCount = 0;
        int rowCount = 0;
        for (Feature feature : table.keySet())
        {
            Measurement measurement = table.get(feature);
            columnCount += measurement.getColumnCount();
            rowCount = measurement.getRowCount();
        }

        this.setColumnCount(columnCount);
        this.setRowCount(rowCount);

        columnCount = 0;
        for (Feature feature : table.keySet()) {
            Measurement measurement = table.get(feature);
            for (int c = 0; c < measurement.getColumnCount(); c++) {

                //DebugHelper.print(this, "col1 " + feature.toString());
                //DebugHelper.print(this, "col2 " + feature.name());

                if (measurement.getColumnCount() == 1) {
                    this.setColumnHeader(c + columnCount, feature.toString());
                }
                else
                {
                    this.setColumnHeader(c + columnCount, feature.toString() + "" + c);
                }

                for (int r = 0; r < measurement.getRowCount(); r++) {
                    this.setValue(c + columnCount, r, measurement.getValue(c, r));
                }
            }
            columnCount += measurement.getColumnCount();
        }
    }

    public float getValue(int col, int row) {
        return (float)((DoubleColumn)this.get(col)).getValue(row);
    }

    public void setValue(int col, int row, double value) {
        ((DoubleColumn)this.get(col)).setValue(row, value);
    }

    public ImgPlus<DoubleType> img() {
        /*ResultsImg img = new ResultsImg(this);
        AxisType[] axes = new AxisType[]{Axes.X, Axes.Y};
        String name = "Results";
        ImgPlus imgPlus = new ImgPlus(img, "Results", axes);
        return imgPlus;*/
        return null;
    }

    protected DoubleColumn createColumn(String header) {
        return new DoubleColumn(header);
    }


    @Override
    public String toString()
    {
        return table.toString();
    }
}
