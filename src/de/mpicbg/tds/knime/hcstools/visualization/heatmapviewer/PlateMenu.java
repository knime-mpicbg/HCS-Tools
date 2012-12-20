package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.GlobalMinMaxStrategy;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.LinearGradientTools;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.QuantileSmoothedStrategy;
import org.knime.core.node.property.hilite.HiLiteHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Author: Felix Meyenhofer
 * Date: 18/12/12
 *
 * Menu for the PlateViewer
 */

public class PlateMenu extends JMenuBar {

    protected HeatMapModel2 heatMapModel;
    protected HeatMapViewer window;


    /**
     * Constructors
     */
    public PlateMenu() {
        this(null);
    }

    public PlateMenu(HeatMapViewer parent) {
        configure(parent);

        this.add(createHiLiteMenu());
        this.add(createViewMenu());
    }


    /**
     * Self configuration (overwritten by sub-classes)
     * @param parent parent window
     */
    private void configure(HeatMapViewer parent) {
        if (parent != null) {
            this.window = parent;
            this.heatMapModel = parent.getHeatMapModel();
        } else {
            this.heatMapModel = new HeatMapModel2();
        }
    }


    /**
     * Methods for menu creation
     */
    protected ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    protected JMenu createViewMenu() {
        JMenu menu = new JMenu("View");

        JCheckBoxMenuItem alwaysOnTop = new JCheckBoxMenuItem("Always on Top");
        alwaysOnTop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                alwaysOnTopAction(item);
            }
        });
        menu.add(alwaysOnTop);

        JCheckBoxMenuItem markSelection = new JCheckBoxMenuItem("Mark Selection");
        markSelection.setSelected(heatMapModel.doMarkSelection());
        markSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                markSelectionAction(actionEvent);
            }
        });
        menu.add(markSelection);
        menu.add(createOverlaySubMenu());
        menu.add(createOutlierSubMenu());
        menu.add(createColorMapMenu());
        menu.add(createToolBarMenu());

        return menu;
    }

    protected JMenu createOverlaySubMenu() {
        JMenu menu = new JMenu("Overlay");

        JMenuItem overlayLegend = menu.add("Show Legend");
        overlayLegend.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.META_MASK));
        overlayLegend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showOverlayLegendAction();
            }
        });

        JCheckBoxMenuItem overlayHider = new JCheckBoxMenuItem("Hide Most Frequent");
        overlayHider.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                hideOverlayAction(actionEvent);
            }
        });
        menu.add(overlayHider);

        return menu;
    }

    protected JMenu createOutlierSubMenu() {
        JMenu menu = new JMenu("Outlier Handling");
        ButtonGroup group = new ButtonGroup();

        for (String name : new String[]{"Original", "Smoothed"}) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
            group.add(item);
            menu.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    outlierHandlingAction(actionEvent);
                }
            });
        }
        menu.getItem(0).setSelected(true);

        return menu;
    }

    protected JMenu createHiLiteMenu() {
        JMenu menu = new JMenu((HiLiteHandler.HILITE));
        menu.add(HiLiteHandler.HILITE_SELECTED);
        menu.add(HiLiteHandler.UNHILITE_SELECTED);
        menu.add(HiLiteHandler.CLEAR_HILITE);
        menu.add(createHiLiteFilterMenu());
        return menu;
    }

    protected JMenu createHiLiteFilterMenu() {
        JMenu menu = new JMenu("Filter");
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem[] item = new JRadioButtonMenuItem[3];
        item[0] = new JRadioButtonMenuItem("Show All");
        item[0].setSelected(true);
        item[1] = new JRadioButtonMenuItem("Show HiLite Only");
        item[2] = new JRadioButtonMenuItem("Show UnHiLite Only");

        for (JRadioButtonMenuItem anItem : item) {
            menu.add(anItem);
            group.add(anItem);
        }

        return menu;
    }

    protected JMenu createColorMapMenu() {
        JMenu lut = new JMenu("Colormap");
        ButtonGroup group = new ButtonGroup();
        String[] names = {"GB", "GBR", "HSV", "Jet", "Dark", "Custom"};
        JRadioButtonMenuItem[] item = new JRadioButtonMenuItem[names.length];

        for (int i = 0; i < names.length; i++) {
            ImageIcon icon = createImageIcon("icons/" + names[i] + ".gif", names[i] + "color map");
            item[i] = new JRadioButtonMenuItem(names[i],icon);
            group.add(item[i]);
            lut.add(item[i]);
            item[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    toggleColorMapAction(actionEvent);
                }
            });
        }
        item[0].setSelected(true);

        return lut;
    }

    protected JMenu createToolBarMenu() {
        JMenu toolbar = new JMenu("Toolbars");
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show Toolbar");
        item.setSelected(true);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JCheckBoxMenuItem mi = (JCheckBoxMenuItem) actionEvent.getSource();
                window.toggleToolbarVisibility(mi.isSelected());
            }
        });
        toolbar.add(item);
        item = new JCheckBoxMenuItem("Show Colorbar");
        item.setSelected(true);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JCheckBoxMenuItem mi = (JCheckBoxMenuItem) actionEvent.getSource();
                window.toggleColorbarVisibility(mi.isSelected());
            }
        });
        toolbar.add(item);
        return toolbar;
    }


   /**
    * Actions
    */
    protected void toggleColorMapAction(ActionEvent actionEvent) {
        JMenuItem source = (JMenuItem)actionEvent.getSource();
        LinearGradientPaint newGradient;
        if (source.getText().equals("Custom")) {
            ColorGradientDialog dialog = new ColorGradientDialog(heatMapModel);
            dialog.setVisible(true);
            newGradient = dialog.getGradientPainter();
        } else {
            newGradient = LinearGradientTools.getStandardGradient(source.getText());
        }
        heatMapModel.setColorGradient(newGradient);
        heatMapModel.fireModelChanged();
    }

    protected void showOverlayLegendAction() {
        Container parentContainer = HeatWell.getParentContainer(this);

        OverlayLegend overlayLegend;
        if (parentContainer instanceof Dialog) {
            overlayLegend = new OverlayLegend((Dialog) parentContainer);
        } else {
            overlayLegend = new OverlayLegend((Frame) parentContainer);
        }

        overlayLegend.setModel(heatMapModel);
        overlayLegend.setModal(false);
        overlayLegend.setVisible(true);
    }

    protected void hideOverlayAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
        heatMapModel.setHideMostFreqOverlay(item.isSelected());
        heatMapModel.fireModelChanged();
    }

    protected void outlierHandlingAction(ActionEvent event) {
        JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) event.getSource();
        if ( menuItem.isSelected() ) {
            if ( menuItem.getText().equals("Original") ) {
                heatMapModel.setReadoutRescaleStrategy(new GlobalMinMaxStrategy());
            } else if ( menuItem.getText().equals("Smoothed") ) {
                heatMapModel.setReadoutRescaleStrategy(new QuantileSmoothedStrategy());
            } else {
                System.err.println("Don't know the option " + menuItem.getName() + ".");
            }
            heatMapModel.fireModelChanged();
        }
    }

    protected void markSelectionAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
        heatMapModel.setMarkSelection(item.isSelected());
        heatMapModel.fireModelChanged();
    }

    protected void alwaysOnTopAction(JMenuItem menuItem) {
        JFrame frame = (JFrame) getTopLevelAncestor();
        frame.setAlwaysOnTop(menuItem.isSelected());
    }


    /**
     * Testing
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        JTextArea text = new JTextArea("This is some text");
        text.setEnabled(true);
        text.setEditable(false);
        panel.add(text);
        frame.setContentPane(panel);
        frame.setJMenuBar(new PlateMenu());
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
