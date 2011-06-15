package de.mpicbg.tds.knime.hcstools.joinimages;

import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public abstract class AbstractJoinWithImagesModel extends AbstractNodeModel {

    public SettingsModelFilterString propReadouts = AbstractJoinWithImagesDialog.createPropReadouts();

    public SettingsModelString barcode = AbstractJoinWithImagesDialog.createPropBarcode();
    public SettingsModelString propPlateRow = AbstractJoinWithImagesDialog.createPropPlateRow();
    public SettingsModelString propPlateCol = AbstractJoinWithImagesDialog.createPropPlateCol();


    public AbstractJoinWithImagesModel() {
        addSetting(propReadouts);

        addSetting(barcode);
        addSetting(propPlateRow);
        addSetting(propPlateCol);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        return new BufferedDataTable[0];


    }


}
