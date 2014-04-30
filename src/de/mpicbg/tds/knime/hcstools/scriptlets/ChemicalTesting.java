package de.mpicbg.tds.knime.hcstools.scriptlets;

import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.tds.core.chemoinfo.CdkUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ChemicalTesting {

    public BufferedDataTable devScript1(ExecutionContext exec, BufferedDataTable input) throws CanceledExecutionException {
        Attribute<String> moleculeInfo = (Attribute) new InputTableAttribute("molecule", input);
        Attribute<String> smilesAttribute = new Attribute("smile2", StringCell.TYPE);

        TableUpdateCache cache = new TableUpdateCache(input.getDataTableSpec());
        SmilesGenerator sg = new SmilesGenerator();


        //iterate over all rows
        for (DataRow dataRow : input) {

            IMolecule molecule = CdkUtils.parseMolecule(moleculeInfo.getValue(dataRow));

            if (molecule != null) {
                String smiles = sg.createSMILES(molecule);
                System.err.println("smiles" + smiles);

                cache.add(dataRow, smilesAttribute, new StringCell(smiles));
            }
        }

        // convert the plate collection into a knime-table
        ColumnRearranger c = cache.createColRearranger();
        return exec.createColumnRearrangeTable(input, c, exec);
    }
}
