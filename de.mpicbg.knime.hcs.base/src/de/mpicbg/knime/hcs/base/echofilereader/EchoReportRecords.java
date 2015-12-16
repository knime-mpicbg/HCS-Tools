package de.mpicbg.knime.hcs.base.echofilereader;

import java.util.LinkedList;
import java.util.List;

public class EchoReportRecords {
	
	
	
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
	
    public static List<EchoReportRecords> records = new LinkedList<EchoReportRecords>();
    	
    	
		public String getSrcPlateBarcode() {
			return SrcPlateBarcode;
		}

		public void setSrcPlateBarcode(String srcPlateBarcode) {
			SrcPlateBarcode = srcPlateBarcode;
		}

		public String getSrcPlateName() {
			return SrcPlateName;
		}

		public void setSrcPlateName(String srcPlateName) {
			SrcPlateName = srcPlateName;
		}

		public String getSrcWell() {
			return SrcWell;
		}

		public void setSrcWell(String srcWell) {
			SrcWell = srcWell;
		}

		public String getDestPlateName() {
			return DestPlateName;
		}

		public void setDestPlateName(String destPlateName) {
			DestPlateName = destPlateName;
		}

		public String getDestPlateBarcode() {
			return DestPlateBarcode;
		}

		public void setDestPlateBarcode(String destPlateBarcode) {
			DestPlateBarcode = destPlateBarcode;
		}

		public String getDestWell() {
			return DestWell;
		}

		public void setDestWell(String destWell) {
			DestWell = destWell;
		}

		public String getXferVol() {
			return XferVol;
		}

		public void setXferVol(String xferVol) {
			XferVol = xferVol;
		}

		public String getActualVol() {
			return ActualVol;
		}

		public void setActualVol(String actualVol) {
			ActualVol = actualVol;
		}

		public String getCurrentFluidVolume() {
			return CurrentFluidVolume;
		}

		public void setCurrentFluidVolume(String currentFluidVolume) {
			CurrentFluidVolume = currentFluidVolume;
		}

		public String getFluidComposition() {
			return FluidComposition;
		}

		public void setFluidComposition(String fluidComposition) {
			FluidComposition = fluidComposition;
		}

		public String getFluidUnits() {
			return FluidUnits;
		}

		public void setFluidUnits(String fluidUnits) {
			FluidUnits = fluidUnits;
		}

		public String getFluidType() {
			return FluidType;
		}

		public void setFluidType(String fluidType) {
			FluidType = fluidType;
		}

		public String getXferStatus() {
			return XferStatus;
		}

		public void setXferStatus(String xferStatus) {
			XferStatus = xferStatus;
		}
		
}
