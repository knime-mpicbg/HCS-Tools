package de.mpicbg.knime.hcs.base.nodes.manip.col.numformat;

public class NumberFormatSettings {
	
	private int m_nLeading = 1;
	private int m_nTrailing = 1;
	private boolean m_asWholeNumber = false;
	
	public NumberFormatSettings() {
	}

	public int getNLeading() {
		return m_nLeading;
	}

	public int getNTrailing() {
		return m_nTrailing;
	}

	public boolean asWholeNumber() {
		return m_asWholeNumber;
	}

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
