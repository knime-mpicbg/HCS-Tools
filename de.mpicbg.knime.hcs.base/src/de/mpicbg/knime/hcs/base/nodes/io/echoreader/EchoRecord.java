package de.mpicbg.knime.hcs.base.nodes.io.echoreader;

import org.apache.commons.lang.StringUtils;

/**
 * the class acts as container for the content of a single echo file record
 * 
 * @author Magda Rucinska
 *
 */
public class EchoRecord {

	private  String SrcPlateName;
	private  String SrcPlateBarcode;
	private  String SrcWell;
	private  String DestPlateName;
	private  String DestPlateBarcode;
	private  String DestWell;
	private  String XferVol;
	private  String ActualVol;
	private  String CurrentFluidVolume;
	private  String FluidComposition;
	private  String FluidUnits;
	private  String FluidType;
	private  String XferStatus;

	/**
	 * @return barcode of the source plate
	 */
	public String getSrcPlateBarcode() {
		if(SrcPlateBarcode.equals("")){
			SrcPlateBarcode = "Missing value";	
		}
		return SrcPlateBarcode; 
	}

	/**
	 * set barcode of source plate
	 * @param srcPlateBarcode
	 */
	public void setSrcPlateBarcode(String srcPlateBarcode) {
		SrcPlateBarcode = StringUtils.trim(srcPlateBarcode);
	}

	/**
	 * @return name of the source plate
	 */
	public String getSrcPlateName() {
		if(SrcPlateName.equals("")){
			SrcPlateName = "Missing value";	
		}
		return SrcPlateName;
	}

	/**
	 * set name of source plate
	 * @param srcPlateName
	 */
	public void setSrcPlateName(String srcPlateName) {
		SrcPlateName = StringUtils.trim(srcPlateName);
	}

	/**
	 * @return source well
	 */
	public String getSrcWell() {
		if(SrcWell.equals("")){
			SrcWell = "Missing value";	
		}
		return SrcWell;
	}

	/**
	 * set source well
	 * @param srcWell
	 */
	public void setSrcWell(String srcWell) {
		SrcWell = StringUtils.trim(srcWell);
	}

	/**
	 * @return name of the destination plate
	 */
	public String getDestPlateName() {
		if(DestPlateName.equals("")){
			DestPlateName = "Missing value";	
		}
		return DestPlateName;
	}
	
	/**
	 * set name of destination plate
	 * @param destPlateName
	 */
	public void setDestPlateName(String destPlateName) {
		DestPlateName = StringUtils.trim(destPlateName);
	}

	/**
	 * @return destination plate barcode
	 */
	public String getDestPlateBarcode() {
		if(DestPlateBarcode.equals("")){
			DestPlateBarcode = "Missing value";	
		}
		return DestPlateBarcode;
	}

	/**
	 * set desintation plate barcode
	 * @param destPlateBarcode
	 */
	public void setDestPlateBarcode(String destPlateBarcode) {
		DestPlateBarcode = StringUtils.trim(destPlateBarcode);
	}

	/**
	 * @return destination well
	 */
	public String getDestWell() {
		if(DestWell.equals("")){
			DestWell = "Missing value";	
		}
		return DestWell;
	}

	/**
	 * set destination well
	 * @param destWell
	 */
	public void setDestWell(String destWell) {
		DestWell = StringUtils.trim(destWell);
	}

	/**
	 * @return transfer volume
	 */
	public String getXferVol() {
		if(XferVol.equals("")){
			XferVol = "Missing value";	
		}
		return XferVol;
	}

	/**
	 * get transfer volume
	 * @param xferVol
	 */
	public void setXferVol(String xferVol) {
		XferVol = StringUtils.trim(xferVol);
	}

	/**
	 * @return actual volume
	 */
	public String getActualVol() {
		if(ActualVol.equals("")){
			ActualVol = "Missing value";	
		}
		return ActualVol;
	}

	/**
	 * set actual volume
	 * @param actualVol
	 */
	public void setActualVol(String actualVol) {
		ActualVol = StringUtils.trim(actualVol);
	}

	/**
	 * @return current fluid volume
	 */
	public String getCurrentFluidVolume() {
		if(CurrentFluidVolume.equals("")){
			CurrentFluidVolume = "Missing value";	
		}
		return CurrentFluidVolume;
	}

	/**
	 * set current fluid volume
	 * @param currentFluidVolume
	 */
	public void setCurrentFluidVolume(String currentFluidVolume) {
		CurrentFluidVolume = StringUtils.trim(currentFluidVolume);
	}

	/**
	 * @return fluid composition
	 */
	public String getFluidComposition() {
		if(FluidComposition.equals("")){
			FluidComposition = "Missing value";	
		}
		return FluidComposition;
	}

	/**
	 * set fluid composition
	 * @param fluidComposition
	 */
	public void setFluidComposition(String fluidComposition) {
		FluidComposition = StringUtils.trim(fluidComposition);
	}

	/**
	 * @return fluid units
	 */
	public String getFluidUnits() {
		if(FluidUnits.equals("")){
			FluidUnits = "Missing value";	
		}
		return FluidUnits;
	}

	/**
	 * set fluid units
	 * @param fluidUnits
	 */
	public void setFluidUnits(String fluidUnits) {
		FluidUnits = StringUtils.trim(fluidUnits);
	}

	/**
	 * @return fluid type
	 */
	public String getFluidType() {
		if(FluidType.equals("")){
			FluidType = "Missing value";	
		}
		return FluidType;
	}

	/**
	 * set fluid type
	 * @param fluidType
	 */
	public void setFluidType(String fluidType) {
		FluidType = StringUtils.trim(fluidType);
	}

	/**
	 * @return transfer status
	 */
	public String getXferStatus() {
		if(XferStatus.equals("")){
			XferStatus = "ok";	
		}
		return XferStatus;
	}

	/**
	 * set transfer status
	 * @param xferStatus
	 */
	public void setXferStatus(String xferStatus) {
		XferStatus = StringUtils.trim(xferStatus);
	}

}
