package de.mpicbg.tds.knime.hcstools.scriptlets.chemoinfo;

import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.tds.core.chemoinfo.CdkUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IMolecule;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ChemicalConnectivity {

    public static BufferedDataTable devScript1(ExecutionContext exec, BufferedDataTable input) throws CanceledExecutionException {
        Attribute<String> moleculeInfo = (Attribute) new InputTableAttribute("molecule", input);
        Attribute<String> isConnectedAttribute = new Attribute("isConnected", StringCell.TYPE);

        TableUpdateCache cache = new TableUpdateCache(input.getDataTableSpec());

        //iterate over all rows
        for (DataRow dataRow : input) {
            IMolecule molecule = CdkUtils.parseMolecule(moleculeInfo.getValue(dataRow));

            if (molecule == null) {
                cache.add(dataRow, isConnectedAttribute, DataType.getMissingCell());

            } else {
                boolean isConnected = ConnectivityChecker.isConnected(molecule);
                cache.add(dataRow, isConnectedAttribute, new StringCell(isConnected ? "true" : "false"));
            }
        }

        // convert the plate collection into a knime-table
        return exec.createColumnRearrangeTable(input, cache.createColRearranger(), exec);
    }
}