####
# Estiamtes a set of dr-models in a robust fashion
# @Deprecated

calcDRModels <- function(drdata, readoutIndices, treatmentIndex, concIndex, model = LL.4()){


    conditions <- unique(drdata[, treatmentIndex]);


	# readoutIndex = readoutIndices[1];
	drmodels <- lapply(readoutIndices, FUN = function(readoutIndex){
		#readoutIndex=1
	    subDRData <- data.frame(meas = drdata[,readoutIndex], treatment = drdata[,treatmentIndex], conc = drdata[,concIndex]);
		drcmodel <- try(drm(meas ~ conc, treatment, data= subDRData, fct= model), silent=TRUE);
		if(inherits(drcmodel, "try-error")){
			return(subDRData);
		}else{
			# put it into our output datastrucutre
			return(drcmodel);
		}
	});

	names(drmodels) <- names(drdata)[readoutIndices];

    return(drmodels);
}


