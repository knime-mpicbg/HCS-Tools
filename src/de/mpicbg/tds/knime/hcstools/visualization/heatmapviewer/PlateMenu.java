package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.MinMaxStrategy;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.LinearGradientTools;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.QuantileStrategy;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import org.knime.core.data.RowKey;
import org.knime.core.node.property.hilite.HiLiteHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;

/**
 * Author: Felix Meyenhofer
 * Date: 18/12/12
 *
 * Menu for the PlateViewer
 */

public class PlateMenu extends JMenuBar {

    protected HeatMapModel2 heatMapModel;
    protected HeatMapViewer window;
    protected JMenuItem colorMenu;


    /**
     * Constructors
     */
    public PlateMenu() {
        this(null);
    }

    public PlateMenu(HeatMapViewer parent) {
        this.window = parent;
        if (parent == null) {
            configure(new HeatMapModel2());
        } else {
            configure(parent.getHeatMapModel());
            JMenuBar parentMenu = parent.getDefaultMenu();
            if (parentMenu != null)
                for (int i=0; i<parentMenu.getMenuCount(); i++)
                    this.add(parentMenu.getMenu(i));
        }

        this.add(createHiLiteMenu());
        this.add(createViewMenu());
    }


    /**
     * Self configuration (overwritten by sub-classes)
     * @param model parent window
     */
    protected void configure(HeatMapModel2 model) {
        if (model != null) {
            this.heatMapModel = model;
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
                alwaysOnTopAction(e);
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
        colorMenu = menu.add(createColorMapMenu());
        colorMenu.setEnabled(!heatMapModel.isGlobalScaling());
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
        JMenuItem item = menu.add(HiLiteHandler.HILITE_SELECTED);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                hiLiteAction();
            }
        });
        item = menu.add(HiLiteHandler.UNHILITE_SELECTED);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                unHiLiteAction();
            }
        });
        item = menu.add(HiLiteHandler.CLEAR_HILITE);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearHiLiteAction();
            }
        });

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
                heatMapModel.setReadoutRescaleStrategy(new MinMaxStrategy());
            } else if ( menuItem.getText().equals("Smoothed") ) {
                heatMapModel.setReadoutRescaleStrategy(new QuantileStrategy());
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

    protected void alwaysOnTopAction(ActionEvent e) {
        JMenuItem item = (JMenuItem) e.getSource();
        JFrame frame = (JFrame) getTopLevelAncestor();
        frame.setAlwaysOnTop(item.isSelected());
    }

    private void unHiLiteAction() {
        Collection<Well> selection = heatMapModel.getWellSelection();
        heatMapModel.removeHilLites(selection);
        heatMapModel.fireModelChanged();

        HashSet<RowKey> keys = new HashSet<RowKey>();
        for (Well well : selection) {
            keys.add(well.getKnimeTableRowKey());
        }

        if (window.getNodeModel() == null) {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        } else {
            window.getNodeModel().getInHiLiteHandler(0).fireUnHiLiteEvent(keys); //TODO: put the port number in a variable in the nodemodel
        }
    }

    private void clearHiLiteAction() {
        heatMapModel.clearHiLites();
        heatMapModel.fireModelChanged();

        if (window.getNodeModel() == null) {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        } else {
            window.getNodeModel().getInHiLiteHandler(0).fireClearHiLiteEvent(); //TODO: put the port number in a variable in the nodemodel
        }
    }

    private void hiLiteAction() {
        Collection<Well> selection = heatMapModel.getWellSelection();
        heatMapModel.addHilLites(selection);
        heatMapModel.fireModelChanged();

        HashSet<RowKey> keys = new HashSet<RowKey>();
        for (Well well : selection) {
            keys.add(well.getKnimeTableRowKey());
        }

        if (window.getNodeModel() == null) {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        } else {
            window.getNodeModel().getInHiLiteHandler(0).fireHiLiteEvent(keys); //TODO: put the port number in a variable in the nodemodel
        }
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
