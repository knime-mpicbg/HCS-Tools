package de.mpicbg.knime.hcs.core.barcodes.namedregexp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 
 * @author Antje Janosch / Holger Brandl
 *
 * This class handles barcode patterns (regex) with group name definitions 
 * e.g. (?<mygroupname>.*)
 * 
 * since Java 1.7 regex groups are supported => TODO: adapt this class
 * 
 */
public class NamedPattern {

	/**
	 * pattern to define group names like 'mygroupname' in (?<mygroupname>
	 */
    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(\\w+)>");
    
    /**
     * matches the whole pattern group definition like (?<libplatenumber>[0-9]{3})
     */
    public static final Pattern GROUP_REGEX_PATTERN = Pattern.compile("(\\(\\?<.*?\\))");
    /**
     * matches the pattern group definition start like (?<libplatenumber>
     */
    public static final Pattern GROUP_START_PATTERN = Pattern.compile("(\\(\\?<.*?>)");

    private Pattern pattern;
    private String namedPattern;
    private List<String> groupNames;


    public static NamedPattern compile(String regex) {
        return new NamedPattern(regex, 0);
    }


    public static NamedPattern compile(String regex, int flags) {
        return new NamedPattern(regex, flags);
    }


    private NamedPattern(String regex, int i) {
        namedPattern = regex;
        pattern = buildStandardPattern(regex);
        groupNames = extractGroupNames(regex);
    }


    public int flags() {
        return pattern.flags();
    }


    public NamedMatcher matcher(CharSequence input) {
        return new NamedMatcher(this, input);
    }


    Pattern getPattern() {
        return pattern;
    }
    
    /**
     * checks whether the given group-pattern could be transformed into a valid regular expression,
     * if groups have been defined at all,
     * if groups are unique
     * @return true, if pattern is valid; false otherwise
     */
    public boolean isValidPattern() {
    	if(pattern == null) return false;
    	if(groupNames.size() == 0) return false;
    	for(String group : groupNames) {
    		if(Collections.frequency(groupNames, group) > 1) return false;
    	}
    	return true;
    }


    public String standardPattern() {
        return pattern.pattern();
    }


    public String namedPattern() {
        return namedPattern;
    }


    public List<String> groupNames() {
        return groupNames;
    }


    public String[] split(CharSequence input, int limit) {
        return pattern.split(input, limit);
    }


    public String[] split(CharSequence input) {
        return pattern.split(input);
    }


    public String toString() {
        return namedPattern;
    }


    private List<String> extractGroupNames(String namedPattern) {
        List<String> groupNames = new ArrayList<String>();
        Matcher matcher = NAMED_GROUP_PATTERN.matcher(namedPattern);
        while (matcher.find()) {
            groupNames.add(matcher.group(1));
        }
        return groupNames;
    }

    private Pattern buildStandardPattern(String namedPattern) {
    	try { 
    		return Pattern.compile(NAMED_GROUP_PATTERN.matcher(namedPattern).replaceAll("("));
    	} catch(PatternSyntaxException e) {
    		return null; 
    	}
    }
    
	/**
	 * splits the barcode pattern into parts (groups, and in-between-stuff) using this pattern {@link #GROUP_REGEX_PATTERN}
	 * and returns a list with these parts
	 * 
	 * @param barcodePattern
	 * @return list of pattern groups and whatever is in between
	 */
    public List<String> getGroupedPatternList() {
    	List<String> groupStrings = new ArrayList<String>();
		
        Matcher matcher = GROUP_REGEX_PATTERN.matcher(namedPattern);

        //match named groups in regex
        List<Integer[]> groupIdx = new ArrayList<Integer[]>();
        while(matcher.find()) {
            groupIdx.add(new Integer[]{matcher.start(),matcher.end()-1});
        }
        
        // if no group was found, return the whole string in one
        if(groupIdx.size() == 0) {
        	groupStrings.add(namedPattern);
        	return groupStrings;
        }
        
        //character index
        int regexIdx = 0;
        // named group index
        int gIdx = 0;
        // start/end of next group
        int[] nextGroup = ArrayUtils.toPrimitive(groupIdx.get(gIdx));
        // string to collect non-group parts
        String tempString = "";
        
        // collect all groups and non-group parts from the barcode pattern string
        while(regexIdx < namedPattern.length()) {
        	//check if the given index is starting point for a group
        	if(nextGroup[0] == regexIdx) {
        		// save non-group-string if there is any
        		if(tempString.length() > 0) {
        			groupStrings.add(tempString);
        			tempString = "";
        		}
        		// add group string
        		groupStrings.add(namedPattern.substring(nextGroup[0], nextGroup[1] + 1));
        		regexIdx = nextGroup[1] + 1;
        		gIdx++;
        		// get next group indices
        		if(regexIdx < namedPattern.length() && gIdx < groupIdx.size()) {
        			nextGroup = ArrayUtils.toPrimitive(groupIdx.get(gIdx));
        		}
        	} else {
        		// add character to non-group string
        		tempString = tempString + namedPattern.charAt(regexIdx);
        		regexIdx++;
        	}
        }
        // save last non-group string if there is any
        if(tempString.length() > 0)
			groupStrings.add(tempString);
		
        return groupStrings;
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(namedPattern).
	            toHashCode();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NamedPattern))
            return false;
        if (obj == this)
            return true;

        NamedPattern np = (NamedPattern) obj;
        return new EqualsBuilder().
            append(namedPattern, np.namedPattern()).
            isEquals();
	}

}
