package de.mpicbg.scf.imgtools.geometry.data;

import java.util.EventListener;
import java.util.EventObject;


/**
 * This class represents the mother class of all Surface objects. As long as there is only one
 * class deriving from it, its interface may change.
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: June 2015
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
public abstract class Surface {
    public static class SurfaceChangedEvent extends EventObject {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public SurfaceChangedEvent(Object source) {
            super(source);
        }
    }

    public interface SurfaceChangeListener extends EventListener {
        void surfaceChanged(SurfaceChangedEvent e);
    }

    protected String title;
    protected boolean automaticTitleChanged = false;
    private static int count = 0;
    private final int myCount;

    /**
     * Constructor
     *
     * @param title name of the Surface object. If "", a systematic name will be generated.
     */
    public Surface(String title) {
        setTitle(title);
        automaticTitleChanged = false;
        count++;
        myCount = count;
    }

    /**
     * Return the name of the Surface object. This name is created automatically for the object and may be changed.
     *
     * @return the title of the surface object
     */
    public String getTitle() {
        return title;
    }

    /**
     * Change the title of the surface object.
     *
     * @param val the new title of the surface
     */
    public void setTitle(String val) {
        title = val;
        if (title.length() == 0) {
            createNewAutomaticTitle();
        }
    }

    /**
     * Create a new dummy surface name
     */
    protected void createNewAutomaticTitle() {
        title = "Surface " + new Integer(myCount).toString();
    }
}
