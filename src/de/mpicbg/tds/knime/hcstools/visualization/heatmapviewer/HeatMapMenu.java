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

public class HeatMapMenu extends JMenuBar implements ActionListener, ItemListener {

    private String SORT_PLATES = "Sort Plates";
    private String ZOOM_IN = "Zoom in";
    private String ZOOM_OUT = "Zoom out";
    private String MAP_HSV = "HSV";
    private String MAP_DARK = "Dark";
    private String MAP_GB = "GB";
    private String MAP_GBR = "GBR";
    private String MAP_JET = "Jet";
    private String MAP_CUSTOM = "Custom";
    private String ROWS_COLUMNS = "Rows/Columns";
    private String ALWAYS_ON_TOP = "Always on Top";
    private String MARK_SELECTION = "Mark Selection";
    private String OUTLIER_HANDLING = "Outlier Handling";
    private String OUTLIER_HANDLING_ORIGINAL = "Original";
    private String OUTLIER_HANDLING_SMOOTHED = "Smooth";
    private String OVERLAY = "Overlay";
    private String OVERLAY_SHOW_LEGEND = "Show Legend";
    private String OVERLAY_HIDE_MOST_FREQUENT = "Hide Most Frequent";
    private String HILITE_SHOW_ALL = "Show All";
    private String HILITE_SHOW_HILITE = "Show HiLite Only";
    private String HILITE_SHOW_UNHILITE = "Show UnHiLite Only";

    JCheckBoxMenuItem alwaysontop;
    JCheckBoxMenuItem markseleciton;
    JMenuItem overlaylegend;
    JCheckBoxMenuItem overlayhider;
    JMenuItem zoomin;
    JMenuItem zoomout;
    JMenuItem rowscolumns;
    JMenuItem sortplates;

    HeatMapModel2 heatMapModel;
    ScreenViewer.ScreenHeatMapsPanel heatMapsPanel;


    //Constructors
    public HeatMapMenu() {
        add(createHiLiteMenu());
        add(createViewMenu());
        add(createTrellisMenu());
    }

    public HeatMapMenu(ScreenViewer.ScreenHeatMapsPanel actOn) {
        this();
        heatMapsPanel = actOn;
        heatMapModel = actOn.heatMapModel;
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

        alwaysontop = new JCheckBoxMenuItem(ALWAYS_ON_TOP);
        alwaysontop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                alwaysOnTopAction(item);
            }
        });
        menu.add(alwaysontop);

        markseleciton = new JCheckBoxMenuItem(MARK_SELECTION);
        markseleciton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                markSelectionAction();
            }
        });
        menu.add(markseleciton);

        menu.add(createOverlaySubMenu());

        menu.add(createOutlierSubMenu());

        menu.add(createToolBarMenu());

        return menu;
    }

    private JMenu createOverlaySubMenu() {
        JMenu menu = new JMenu(OVERLAY);

        overlaylegend = menu.add(OVERLAY_SHOW_LEGEND);
        overlaylegend.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.META_MASK));
        overlaylegend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showOverlayLegendAction();
            }
        });

        overlayhider = new JCheckBoxMenuItem(OVERLAY_HIDE_MOST_FREQUENT);
        overlayhider.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                hideOverlayAction();
            }
        });
        menu.add(overlayhider);

        return menu;
    }

    private JMenu createOutlierSubMenu() {
        JMenu menu = new JMenu(OUTLIER_HANDLING);
        ButtonGroup group = new ButtonGroup();
        String[] list = {OUTLIER_HANDLING_ORIGINAL, OUTLIER_HANDLING_SMOOTHED};

        for (String name : list) {
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
        
        zoomin = new JMenuItem(ZOOM_IN, KeyEvent.VK_T);
        zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.META_MASK));
        zoomin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoomInAction();
            }
        });
        menu.add(zoomin);
        
        zoomout = new JMenuItem(ZOOM_OUT);
        zoomout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.META_MASK));
        zoomout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoomOutAction();
            }
        });
        menu.add(zoomout);
        
        rowscolumns = new JMenuItem(ROWS_COLUMNS);
        rowscolumns.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                rowsColumnsAction();
            }
        });
        menu.add(rowscolumns);

        sortplates = menu.add(SORT_PLATES);
        sortplates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sortPlatesAction();
            }
        });
        sortplates.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK));
        menu.add(sortplates);

        menu.add(createColorMapMenu());
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
        item[0] = new JRadioButtonMenuItem(HILITE_SHOW_ALL);
        item[0].setSelected(true);
        item[1] = new JRadioButtonMenuItem(HILITE_SHOW_HILITE);
        item[2] = new JRadioButtonMenuItem(HILITE_SHOW_UNHILITE);

        for (JRadioButtonMenuItem anItem : item) {
            menu.add(anItem);
            group.add(anItem);
            anItem.addItemListener(this);
        }

        return menu;
    }

    private JMenu createColorMapMenu() {
        JMenu lut = new JMenu("Colormap");
        ButtonGroup group = new ButtonGroup();
        String[] names = {MAP_GB, MAP_GBR, MAP_HSV, MAP_JET, MAP_DARK, MAP_CUSTOM};
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
                heatMapsPanel.toolbar.setVisible(mi.isSelected());
            }
        });
        toolbar.add(item);
        item = new JCheckBoxMenuItem("Show Colorbar");
        item.setSelected(true);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JCheckBoxMenuItem mi = (JCheckBoxMenuItem) actionEvent.getSource();
                heatMapsPanel.colorbar.setVisible(mi.isSelected());
            }
        });
        toolbar.add(item);
        return toolbar;
    }


    // Actions
    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        System.err.println("There's an ItemEvent I don't care about!!!");
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        System.err.println("There's an ActionEvent I don't care about!!!");
    }

    private void toggleColorMapAction(ActionEvent actionEvent) {
        JMenuItem source = (JMenuItem)actionEvent.getSource();
        LinearGradientPaint newGradient;
        if (source.getText().equals(MAP_CUSTOM)) {
            ColorGradientDialog dialog = new ColorGradientDialog(heatMapModel);
            dialog.setVisible(true);
            newGradient = dialog.getGradientPainter();
        } else {
            newGradient = LinearGradientTools.getStandardGradient(source.getText());
        }
        heatMapModel.setColorGradient(newGradient);
    }

    private void showOverlayLegendAction() {
        Container parentContainer = HeatWellPanel.getParentContainer(this);

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

    private void hideOverlayAction() {
        heatMapModel.setHideMostFreqOverlay(overlayhider.isSelected());
    }

    private void outlierHandlingAction(ActionEvent event) {
        JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) event.getSource();
        if ( menuItem.isSelected() ) {
            if ( menuItem.getText().equals(OUTLIER_HANDLING_ORIGINAL) ) {
                heatMapModel.setReadoutRescaleStrategy(new GlobalMinMaxStrategy());
            } else if ( menuItem.getText().equals(OUTLIER_HANDLING_SMOOTHED) ) {
                heatMapModel.setReadoutRescaleStrategy(new QuantileSmoothedStrategy());
            } else {
                System.err.println("Don't know the option " + menuItem.getName() + ".");
            }
        }
    }

    private void markSelectionAction() {
        heatMapModel.setShowSelection(markseleciton.isSelected());
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

        if (!dialog.descending.isSelected()) { heatMapModel.revertScreen(); }
        heatMapModel.fireModelChanged();
        heatMapModel.setSortAttributeSelectionByTiles(selectedAttributes);
    }

    private void rowsColumnsAction() {
        RowColumnDialog dialog = new RowColumnDialog();
        dialog.setVisible(true);
        heatMapModel.updateTrellisConfiguration(dialog.getNumberOfRows(), dialog.getNumberOfColumns(), dialog.isAutomatic());
    }

    private void zoomOutAction() {
        heatMapsPanel.zoom(0.75);
    }

    private void zoomInAction() {
        heatMapsPanel.zoom(1.25);
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
//        System.exit(0);
    }


}
