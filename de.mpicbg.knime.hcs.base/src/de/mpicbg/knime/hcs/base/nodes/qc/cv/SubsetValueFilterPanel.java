package de.mpicbg.knime.hcs.base.nodes.qc.cv;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.JCheckBox;

import org.knime.core.data.DataCell;
import org.knime.core.data.NominalValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.util.filter.NameFilterConfiguration;
import org.knime.core.node.util.filter.nominal.NominalValueFilterConfiguration;
import org.knime.core.node.util.filter.nominal.NominalValueFilterPanel;

/**
 * panel with subset filter with sorted domain values (no other reason to extend the parent class)
 * 
 * @author Antje Janosch
 *
 */
@SuppressWarnings("serial")
public class SubsetValueFilterPanel extends NominalValueFilterPanel {
	
	private Set<DataCell> m_domain;

	@Override
	public void loadConfiguration(NameFilterConfiguration config, Set<DataCell> domain) {
		ArrayList<String> names = new ArrayList<>();
        m_domain = new LinkedHashSet<>();
        //get array of domain values
        if (domain != null) {
            m_domain.addAll(domain);
            for (DataCell dc : m_domain) {
                names.add(dc.toString());
            }
        }
        super.loadConfiguration(config, names.toArray(new String[names.size()]));
        Optional<JCheckBox> additionalButtonOptional = getAdditionalButton();
        if (additionalButtonOptional.isPresent() && config instanceof NominalValueFilterConfiguration) {
            additionalButtonOptional.get().setSelected(((NominalValueFilterConfiguration)config).isIncludeMissing());
        }
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    protected NominalValue getTforName(final String name) {
        if (m_domain != null) {
            for (DataCell dc : m_domain) {
                if (dc.toString().equals(name)) {
                    return (NominalValue)dc;
                }
            }
        }
        return new StringCell(name);
    }

}
