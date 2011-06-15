package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.knime.knutils.NumericFilter;
import org.knime.core.data.DataColumnSpec;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class TdsNumbericFilter extends NumericFilter {

    @Override
    public boolean includeColumn(DataColumnSpec dataColumnSpec) {
        return super.includeColumn(dataColumnSpec)
                && !dataColumnSpec.getName().equals(TdsUtils.SCREEN_MODEL_WELL_ROW)
                && !dataColumnSpec.getName().equals(TdsUtils.SCREEN_MODEL_WELL_COLUMN);
    }
}
