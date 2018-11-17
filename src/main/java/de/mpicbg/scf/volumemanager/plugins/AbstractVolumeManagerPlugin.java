package de.mpicbg.scf.volumemanager.plugins;

import de.mpicbg.scf.volumemanager.VolumeManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.scijava.Context;
import org.scijava.plugin.SciJavaPlugin;

/**
 * All plugins need to be derived from this class to allow SciJava to detect
 * them properly
 * <p>
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
public abstract class AbstractVolumeManagerPlugin implements SciJavaPlugin {
    protected String name;
    JMenuItem menuItem;
    private VolumeManager volumeManager;

    public AbstractVolumeManagerPlugin() {
        Context context = VolumeManager.getContext();
        VolumeManagerPluginService vmps = context.getService(VolumeManagerPluginService.class);
        name = vmps.getName(this);
    }

    public abstract void run();

    public JMenuItem getMenuItem() {
        if (menuItem == null) {
            menuItem = new JMenuItem(name);
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    run();
                }
            });
        }
        return menuItem;
    }

    public void setVolumeManager(VolumeManager volumeManager) {
        this.volumeManager = volumeManager;
    }

    protected VolumeManager getVolumeManager() {
        if (volumeManager == null) {
            return VolumeManager.getInstance();
        } else {
            return volumeManager;
        }
    }

}
