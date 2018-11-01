package de.mpicbg.knime.hcs.core.math;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BinningAnalysisModel {
	
	private List<String> m_columns;
	
	private int m_nBins;
	
	private HashMap<String,LinkedList<Interval>> m_binningMap;
	
	public BinningAnalysisModel() {
		this.m_columns = null;
		this.m_nBins = -1;
		this.m_binningMap = null;
	}
	
	public BinningAnalysisModel(List<String> selectedCols, int nBins, HashMap<String,LinkedList<Interval>> binningMap) {
		this.m_columns = selectedCols;
		this.m_nBins = nBins;
		this.m_binningMap = binningMap;
	}

	public List<String> getColumns() {
		return m_columns;
	}

	public int getNBins() {
		return m_nBins;
	}

	public HashMap<String,LinkedList<Interval>> getModel() {
		return m_binningMap;
	}

	public void setColumns(List<String> selectedCols) {
		this.m_columns = selectedCols;
	}
	
	public void setNBins(int nBins) {
		this.m_nBins = nBins;
	}
	
	public void setBinningMap(HashMap<String,LinkedList<Interval>> binningMap) {
		this.m_binningMap = binningMap;
	}
}
