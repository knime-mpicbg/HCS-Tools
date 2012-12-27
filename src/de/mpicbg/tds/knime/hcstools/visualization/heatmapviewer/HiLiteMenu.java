package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import org.knime.core.data.RowKey;
import org.knime.core.node.NodeModel;
import org.knime.core.node.property.hilite.HiLiteHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;

/**
 * Creating a HiLiteMenu
 *
 * @author Felix Meyenhofer
 *         creation: 12/27/12
 */

public class HiLiteMenu extends JMenu {

    /**
     * Items for the gui, accessible by the {@link HeatMapViewer} interface that will be accessed by the menu actions.
     */
    private NodeModel nodeModel;
    private HeatMapModel2 heatMapModel;


    /**
     * Constructor of a HiLiteMenu
     * @param parent an HeatMapViewer object
     *
     * @see HeatMapViewer
     */
    public HiLiteMenu(HeatMapViewer parent) {
        
        this.nodeModel = parent.getNodeModel();
        this.heatMapModel = parent.getHeatMapModel();
        
//        JMenu menu = new JMenu((HiLiteHandler.HILITE));
        this.setText(HiLiteHandler.HILITE);
        JMenuItem item = this.add(HiLiteHandler.HILITE_SELECTED);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                hiLiteAction();
            }
        });
        item = this.add(HiLiteHandler.UNHILITE_SELECTED);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                unHiLiteAction();
            }
        });
        item = this.add(HiLiteHandler.CLEAR_HILITE);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearHiLiteAction();
            }
        });
    }

    /**
     * Un-Hilite the current selection
     */
    private void unHiLiteAction() {
        Collection<Well> selection = heatMapModel.getWellSelection();
        heatMapModel.removeHilLites(selection);
        heatMapModel.fireModelChanged();

        HashSet<RowKey> keys = new HashSet<RowKey>();
        for (Well well : selection) {
            keys.add(well.getKnimeTableRowKey());
        }

        if (nodeModel == null) {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        } else {
            nodeModel.getInHiLiteHandler(0).fireUnHiLiteEvent(keys); //TODO: put the port number in a variable in the nodemodel
        }
    }

    /**
     * Clear all HiLites
     */
    private void clearHiLiteAction() {
        heatMapModel.clearHiLites();
        heatMapModel.fireModelChanged();

        if (nodeModel == null) {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        } else {
            nodeModel.getInHiLiteHandler(0).fireClearHiLiteEvent(); //TODO: put the port number in a variable in the nodemodel
        }
    }

    /**
     * Hilite the current selection
     */
    private void hiLiteAction() {
        Collection<Well> selection = heatMapModel.getWellSelection();
        heatMapModel.addHilLites(selection);
        heatMapModel.fireModelChanged();

        HashSet<RowKey> keys = new HashSet<RowKey>();
        for (Well well : selection) {
            keys.add(well.getKnimeTableRowKey());
        }

        if (nodeModel == null) {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        } else {
            nodeModel.getInHiLiteHandler(0).fireHiLiteEvent(keys); //TODO: put the port number in a variable in the nodemodel
        }
    }

}
