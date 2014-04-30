package de.mpicbg.tds.knime.hcstools.scriptlets;

import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.tds.core.chemoinfo.CdkUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.ExtendedFingerprinter;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.interfaces.IMolecule;

import java.io.IOException;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class CompareFingerprints {

    public static BufferedDataTable execute(ExecutionContext exec, BufferedDataTable input, BufferedDataTable input2) throws CanceledExecutionException, CDKException, IOException {
        Attribute<String> moleculeInfoA = (Attribute) new InputTableAttribute("molCol", input);
        Attribute<String> moleculeInfoB = (Attribute) new InputTableAttribute("molCol", input2);

        Attribute<String> chemName = (Attribute) new InputTableAttribute("name", input);
        TableUpdateCache cache = new TableUpdateCache(input.getDataTableSpec());
        List<DataRow> input2Data = BufTableUtils.toList(input2);

        //iterate over all rows

        int rowCounter = 0;
        for (DataRow dataRow : input) {

            IMolecule moleculeA = CdkUtils.parseCML(moleculeInfoA.getRawValue(dataRow));
            IMolecule moleculeB = CdkUtils.parseCML(moleculeInfoB.getRawValue(input2Data.get(rowCounter++)));

            if (moleculeA != null && moleculeB != null) {
                IFingerprinter fingerPrinter = new ExtendedFingerprinter();
                boolean areFPsEqual = fingerPrinter.getFingerprint(moleculeA).equals(fingerPrinter.getFingerprint(moleculeB));

                if (!areFPsEqual) {
                    NodeLogger.getLogger(CompareFingerprints.class).warn("fingerprints are not equal for chemical " + chemName.getValue(dataRow));
                }
            }
        }

        // convert the plate collection into a knime-table
        ColumnRearranger c = cache.createColRearranger();
        return exec.createColumnRearrangeTable(input, c, exec);
    }


}