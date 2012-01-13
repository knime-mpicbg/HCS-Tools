package de.mpicbg.tds.knime.hcstools.normalization.zscore;
/**
 * Class implements the functionality of the zscore-normalization node
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/22/11
 * Time: 8:36 AM
 */

import de.mpicbg.tds.knime.hcstools.normalization.AbstractNormalizerModel;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

public class ZScoreNormalizerV2Model extends AbstractNormalizerModel {

    // indentifier for configuration settings
    public static final String REPLACE_VALUES = "replace";
    public static final String READOUT_SELECTION = "readouts";
    public static final String ROBUST_STATS = "use.robust.statistics";

    public ZScoreNormalizerV2Model() {
        //TODO: adapt numer of in and out ports
        super(1, 1);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return super.configure(inSpecs);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        return new BufferedDataTable[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected String getAppendSuffix() {
        return ".zscore";
    }
}
