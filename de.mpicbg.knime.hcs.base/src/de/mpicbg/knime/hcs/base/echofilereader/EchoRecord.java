package de.mpicbg.knime.hcs.base.echofilereader;

import org.apache.commons.lang.StringUtils;

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

	public String getSrcPlateBarcode() {
		if(SrcPlateBarcode.equals("")){
			SrcPlateBarcode = "Missing value";	
		}
		return SrcPlateBarcode;
	}

	public void setSrcPlateBarcode(String srcPlateBarcode) {
		SrcPlateBarcode = StringUtils.trim(srcPlateBarcode);
	}

	public String getSrcPlateName() {
		if(SrcPlateName.equals("")){
			SrcPlateName = "Missing value";	
		}
		return SrcPlateName;
	}

	public void setSrcPlateName(String srcPlateName) {
		SrcPlateName = StringUtils.trim(srcPlateName);
	}

	public String getSrcWell() {
		if(SrcWell.equals("")){
			SrcWell = "Missing value";	
		}
		return SrcWell;
	}

	public void setSrcWell(String srcWell) {
		SrcWell = StringUtils.trim(srcWell);
	}

	public String getDestPlateName() {
		if(DestPlateName.equals("")){
			DestPlateName = "Missing value";	
		}
		return DestPlateName;
	}

	public void setDestPlateName(String destPlateName) {
		DestPlateName = StringUtils.trim(destPlateName);
	}

	public String getDestPlateBarcode() {
		if(DestPlateBarcode.equals("")){
			DestPlateBarcode = "Missing value";	
		}
		return DestPlateBarcode;
	}

	public void setDestPlateBarcode(String destPlateBarcode) {
		DestPlateBarcode = StringUtils.trim(destPlateBarcode);
	}

	public String getDestWell() {
		if(DestWell.equals("")){
			DestWell = "Missing value";	
		}
		return DestWell;
	}

	public void setDestWell(String destWell) {
		DestWell = StringUtils.trim(destWell);
	}

	public String getXferVol() {
		if(XferVol.equals("")){
			XferVol = "Missing value";	
		}
		return XferVol;
	}

	public void setXferVol(String xferVol) {
		XferVol = StringUtils.trim(xferVol);
	}

	public String getActualVol() {
		if(ActualVol.equals("")){
			ActualVol = "Missing value";	
		}
		return ActualVol;
	}

	public void setActualVol(String actualVol) {
		ActualVol = StringUtils.trim(actualVol);
	}

	public String getCurrentFluidVolume() {
		if(CurrentFluidVolume.equals("")){
			CurrentFluidVolume = "Missing value";	
		}
		return CurrentFluidVolume;
	}

	public void setCurrentFluidVolume(String currentFluidVolume) {
		CurrentFluidVolume = StringUtils.trim(currentFluidVolume);
	}

	public String getFluidComposition() {
		if(FluidComposition.equals("")){
			FluidComposition = "Missing value";	
		}
		return FluidComposition;
	}

	public void setFluidComposition(String fluidComposition) {
		FluidComposition = StringUtils.trim(fluidComposition);
	}

	public String getFluidUnits() {
		if(FluidUnits.equals("")){
			FluidUnits = "Missing value";	
		}
		return FluidUnits;
	}

	public void setFluidUnits(String fluidUnits) {
		FluidUnits = StringUtils.trim(fluidUnits);
	}

	public String getFluidType() {
		if(FluidType.equals("")){
			FluidType = "Missing value";	
		}
		return FluidType;
	}

	public void setFluidType(String fluidType) {
		FluidType = StringUtils.trim(fluidType);
	}

	public String getXferStatus() {
		if(XferStatus.equals("")){
			XferStatus = "ok";	
		}
		return XferStatus;
	}

	public void setXferStatus(String xferStatus) {

		XferStatus = StringUtils.trim(xferStatus);



	}

}
