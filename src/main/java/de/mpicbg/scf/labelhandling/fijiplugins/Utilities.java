package de.mpicbg.scf.labelhandling.fijiplugins;

import de.mpicbg.scf.labelhandling.data.Feature;
import ij.measure.ResultsTable;
import java.util.ArrayList;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
 */
public class Utilities {

    public static <T extends RealType<T>> boolean isBinary(RandomAccessibleInterval<T> img)
    {

        Cursor<T> cursor = Views.iterable(img).cursor();
        ArrayList<Integer> differentValues = new ArrayList<Integer>();
        while(cursor.hasNext()) {
            cursor.next();
            int value = (int)cursor.get().getRealDouble();
            if (!differentValues.contains(value)) {
                differentValues.add(value);
                if (differentValues.size() > 2) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Img<IntType> convertFloatToInteger(Img<FloatType> img)
    {
        long[] dims = new long[img.numDimensions()];
        img.dimensions(dims);

        Img<IntType> result = ArrayImgs.ints(dims);
        Cursor<FloatType> curIn = img.cursor();
        Cursor<IntType> curOut = result.cursor();

        while (curIn.hasNext() && curOut.hasNext()) {
            curOut.next().set((int)curIn.next().get());
        }

        return result;
    }

    public static String makeNiceString(Feature[] features)
    {
        String result = "";
        String line = "";
        for (Feature feature : features) {
            String name = feature.toString();
            if (line.length() + name.length() > 40)
            {
                result = result + line + "\n";
                line =  "";
            }
            line = line + name + ", ";
        }

        result = result + line.substring(0, line.length() - 2) + "\n";

        return result;
    }

    public static void addRowsToResultsTable(ResultsTable tableIn, ResultsTable tableOut) {
        for (int r = 0; r < tableIn.getCounter(); r++) {
            tableOut.incrementCounter();
            for (int c = 0; tableIn.columnExists(c); c++) {
                tableOut.addValue(tableIn.getColumnHeading(c), tableIn.getValueAsDouble(c, r));
            }
        }
    }
}
