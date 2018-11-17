package de.mpicbg.scf.volumemanager.core;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.ImageJUtilities;
import de.mpicbg.scf.imgtools.ui.JExtendedButton;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.manipulation.colors.CopyLineColorToFillColorPlugin;
import de.mpicbg.scf.volumemanager.plugins.manipulation.colors.MakeVolumesTransparentPlugin;
import de.mpicbg.scf.volumemanager.plugins.manipulation.colors.RandomColorsDistributorPlugin;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * The LineStylePanel allows the user to configure line color, fill color,
 * line thickness and filling transparency of volumes.
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: April 2017
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
public class LineStylePanel extends JPanel {

    VolumeManager volumeManager;

    Color lineColor = Color.red;
    Color fillColor = Color.green;

    double thickness = 1;
    double transparency = 0;

    boolean dotted = true;

    JPanel[] lineColorButtons = {new JPanel(), new JPanel(), new JPanel(), new JPanel()};

    JPanel fillColorButton = new JPanel();
    JExtendedButton lineDottedButton = new JExtendedButton("");
    JComboBox<Integer> thicknessComboBox = new JComboBox<>();
    JComboBox<Integer> transparencyComboBox = new JComboBox<>();

    public LineStylePanel(VolumeManager volumeManager) {
        this.volumeManager = volumeManager;
        buildUi();
        refreshUi();
    }


    private void buildUi() {
        setLayout(null);


        fillColorButton.setSize(14, 14);
        fillColorButton.setLocation(5, 5);
        fillColorButton.setToolTipText("Fill color");
        fillColorButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fillColor = JColorChooser.showDialog(null, "Choose fill color", fillColor);
                refreshUi();
                fireChangeEvent();
            }
        });
        this.add(fillColorButton);


        // colour configuration
        lineColorButtons[0].setSize(24, 5);
        lineColorButtons[0].setLocation(0, 0);
        lineColorButtons[1].setSize(24, 5);
        lineColorButtons[1].setLocation(0, 19);
        lineColorButtons[2].setSize(5, 14);
        lineColorButtons[2].setLocation(0, 5);
        lineColorButtons[3].setSize(5, 14);
        lineColorButtons[3].setLocation(19, 5);
        for (int i = 0; i < 4; i++) {
            lineColorButtons[i].setToolTipText("Line color");
            lineColorButtons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    lineColor = JColorChooser.showDialog(null, "Choose line color", lineColor);
                    refreshUi();
                    fireChangeEvent();
                }
            });
            this.add(lineColorButtons[i]);
        }


        JExtendedButton moreButton = new JExtendedButton("");
        moreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(newMenuItem("Make all volumes as transparent as this one", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        PolylineSurface currentPls = volumeManager.getCurrentVolumeUnsafe();
                        if (currentPls == null) {
                            return;
                        }

                        for (int i = 0; i < volumeManager.length(); i++) {
                            PolylineSurface pls = volumeManager.getVolume(i);
                            pls.setTransparency(currentPls.getTransparency());
                        }
                        volumeManager.refresh();
                        refreshUi();
                    }
                }, false));
                menu.add(new JPopupMenu.Separator());

                menu.add(newMenuItem("Make all volumes fill color equal to their line color", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new CopyLineColorToFillColorPlugin(volumeManager).run();
                        refreshUi();
                    }
                }, false));
                menu.add(newMenuItem("Make all volumes colored randomly", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new RandomColorsDistributorPlugin(volumeManager).run();
                        refreshUi();
                    }
                }, false));
                menu.add(newMenuItem("Remove fill colors from all volumes", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new MakeVolumesTransparentPlugin(volumeManager).run();
                        refreshUi();
                    }
                }, false));
                menu.add(new JPopupMenu.Separator());
                menu.add(newMenuItem("Show all volumes interpolated lines solid", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        for (int i = 0; i < volumeManager.length(); i++) {
                            PolylineSurface pls = volumeManager.getVolume(i);
                            pls.viewInterpolatedLinesDotted = false;
                        }
                        volumeManager.refresh();
                        refreshUi();
                    }
                }, false));
                menu.add(newMenuItem("Show all volumes interpolated lines dotted", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        for (int i = 0; i < volumeManager.length(); i++) {
                            PolylineSurface pls = volumeManager.getVolume(i);
                            pls.viewInterpolatedLinesDotted = true;
                        }
                        volumeManager.refresh();
                        refreshUi();
                    }
                }, false));

                menu.show(moreButton, 0, moreButton.getHeight());
            }
        });
        moreButton.setSize(25, 25);
        moreButton.setLocation(125, 0);
        moreButton.setIcon(new ImageIcon(ImageJUtilities.getImageFromString("" +
                // 0123456789abcdef
                        /* c */"                " +
                        /* d */"                " +
                        /* e */"                " +
						/* 0 */"                " +
						/* 1 */"                " +
						/* 2 */"                " +
						/* 3 */"                " +
						/* 4 */"                " +
						/* 5 */"                " +
						/* 6 */"                " +
						/* 7 */"                " +
						/* 8 */" ###  ###  ###  " +
						/* 9 */" ###  ###  ###  " +
						/* a */" ###  ###  ###  " +
						/* b */"                " +
						/* f */"                ")));
        add(moreButton);

        transparencyComboBox.setSize(70, 28);
        transparencyComboBox.setLocation(80, 0);
        transparencyComboBox.setToolTipText("Transparency");
        transparencyComboBox.addItem(0);
        transparencyComboBox.addItem(25);
        transparencyComboBox.addItem(50);
        transparencyComboBox.addItem(75);
        transparencyComboBox.addItem(100);
        transparencyComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new JButton() {
                    @Override
                    public int getWidth() {
                        return 0;
                    }
                };
            }
        });
        transparencyComboBox.setRenderer(new TransparencyComboBoxRenderer());
        transparencyComboBox.setToolTipText("Transparency");
        transparencyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transparency = 0.01 * ((Integer) transparencyComboBox.getSelectedItem());
                fireChangeEvent();
            }
        });
        this.add(transparencyComboBox);


        lineDottedButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                dotted = !dotted;
                fireChangeEvent();
            }
        });
        lineDottedButton.setSize(25, 25);
        lineDottedButton.setLocation(50, 0);

        this.add(lineDottedButton);

        thicknessComboBox.setSize(30, 28);
        thicknessComboBox.setLocation(28, 0);
        thicknessComboBox.addItem(1);
        thicknessComboBox.addItem(2);
        thicknessComboBox.addItem(3);
        thicknessComboBox.addItem(5);
        thicknessComboBox.addItem(10);
        thicknessComboBox.addItem(15);
        thicknessComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new JButton() {
                    @Override
                    public int getWidth() {
                        return 0;
                    }
                };
            }
        });
        thicknessComboBox.setRenderer(new ThicknessComboBoxRenderer());
        thicknessComboBox.setToolTipText("Line thickness");
        thicknessComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thickness = ((Integer) thicknessComboBox.getSelectedItem());
                fireChangeEvent();
            }
        });
        this.add(thicknessComboBox);
    }

    private JMenuItem newMenuItem(String text, ActionListener actionListener, boolean checked) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(actionListener);
        item.setSelected(checked);
        return item;
    }

    class ThicknessComboBoxRenderer extends JLabel
            implements ListCellRenderer {

        public ThicknessComboBoxRenderer() {

        }

        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            return thicknesItem((Integer) value);
        }

        private JLabel thicknesItem(int width) {
            JLabel label = new JLabel();


            String iconString = "";

            int spacerTop = (16 - width) / 2;
            int spacerBottom = 16 - width - spacerTop;

            for (int i = 0; i < spacerTop; i++) {
                iconString = iconString.concat("                ");
            }
            for (int i = 0; i < width; i++) {
                iconString = iconString.concat("################");
            }
            for (int i = 0; i < spacerBottom; i++) {
                iconString = iconString.concat("                ");
            }

            label.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(iconString)));

            return label;
        }
    }


    class TransparencyComboBoxRenderer extends JLabel
            implements ListCellRenderer {

        public TransparencyComboBoxRenderer() {

        }

        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            return transparencyItem((Integer) value, isSelected);
        }

        private JLabel transparencyItem(int transparency, boolean isSelected) {
            JLabel label = new JLabel();

            String[] possibleElements = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
            String element = possibleElements[(100 - transparency) * (possibleElements.length - 1) / 100];

            String iconString = "";

            for (int i = 0; i < 256; i++) {
                iconString = iconString.concat(element);
            }

            label.setIcon(new ImageIcon(ImageJUtilities.getImageFromString(iconString)));
            if (isSelected) {
                label.setText("" + transparency + "% transparent");
            } else {
                label.setText("" + transparency + "%");
            }
            return label;
        }
    }


    private synchronized void refreshUi() {
        fireEventsBlocked = true;
        for (int i = 0; i < 4; i++) {
            lineColorButtons[i].setOpaque(lineColor != null);
            lineColorButtons[i].setBackground(lineColor);
        }

        fillColorButton.setOpaque(fillColor != null);
        fillColorButton.setBackground(fillColor);

        lineDottedButton.setSelected(dotted);
        if (dotted) {
            lineDottedButton.setToolTipText("Interpolated lines appear dotted");
            lineDottedButton.setIcon(new ImageIcon(ImageJUtilities.getImageFromString("" +
                    // 0123456789abcdef
                        /* c */"                " +
                        /* d */"##              " +
						/* e */"###             " +
						/* 0 */" ##             " +
						/* 1 */"                " +
						/* 2 */"                " +
						/* 3 */"     ##         " +
						/* 4 */"     ###        " +
						/* 5 */"      ##        " +
						/* 6 */"                " +
						/* 7 */"                " +
						/* 8 */"          ##    " +
						/* 9 */"          ###   " +
						/* a */"           ##   " +
						/* b */"                " +
						/* f */"                ")));
        } else {
            lineDottedButton.setToolTipText("Interpolated lines appear solid");
            lineDottedButton.setIcon(new ImageIcon(ImageJUtilities.getImageFromString("" +
                    // 0123456789abcdef
                        /* c */"                " +
                        /* d */"##              " +
						/* e */"###             " +
						/* 0 */" ###            " +
						/* 1 */"  ###           " +
						/* 2 */"   ###          " +
						/* 3 */"    ###         " +
						/* 4 */"     ###        " +
						/* 5 */"      ###       " +
						/* 6 */"       ###      " +
						/* 7 */"        ###     " +
						/* 8 */"         ###    " +
						/* 9 */"          ###   " +
						/* a */"           ##   " +
						/* b */"                " +
						/* f */"                ")));
        }

        for (int i = 0; i < transparencyComboBox.getItemCount(); i++) {
            if (transparencyComboBox.getItemAt(i) >= transparency * 100) {
                transparencyComboBox.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < thicknessComboBox.getItemCount(); i++) {
            if (thicknessComboBox.getItemAt(i) >= thickness) {
                thicknessComboBox.setSelectedIndex(i);
                break;
            }
        }

        fireEventsBlocked = false;
    }

    private ArrayList<ActionListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ActionListener a) {
        changeListeners.add(a);
    }

    public void removeChangeListener(ActionListener a) {
        changeListeners.remove(a);
    }

    private boolean fireEventsBlocked = false;

    private void fireChangeEvent() {
        if (fireEventsBlocked) {
            return;
        }
        for (ActionListener a : changeListeners) {
            a.actionPerformed(null);
        }
    }

    public double getLineThickness() {
        return thickness;
    }

    public void setLineThickness(double thickness) {
        this.thickness = thickness;
        refreshUi();
    }

    public double getTransparency() {
        return transparency;
    }

    public void setTransparency(double transparency) {
        if (transparency > 1.0) {
            transparency = 1;
        }
        if (transparency < 0.0) {
            transparency = 0;
        }
        this.transparency = transparency;
        refreshUi();
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        refreshUi();
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
        refreshUi();
    }

    public boolean getInterpolatedLinesDotted() {
        return dotted;
    }

    public void setInterpolatedLinesDotted(boolean dotted) {
        this.dotted = dotted;
    }

    public void blockFireEvents() {
        fireEventsBlocked = true;
    }

    public void unblockFireEvents() {
        fireEventsBlocked = false;
    }

}
