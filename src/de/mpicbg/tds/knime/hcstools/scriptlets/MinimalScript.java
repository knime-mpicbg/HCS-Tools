package de.mpicbg.tds.knime.hcstools.scriptlets;

import de.mpicbg.tds.knime.knutils.TableUpdateCache;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class MinimalScript {

    public static BufferedDataTable devScript1(ExecutionContext exec, BufferedDataTable input) throws CanceledExecutionException {
        TableUpdateCache cache = new TableUpdateCache(input.getDataTableSpec());

        // convert the plate collection into a knime-table
        return exec.createColumnRearrangeTable(input, cache.createColRearranger(), exec);
    }
}
