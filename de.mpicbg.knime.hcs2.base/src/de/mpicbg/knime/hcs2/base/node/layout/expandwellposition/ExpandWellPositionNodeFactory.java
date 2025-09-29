/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 */
package de.mpicbg.knime.hcs2.base.node.layout.expandwellposition;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;

/** 
 * Node Factory for the "Expand Well Position" node. 
 **/
public final class ExpandWellPositionNodeFactory extends DefaultNodeFactory {

    private static final DefaultNode NODE = DefaultNode.create() 
        .name("Expand Well Position") 
        .icon("ExpandWellPosition.png") 
        .shortDescription("Splits an input column containing plate well positions into two columns (plate row position, plate column position)") 
        .fullDescription("""
        		<p>
        		This node expects a string input column containing well positions. Expected well position format:
        		<ul>
        		<li>row position may be one or two letters</li>
        		<li>column position may be one or two numbers</li>
        		<li>supports plate formats up to 1536 well plates</li> 
        		</ul>
        		Examples: E01, c7, AE12    
        		</p>   
        		<p>
        		Two new columns will be created containing the plate row identifier and plate column identifier respectively.
        		</p>        
                   """) //
        .sinceVersion(5, 8, 0)
        .ports(p -> p
            .addInputTable("Input table", "Table with column containing well position to split") 
            .addOutputTable("Output table", "Table with additional columns for plate row and plate column") 
        )
        .model(m -> m 
            .parametersClass(ExpandWellPositionNodeSettings.class)
            .rearrangeColumns(ExpandWellPositionNodeModel::rearrangeColumns) );

    /**
     * Default constructor for the node factory.
     */
    public ExpandWellPositionNodeFactory() {
        super(NODE);
    }
}
