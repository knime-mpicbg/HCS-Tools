package de.mpicbg.tds.knime;

import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.apache.commons.lang.StringUtils;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;
import java.util.List;

import static de.mpicbg.tds.core.TdsUtils.*;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class HCSAttributeUtils {

    public static List<String> getTreatments(DataTableSpec inputModel, String treatmentAttrName) {
        DataColumnSpec spec = inputModel.getColumnSpec(treatmentAttrName);
        if (spec == null || !spec.getType().equals(StringCell.TYPE)) {
            return null;
//            throw new RuntimeException("no treatment column or invalid type!");
        }

        if (spec.getDomain().getValues() == null) {
            NodeLogger.getLogger(HCSAttributeUtils.class).error("The domain of the treatment-attribute can not be dermined. Use the 'Domain Caclulator' to update it");
//            throw new RuntimeException("The domain of the treatment-attribute can not be dermined. Use the 'Domain Caclulator' to update it");

            return new ArrayList<String>();
        }

        return AttributeUtils.toStringList(spec.getDomain().getValues());

    }


    public static void updateTreatmentControl(DialogComponent treatmentControl, String treatAttrName, DataTableSpec spec) {
        updateTreatmentControl(treatmentControl, spec, treatAttrName);
    }


    public static void updateTreatmentControl(DialogComponent dialogComponent, DataTableSpec spec, String treatAttrName) {
        if (treatAttrName == null || StringUtils.isBlank(treatAttrName)) {
            treatAttrName = SCREEN_MODEL_TREATMENT;
        }


        List<String> treatments = getTreatments(spec, treatAttrName);

        if (dialogComponent instanceof DialogComponentStringSelection) {
            String curValue = ((SettingsModelString) dialogComponent.getModel()).getStringValue();

            if (treatments != null) {
                if (!treatments.contains(curValue)) {
                    treatments.add(curValue);
                }

                ((DialogComponentStringSelection) dialogComponent).replaceListItems(treatments, curValue);
            }
        } else if (dialogComponent instanceof DialogComponentStringListSelection) {
            String[] curValue = ((SettingsModelStringArray) dialogComponent.getModel()).getStringArrayValue();

//              if (treatments != null) {
//                if (!treatments.contains(curValue)) {
//                    treatments.add(curValue);
//                }

            if (treatments == null) {
                treatments = new ArrayList<String>();
            }

            if (treatments.isEmpty())
                treatments.add("");

            ((DialogComponentStringListSelection) dialogComponent).replaceListItems(treatments, curValue);
        }
    }


    public static Attribute<String> getTreatmentAttribute(BufferedDataTable input) {
        return new InputTableAttribute(SCREEN_MODEL_TREATMENT, input);
    }


    public static Attribute<String> getBarcodeAttribute(BufferedDataTable input) {
        return new InputTableAttribute(SCREEN_MODEL_BARCODE, input);
    }


    public static Attribute<String> getBarcodeAttribute(DataTableSpec tableSpecs) {
        return new InputTableAttribute(SCREEN_MODEL_BARCODE, tableSpecs);
    }


    public static Attribute getPlateColumnAttribute(BufferedDataTable input) {
        return new InputTableAttribute(fixLowerCasePSQLDBNames(SCREEN_MODEL_WELL_COLUMN, input), input);
    }


    public static Attribute getPlateRowAttribute(BufferedDataTable input) {
        return new InputTableAttribute(fixLowerCasePSQLDBNames(SCREEN_MODEL_WELL_ROW, input), input);
    }


    public static Attribute getLibCodeAttribute(BufferedDataTable input) {
        return new InputTableAttribute(fixLowerCasePSQLDBNames(SCREEN_MODEL_LIB_CODE, input), input);
    }


    public static Attribute getLibPlateNumberAttribute(BufferedDataTable input) {
        return new InputTableAttribute(fixLowerCasePSQLDBNames(SCREEN_MODEL_LIB_PLATE_NUMBER, input), input);
    }


    public static String fixLowerCasePSQLDBNames(String colName, BufferedDataTable input) {
        DataTableSpec tableSpec = input.getDataTableSpec();

        if (!tableSpec.containsName(colName) && tableSpec.containsName(colName.toLowerCase())) {
            // use the lowercase variant (which is likely to come from a psql query
            colName = colName.toLowerCase();
        }

        return colName;
    }


}
