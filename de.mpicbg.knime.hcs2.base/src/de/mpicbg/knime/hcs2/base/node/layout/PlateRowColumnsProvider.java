package de.mpicbg.knime.hcs2.base.node.layout;

import java.util.List;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;

public final class PlateRowColumnsProvider extends CompatibleColumnsProvider {
	
	static final List<Class<? extends DataValue>> COMPATIBLE_TYPES =
            List.of(DoubleValue.class, StringValue.class);

	PlateRowColumnsProvider() {
		super(COMPATIBLE_TYPES);
	}
	
	static boolean isCompatible(final DataColumnSpec colSpec) {
        return COMPATIBLE_TYPES.stream().anyMatch(colSpec.getType()::isCompatible);
    }

	public static List<Class<? extends DataValue>> getValueClassesList() {
		// TODO Auto-generated method stub
		return COMPATIBLE_TYPES;
	}
	
}