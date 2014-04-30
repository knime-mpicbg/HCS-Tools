package de.mpicbg.tds.knime.hcstools;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * Document me!
 *
 * @author Holger Brandl
 */


public class HCSSettingsFactory {

    public static SettingsModelString createGroupBy() {
        return new SettingsModelString(AbstractScreenTrafoModel.GROUP_WELLS_BY_PROPERTY, AbstractScreenTrafoModel.GROUP_WELLS_BY_DEFAULT);
    }


    public static SettingsModelString createPropPlateRow() {
        return new SettingsModelString("plate.row", TdsUtils.SCREEN_MODEL_WELL_ROW);
    }


    public static SettingsModelString createPropPlateCol() {
        return new SettingsModelString("plate.col", TdsUtils.SCREEN_MODEL_WELL_COLUMN);
    }


    public static SettingsModelString createPropLibCode() {
        return new SettingsModelString("plate.libcode", TdsUtils.SCREEN_MODEL_LIB_CODE);
    }


    public static SettingsModelString createPropPlateNumber() {
        return new SettingsModelString("plate.libplatenumber", TdsUtils.SCREEN_MODEL_LIB_PLATE_NUMBER);
    }


    public static SettingsModelString createPropCompoundID() {
        return new SettingsModelString("plate.compoundid", TdsUtils.SCREEN_MODEL_COMPOUND_ID);
    }
}
