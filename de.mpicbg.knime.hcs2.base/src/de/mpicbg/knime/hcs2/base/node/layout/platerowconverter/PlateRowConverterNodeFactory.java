package de.mpicbg.knime.hcs2.base.node.layout.platerowconverter;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;


public class PlateRowConverterNodeFactory extends DefaultNodeFactory {
	
	private static final DefaultNode NODE = DefaultNode.create() 
	        .name("Plate Row Converter") 
	        .icon("PlateRowConverter.png") 
	        .shortDescription("Converts plate row characters to numbers and vice versa") 
	        .fullDescription("""
	        		<p>
	        		Converts plate row characters to numbers and vice versa. The node automatically detects the required direction of conversion based on the type of the selected source columns (numeric or alphabetic). 
	        		Row characters (e.g. B or C) will be replaced with their corresponding row number (e.g. 2 or 3) or vice versa.<br/>
The output column can either replace the input column or it can be appended to the input table (advanced setting).
	        		</p>   
	        		<ul>
	        		<li>row position may be numeric or up to two letters (case insensitive) </li>
	        		<li>supports plate formats up to 1536 well plates </li>
	        		</ul>  
	        		<p>Examples:</p>
					<table>
					
					<tr>
					<th>plateRow (input / characters)</th>
					<th>plateRow (output / numbers)</th>
					</tr>
					
					<tr>
					<td>A</td>
					<td>1</td>
					</tr>
					<tr>
					<td>C</td>
					<td>3</td>
					</tr>
					<tr>
					<td>af</td>
					<td>32</td>
					</tr>
					
					</table>
	                   """) //
	        .sinceVersion(5, 8, 0)
	        .ports(p -> p
	            .addInputTable("Input table", "Table with a column containing plate row identifiers") 
	            .addOutputTable("Output table", "Input table with converted plate row identifiers<br/>"
	            		+ "(either as an replacement of the input column or as additional column)") 
	        )
	        .model(m -> m 
	            .parametersClass(PlateRowConverterNodeSettings.class)
	            .rearrangeColumns(PlateRowConverterNodeModel::rearrangeColumns) );

	public PlateRowConverterNodeFactory() {
		super(NODE);
	}

}
