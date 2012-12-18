package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.PlateComparators;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.GlobalMinMaxStrategy;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.LinearGradientTools;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.QuantileSmoothedStrategy;
import org.knime.core.node.property.hilite.HiLiteHandler;

import javax.swing.*;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.LinearGradientPaint;
import java.awt.Frame;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 10/11/12
 * Time: 20:45
 *
 * This Class was created to solve the name problem of the KeyEvent class
 * (existing in java.awt.event and org.knime.core.node.property.hilite)
 */

public class HeatMapMenu extends JMenuBar {

    private HeatMapModel2 heatMapModel;
    private HeatTrellis heatTrellis;
    private ScreenViewer window;


    //Constructors
    public HeatMapMenu() {
        add(createHiLiteMenu());
        add(createViewMenu());
        add(createTrellisMenu());
    }

    public HeatMapMenu(ScreenViewer parent) {
        this();
        this.window = parent;
        heatTrellis = parent.getHeatTrellis();
        heatMapModel = parent.getHeatMapModel();
    }


    // Methods for menu creation
    private ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private JMenu createViewMenu() {
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
        markSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                markSelectionAction(actionEvent);
            }
        });
        menu.add(markSelection);
        menu.add(createOverlaySubMenu());
        menu.add(createOutlierSubMenu());
        menu.add(createToolBarMenu());

        return menu;
    }

    private JMenu createOverlaySubMenu() {
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

    private JMenu createOutlierSubMenu() {
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

    private JMenu createTrellisMenu() {
        JMenu menu = new JMenu("Trellis");

        JMenuItem zoomIn = new JMenuItem("Zoom in", KeyEvent.VK_T);
        zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.META_MASK));
        zoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoomInAction();
            }
        });
        menu.add(zoomIn);

        JMenuItem zoomOut = new JMenuItem("Zoom out");
        zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.META_MASK));
        zoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoomOutAction();
            }
        });
        menu.add(zoomOut);

        JMenuItem rowsColumns = new JMenuItem("Rows/Columns");
        rowsColumns.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                rowsColumnsAction();
            }
        });
        rowsColumns.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.META_MASK));
        menu.add(rowsColumns);

        JMenuItem sortPlates = menu.add("Sort Plates");
        sortPlates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sortPlatesAction();
            }
        });
        sortPlates.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
        menu.add(sortPlates);

        menu.add(createColorMapMenu());

        JCheckBoxMenuItem plateDimensions = new JCheckBoxMenuItem("Fix Plate Proportions");
        plateDimensions.setSelected(true);
        plateDimensions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                plateDimensionsAction(actionEvent);
            }
        });
        menu.add(plateDimensions);

        return menu;
    }

    private JMenu createHiLiteMenu() {
        JMenu menu = new JMenu((HiLiteHandler.HILITE));
        menu.add(HiLiteHandler.HILITE_SELECTED);
        menu.add(HiLiteHandler.UNHILITE_SELECTED);
        menu.add(HiLiteHandler.CLEAR_HILITE);
        menu.add(createHiLiteFilterMenu());
        return menu;
    }

    private JMenu createHiLiteFilterMenu() {
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

    private JMenu createColorMapMenu() {
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

    private JMenu createToolBarMenu() {
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


    // Actions
    private void toggleColorMapAction(ActionEvent actionEvent) {
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
    }

    private void showOverlayLegendAction() {
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

    private void hideOverlayAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
        heatMapModel.setHideMostFreqOverlay(item.isSelected());
    }

    private void outlierHandlingAction(ActionEvent event) {
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

    private void markSelectionAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
        heatMapModel.setShowSelection(item.isSelected());
    }

    private void alwaysOnTopAction(JMenuItem menuItem) {
        JFrame frame = (JFrame) getTopLevelAncestor();
        frame.setAlwaysOnTop(menuItem.isSelected());
    }

    private void sortPlatesAction() {
        PlateAttributeDialog dialog = new PlateAttributeDialog(heatMapModel);
        dialog.setVisible(true);
        String[] selectedAttributes = dialog.getSelectedAttributeTitles();
        List<String> inverted = Arrays.asList(selectedAttributes);
        Collections.reverse(inverted);
        for (String key : inverted) {
            PlateComparators.PlateAttribute attribute = PlateComparators.getPlateAttributeByTitle(key);
            heatMapModel.sortPlates(attribute);
        }

        if (!dialog.isDescending()) { heatMapModel.revertScreen(); }
        heatMapModel.fireModelChanged();
        heatMapModel.setSortAttributeSelectionByTiles(selectedAttributes);
    }

    private void rowsColumnsAction() {
        RowColumnDialog dialog = new RowColumnDialog(heatMapModel);
        dialog.setVisible(true);
        heatMapModel.updateTrellisConfiguration(dialog.getNumberOfRows(), dialog.getNumberOfColumns(), dialog.isAutomatic());
        heatMapModel.fireModelChanged();
    }

    private void zoomOutAction() {
        heatTrellis.zoom(0.75);
    }

    private void zoomInAction() {
        heatTrellis.zoom(1.25);
    }

    private void plateDimensionsAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
        heatMapModel.setPlateProportionMode(item.isSelected());
        heatMapModel.fireModelChanged();
    }


    // Testing
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        JTextArea text = new JTextArea("This is some text");
        text.setEnabled(true);
        text.setEditable(false);
        panel.add(text);
        frame.setContentPane(panel);
        frame.setJMenuBar(new HeatMapMenu());
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


}
