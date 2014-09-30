package de.mpicbg.knime.hcs.base.nodes.scriptlets;

import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.InputTableAttribute;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class MultiSelectFilter {


    public static BufferedDataTable multiFilter(ExecutionContext exec, BufferedDataTable input) throws CanceledExecutionException, SQLException {

        //Input parameters
        Attribute<String> filterAttribute = new InputTableAttribute("Barcode", input);
        List<String> selection = Arrays.asList("value a", "another value");
        boolean keepSelection = true;


        BufferedDataContainer container = exec.createDataContainer(input.getSpec());

        for (DataRow dataRow : input) {
            String curAttrValue = filterAttribute.getValue(dataRow);

            boolean isSelected = selection.contains(curAttrValue);

            if (keepSelection) {
                if (isSelected) {
                    container.addRowToTable(dataRow);
                }
            } else {
                if (!isSelected) {
                    container.addRowToTable(dataRow);
                }
            }
        }

        container.close();
        return container.getTable();
    }


    public static BufferedDataTable multiFilterUnique(ExecutionContext exec, BufferedDataTable input) throws CanceledExecutionException, SQLException {

        //Input parameters
        Attribute<String> filterAttribute = new InputTableAttribute("Barcode", input);
        List<String> selection = Arrays.asList("value a", "another value");
        boolean keepSelection = true;

        List<String> consumedFilters = new ArrayList<String>();

        BufferedDataContainer container = exec.createDataContainer(input.getSpec());

        for (DataRow dataRow : input) {
            String curAttrValue = filterAttribute.getValue(dataRow);

            boolean isSelected = selection.contains(curAttrValue);

            if (isSelected) {
                if (consumedFilters.contains(curAttrValue)) {
                    throw new IllegalArgumentException("Could not apply unqiuqe-filtering because filter-expressions refered to several rows in the table");
                }
                consumedFilters.add(curAttrValue);
            }

            if (keepSelection) {
                if (isSelected) {
                    container.addRowToTable(dataRow);
                }
            } else {
                if (!isSelected) {
                    container.addRowToTable(dataRow);
                }
            }
        }

        container.close();
        return container.getTable();
    }
}
