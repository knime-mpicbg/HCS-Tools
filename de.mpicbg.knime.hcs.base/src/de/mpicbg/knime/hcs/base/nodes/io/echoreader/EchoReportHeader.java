package de.mpicbg.knime.hcs.base.echofilereader;

import java.util.LinkedList;
import java.util.List;

public class EchoReportHeader {
	
			private String runID;
		    private  String runDateTime;
			private  String appName;
		    private  String appVersion;
		    private  String protocolName;
		    private  String UserName;
		    
		    
			public String getRunID() {
				return runID;
			}
			public void setRunID(String runID) {
				this.runID = runID;
			}
			 public String getRunDateTime() {
				return runDateTime;
			}
			public void setRunDateTime(String runDateTime) {
				this.runDateTime = runDateTime;
			}
			public String getAppName() {
				return appName;
			}
			public void setAppName(String appName) {
				this.appName = appName;
			}
			public String getAppVersion() {
				return appVersion;
			}
			public void setAppVersion(String appVersion) {
				this.appVersion = appVersion;
			}
			public String getProtocolName() {
				return protocolName;
			}
			public void setProtocolName(String protocolName) {
				this.protocolName = protocolName;
			}
			public String getUserName() {
				return UserName;
			}
			public void setUserName(String userName) {
				UserName = userName;
			}
			//public static final List<EchoReportHeader> headers = new LinkedList<EchoReportHeader>();
			
			
}
