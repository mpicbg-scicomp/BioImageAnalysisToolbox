package de.mpicbg.scf.labelhandling.imgtools;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.measure.ResultsTable;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.DoubleColumn;
import org.scijava.table.Table;


/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
public class ResultsTableConverter {
    public static ResultsTable convertIJ2toIJ1(Table tableIn)
    {
        ResultsTable tableOut = new ResultsTable();

        for (int r = 0; r < tableIn.getRowCount(); r++)
        {
            tableOut.incrementCounter();
            for (int c = 0; c < tableIn.getColumnCount(); c++)
            {
                //DebugHelper.print(ResultsTableConverter.class, "Column " + tableIn.getColumnHeader(c));
                tableOut.addValue(tableIn.getColumnHeader(c), (Double) tableIn.get(c, r));
            }
        }

        return tableOut;
    }

    public static DefaultGenericTable convertIJ1toIJ2(ResultsTable tableIn) {
        DefaultGenericTable table = new DefaultGenericTable();

        for (int c = 0; tableIn.columnExists(c); c++) {
            DoubleColumn column = new DoubleColumn(tableIn.getColumnHeading(c));
            for (int r = 0; r < tableIn.getCounter(); r++) {
                column.add(tableIn.getValueAsDouble(c, r));
            }
            table.add(column);
        }
        return table;
    }
}
