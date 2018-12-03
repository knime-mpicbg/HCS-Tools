package de.mpicbg.knime.hcs.base;

import java.text.NumberFormat;
import java.util.Locale;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.IntervalValue;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.DataValueRendererFactory;
import org.knime.core.data.renderer.DefaultDataValueRenderer;

/**
 * DataValueRenderer extension for {@link IntervalValue}
 * IntervalValues will be rendered with human readable format (rounded double values)
 * 
 * @author Antje Janosch
 *
 */
@SuppressWarnings("serial")
public class IntervalValueRenderer extends DefaultDataValueRenderer {

	private NumberFormat m_format = NumberFormat.getNumberInstance(Locale.US);

	public IntervalValueRenderer(String description) {
		super(description);
		if (description == null) {
			throw new IllegalArgumentException("Description must not be null.");
		}
	}

	/**
    /** Sets the interval values to a human readable format
	 * @param value The value to be rendered.
	 * @see javax.swing.table.DefaultTableCellRenderer#setValue(Object)
	 */
	@Override
	protected void setValue(final Object value) {
		Object newValue;
		if (value instanceof IntervalValue) {

			IntervalValue cell = (IntervalValue)value;
			double leftBound = cell.getLeftBound();
			double rightBound = cell.getRightBound();
			boolean inclLeft = cell.leftBoundIncluded();
			boolean inclRight = cell.rightBoundIncluded();

			String left = m_format != null ? m_format.format(leftBound) : Double.toString(leftBound);
			String right = m_format != null ? m_format.format(rightBound) : Double.toString(rightBound);

			String leftIncl = inclLeft ? "[ " : "( ";
			String rightIncl = inclRight ? " ]" : " )";

			newValue = (leftIncl + left + " ; " + right + rightIncl);

		} else {
			// missing data cells will also end up here
			newValue = value;
		}
		super.setValue(newValue);
	}

	/**
	 * Factory for a {@link IntervalValueRenderer} that shows a human readable format
	 */
	public static final class IntervalValueRendererFactory implements DataValueRendererFactory {

		private static final String DESCRIPTION = "Human Readable";

		@Override
		public String getDescription() {
			return DESCRIPTION;
		}

		@Override
		public DataValueRenderer createRenderer(DataColumnSpec colSpec) {
			return new IntervalValueRenderer(DESCRIPTION);
		}

		@Override
		public String getId() {
			return this.getClass().getName();
		}
	}

}
