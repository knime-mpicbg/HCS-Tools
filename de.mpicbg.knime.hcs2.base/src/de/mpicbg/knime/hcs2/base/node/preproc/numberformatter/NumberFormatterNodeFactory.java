package de.mpicbg.knime.hcs2.base.node.preproc.numberformatter;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;

public class NumberFormatterNodeFactory extends DefaultNodeFactory {
	
	private static final DefaultNode NODE = DefaultNode.create() 
	        .name("Number Formatter") 
	        .icon("NumberFormatterIcon.png") 
	        .shortDescription("Formats all numbers of a given input column") 
	        .fullDescription("""
	        		<p>This node applies formatting rules to numbers. The input column may be numeric or String. Cell content which can be recognized as numbers will be formatted.</p>
<p>Numbers can be formatted either with decimal notation or with scientific notation. To increase readability digit grouping can be applied. Additionally it&#39;s possible to set the output width by adding leading characters and/or restricting the numbers of decimals.</p>
<p><strong>Possible Outputs:</strong></p>
<table>
<thead>
<tr>
<th><strong>Input</strong></th>
<th><strong>Output 1</strong></th>
<th><strong>Ouput 2</strong></th>
<th><strong>Output 3</strong></th>
</tr>
</thead>
<tbody>
<tr>
<td>0.5</td>
<td>001</td>
<td>00.50 €</td>
<td>+5.0E-1</td>
</tr>
<tr>
<td>7.11</td>
<td>007</td>
<td>07.11 €</td>
<td>+7.1E+0</td>
</tr>
<tr>
<td>-99.9</td>
<td>-100</td>
<td>-99.90 €</td>
<td>-1.0E+2</td>
</tr>
</tbody>
</table>
	                   """) //
	        .sinceVersion(5, 8, 0)
	        .ports(p -> p
	            .addInputTable("Input table", "The input table containing numeric values to format.") 
	            .addOutputTable("Output table", "Input table with formatted numbers<br/>"
	            		+ "(either as an replacement of the input column or as additional column)") 
	        )
	        .model(m -> m 
	            .parametersClass(NumberFormatterNodeSettings.class)
	            .rearrangeColumns(NumberFormatterNodeModel::rearrangeColumns) );

	public NumberFormatterNodeFactory() {
		super(NODE);
	}

}
