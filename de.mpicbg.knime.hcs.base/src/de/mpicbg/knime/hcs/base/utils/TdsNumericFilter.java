package de.mpicbg.knime.hcs.base.utils;

import de.mpicbg.knime.knutils.NumericFilter;
import de.mpicbg.knime.hcs.core.TdsUtils;
import org.knime.core.data.DataColumnSpec;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class TdsNumericFilter extends NumericFilter {

    @Override
    public boolean includeColumn(DataColumnSpec dataColumnSpec) {
        return super.includeColumn(dataColumnSpec)
                && !dataColumnSpec.getName().equals(TdsUtils.SCREEN_MODEL_WELL_ROW)
                && !dataColumnSpec.getName().equals(TdsUtils.SCREEN_MODEL_WELL_COLUMN);
    }
}
