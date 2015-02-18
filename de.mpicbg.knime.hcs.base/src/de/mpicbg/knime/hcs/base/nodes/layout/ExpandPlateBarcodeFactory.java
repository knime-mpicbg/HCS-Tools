package de.mpicbg.knime.hcs.base.nodes.layout;

import de.mpicbg.knime.hcs.base.HCSToolsBundleActivator;
import de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel;
import de.mpicbg.knime.hcs.base.prefs.BarcodePatternsEditor;
import de.mpicbg.knime.hcs.base.prefs.HCSToolsPreferenceInitializer;
import de.mpicbg.knime.knutils.AbstractConfigDialog;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class ExpandPlateBarcodeFactory extends NodeFactory<ExpandPlateBarcodeModel> {

	/**
	 * {@inheritDoc}
	 */
    @Override
    public ExpandPlateBarcodeModel createNodeModel() {
        return new ExpandPlateBarcodeModel();
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

	/**
	 * {@inheritDoc}
	 */
    public NodeView<ExpandPlateBarcodeModel> createNodeView(final int viewIndex, final ExpandPlateBarcodeModel nodeModel) {
        return null;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public boolean hasDialog() {
        return true;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public NodeDialogPane createNodeDialogPane() {
    	
    	return new ExpandPlateBarcodeDialog();
        /*return new AbstractConfigDialog() {

        	*//**
        	 * {@inheritDoc}
        	 *//*
            @SuppressWarnings("unchecked")
			@Override
            public void createControls() {
            	IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
            	addDialogComponent(new DialogComponentColumnNameSelection(ExpandPlateBarcodeModel.createBarcodeColumnSM(), "Barcode column", 0, true, StringValue.class));
                addDialogComponent(new DialogComponentStringSelection(ExpandPlateBarcodeModel.createBarcodePatternSM(), "Barcode pattern", BarcodePatternsEditor.getPatternList(prefStore.getString(HCSToolsPreferenceInitializer.BARCODE_PATTERNS))));
            }
        };*/
    }
}