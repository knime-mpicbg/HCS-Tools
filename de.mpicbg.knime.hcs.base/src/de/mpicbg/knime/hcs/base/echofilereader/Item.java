package de.mpicbg.knime.hcs.base.echofilereader;

public class Item {
	private String SrcPlateName; 
	  private String SrcPlateBarcode;
	  private String SrcWell;
	  private String DestPlateName;
	  private String DestPlateBarcode;
	  private String DestWell;
	  private String XferVol;
	  private String ActualVol;
	  private String CurrentFluidVolume;
	  private String FluidComposition;
	  private String FluidUnits;
	  private String FluidType;
	  private String XferStatus;
	  
	  
	  public String getSrcPlateName() {
	    return SrcPlateName;
	  }
	  
	  public void setSrcPlateName(String SrcPlateName) {
	    this.SrcPlateName = SrcPlateName;
	  }
	  public String getSrcPlateBarcode() {
	    return SrcPlateBarcode;
	  }
	  public void setSrcPlateBarcode(String SrcPlateBarcode) {
	    this.SrcPlateBarcode = SrcPlateBarcode;
	  }
	  public String getSrcWell() {
	    return SrcWell;
	  }
	  public void setSrcWell(String SrcWell) {
	    this.SrcWell = SrcWell;
	  }
	  public String getDestPlateName() {
	    return DestPlateName;
	  }
	  public void setDestPlateName(String DestPlateName) {
	    this.DestPlateName = DestPlateName;
	  }
	  public String getDestPlateBarcode() {
	    return DestPlateBarcode;
	  }
	  public void setDestPlateBarcode(String DestPlateBarcode) {
	    this.DestPlateBarcode = DestPlateBarcode;
	  }
	  public String getDestWell() {
		    return DestWell;
		  }
	  public void setDestWell(String DestWell) {
		    this.DestWell = DestWell;
		  } 
	  public String getXferVol() {
			    return XferVol;
		  }
	  public void setXferVol(String XferVol) {
		    this.XferVol = XferVol;
		  } 
	  public String getActualVol() {
			    return ActualVol;
		  }
	  public void setActualVol(String ActualVol) {
		    this.ActualVol = ActualVol;
		  }
	  public String getCurrentFluidVolume() {
			    return CurrentFluidVolume;
			  }
	  public void setCurrentFluidVolume(String CurrentFluidVolume) {
			    this.CurrentFluidVolume = CurrentFluidVolume;
			  }
	  public String getFluidComposition() {
				    return FluidComposition;
				  }
	  public void setFluidComposition(String FluidComposition) {
				    this.FluidComposition = FluidComposition;
				  }
	  public String getFluidUnits() {
					    return FluidUnits;
					  }
	  public void setFluidUnits(String FluidUnits) {
					    this.FluidUnits = FluidUnits;
					    }
	  public String getFluidType() {
						    return FluidType;
						  }
	  public void setFluidType(String FluidType) {
						    this.FluidType = FluidType;
						    }
	  public String getXferStatus() {
							    return XferStatus;
				}
	  public void setXferStatus(String XferStatus) {
							    this.XferStatus = XferStatus;
							    }
		  
}
