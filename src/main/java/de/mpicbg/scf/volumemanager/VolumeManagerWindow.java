package de.mpicbg.scf.volumemanager;

import de.mpicbg.scf.fijiplugins.ui.roi.BrushPluginTool;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.ImageJUtilities;
import de.mpicbg.scf.imgtools.ui.JExtendedButton;
import de.mpicbg.scf.volumemanager.core.LineStylePanel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Toolbar;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The VolumeManager is a window displaying a list of available surfaces. It allows manipulating them, saving and loading.
 * This class represents the window showing available volumes as a list.
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
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
public class VolumeManagerWindow extends JFrame {

    private VolumeManager volumeManager;

    Timer heartbeat = null;
    int delay = 300; // milliseconds


    // ============================================================================
    // UI members
    //

    JButton btnRename = new JExtendedButton("Rename");
    JButton btnNew = new JExtendedButton("New");


    JButton btnDelete = new JExtendedButton("Delete");
    JButton btnDuplicate = new JExtendedButton("Duplicate");
    JButton btnRevertChanges = new JExtendedButton("Revert changes");

    JButton btnNextKeySlice = new JExtendedButton("Next key slice");
    JButton btnPreviousKeySlice = new JExtendedButton("Previous key slice");
    JButton btnNewKeySlice = new JExtendedButton("Create new key slice here");
    JButton btnDeleteKeySlice = new JExtendedButton("Delete key slice");

    LineStylePanel lineStylePanel;


    JCheckBox chckbxShowAll = new JCheckBox("Show all");
    JCheckBox chckbxShowLabels = new JCheckBox("Show labels");
    JCheckBox chckbxAllowExtrapolation = new JCheckBox("Allow extrapolation");
    JCheckBox chckbxAllowSwitch = new JCheckBox("Allow image switch");

    JButton spacer = new JButton();


    private final JCheckBox cbHelp = new JCheckBox("?");

    JComponent[] buttons;


    JScrollPane scrollPane = new JScrollPane();

    JList volumeList = null;

    private JPanel contentPane;

    long lastTimeStamp = -1;
    int lastSelectedIndex = -1;


    public VolumeManagerWindow(VolumeManager volumeManager) {
        this.volumeManager = volumeManager;

        lineStylePanel = new LineStylePanel(volumeManager);

        buttons = new JComponent[]{
                btnNew,
                btnDuplicate,
                btnRevertChanges,
                btnRename,
                btnDelete,
                spacer,
                btnNextKeySlice,
                btnPreviousKeySlice,
                btnNewKeySlice,
                btnDeleteKeySlice,
                spacer,
                lineStylePanel,
                spacer,
                chckbxAllowExtrapolation,
                chckbxAllowSwitch
        };

        buildUi();
    }

    private void buildUi() {
        VolumeManagerWindow vmw = this;
        System.setProperty("apple.laf.useScreenMenuBar", "false");

        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] instanceof JButton) {
                buttons[i].setToolTipText(((JButton) buttons[i]).getText());
            }
            if (buttons[i] instanceof JCheckBox) {
                buttons[i].setToolTipText(((JCheckBox) buttons[i]).getText());
            }
        }

        spacer.setBounds(0, 0, 1, 10);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                resized();
            }

        });
        setTitle("Volume manager");
        setBounds(100, 100, 337, 620);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                vmw.windowClosed();
            }
        });

        btnDuplicate.setHorizontalAlignment(SwingConstants.LEFT);

        btnDuplicate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.duplicateButtonClicked();
            }
        });
        btnDuplicate.setBounds(210, 5, 117, 29);
        btnDuplicate.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
                        /* d */"                " +
                        /* e */"                " +
						/* 0 */"           #####" +
						/* 1 */"           #   #" +
						/* 2 */"           #   #" +
						/* 3 */"           #   #" +
						/* 4 */"#####   #  #####" +
						/* 5 */"#   #    #      " +
						/* 6 */"#   # #####     " +
						/* 7 */"#   #    #      " +
						/* 8 */"#####   #  #####" +
						/* 9 */"           #   #" +
						/* a */"           #   #" +
						/* b */"           #   #" +
						/* c */"           #####" +
						/* f */"                ")));
        contentPane.add(btnDuplicate);

        btnRevertChanges.setHorizontalAlignment(SwingConstants.LEFT);

        btnRevertChanges.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {

                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.revertChangesButtonClicked();
            }
        });
        btnRevertChanges.setBounds(210, 5, 117, 29);
        btnRevertChanges.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
						/* 0 */"#               " +
						/* 1 */"#      ####     " +
						/* 2 */"#    ##    ##   " +
						/* 3 */"#  #         #  " +
						/* 4 */"# #           # " +
						/* 5 */"##            # " +
						/* 6 */"#######        #" +
						/* 7 */"               #" +
						/* 8 */"               #" +
						/* 9 */"               #" +
						/* a */"              # " +
						/* b */"              # " +
						/* c */"             #  " +
						/* d */"           ##   " +
						/* e */"       ####     " +
						/* f */"                ")));
        contentPane.add(btnRevertChanges);


        btnDelete.setHorizontalAlignment(SwingConstants.LEFT);

        btnDelete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {

                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.deleteSelectionOrAllButtonClicked();
            }
        });
        btnDelete.setBounds(210, 35, 117, 29);
        btnDelete.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
						/* 0 */"                " +
						/* 1 */"                " +
						/* 2 */"                " +
						/* 3 */"   ##########   " +
						/* 4 */"  #          #  " +
						/* 5 */"  ############  " +
						/* 6 */"   #        #   " +
						/* 7 */"   #  #  #  #   " +
						/* 8 */"   #  #  #  #   " +
						/* 9 */"   #  #  #  #   " +
						/* a */"   #  #  #  #   " +
						/* b */"   #  #  #  #   " +
						/* c */"   #  #  #  #   " +
						/* d */"    ########    " +
						/* e */"                " +
						/* f */"                ")));
        contentPane.add(btnDelete);


        btnNew.setHorizontalAlignment(SwingConstants.LEFT);

        btnNew.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {

                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.createNewButtonClicked();
            }
        });
        btnNew.setBounds(210, 95, 117, 29);
        btnNew.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
						/* 0 */"                " +
						/* 1 */"  ########      " +
						/* 2 */"  #       #     " +
						/* 3 */"  #        #    " +
						/* 4 */"  #         #   " +
						/* 5 */"  #          #  " +
						/* 6 */"  #          #  " +
						/* 7 */"  #          #  " +
						/* 8 */"  #          #  " +
						/* 9 */"  #          #  " +
						/* a */"  #          #  " +
						/* b */"  #          #  " +
						/* c */"  #          #  " +
						/* d */"  #          #  " +
						/* e */"  ############  " +
						/* f */"                ")));
        contentPane.add(btnNew);
        btnRename.setHorizontalAlignment(SwingConstants.LEFT);

        btnRename.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {

                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.renameButtonClicked();
            }
        });
        btnRename.setBounds(210, 65, 117, 29);
        btnRename.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
						/* 0 */"                " +
						/* 1 */"                " +
						/* 2 */" ####           " +
						/* 3 */" #   #          " +
						/* 4 */" #   #          " +
						/* 5 */" #   #          " +
						/* 6 */" ####           " +
						/* 7 */" ##             " +
						/* 8 */" # #            " +
						/* 9 */" #  #           " +
						/* a */" #   #          " +
						/* b */"                " +
						/* c */"        #####   " +
						/* d */"                " +
						/* e */"                " +
						/* f */"                ")));
        contentPane.add(btnRename);


        btnDeleteKeySlice.setHorizontalAlignment(SwingConstants.LEFT);

        btnDeleteKeySlice.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.deleteCurrentKeySliceButtonClicked();
            }
        });
        btnDeleteKeySlice.setBounds(230, 125, 100, 29);
        btnDeleteKeySlice.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
						/* 0 */" ########       " +
						/* 1 */" #      #       " +
						/* d */" # #  # #       " +
						/* 2 */" # # #  #       " +
						/* 3 */" # ###  #       " +
						/* 4 */" # #  # #       " +
						/* 5 */" #      #       " +
						/* 6 */" ########       " +
						/* e */"                " +
						/* 7 */"        #####   " +
						/* 8 */"       #     #  " +
						/* 9 */"        # # #   " +
						/* a */"        # # #   " +
						/* b */"        # # #   " +
						/* c */"        #####   " +
						/* f */"                ")));
        contentPane.add(btnDeleteKeySlice);
        btnNewKeySlice.setHorizontalAlignment(SwingConstants.LEFT);

        btnNewKeySlice.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                volumeManager.createNewKeySliceButtonClicked();
            }
        });
        btnNewKeySlice.setBounds(233, 155, 100, 29);
        btnNewKeySlice.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
						/* 0 */" ########       " +
						/* 1 */" #      #       " +
						/* d */" # #  # #       " +
						/* 2 */" # # #  #       " +
						/* 3 */" # ###  #       " +
						/* 4 */" # #  # #       " +
						/* 5 */" #      #       " +
						/* 6 */" ########       " +
						/* 7 */"          #     " +
						/* 8 */"          #     " +
						/* 9 */"          #     " +
						/* a */"       #######  " +
						/* b */"          #     " +
						/* c */"          #     " +
						/* d */"          #     " +
						/* f */"                ")));
        contentPane.add(btnNewKeySlice);
        btnNextKeySlice.setHorizontalAlignment(SwingConstants.LEFT);

        btnNextKeySlice.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {

                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.goToNextKeySliceButtonClicked();
            }
        });
        btnNextKeySlice.setBounds(233, 185, 100, 29);
        btnNextKeySlice.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
						/* 0 */"                " +
						/* 1 */"                " +
						/* 2 */"      #         " +
						/* 3 */"       #        " +
						/* 4 */"        #       " +
						/* 5 */"         #      " +
						/* 6 */"          #     " +
						/* 7 */"           #    " +
						/* 8 */"          #     " +
						/* 9 */"         #      " +
						/* a */"        #       " +
						/* b */"       #        " +
						/* c */"      #         " +
						/* d */"                " +
						/* e */"                " +
						/* f */"                ")));
        contentPane.add(btnNextKeySlice);


        btnPreviousKeySlice.setHorizontalAlignment(SwingConstants.LEFT);
        btnPreviousKeySlice.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.goToPreviousKeySliceButtonClicked();
            }
        });
        btnPreviousKeySlice.setBounds(233, 215, 100, 29);
        btnPreviousKeySlice.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(
                // 0123456789abcdef
						/* 0 */"                " +
						/* 1 */"                " +
						/* 2 */"          #     " +
						/* 3 */"         #      " +
						/* 4 */"        #       " +
						/* 5 */"       #        " +
						/* 6 */"      #         " +
						/* 7 */"     #          " +
						/* 8 */"      #         " +
						/* 9 */"       #        " +
						/* a */"        #       " +
						/* b */"         #      " +
						/* c */"          #     " +
						/* d */"                " +
						/* e */"                " +
						/* f */"                ")));
        contentPane.add(btnPreviousKeySlice);


        btnNextKeySlice.setEnabled(false);
        btnPreviousKeySlice.setEnabled(false);
        btnNewKeySlice.setEnabled(false);
        btnDeleteKeySlice.setEnabled(false);

        chckbxShowAll.setBounds(114, 568, 84, 23);
        contentPane.add(chckbxShowAll);
        chckbxShowAll.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                volumeManager.refresh();
            }
        });

        chckbxShowLabels.setBounds(6, 568, 117, 23);
        contentPane.add(chckbxShowLabels);
        chckbxShowLabels.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                DebugHelper.print(this, "show labels change calls refresh");
                volumeManager.refresh();
            }
        });

        chckbxAllowExtrapolation.setBounds(6, 568, 157, 23);
        chckbxAllowExtrapolation.setSelected(false);
        contentPane.add(chckbxAllowExtrapolation);
        chckbxAllowExtrapolation.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                DebugHelper.print(this, "allow extrapolation change calls refresh");
                volumeManager.refresh();

            }
        });

        cbHelp.setVisible(false);
        cbHelp.setEnabled(false);
        cbHelp.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                resized();
            }
        });
        cbHelp.setSelected(true);
        cbHelp.setBounds(314, 568, 49, 23);
        contentPane.add(cbHelp);

        scrollPane.setBounds(6, 8, 100, 72);
        contentPane.add(scrollPane);
        volumeList = new JList(volumeManager.volumeData);
        scrollPane.setViewportView(volumeList);
        volumeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                if (arg0.getValueIsAdjusting() == false) {
                    DebugHelper.print(this, "dblclicka " + volumeManager.isManipulationLocked());
                    if (volumeManager.isManipulationLocked()) {
                        return;
                    }
                    volumeManager.selectionChanged();

                }

            }
        });
        volumeList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                DebugHelper.print(this, "dblclick " + (System.currentTimeMillis() - lastTimeStamp));
                if (System.currentTimeMillis() - lastTimeStamp < 1000 && lastSelectedIndex == volumeList.getSelectedIndex() /*TODO remove magic number; it corresponds to double-click faster than a second*/) {
                    volumeManager.listDoubleClicked();
                }
                lastSelectedIndex = volumeList.getSelectedIndex();

                lastTimeStamp = System.currentTimeMillis();
            }
        });
        volumeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        chckbxAllowSwitch.setBounds(199, 568, 157, 23);
        chckbxAllowSwitch.setSelected(volumeManager.switchingLocked == 0);
        chckbxAllowSwitch.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if (volumeManager == null || volumeManager.initializing) {
                    return;
                }
                if (chckbxAllowSwitch.isSelected()) {
                    volumeManager.switchingLocked = 0;
                } else {
                    volumeManager.switchingLocked = 1;
                }

                DebugHelper.print(this, "allow switch calls refresh");
                volumeManager.refresh();
            }
        });
        contentPane.add(chckbxAllowSwitch);
        volumeList.removeAll();

        lineStylePanel.addChangeListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PolylineSurface pls = volumeManager.getCurrentVolume();
                pls.fillColor = lineStylePanel.getFillColor();
                pls.lineColor = lineStylePanel.getLineColor();
                pls.setTransparency(lineStylePanel.getTransparency());
                pls.viewInterpolatedLinesDotted = lineStylePanel.getInterpolatedLinesDotted();
                pls.setLineThickness(lineStylePanel.getLineThickness());
                volumeManager.refresh();
            }
        });

        contentPane.add(lineStylePanel);

        VolumeManagerWindow sm = this;

        final BrushPluginTool tempBrushPluginTool = new BrushPluginTool();

        ActionListener taskPerformer = new ActionListener() {
            private boolean neverResized = true;
            private ImagePlus formerImagePlus = null;

            public void actionPerformed(ActionEvent evt) {
                if (!sm.isVisible()) {
                    return;
                }

                String toolName = (Toolbar.getInstance() != null) ? Toolbar.getToolName() : "";

                if (neverResized) {
                    resized();
                    neverResized = false;
                }

                ImagePlus imp = volumeManager.getCurrentImagePlus();

                if (WindowManager.getIDList() == null || WindowManager.getIDList().length == 0) {
                    return;
                }

                try {
                    if (IJ.getImage() != imp) {
                        volumeManager.imageUpdated(IJ.getImage());
                    }
                    imp = volumeManager.getCurrentImagePlus();

                    if (imp != formerImagePlus) {
                        formerImagePlus = imp;

                        DebugHelper.print(this, "former imp change calls refresh");
                        volumeManager.refresh();
                    }
                } catch (Exception e) {
                    DebugHelper.print(this, e.getStackTrace().toString());
                }
            }
        };
        heartbeat = new Timer(delay, taskPerformer);
        heartbeat.start();
    }

    public void refreshUi() {
        resized();

        ImagePlus imp = volumeManager.getCurrentImagePlus();
        if (imp == null) {
            btnNextKeySlice.setEnabled(false);
            btnPreviousKeySlice.setEnabled(false);
            btnNewKeySlice.setEnabled(false);
            btnDeleteKeySlice.setEnabled(false);
            return;
        }

        if (chckbxAllowSwitch.isSelected() && (volumeManager.switchingLocked > 0)) {
            chckbxAllowSwitch.setSelected(false);
        }

        if ((!chckbxAllowSwitch.isSelected()) && (volumeManager.switchingLocked == 0)) {
            chckbxAllowSwitch.setSelected(true);
        }

        PolylineSurface currentVolume = volumeManager.getCurrentVolumeUnsafe();

        DebugHelper.print(this, "refresh");

        boolean somethingSelected = volumeList.getSelectedIndex() >= 0;
        if (somethingSelected && currentVolume != null) {
            boolean nextAvailable = false;
            boolean previousAvailable = false;

            for (int z = imp.getZ() - 1; z > 0; z--) {
                if (currentVolume.getRoi(z) != null) {
                    previousAvailable = true;
                    break;
                }
            }
            for (int z = imp.getZ() + 1; z <= imp.getNSlices(); z++) {
                if (currentVolume.getRoi(z) != null) {
                    nextAvailable = true;
                    break;
                }
            }

            btnNextKeySlice.setEnabled(nextAvailable);
            btnPreviousKeySlice.setEnabled(previousAvailable);

            if (currentVolume.getRoi(imp.getZ()) != null) // roi available on current slice
            {
                btnNewKeySlice.setEnabled(false);
                btnDeleteKeySlice.setEnabled(true);
            } else {
                btnNewKeySlice.setEnabled(true);
                btnDeleteKeySlice.setEnabled(false);
            }
        } else {
            btnNextKeySlice.setEnabled(false);
            btnPreviousKeySlice.setEnabled(false);
            btnNewKeySlice.setEnabled(false);
            btnDeleteKeySlice.setEnabled(false);
        }

        if (currentVolume != null) {
            lineStylePanel.blockFireEvents();
            lineStylePanel.setFillColor(currentVolume.fillColor);
            lineStylePanel.setLineColor(currentVolume.lineColor);
            lineStylePanel.setInterpolatedLinesDotted(currentVolume.viewInterpolatedLinesDotted);
            lineStylePanel.setTransparency(currentVolume.getTransparency());
            lineStylePanel.setLineThickness(currentVolume.getLineThickness());
            lineStylePanel.unblockFireEvents();

        }

        if (volumeList.getSelectedIndex() < 0) {
            btnDelete.setText("Delete all");
        } else {
            btnDelete.setText("Delete " + volumeManager.volumeData.getSurface(volumeList.getSelectedIndex()).getTitle());
        }

        boolean somethingInTheList = volumeManager.volumeData.size() > 0;

        btnDelete.setEnabled(somethingInTheList);
        btnDuplicate.setEnabled(somethingSelected);
        btnRevertChanges.setEnabled(somethingSelected);
        btnRename.setEnabled(somethingSelected);

        DebugHelper.print(this, "/refresh");
    }

    /**
     * Launch the application (for the UI-development environment).
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    VolumeManagerWindow frame = new VolumeManagerWindow(null);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ============================================================================
    // UI events
    //
    private synchronized void windowClosed() {
        if (volumeManager != null) {
            ImagePlus imp = volumeManager.getCurrentImagePlus();
            if (imp != null) {
                imp.setOverlay(null);
                imp = null;
            }
            DebugHelper.print(this, "closing...");
            // -------------------------------------------------
            // after this, we no longer listen to any open image, even to images which may be opened in the future.
            volumeManager.deactivateImageInteraction();
            volumeManager.dispose();
        }

        if (heartbeat != null) {
            heartbeat.stop();
            heartbeat = null;
        }
    }

    private void resized() {
        if (contentPane == null) {
            return;
        }

        int yPos = 0;
        for (int i = 0; i < buttons.length; i++) {

            if (!cbHelp.isSelected()) {
                if (buttons[i] instanceof JButton) {
                    ((JButton) buttons[i]).setText("");
                }
                if (buttons[i] == spacer) {
                    buttons[i].setSize(24, 10);
                } else {
                    buttons[i].setSize(24, 24);
                }
            } else {
                if (buttons[i] instanceof JButton) {
                    ((JButton) buttons[i]).setText(buttons[i].getToolTipText());
                }
                if (buttons[i] == spacer) {
                    buttons[i].setSize(150, 10);
                } else {
                    buttons[i].setSize(150, 24);
                }
            }


            buttons[i].setLocation(contentPane.getWidth() - buttons[i].getWidth(), yPos);
            yPos += buttons[i].getHeight();
        }

        if (scrollPane != null) {
            scrollPane.setBounds(0, 0, contentPane.getWidth() - buttons[0].getWidth(), contentPane.getHeight() - chckbxShowAll.getHeight());
            chckbxShowAll.setLocation(0, scrollPane.getHeight());
            chckbxShowLabels.setLocation(chckbxShowAll.getWidth(), scrollPane.getHeight());

            cbHelp.setLocation(chckbxShowLabels.getX() + chckbxShowLabels.getWidth(), scrollPane.getHeight());
        }
    }

    public int getSelectedIndex() {
        return volumeList.getSelectedIndex();
    }

    public void setSelectedIndex(int idx) {
        volumeList.setSelectedIndex(idx);
        volumeList.ensureIndexIsVisible(idx);
    }

    public boolean isExtrapolationAllowed() {
        return chckbxAllowExtrapolation.isSelected();
    }


    public void setShowingLabels(boolean showLabels) {
        chckbxShowLabels.setSelected(showLabels);
    }

    public void setShowingAll(boolean showAll) {
        chckbxShowAll.setSelected(showAll);
    }

    public boolean isShowingLabels() {
        return chckbxShowLabels.isSelected();
    }

    public boolean isShowingAll() {
        return chckbxShowAll.isSelected();
    }

    void initializeMenuBar() {
        setJMenuBar(new JMenuBar());
    }

    /**
     * This function allows to add a plugin to volume managers menu structure. However is deprecated as
     * this should be done using the service/plugin concept of ImageJ/SciJava. If you want to make a plugin for the
     * volume manager, derive it from AbstractVolumeManagerPlugin class and use the @plugin annotation.
     *
     * @param mainMenuText name of the main menu entry
     * @param subMenuText  name of the sub menu entry
     * @param plugin       the plugin to add
     * @return true if the operation succeeded
     */
    @Deprecated
    public boolean addPlugin(String mainMenuText, String subMenuText, AbstractVolumeManagerPlugin plugin) {
        if (mainMenuText == null || mainMenuText.length() == 0) {
            DebugHelper.print(this, "Cannot insert menu, no main menu given");
            return false;
        }

        if (getJMenuBar() == null) {
            initializeMenuBar();
        }


        JMenuItem menuItemToInsert = plugin.getMenuItem();

        JMenu menuToAddTo = null;

        JMenuBar menuBar = getJMenuBar();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu.getText().trim().equals(mainMenuText.trim())) {
                menuToAddTo = menu;
                break;
            }
        }

        if (menuToAddTo == null) {
            menuToAddTo = new JMenu(mainMenuText);
            menuBar.add(menuToAddTo);
        }

        if (subMenuText != null && subMenuText.length() > 0) {
            JMenu mainMenu = menuToAddTo;
            for (int i = 0; i < mainMenu.getItemCount(); i++) {
                JMenuItem menuItem = mainMenu.getItem(i);
                if (menuItem instanceof JMenu && menuItem.getText().trim().equals(subMenuText.trim())) {
                    menuToAddTo = (JMenu) menuItem;
                    break;
                }
            }
            if (menuToAddTo == mainMenu) //that means, the submenu wasnt found
            {
                menuToAddTo = new JMenu(subMenuText);
                mainMenu.add(menuToAddTo);
            }
        }

        menuToAddTo.add(menuItemToInsert);
        return true;
    }

}
