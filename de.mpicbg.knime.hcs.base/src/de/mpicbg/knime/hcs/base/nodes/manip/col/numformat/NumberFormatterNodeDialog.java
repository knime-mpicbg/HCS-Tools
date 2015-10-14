package de.mpicbg.knime.hcs.base.nodes.manip.col.numformat;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "NumberFormatter" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class NumberFormatterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring NumberFormatter node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected NumberFormatterNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    NumberFormatterNodeModel.CFGKEY_COUNT,
                    NumberFormatterNodeModel.DEFAULT_COUNT,
                    Integer.MIN_VALUE, Integer.MAX_VALUE),
                    "Counter:", /*step*/ 1, /*componentwidth*/ 5));
                    
    }
}

