package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateAttribute;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Felix Meyenhofer
 *         creation: 12/27/12
 */

public class TrellisMenu extends JMenu {

    private HeatTrellis heatTrellis;
    private HeatMapModel heatMapModel;

    /**
     * Constructor of the heatmap trellis menu
     * @param parent a HeatMapViewer object
     */
    public TrellisMenu(ScreenViewer parent) {
        this.heatTrellis = parent.getHeatTrellis();
        this.heatMapModel = parent.getHeatMapModel();

        this.setText("Trellis");

        JMenuItem zoomIn = new JMenuItem("Zoom in", KeyEvent.VK_T);
        zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.META_MASK));
        zoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoomInAction();
            }
        });
        this.add(zoomIn);

        JMenuItem zoomOut = new JMenuItem("Zoom out");
        zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.META_MASK));
        zoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoomOutAction();
            }
        });
        this.add(zoomOut);

        JMenuItem rowsColumns = new JMenuItem("Rows/Columns");
        rowsColumns.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                rowsColumnsAction();
            }
        });
        rowsColumns.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.META_MASK));
        this.add(rowsColumns);

        JMenuItem sortPlates = this.add("Sort Plates");
        sortPlates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sortPlatesAction();
            }
        });
        sortPlates.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
        this.add(sortPlates);

        this.add(createPlatViewerMenu());

        JCheckBoxMenuItem plateDimensions = new JCheckBoxMenuItem("Fix Plate Proportions");
        plateDimensions.setSelected(true);
        plateDimensions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                plateDimensionsAction(actionEvent);
            }
        });
        this.add(plateDimensions);

        JCheckBoxMenuItem globalScaling = new JCheckBoxMenuItem("Global Color Scale");
        globalScaling.setSelected(heatMapModel.isGlobalScaling());
        globalScaling.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                globalScalingAction(event);
            }
        });
        this.add(globalScaling);

        this.add(createHiLiteFilterMenu());
    }

    /**
     * Returns the HiLite filter sub-menu.
     * @return sub-menu
     */
    private JMenu createHiLiteFilterMenu() {
        JMenu menu = new JMenu("HiLite Filter");
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem[] item = new JRadioButtonMenuItem[3];
        item[0] = new JRadioButtonMenuItem("Show All");
        item[0].setSelected(true);
        item[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                displayModeAction(HeatMapModel.HiLiteDisplayMode.ALL);
            }
        });
        item[1] = new JRadioButtonMenuItem("Show HiLite Only");
        item[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                displayModeAction(HeatMapModel.HiLiteDisplayMode.HILITE_ONLY);
            }
        });
        item[2] = new JRadioButtonMenuItem("Show UnHiLite Only");
        item[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                displayModeAction(HeatMapModel.HiLiteDisplayMode.UNHILITE_ONLY);
            }
        });

        for (JRadioButtonMenuItem anItem : item) {
            menu.add(anItem);
            group.add(anItem);
        }

        return menu;
    }

    /**
     * Returns a plate viewer menu to coordinate the PlateViewer windos
     * @return plate viewer sub-menu
     */
    private JMenu createPlatViewerMenu() {
        JMenu menu = new JMenu("PlateViewer");
        JMenuItem item = menu.add(new JMenuItem("Close All"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                heatTrellis.closePlateViewers();
            }
        });
        item = menu.add(new JMenuItem("Bring All to Front"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                heatTrellis.bringToFrontPlateViewers();
            }
        });

        return menu;
    }


    /**
     * Actions
     */
    private void sortPlatesAction() {
        PlateAttributeDialog dialog = new PlateAttributeDialog(heatMapModel);
        dialog.setVisible(true);
        String[] selectedAttributes = dialog.getSelectedAttributeTitles();
        List<String> inverted = Arrays.asList(selectedAttributes);
        Collections.reverse(inverted);
        for (String key : inverted) {
            PlateAttribute attribute = PlateUtils.getPlateAttributeByTitle(key);
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

    private void globalScalingAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();

        if (heatTrellis.plateViewers.isEmpty())
            return;

        Object[] options = {"Cancel", "Proceed"};
        int optionIndex = JOptionPane.showOptionDialog(getTopLevelAncestor(),
                "<html>To keep the windows consistent,<br/>" +
                        "all the Plate Viewers will be closed</htmal>",
                "Changing the global scaling Option",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]);
        if ( optionIndex == 0 ) {
            item.setSelected(heatMapModel.isGlobalScaling());
        } else {
            heatMapModel.setGlobalScaling(item.isSelected());
            heatTrellis.closePlateViewers();
        }
    }

    private void displayModeAction(HeatMapModel.HiLiteDisplayMode mode) {
        heatMapModel.setHiLiteDisplayModus(mode);
        heatMapModel.fireModelChanged();
    }

}
