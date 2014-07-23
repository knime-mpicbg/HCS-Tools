package de.mpicbg.knime.hcs.base.heatmap.menu;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.HeatMapViewer;
import de.mpicbg.knime.hcs.base.heatmap.LegendViewer;
import de.mpicbg.knime.hcs.base.heatmap.PlateViewer;
import de.mpicbg.knime.hcs.base.heatmap.color.LinearGradientTools;
import de.mpicbg.knime.hcs.base.heatmap.color.MinMaxStrategy;
import de.mpicbg.knime.hcs.base.heatmap.color.QuantileStrategy;
import de.mpicbg.knime.hcs.base.heatmap.dialog.ColorGradientDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Menu for the view manipulation.
 *
 * @author Felix Meyenhofer
 *         creation: 12/27/12
 */

public class ViewMenu extends JMenu {

    /**
     * Items for the gui, accessible by the {@link de.mpicbg.knime.hcs.base.heatmap.HeatMapViewer} interface that will be accessed by the menu actions.
     */
    private HeatMapModel heatMapModel;
    private HeatMapInputToolbar toolbar;
    private HeatMapColorToolBar colorbar;

    /** This menu item must be accessed from outside (PlateViewer) */
    private JMenuItem colorMapMenu;

    /** The updater holding the list of the PlateViewers */
    private HeatMapViewer parent;


    /**
     * Constructor of the view menu
     *
     * @param parent a HeatMapViewer object.
     */
    public ViewMenu(HeatMapViewer parent) {
        // Make certain elements available for the menu actions
        this.heatMapModel = parent.getHeatMapModel();
        this.toolbar = parent.getToolBar();
        this.colorbar = parent.getColorBar();
        this.parent = parent;

        // Create the menu
        this.setText("View");

        JCheckBoxMenuItem alwaysOnTop = new JCheckBoxMenuItem("Always on Top");
        alwaysOnTop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                alwaysOnTopAction(e);
            }
        });
        this.add(alwaysOnTop);

        JCheckBoxMenuItem markSelection = new JCheckBoxMenuItem("Mark Selection");
        markSelection.setSelected(heatMapModel.doMarkSelection());
        markSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                markSelectionAction(actionEvent);
            }
        });
        this.add(markSelection);

        this.add(createOverlaySubMenu());
        this.add(createOutlierSubMenu());

        colorMapMenu = this.add(createColorMapMenu());
        colorMapMenu.setEnabled(!heatMapModel.isGlobalScaling());

        this.add(createToolBarMenu());
    }


    /**
     * Accessor to the color map menu item
     *
     * @return the color map item.
     */
    public JMenuItem getColorMenuItem() {
        return colorMapMenu;
    }


    /**
     * Returns an image icon created from the image indicated by path
     *
     * @param path reference path to the image icon
     * @param description description of the icon
     * @return image icon
     *
     * @see ImageIcon
     */
    private Icon createImageIcon(String path, String description) {
        java.net.URL imgURL = this.getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }


    /**
     * Returns the overlay sub-menu
     *
     * @return overlay sub-menu
     */
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


    /**
     * Returns the sub-menu for outlier handling in the color scale computation.
     *
     * @return outlier sub-menu
     */
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

        // Set the selected item
        if ((heatMapModel != null) && heatMapModel.getReadoutRescaleStrategy().getClass().equals(MinMaxStrategy.class)) {
            menu.getItem(0).setSelected(true);
        } else {
            menu.getItem(1).setSelected(true);
        }

        return menu;
    }


    /**
     * Returns the menu of the color map manipulations
     *
     * @return color map sub-menu
     */
    private JMenu createColorMapMenu() {
        JMenu lut = new JMenu("Colormap");
        ButtonGroup group = new ButtonGroup();
        String[] names = {"GB", "GBR", "RWB", "HSV", "Jet", "Dark", "Custom"};
        JRadioButtonMenuItem[] item = new JRadioButtonMenuItem[names.length];

        for (int i = 0; i < names.length; i++) {
            Icon icon = createImageIcon("icons/" + names[i].toLowerCase() + ".png", names[i] + "color map");
            item[i] = new JRadioButtonMenuItem(names[i],icon);
            group.add(item[i]);
            lut.add(item[i]);
            item[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    toggleColorMapAction(actionEvent);
                }
            });

            // Set the active item.
            if ((heatMapModel != null) && heatMapModel.getColorGradient().getGradientName().equals(names[i]))
                item[i].setSelected(true);
        }

        return lut;
    }


    /**
     * Returns the sub-menu to set the toolbar visibility
     * @return toolbar sub-menu
     */
    private JMenu createToolBarMenu() {
        JMenu toolbar = new JMenu("Toolbars");
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show Toolbar");
        item.setSelected(true);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                toggleToolbarVisibility(actionEvent);
            }
        });
        toolbar.add(item);
        item = new JCheckBoxMenuItem("Show Colorbar");
        item.setSelected(true);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                toggleColorbarVisibility(actionEvent);
            }
        });
        toolbar.add(item);
        return toolbar;
    }



    /**
     * Action to control the toolbar visibility
     *
     * @param event menu action event
     */
    private void toggleColorbarVisibility(ActionEvent event) {
        JCheckBoxMenuItem mi = (JCheckBoxMenuItem) event.getSource();
        this.colorbar.setVisible(mi.isSelected());
    }

    /**
     * Action to control the colorbar visibility
     *
     * @param event menu action event
     */
    private void toggleToolbarVisibility(ActionEvent event) {
        JCheckBoxMenuItem mi = (JCheckBoxMenuItem) event.getSource();
        this.toolbar.setVisible(mi.isSelected());
    }

    /**
     * Action updating the colormap
     *
     * @param event menu action event
     */
    protected void toggleColorMapAction(ActionEvent event) {
        JMenuItem source = (JMenuItem)event.getSource();
        LinearGradientPaint newGradient;
        String gradientName = source.getText();
        if (gradientName.equals("Custom")) {
            ColorGradientDialog dialog = new ColorGradientDialog(heatMapModel);
            dialog.setVisible(true);
            newGradient = dialog.getGradientPainter();
        } else {
            newGradient = LinearGradientTools.getStandardGradient(source.getText());
        }
        heatMapModel.setColorGradient(gradientName, newGradient);

        // Propagate the color map to the PlateViewers
        if ((parent.getChildViews() != null) && heatMapModel.isGlobalScaling()) {
            for (PlateViewer viewer : parent.getChildViews().values()){
                viewer.getHeatMapModel().setColorGradient(gradientName, newGradient);
            }
        }

        heatMapModel.fireModelChanged();
    }

    /**
     * Action to control the legend visibility.
     * Creates an overlay legend window.
     */
    protected void showOverlayLegendAction() {
        LegendViewer overlayLegend = new LegendViewer((Window) this.getTopLevelAncestor());
        overlayLegend.configure(heatMapModel);
        overlayLegend.setModal(false);
        overlayLegend.setVisible(true);
    }

    /**
     * Action controlling the overlay visibility
     *
     * @param event menu action event
     */
    protected void hideOverlayAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
        heatMapModel.setHideMostFreqOverlay(item.isSelected());
        heatMapModel.fireModelChanged();
    }

    /**
     * Action controlling the color scaling
     *
     * @param event menu action event
     */
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

    /**
     * Action controlling the visibility of the selection dots/frame
     *
     * @param event menu action event
     */
    protected void markSelectionAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
        heatMapModel.setMarkSelection(item.isSelected());
        heatMapModel.fireModelChanged();
    }

    /**
     * Widow behavior
     *
     * @param event menu action event
     */
    protected void alwaysOnTopAction(ActionEvent event) {
        JMenuItem item = (JMenuItem) event.getSource();
        JFrame frame = (JFrame) getTopLevelAncestor();
        frame.setAlwaysOnTop(item.isSelected());
    }

}