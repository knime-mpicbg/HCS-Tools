package de.mpicbg.tds.knime.hcstools.visualization.heatmap;

/**
 * Interface for the for all view classes that need updating.
 *
 * @author Holger Brandl
 */
public interface HeatMapModelChangeListener {

    /** Method called when the data model ({@link HeatMapModel}) changed */
	public void modelChanged();

}
