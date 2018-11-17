package de.mpicbg.scf.imgtools.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: March 2017
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
public class IOUtilities {

    public static boolean fileExists(String filename) {
        File f = new File(filename);
        return (f.exists() && !f.isDirectory());
    }


    public static String readTextFile(String path, Charset encoding) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (Exception e) {
            return "";
        }

        return new String(encoded, encoding);
    }

    public static boolean writeTextFile(String filename, String content) {
        BufferedWriter writer = null;
        try {
            File logFile = new File(filename);

            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                return true;
            } catch (Exception e) {

            }
        }
        return false;
    }

    public static boolean backupFile(String filename) {
        int count = 0;

        String backupFilename = filename + ".backup.xml";

        while (fileExists(backupFilename)) {
            count++;
            backupFilename = filename + ".backup." + count + ".xml";
        }

        Path from = Paths.get(filename);
        Path to = Paths.get(backupFilename);

        try {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            return false;
        }

        return fileExists(backupFilename);
    }
}
