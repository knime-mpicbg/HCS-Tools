package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import org.knime.core.node.property.hilite.HiLiteHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * User: Felix Meyenhofer
 * Date: 10/11/12
 * Time: 20:45
 * To change this template use File | Settings | File Templates.
 *
 * This Class was created to solve the name problem of the KeyEvent class
 * (existing in java.awt.event and org.knime.core.node.property.hilite)
 */

public class HeatMapMenu extends JMenuBar implements ActionListener, ItemListener {

    private String SORT_PLATES = "Sort Plates";
    private String ZOOM_IN = "Zoom in";
    private String ZOOM_OUT = "Zoom out";
    private String MAP_HSV = "hsv";
    private String MAP_DARK = "dark";
    private String MAP_GB = "gb";
    private String MAP_GBR = "gbr";
    private String MAP_JET = "jet";
    private String MAP_CUSTOM = "Custom";
    private String ROWS_COLUMNS = "Rows/Columns";
    private String ALWAYS_ON_TOP = "Always on Top";
    private String MARK_SELECTION = "Mark Selection";
    private String SHOW_LEGEND = "Show Legend";
    private String HILITE_SHOW_ALL = "Show All";
    private String HILITE_SHOW_HILITE = "Show HiLite Only";
    private String HILITE_SHOW_UNHILITE = "Show UnHiLite Only";

    JCheckBoxMenuItem alwaysontop;
    JCheckBoxMenuItem markseleciton;
    JMenuItem overlaylegend;
    JMenuItem zoomin;
    JMenuItem zoomout;
    JMenuItem rowscolumns;
    JMenuItem sortplates;

    HeatMapModel heatMapModel;


    //Constructor
    public HeatMapMenu() {
        add(createHiLiteMenu());
        add(createViewMenu());
        add(createTrellisMenu());
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
                alwaysOnTopAction();
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

        overlaylegend = menu.add(SHOW_LEGEND);
        overlaylegend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showOverlayLegendAction();
            }
        });

        return menu;
    }

    private JMenu createTrellisMenu() {
        JMenu menu = new JMenu("Trellis");
        
        zoomin = new JMenuItem(ZOOM_IN, KeyEvent.VK_T);
        zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
        zoomin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoomInAction();
            }
        });
        menu.add(zoomin);
        
        zoomout = new JMenuItem(ZOOM_OUT);
        zoomout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
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
        JRadioButtonMenuItem[] item = new JRadioButtonMenuItem[3];
        item[0] = new JRadioButtonMenuItem(HILITE_SHOW_ALL);
        item[1] = new JRadioButtonMenuItem(HILITE_SHOW_HILITE);
        item[2] = new JRadioButtonMenuItem(HILITE_SHOW_UNHILITE);

        for (JRadioButtonMenuItem anItem : item) {
            menu.add(anItem);
            anItem.addItemListener(this);
        }

        return menu;
    }

    private JMenu createColorMapMenu() {
        JMenu lut = new JMenu("Colormap");
        ImageIcon icon;
        JMenuItem[] item = new JMenuItem[6];
        icon = createImageIcon("icons/"+ MAP_GB +".gif", MAP_GB +"color map");
        item[0] = lut.add(new JMenuItem(MAP_GB, icon));
        icon = createImageIcon("icons/"+ MAP_GBR +".gif", MAP_GBR +"color map");
        item[1] = lut.add(new JMenuItem(MAP_GBR, icon));
        icon = createImageIcon("icons/"+ MAP_DARK +".gif", MAP_DARK +"color map");
        item[2] = lut.add(new JMenuItem(MAP_DARK, icon));
        icon = createImageIcon("icons/"+ MAP_JET +".gif", MAP_JET +"color map");
        item[3] = lut.add(new JMenuItem(MAP_JET, icon));
        icon = createImageIcon("icons/"+ MAP_HSV +".gif", MAP_HSV +"color map");
        item[4] = lut.add(new JMenuItem(MAP_HSV, icon));
        lut.add(new JSeparator());
        item[5] = lut.add(MAP_CUSTOM);

        for (JMenuItem anItem : item) {
            anItem.addActionListener(this);
        }

        return lut;
    }

    //////////////////////////////////////
    // Actions
    //////////////////////////////////////
    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void showOverlayLegendAction() {
        Container parentContainer = HeatWellPanel.getParentContainer(this);

        OverlayLegendDialog overlayLegendDialog;
        if (parentContainer instanceof Dialog) {
            overlayLegendDialog = new OverlayLegendDialog((Dialog) parentContainer);
        } else {
            overlayLegendDialog = new OverlayLegendDialog((Frame) parentContainer);
        }

        overlayLegendDialog.setModel(heatMapModel);
        overlayLegendDialog.setModal(false);
        overlayLegendDialog.setVisible(true);
    }

    private void markSelectionAction() {
        heatMapModel.setShowSelection(markseleciton.isSelected());
    }

    private void alwaysOnTopAction() {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void sortPlatesAction() {
        WellPropertyDialog dialog = new WellPropertyDialog();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void rowsColumnsAction() {
        RowColumnDialog dialog = new RowColumnDialog();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void zoomOutAction() {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void zoomInAction() {
    }

    // The somewhat particular color submenu.
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JMenuItem source = (JMenuItem)actionEvent.getSource();
        if (source.getText().equals(MAP_DARK)) {

        } else if (source.getText().equals(MAP_HSV)) {

        } else if (source.getText().equals(MAP_JET)) {

        } else if (source.getText().equals(MAP_GB)) {

        } else if (source.getText().equals(MAP_GBR)) {

        } else if (source.getText().equals(MAP_CUSTOM)) {
            ColorGradientDialog dialog = new ColorGradientDialog();
            dialog.pack();
            dialog.setVisible(true);
        } else if (source.getText().equals(SHOW_LEGEND)) {

        }
    }

    
    //////////////////////////////////////
    // Testing
    //////////////////////////////////////
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