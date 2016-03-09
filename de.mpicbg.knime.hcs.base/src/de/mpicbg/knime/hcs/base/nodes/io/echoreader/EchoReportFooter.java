package de.mpicbg.knime.hcs.base.nodes.io.echoreader;

/**
 * this class holds the footer information of an echo file
 * 
 * @author Magda Rucinska
 *
 */
public class EchoReportFooter {

	private  String InstrName;
	private  String InstrModel;
	private  String InstrSN;
	private  String InstrSWVersion;

	/**
	 * @return instrument name
	 */
	public String getInstrName() {
		return InstrName;
	}

	/**
	 * set instrument name
	 * @param instrName
	 */
	public void setInstrName(String instrName) {
		InstrName = instrName;
	}

	/**
	 * @return instrument model
	 */
	public String getInstrModel() {
		return InstrModel;
	}

	/**
	 * set instrument model
	 * @param instrModel
	 */
	public void setInstrModel(String instrModel) {
		InstrModel = instrModel;
	}

	/**
	 * @return instrument serial number
	 */
	public String getInstrSN() {
		return InstrSN;
	}

	/**
	 * set instrument serial number
	 * @param instrSN
	 */
	public void setInstrSN(String instrSN) {
		InstrSN = instrSN;
	}

	/**
	 * @return instrument software version
	 */
	public String getInstrSWVersion() {
		return InstrSWVersion;
	}

	/**
	 * set instrument software version
	 * @param instrSWVersion
	 */
	public void setInstrSWVersion(String instrSWVersion) {
		InstrSWVersion = instrSWVersion;
	}
}
