package de.mpicbg.tds.knime.hcstools.screenmining;

import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.distribution.HypergeometricDistribution;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
import org.knime.core.data.*;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.HashSet;
import java.util.Set;

import static de.mpicbg.tds.knime.knutils.BufTableUtils.updateProgress;


/**
 * This is the model implementation of ZScoreNormalizer.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class EnrichmentAnalyzer extends AbstractNodeModel {

    public static final String GROUP_BY_COLUMN = "hit.class.column";
    public static final String GROUP_BY_COLUMN_DESC = "Group by";
    public static final String ONTOLOGY_TERMS_COLUMN = "ontology.terms.column";
    public static final String ONTOLOGY_TERMS_COLUMN_DESC = "Ontology terms";

    public static final String USE_BONFERRONI = "use.bonferroni";
    public static final String USE_BONFERRONI_DESC = "Use Bonferroni-correction";

    private SettingsModelString ontTerms = EnrichmentAnalyzerFactory.createOntologyTermProperty();
    private SettingsModelString groupBy = EnrichmentAnalyzerFactory.createGroupBy();
    private SettingsModelBoolean useBonferroniCorrection = EnrichmentAnalyzerFactory.useBonferroniCorrection();


    public EnrichmentAnalyzer() {
        addSetting(ontTerms);
        addSetting(groupBy);
//        addSetting(useBonferroniCorrection);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        Attribute groupingAttribute = new InputTableAttribute(groupBy.getStringValue(), input);
        Attribute ontoTermsAttribute = new InputTableAttribute(ontTerms.getStringValue(), input);

        Boolean useBonferroni = useBonferroniCorrection.getBooleanValue();


        // build up the list of go terms and the list of groups of interest and the statistics
        Set<String> ontologyTerms = new HashSet<String>();
        Set<String> groups = new HashSet<String>();

        Frequency ontClassFrequencies = new Frequency();
        Frequency groupFrequencies = new Frequency();

        MultiKeyMap contTable = new MultiKeyMap();

        for (DataRow dataRow : input) {

            // first process the ontology terms of the current row
            Set<String> rowOntTerms = getTerms(ontoTermsAttribute, dataRow);
            for (String rowOntTerm : rowOntTerms) {
                ontClassFrequencies.addValue(rowOntTerm);
            }
            ontologyTerms.addAll(rowOntTerms);

            // ... and now do the same for the groups
            Set<String> rowGroupTerms = getTerms(groupingAttribute, dataRow);
            for (String rowGroupTerm : rowGroupTerms) {
                groupFrequencies.addValue(rowGroupTerm);
            }
            groups.addAll(rowGroupTerms);

            // update the contingency-table
            for (String rowOntTerm : rowOntTerms) {
                for (String rowGroupTerm : rowGroupTerms) {
                    if (!contTable.containsKey(rowOntTerm, rowGroupTerm)) {
                        contTable.put(rowOntTerm, rowGroupTerm, 0);
                    }

                    contTable.put(rowOntTerm, rowGroupTerm, ((Integer) contTable.get(rowOntTerm, rowGroupTerm)) + 1);
                }
            }
        }


        // create a simple table with just one column which contains the barcodes of the plates
        DataTableSpec tableSpecs = prepareSpecs();

        BufferedDataContainer container = exec.createDataContainer(tableSpecs);

        MultiKeyMap hypGemoDistCache = new MultiKeyMap();

        int rowCounter = 0;
        for (String ontologyTerm : ontologyTerms) {
            for (String group : groups) {

                int populationSize = input.getRowCount();
                int numAnnotatedGenes = (int) ontClassFrequencies.getCount(ontologyTerm);
                int numGenesInGroup = (int) groupFrequencies.getCount(group);

                if (!hypGemoDistCache.containsKey(ontologyTerm, group)) {
                    hypGemoDistCache.put(ontologyTerm, group, new HypergeometricDistributionImpl(populationSize, numAnnotatedGenes, numGenesInGroup));
                }

                int numAnnoatedGenesInGroup = 0;
                if (contTable.containsKey(ontologyTerm, group)) {
                    numAnnoatedGenesInGroup = (Integer) contTable.get(ontologyTerm, group);
                }

                HypergeometricDistribution hypGeomDist = (HypergeometricDistribution) hypGemoDistCache.get(ontologyTerm, group);

                double pValue = hypGeomDist.cumulativeProbability(numAnnoatedGenesInGroup, Math.min(numGenesInGroup, numAnnotatedGenes));

                DataCell[] cells = new DataCell[]{new StringCell(ontologyTerm), new StringCell(group), new DoubleCell(pValue)};

                DataRow row = new DefaultRow(new RowKey("Row " + rowCounter), cells);
                container.addRowToTable(row);

                rowCounter++;
                updateProgress(exec, rowCounter, ontologyTerms.size() * groups.size());
            }
        }

        // once we are done, we close the container and return its table
        container.close();

        return new BufferedDataTable[]{container.getTable()};
    }


    /**
     * Return all terms that are assumed to be ;-separated.
     */
    private Set<String> getTerms(Attribute termsAttribute, DataRow dataRow) {
        Set<String> terms = new HashSet<String>();

        String rawValue = termsAttribute.getRawValue(dataRow);
        if (rawValue.isEmpty())
            return terms;

        for (String subTerm : rawValue.split(";")) {
            if (!StringUtils.isBlank(subTerm)) {
                terms.add(subTerm);
            }
        }

        return terms;

    }


    private DataTableSpec prepareSpecs() {
        // the table will have three columns:
        DataColumnSpec[] allColSpecs = new DataColumnSpec[3];

        allColSpecs[0] = new DataColumnSpecCreator("Ontology class", StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator("Group", StringCell.TYPE).createSpec();
        allColSpecs[2] = new DataColumnSpecCreator("p-value", DoubleCell.TYPE).createSpec();

        return new DataTableSpec(allColSpecs);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[]{prepareSpecs()};
    }
}