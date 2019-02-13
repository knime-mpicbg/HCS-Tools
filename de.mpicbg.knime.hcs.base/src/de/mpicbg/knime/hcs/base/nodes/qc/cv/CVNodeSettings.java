package de.mpicbg.knime.hcs.base.nodes.qc.cv;

import java.util.LinkedList;
import java.util.List;

/**
 * class holding node settings for CV node
 * 
 * @author Antje Janosch
 *
 */
public class CVNodeSettings {
	
	// name of group column
	private String m_groupColumn =  CVCalculatorNodeModel.CFG_GROUP_DFT;
	// name of subset column
	private String m_subsetColumn = CVCalculatorNodeModel.CFG_SUBSET_COL_DFT;
	
	// ?calculate robust statistics?
	private boolean m_useRobustStatistics = CVCalculatorNodeModel.CFG_USE_ROBUST_DFT;
	// ?change suffix?
	private boolean m_changeSuffix = CVCalculatorNodeModel.CFG_CHANGE_SUFFIX_DFT;
	
	// suffix
	private String m_suffix = CVCalculatorNodeModel.CFG_SUFFIX_DFT;
	
	// list of parameter columns
	private List<String> m_parameterColumns;
	// list of included subset values
	private List<String> m_subsetSelection;
	// ?include missing to subsets?
	private boolean m_includeMissing = false;
	
	public CVNodeSettings() {
		m_parameterColumns = new LinkedList<String>();
		m_subsetSelection = new LinkedList<String>();
	}
	
	public void setGroupColumn(String groupColumn) {
		m_groupColumn = groupColumn;
	}

	public void setSubsetColumn(String subsetColumn) {
		m_subsetColumn = subsetColumn;
	}

	public void setRobustStatisticsFlag(boolean useRobustStatistics) {
		m_useRobustStatistics = useRobustStatistics;
	}
	
	public void setSuffixFlag(boolean changeSuffix) {
		m_changeSuffix = changeSuffix;
	}
	
	public void setSuffix(String suffix) {
		m_suffix = suffix;
	}
	
	public void setParameterColumns(String[] columns) {
		for(String col : columns)
			m_parameterColumns.add(col);
	}
	
	public void setSubsetSelection(String[] subsetSelection) {
		for(String sub : subsetSelection)
			m_subsetSelection.add(sub);
	}
	
	public void setIncludeMissingFlag(boolean inclMissing) {
		m_includeMissing = inclMissing;
	}
	
	public String getGroupColumn() {
		return m_groupColumn;
	}
	
	public String getSubsetColumn() {
		return m_subsetColumn;
	}
	
	public boolean getRobustStatisticsFlag() {
		return m_useRobustStatistics;
	}
	
	public boolean getSuffixFlag() {
		return m_changeSuffix;
	}
	
	public String getSuffix() {
		return m_suffix;
	}
	
	public List<String> getParameterColumns() {
		return m_parameterColumns;
	}

	public boolean doesSubsetContain(String subsetValue) {
		return m_subsetSelection.contains(subsetValue);
	}

	public boolean getIncludeMissingFlag() {
		return m_includeMissing;
	}
}
