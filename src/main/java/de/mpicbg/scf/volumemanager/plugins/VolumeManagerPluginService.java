package de.mpicbg.scf.volumemanager.plugins;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.imagej.ImageJService;
import org.scijava.plugin.AbstractPTService;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.Service;

/**
 * This service manages all Volume Manager plugins
 * <p>
 * <p>
 * Adapted from https://github.com/imagej/imagej-tutorials/blob/master/create-a-new-plugin-type/src/main/java/AnimalService.java
 * <p>
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: August 2016
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
@Plugin(type = Service.class)
public class VolumeManagerPluginService extends AbstractPTService<AbstractVolumeManagerPlugin> implements ImageJService {

    private HashMap<String, PluginInfo<AbstractVolumeManagerPlugin>> plugins =
            new HashMap<String, PluginInfo<AbstractVolumeManagerPlugin>>();

    public List<String> getPluginNames() {
        List<String> orderedSet = new ArrayList<String>();

        for (String item : plugins.keySet()) {
            boolean added = false;
            int count = 0;
            for (String listEntry : orderedSet) {
                if (plugins.get(listEntry).getPriority() > plugins.get(item).getPriority()) {
                    orderedSet.add(count, item);
                    added = true;
                    break;
                }
                count++;
            }

            if (!added) {
                orderedSet.add(item);
            }

        }

        return orderedSet;
    }

    public AbstractVolumeManagerPlugin createPlugin(final String name) {
        // First, we get the animal plugin with the given name.
        final PluginInfo<AbstractVolumeManagerPlugin> info = plugins.get(name);

        if (info == null) {
            throw new IllegalArgumentException("No animal of that name");
        }

        // Next, we use the plugin service to create an animal of that kind.
        final AbstractVolumeManagerPlugin plugin = pluginService().createInstance(info);
        if (plugin != null) {
            plugin.name = name;
        }
        return plugin;
    }

    public String getName(AbstractVolumeManagerPlugin avmp) {
        for (String item : plugins.keySet()) {
            final PluginInfo<AbstractVolumeManagerPlugin> info = plugins.get(item);

            if (info != null && info.getIdentifier() != null && info.getIdentifier().toString().equals("plugin:" + avmp.getClass().toString())) {
                return item;
            }
        }
        DebugHelper.print(this, "Warning: plugin not found in list: " + avmp.getClass().toString());
        return null;
    }


    public String[] getMenuPath(String name) {
        // First, we get the animal plugin with the given name.
        final PluginInfo<AbstractVolumeManagerPlugin> info = plugins.get(name);

        if (info == null) {
            throw new IllegalArgumentException("No animal of that name");
        }

        return info.getMenuPath().getMenuString().split(">");
    }


    public String getLabel(String name) {
        // First, we get the animal plugin with the given name.
        final PluginInfo<AbstractVolumeManagerPlugin> info = plugins.get(name);

        if (info == null) {
            throw new IllegalArgumentException("No animal of that name");
        }

        return info.getLabel();
    }


    @Override
    public void initialize() {

        // We loop over all available animal plugins.
        for (final PluginInfo<AbstractVolumeManagerPlugin> info : getPlugins()) {
            String name = info.getName();
            if (name == null || name.isEmpty()) {
                name = info.getClassName();
            }
            // Add the plugin to the list of known animals.
            plugins.put(name, info);
        }
    }

    @Override
    public Class<AbstractVolumeManagerPlugin> getPluginType() {
        return AbstractVolumeManagerPlugin.class;
    }

}
