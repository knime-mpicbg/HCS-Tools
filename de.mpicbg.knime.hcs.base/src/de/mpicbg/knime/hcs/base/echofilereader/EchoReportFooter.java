package de.mpicbg.knime.hcs.base.echofilereader;

import java.util.LinkedList;
import java.util.List;

public class EchoReportFooter {

	private  String InstrName;
    private  String InstrModel;
	private  String InstrSN;
    private  String InstrSWVersion;
    
    public static final List<EchoReportFooter> footers = new LinkedList<EchoReportFooter>();
    
    public String getInstrName() {
		return InstrName;
	}

	public void setInstrName(String instrName) {
		InstrName = instrName;
	}

	public String getInstrModel() {
		return InstrModel;
	}

	public void setInstrModel(String instrModel) {
		InstrModel = instrModel;
	}

	public String getInstrSN() {
		return InstrSN;
	}

	public void setInstrSN(String instrSN) {
		InstrSN = instrSN;
	}

	public String getInstrSWVersion() {
		return InstrSWVersion;
	}

	public void setInstrSWVersion(String instrSWVersion) {
		InstrSWVersion = instrSWVersion;
	}
}
