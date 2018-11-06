package de.mpicbg.knime.hcs.core.math;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import de.mpicbg.knime.hcs.core.math.Interval.Mode;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Binning analysis for double arrays follows this concept:
 * TODO: enter description here
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 2/27/12
 * Time: 9:57 AM
 */

public class BinningAnalysis {

	// name of the parameter
	private String parameterName;

	// list of bins
	private LinkedList<Interval> bins;

	// reference data, Key: group (e.g. well), Values: data points of this group
	private HashMap<Object, List<Double>> refData;

	// Key: Interval, Values: Mean, Sd
	private HashMap<Interval, Double[]> refStats;

	// hashmap which contains the binning analysis results per group
	private HashMap<Object, List<BinningData>> zScoreData;

	// number of Bins
	int nBins;

	public BinningAnalysis(HashMap<Object, List<Double>> refData, int nBins, String parameterName) {
		this.parameterName = parameterName;
		this.nBins = nBins;

		this.bins = new LinkedList<Interval>();
		this.refStats = new HashMap<Interval, Double[]>();
		this.zScoreData = new HashMap<Object, List<BinningData>>();

		this.refData = refData;

		createBins();
		calculateRefStats();
	}

	public BinningAnalysis(HashMap<Object, List<Double>> refData, int nBins, String parameterName, boolean opt) {
		this.parameterName = parameterName;
		this.nBins = nBins;

		this.bins = new LinkedList<Interval>();
		this.refStats = new HashMap<Interval, Double[]>();
		this.zScoreData = new HashMap<Object, List<BinningData>>();

		this.refData = refData;

		createBins();

	}


	private void createBins() {
		double[] percentiles = new double[nBins + 1];

		double delta = (double) 100 / nBins;

		// create a sequence of breaks from 0 - 100 to calculate the percentiles
		percentiles[0] = 0;
		for (int i = 1; i < percentiles.length; i++) {
			percentiles[i] = percentiles[i - 1] + delta;
		}

		double[] data = {};

		// extract all data values into a double vector
		for (List<Double> values : refData.values()) {
			double[] groupData = new double[values.size()];
			int i = 0;
			for (Double val : values) {
				groupData[i] = val;
				i++;
			}
			data = ArrayUtils.addAll(data, groupData);
		}

		// Each bin is defined like an Interval with upper and lower bound
		double lowerBreak = NumberUtils.min(data);
		double upperBreak;

		// sort data
		Arrays.sort(data);

		// calculate the percentiles
		// comment: add minBin and maxBin [-Infinity,minRef] and [maxRef,Infinity] does not make sense
		// as the z-score could not be calculated for these bins (no data points present in referenceData)
		for (int i = 1; i < percentiles.length; i++) {

			// tried to use Percentile class of commons-Math but it was much too slow for the example data
			// therefor the calculation was reimplemented
			upperBreak = evalPercentile(percentiles[i], data);

			// only keep bin, if the bounds differ
			if (upperBreak > lowerBreak) {
				if(i == (percentiles.length - 1)){
					bins.add(new Interval(lowerBreak, upperBreak, percentiles[i] + "%", Mode.INCL_BOTH));
				}
				else {
					bins.add(new Interval(lowerBreak, upperBreak, percentiles[i] + "%", Mode.INCL_LEFT));
				}
			}



			lowerBreak = upperBreak;
		}
	}

	/**
	 * creates percentile labels based on the number of bins
	 * like [20%,40%,60%,80%,100%]
	 * starting from 0 + 100/nBins
	 * 
	 * @return String array with percentile labels
	 */
	public String[] getPercentileLabels() {
		String[] percentiles = new String[nBins];

		double delta = (double) 100 / nBins;

		// create a sequence of breaks from 0 - 100 to calculate the percentiles
		for (int i = 0; i < percentiles.length; i++) {
			if(i == 0)
				percentiles[i] = (delta) + "%";
			else 
				percentiles[i] = ((i + 1 ) * delta) + "%";
		}		
		return percentiles;
	}



	/**
	 * percentile implementation
	 * https://www.itl.nist.gov/div898/handbook/prc/section2/prc262.htm
	 * Engineering Statistics Handbook (NIST / SEMATECH)
	 * Chapter: 7.2.6.2. Percentiles
	 *
	 * @param p percentile to calculate, 0 <= p <= 100
	 * @return
	 */
	private double evalPercentile(double p, double[] data) {
		int n = data.length;
		double kd = p * (n + 1) / 100;
		double k = Math.floor(kd);
		double d = kd - k;

		if (Double.compare(k, 0) == 0) return data[0];
		if (Double.compare(k, n) >= 0) return data[n - 1];

		int ik = (int) k;
		return data[ik] + d * (data[ik + 1] - data[ik]);
	}

	/**
	 * Calculates the percentage of datapoints falling into each of the bins for each group
	 * and get statistics (mean + sd) of all values per bin
	 */
	private void calculateRefStats() {
		HashMap<Interval, List<Double>> ratios = new HashMap<Interval, List<Double>>();

		// iterate through all groups
		for (List<Double> values : refData.values()) {
			double n = values.size();

			// sort data acsending
			Collections.sort(values);

			// iterate through intervals, use right open intervals except for the last bin (use closed interval)
			int nBins = bins.size();
			Interval.Mode inclMode = Interval.Mode.INCL_LEFT;
			for (int i = 0; i < nBins; i++) {
				// current interval
				Interval iv = bins.get(i);
				// check for last interval
				if (i == nBins - 1) inclMode = Interval.Mode.INCL_BOTH;

				double[] ratio = calculateRatios(values, iv, inclMode, n);

				// update list: add the ratio of the current well
				List<Double> ratioList;
				if (ratios.containsKey(iv)) ratioList = ratios.get(iv);
				else ratioList = new ArrayList<Double>();

				ratioList.add(ratio[0]);
				ratios.put(iv, ratioList);
			}
		}

		// calculate mean and standard deviation
		for (Interval iv : ratios.keySet()) {
			List<Double> ivList = ratios.get(iv);

			DescriptiveStatistics stats = new DescriptiveStatistics();
			for (Double val : ivList) {
				stats.addValue(val);
			}
			// update statistics
			Double[] ivStats = {stats.getMean(), stats.getStandardDeviation()};
			refStats.put(iv, ivStats);
		}
	}

	private double[] calculateRatios(List<Double> values, Interval iv, Interval.Mode inclMode, double n) {
		// count values
		double m = 0;
		for (Double val : values) {
			if (iv.contains(val, inclMode)) m++;
		}

		return new double[]{m / n * 100, m};
	}

	/**
	 * @param data
	 * @return Key: Group, Value: a List of Measurements (with name and value)
	 *         interval
	 *         percentage
	 *         count
	 *         z-score
	 */
	public HashMap<Object, List<BinningData>> getZscore(HashMap<Object, List<Double>> data) {
		// clear zscore-list
		zScoreData.clear();

		// iterate over groups
		for (Object currentGroup : data.keySet()) {
			List<Double> values = data.get(currentGroup);
			double n = values.size();
			Collections.sort(values);

			// iterate through intervals, use right open intervals except for the last bin (use closed interval)
			int nBins = bins.size();
			Interval.Mode inclMode = Interval.Mode.INCL_LEFT;
			List<BinningData> entryList = new ArrayList<BinningData>();
			for (int i = 0; i < nBins; i++) {
				// current interval
				Interval iv = bins.get(i);

				// modify first and last interval that the bounds are extended to Infinity to catch all data points
				// last interval has to use a closed interval
				if (i == 0) iv.setLowerBound(Double.NEGATIVE_INFINITY);
				if (i == nBins - 1) {
					iv.setUpperBound(Double.POSITIVE_INFINITY);
					inclMode = Interval.Mode.INCL_BOTH;
				}

				double[] ratio = calculateRatios(values, iv, inclMode, n);
				double mean = refStats.get(iv)[0];
				double sd = refStats.get(iv)[1];

				double zscore = (ratio[0] - mean) / sd;

				BinningData entry = new BinningData(iv, zscore, ratio[0], ratio[1]);
				entryList.add(entry);
			}
			zScoreData.put(currentGroup, entryList);
		}
		return zScoreData;
	}

	public LinkedList<Interval> getBins() {
		return bins;
	}


	public String getParameterName() {
		return parameterName;
	}

	public static void main(String[] args) {

		/**
		 * This example reads in 252618 lines with 115 parameters = 29,556,306 data points
		 * To load the data into the memory approximately 1.4 GB RAM
		 * --> should be better to collect data and process parameter by parameter
		 * processing time : 13 seconds (1/4 th of the time needed by R (excluding time for data transfer from/to R-server)
		 */

		// test file located in resources/BinningAnalysis
		String fileLocation = "/Users/niederle/projects/knime/hcscore/resources/BinningAnalysis/exportData.csv";

		// parameter / well / values
		HashMap<String, HashMap<Object, List<Double>>> refData = new HashMap<String, HashMap<Object, List<Double>>>();
		HashMap<String, HashMap<Object, List<Double>>> allData = new HashMap<String, HashMap<Object, List<Double>>>();

		try {
			CSVReader reader = new CSVReader(new FileReader(fileLocation));
			String[] nextLine;
			List<Double> doubleList;


			// skip column header
			String[] parameters = reader.readNext();
			while ((nextLine = reader.readNext()) != null) {

				String well = nextLine[0];
				String compound = nextLine[1];

				for (int i = 2; i < nextLine.length; i++) {
					if (nextLine[i].equals("NA")) continue;
					double val = new Double(nextLine[i]);
					String param = parameters[i];

					// fill refData List
					if (compound.equals("DMSO")) {
						if (!refData.containsKey(param)) refData.put(param, new HashMap<Object, List<Double>>());

						if (refData.get(param).containsKey(well)) doubleList = refData.get(param).get(well);
						else doubleList = new ArrayList<Double>();

						doubleList.add(val);
						refData.get(param).put(well, doubleList);
					}

					// fill allData List
					if (!allData.containsKey(param)) allData.put(param, new HashMap<Object, List<Double>>());

					if (allData.get(param).containsKey(well)) doubleList = allData.get(param).get(well);
					else doubleList = new ArrayList<Double>();

					doubleList.add(val);
					allData.get(param).put(well, doubleList);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(new Date());
		HashMap<String, HashMap<Object, List<BinningData>>> binResults = new HashMap<String, HashMap<Object, List<BinningData>>>();
		for (String param : refData.keySet()) {
			BinningAnalysis binAnalysis = new BinningAnalysis(refData.get(param), 10, param);
			HashMap<Object, List<BinningData>> ret = binAnalysis.getZscore(allData.get(param));
			binResults.put(param, ret);
		}
		System.out.println(new Date());

		String destLocation = "/Users/niederle/Desktop/temp-files/proMebs_binData.csv";

		try {
			CSVWriter writer = new CSVWriter(new FileWriter(destLocation));
			ArrayList<String> nextLine = new ArrayList<String>();
			for (String parameter : binResults.keySet()) {
				System.out.println(parameter);
				for (Object well : binResults.get(parameter).keySet()) {
					for (BinningData binData : binResults.get(parameter).get(well)) {
						nextLine.add(parameter);
						nextLine.add((String) well);
						nextLine.add(binData.getInterval().getLabel());
						nextLine.add(Double.toString(binData.getPercentage()));
						nextLine.add(Double.toString(binData.getZscore()));
						nextLine.add(Double.toString(binData.getCount()));

						writer.writeNext(nextLine.toArray(new String[nextLine.size()]));
						nextLine.clear();
					}
				}
			}
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		// just useful to place a breakpoint for debugging
		int k = 0;
	}
}
