package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.mpicbg.tds.knime.hcstools.visualization.PlateComparators;

/**
 * Author: Felix Meyenhofer
 * Date: 20/12/12
 *
 * Menu for the ScreenViewer
 */

public class ScreenMenu extends PlateMenu {

    private HeatTrellis heatTrellis;


    /**
     * Constructors
     */
    public ScreenMenu() {
        this(null);
    }

    public ScreenMenu(ScreenViewer parent) {
        super(parent);

        if (parent != null)
            heatTrellis = parent.getHeatTrellis();

        this.add(createTrellisMenu());
    }


    /**
     * Methods for menu creation
     */
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

        JCheckBoxMenuItem plateDimensions = new JCheckBoxMenuItem("Fix Plate Proportions");
        plateDimensions.setSelected(true);
        plateDimensions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                plateDimensionsAction(actionEvent);
            }
        });
        menu.add(plateDimensions);

        JMenuItem closeAll = new JMenuItem("Close All Plate Viewers");
        closeAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                heatTrellis.closePlateViewers();
            }
        });
        menu.add(closeAll);

        JCheckBoxMenuItem globalScaling = new JCheckBoxMenuItem("Global Color Scale");
        globalScaling.setSelected(heatMapModel.isGlobalScaling());
        globalScaling.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                globalScalingAction(event);
            }
        });
        menu.add(globalScaling);

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

    private void globalScalingAction(ActionEvent event) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();

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
        frame.setJMenuBar(new ScreenMenu());
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
