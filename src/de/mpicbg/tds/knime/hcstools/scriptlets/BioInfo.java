package de.mpicbg.tds.knime.hcstools.scriptlets;

import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.TableUpdateCache;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class BioInfo {


    public static BufferedDataTable writeFasta(ExecutionContext exec, BufferedDataTable input) throws CanceledExecutionException, SQLException, IOException {

        TableUpdateCache cache = new TableUpdateCache(input.getDataTableSpec());

// create a new attribute with a name and a type
        Attribute ensembleID = new Attribute("ensemblID", StringCell.TYPE);
        Attribute sequence = new Attribute("Sequence", StringCell.TYPE);
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/Users/brandl/Desktop/fastaout.fa")));

        for (DataRow dataRow : input) {
            String seqValue = sequence.getNominalAttribute(dataRow).toUpperCase();

            writer.write(">" + ensembleID.getValue(dataRow) + "|" + seqValue.length());
            writer.newLine();
            writer.write("seqValue");
            writer.newLine();

        }

        writer.flush();
        writer.close();
//for (DataRow dataRow : input) {
//    cache.add(dataRow, attribute, new StringCell"hello knime"));
//}

        return exec.createColumnRearrangeTable(input, cache.createColRearranger(), exec);

    }


}
