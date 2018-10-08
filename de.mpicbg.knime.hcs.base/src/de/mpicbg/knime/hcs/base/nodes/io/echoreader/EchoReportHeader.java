package de.mpicbg.knime.hcs.base.nodes.io.echoreader;

/**
 * this class holds the header information of an echo file
 * 
 * @author Magda Rucinska
 *
 */
public class EchoReportHeader {

	private String runID;
	private  String runDateTime;
	private  String appName;
	private  String appVersion;
	private  String protocolName;
	private  String UserName;

	/**
	 * @return run ID
	 */
	public String getRunID() {
		return runID;
	}
	
	/**
	 * set run ID
	 * @param runID
	 */
	public void setRunID(String runID) {
		this.runID = runID;
	}
	
	/**
	 * @return run date/time
	 */
	public String getRunDateTime() {
		return runDateTime;
	}
	
	/**
	 * set run date/time
	 * @param runDateTime
	 */
	public void setRunDateTime(String runDateTime) {
		this.runDateTime = runDateTime;
	}
	
	/**
	 * @return app name
	 */
	public String getAppName() {
		return appName;
	}
	
	/**
	 * set app name
	 * @param appName
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	/**
	 * @return app version
	 */
	public String getAppVersion() {
		return appVersion;
	}
	
	/**
	 * set app version
	 * @param appVersion
	 */
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	
	/**
	 * @return protocol name
	 */
	public String getProtocolName() {
		return protocolName;
	}
	
	/**
	 * set protocol name
	 * @param protocolName
	 */
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}
	
	/**
	 * @return user name
	 */
	public String getUserName() {
		return UserName;
	}
	
	/**
	 * set user name
	 * @param userName
	 */
	public void setUserName(String userName) {
		UserName = userName;
	}
}
