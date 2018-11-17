package de.mpicbg.scf.imgtools.ui;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import net.imglib2.Interval;

/**
 * This class supports Debugging FIJI plugins/applications
 * <p>
 * It allows tracking execution time, memory consumption and more generally logging. The log can be sent to System.out or the ImageJ Log-window.
 * <p>
 * Todo: Allow logging in a File.
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: August 2015
 * <p>
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class DebugHelper implements PlugIn {

    public static final int OUTPUT_TO_SYSTEM_OUT = 1;
    public static final int OUTPUT_TO_SYSTEM_IMAGEJ_LOG = 2;
    public static final int OUTPUT_TO_FILE = 3;
    private static String outputFilename;
    private static PrintWriter out = null;

    public static int output_device = OUTPUT_TO_SYSTEM_OUT;

    private final static String[] outputNames = {"System.out", "IJ.Log", "File"};
    private static String sourceFilter = "";

    /**
     * Track currently used Memory (by ImageJ)
     *
     * @param source The object which called the function.
     */
    public static void trackMemory(Object source) {
        print(source, "Current memory: " + DebugHelper.humanReadableByteNumber(IJ.currentMemory()));
    }

    private static long lastMemoryStamp = 0;

    /**
     * Call this function two times to determine, how the memory consumption changed between the calls.
     *
     * @param source The object which called the function.
     */
    public static void trackDeltaMemory(Object source) {
        if (source != null) {
            print(source, "Delta memory: " + DebugHelper.humanReadableByteNumber(IJ.currentMemory() - lastMemoryStamp));
        }
        lastMemoryStamp = IJ.currentMemory();
    }

    /**
     * Log the current time in nanoseconds.
     *
     * @param source The object which called the function.
     */
    public static void trackTime(Object source) {
        print(source, "Current time: " + new Long(System.currentTimeMillis()).toString() + " ns");
    }

    private static long lastTimeStamp = 0;

    /**
     * Call this function two times to determine, how much time passed by between the calls.
     *
     * @param source The object which called the function.
     */
    public static void trackDeltaTime(Object source) {
        long timeDiff = System.currentTimeMillis() - lastTimeStamp;
        print(source, "Elapsed time: " + new Long(timeDiff).toString() + " ms (" + DebugHelper.humanReadableTimeNumber(timeDiff) + ")");
        lastTimeStamp = System.currentTimeMillis();
    }

    /**
     * Put text to the selected output device
     *
     * @param source The object which called the function.
     * @param text   Text to print
     */
    public static void print(Object source, String text) {
        String sourceText = "";
        if (source != null) {
            sourceText = source.getClass().getSimpleName();
        }
        if (source instanceof String) {
            sourceText = (String) source;
        }
        if (!sourceFilter.isEmpty()) {
            if (!sourceText.contains(sourceFilter)) {
                return;
            }
        }

        text = System.currentTimeMillis() + "\t" + sourceText + "\t" + text;
        switch (output_device) {
            case OUTPUT_TO_SYSTEM_IMAGEJ_LOG:
                IJ.log(text);
                break;
            default:
            case OUTPUT_TO_SYSTEM_OUT:
                System.out.println(text);
                break;
            case OUTPUT_TO_FILE:
                out.println(text);
        }
    }

    /**
     * If not all debug-outputs should be shown in the log, you can enter a filter text here.
     * <p>
     * TODO: (nice to have) implement regular expressions here.
     *
     * @param string Filter to set. Set it to "" to show all messages in the log.
     */
    public static void setFilter(String string) {
        sourceFilter = string;
    }

    /**
     * Accessor function to make the Config window available from FIJIs menu.
     */
    @Override
    public void run(String arg) {
        GenericDialog gd = new GenericDialog("Debug Configuration");


        gd.addRadioButtonGroup("Log to target", outputNames, 1, 2, outputNames[output_device - 1]);
        gd.addStringField("Output filename", outputFilename);
        gd.addStringField("Filter log source by", sourceFilter);
        gd.showDialog();
        if (gd.wasCanceled()) return;
        String newDebugTarget = gd.getNextRadioButton();
        String filename = gd.getNextString();
        sourceFilter = gd.getNextString();

        for (int i = 0; i < outputNames.length; i++) {
            if (outputNames[i] == newDebugTarget) {
                if (i + 1 == OUTPUT_TO_FILE) {
                    outputToFile(filename);
                } else {
                    output_device = i + 1;
                }
                break;
            }
        }

    }

    public static String toString(Interval i) {
        String result = "";
        for (int d = 0; d < i.numDimensions(); d++) {
            if (result.length() > 0) {
                result = result + " / ";
            }
            result = result + " " + i.min(d) + "-" + i.max(d);

        }
        return result;
    }

    /**
     * Returns a number of milliseconds to readable string such as "1h, 5min"
     *
     * @param numberOfMilliSeconds number of milliseconds
     * @return String naming the time
     */
    public static String humanReadableTimeNumber(long numberOfMilliSeconds) {
        double num = (double) numberOfMilliSeconds;
        double formerValue = 0;
        String formerUnit = "";
        String unit = "ms";

        if (num > 1000) {
            formerValue = num % 1000;
            num /= 1000;
            formerUnit = unit;
            unit = "s";

            if (num > 60) {
                formerValue = num % 60;
                num /= 60;
                formerUnit = unit;
                unit = "min";

                if (num > 60) {
                    formerValue = num % 60;
                    num /= 60;
                    formerUnit = unit;
                    unit = "h";
                }
            }
        }

        String res = (long) num + " " + unit;
        if (formerValue > 0) {
            res = res + " " + (long) formerValue + " " + formerUnit;
        }
        return res;
    }

    /**
     * Transforms a number like 10240 to 10k to make it more readable
     *
     * @param numberOfBytes number to process
     * @return shorted number string
     */
    public static String humanReadableByteNumber(long numberOfBytes) {
        double num = (double) numberOfBytes;
        String unit = "B";
        if (num > 1024) {
            num /= 1024;
            unit = "kB";

            if (num > 1024) {
                num /= 1024;
                unit = "MB";

                if (num > 1024) {
                    num /= 1024;
                    unit = "GB";
                }
            }
        }
        return ((double) ((long) (num * 100))) / 100 + " " + unit;
    }

    public static void outputToFile(String filename) {
        outputFilename = filename;
        try {
            out = new PrintWriter(filename);
            output_device = OUTPUT_TO_FILE;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
    }
}
