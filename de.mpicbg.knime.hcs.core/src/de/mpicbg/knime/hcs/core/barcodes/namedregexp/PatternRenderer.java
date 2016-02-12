/**
 * 
 */
package de.mpicbg.knime.hcs.core.barcodes.namedregexp;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.regex.Matcher;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * cell containing a barcode pattern are colored in a way that the pattern gets readable
 * groups get different colors
 * 
 * @author Antje Janosch
 *
 */
public class PatternRenderer implements ListCellRenderer<String> {
	
	/**
	 * list of preference patterns
	 */
	List<String> m_patternList;
	
	/**
	 * color vector for the different pattern-groups
	 */
	private String[] colorVector = { 
			"#0000FF", 			// blue
			"#FF0000",			// red
			"#228B22",			// forestgreen
			"#FFA500",  		// orange
			"#8A2BE2",  		// blueviolet
			"#32CD32", 			// limegreen
			"#8B008B", 			// darkmagenta
			 };		
	
	/**
	 * default renderer
	 */
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends String> list,
			String value, int index, boolean isSelected, boolean cellHasFocus) {
		
		String selectedString = (String)value.toString();
		boolean invalid = !m_patternList.contains(selectedString);
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		// create pattern group list
		NamedPattern np = NamedPattern.compile(selectedString);
		List<String> patternGroups = np.getGroupedPatternList();
		
		// build html with different font colors for groups
		StringBuilder buildHTML = new StringBuilder("<html>");
		int colIdx = 0;
		for(String p : patternGroups) {
			Matcher matcher = NamedPattern.GROUP_REGEX_PATTERN.matcher(p);
			if(matcher.matches()) {
				matcher = NamedPattern.GROUP_START_PATTERN.matcher(p);
				matcher.find();
				buildHTML.append("<font color=\"" + colorVector[colIdx] + "\">");
				buildHTML.append(StringEscapeUtils.escapeHtml(matcher.group(1)));
				buildHTML.append("</font>");
				buildHTML.append("<font color=\"black\">");
				buildHTML.append(StringEscapeUtils.escapeHtml(p.substring(matcher.end(1), p.length()-1)));
				buildHTML.append("</font>");
				buildHTML.append("<font color=\"" + colorVector[colIdx] + "\">)</font>");
				
				// step to next color, or set back to first color if end of the color vector is reached
				colIdx++;
				if(colIdx == colorVector.length) colIdx = 0;
			} else {
				buildHTML.append("<font color=\"black\">");
				buildHTML.append(StringEscapeUtils.escapeHtml(p));
				buildHTML.append("</font>");
			}	
			//buildHTML.append("<br>");
		}
		buildHTML.append("</body>");
		
		renderer.setText(buildHTML.toString());
		
		// pattern is selected => set background color to light blue
		if(isSelected) {
			renderer.setBackground(new Color(204,229,255)); //light blue
		} else {
			renderer.setBackground(Color.WHITE);
		}
		
		// pattern is not listed in preferences anymore => mark it red
		if(invalid) {
			renderer.setBorder(BorderFactory.createLineBorder(Color.RED));
		}
		
		return renderer;
	}
	
	/**
	 * set the list of preference patterns to decide whether a selected pattern is still listed there
	 * @param patternList
	 */
	public void setPreferencePatterns(List<String> patternList) {
		m_patternList = patternList;
	}

}
