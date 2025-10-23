package de.mpicbg.knime.hcs2.base.node.preproc.numberformatter;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;

public class NumberFormatterNodeFactory extends DefaultNodeFactory {
	
	private static final DefaultNode NODE = DefaultNode.create() 
	        .name("Number Formatter") 
	        .icon("...") 
	        .shortDescription("...") 
	        .fullDescription("""
	        		...
	                   """) //
	        .sinceVersion(5, 8, 0)
	        .ports(p -> p
	            .addInputTable("Input table", "...") 
	            .addOutputTable("Output table", "...") 
	        )
	        .model(m -> m 
	            .parametersClass(NumberFormatterNodeSettings.class)
	            .rearrangeColumns(NumberFormatterNodeModel::rearrangeColumns) );

	public NumberFormatterNodeFactory() {
		super(NODE);
	}

}
