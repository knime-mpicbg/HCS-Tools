/**
 * 
 */
package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;
import de.mpicbg.knime.hcs.core.math.BinningAnalysis;
import de.mpicbg.knime.hcs.core.math.BinningData;
import de.mpicbg.knime.hcs.core.math.Interval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.knime.base.node.preproc.autobinner.pmml.DisretizeConfiguration;
import org.knime.base.node.preproc.autobinner.pmml.PMMLDiscretize;
import org.knime.base.node.preproc.autobinner.pmml.PMMLDiscretizeBin;
import org.knime.base.node.preproc.autobinner.pmml.PMMLInterval;
import org.knime.base.node.preproc.autobinner.pmml.PMMLPreprocDiscretize;
import org.knime.base.node.preproc.autobinner.pmml.PMMLInterval.Closure;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.port.pmml.preproc.PMMLPreprocPortObjectSpec;

/**
 * @author nicolais
 *
 */
public class BinningCalculate extends BinningAnalysis {

	private String parameter;

	public BinningCalculate(HashMap<Object, List<Double>> refData, int nBins, String parameterName) {
		super(refData, nBins, parameterName);
		this.parameter = parameterName;
		// TODO Auto-generated constructor stub
	}

	private PMMLPreprocPortObjectSpec m_pmmlOutSpec;
	private DataTableSpec m_tableOutSpec;

	private Map<String, List<PMMLDiscretizeBin>> ConvBinFormate(HashMap<Object, List<Double>> refData) 
	{


		// Getting generate Bins 
		LinkedList<Interval> Bins = getBins();

		// 
		Map<String, List<PMMLDiscretizeBin>> binMap = new HashMap<String, List<PMMLDiscretizeBin>>();

		List<PMMLDiscretizeBin> PMMLDiscretBins = new ArrayList<PMMLDiscretizeBin>();
		int count = 0;           
		for(Interval Bin : Bins	)
		{

			double lbound = Bin.getLowerBound();
			double ubound = Bin.getLowerBound();

			if(count == Bins.size() - 1)
			{
				PMMLDiscretBins.add(new PMMLDiscretizeBin("Bin_" + count,
						Arrays.asList(new PMMLInterval(lbound, ubound,Closure.closedClosed))));
			}

			PMMLDiscretBins.add(new PMMLDiscretizeBin("Bin_" + count,
					Arrays.asList(new PMMLInterval(lbound, ubound,Closure.closedOpen))));
		}

		return binMap;
	}


	
}