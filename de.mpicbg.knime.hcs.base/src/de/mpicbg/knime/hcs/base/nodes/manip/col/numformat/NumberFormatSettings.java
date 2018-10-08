package de.mpicbg.knime.hcs.base.nodes.manip.col.numformat;

/**
 * class to store the settings for formatting a number to make it sortable
 * 
 * @author Antje Janosch
 *
 */
public class NumberFormatSettings {
	
	/** number of leading characters */
	private int m_nLeading = 1;
	/** number of trailing characters */
	private int m_nTrailing = 1;
	/** representation as whole number ? */
	private boolean m_asWholeNumber = false;
	
	/**
	 * constructor
	 */
	public NumberFormatSettings() {
	}

	/** GETTER */
	
	public int getNLeading() {
		return m_nLeading;
	}

	public int getNTrailing() {
		return m_nTrailing;
	}

	public boolean asWholeNumber() {
		return m_asWholeNumber;
	}
	
	/** SETTER */

	public void setNLeading(int m_nLeading) {
		this.m_nLeading = m_nLeading;
	}

	public void setNTrailing(int m_nTrailing) {
		this.m_nTrailing = m_nTrailing;
	}

	public void setAsWholeNumber(boolean asWholeNumber) {
		this.m_asWholeNumber = asWholeNumber;
	}
	
	
}
