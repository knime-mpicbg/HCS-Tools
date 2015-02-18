package de.mpicbg.knime.hcs.core.barcodes.namedregexp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
	 * pattern to define group names like (?<mygroupname>
	 */
    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(\\w+)>");

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

}
