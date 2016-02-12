library(drc)
library(ggplot2)

# This needs to be used if the drc-plotting mechanism should be used
calcDRModels <- function(drdata, readoutIndices, treatmentIndex, concIndex, model = LL.4(), ...){


    conditions <- unique(drdata[, treatmentIndex])


	# readoutIndex = readoutIndices[1]
	drmodels <- lapply(readoutIndices, FUN = function(readoutIndex){
		#readoutIndex=1
	    subDRData <- data.frame(meas = drdata[,readoutIndex], treatment = drdata[,treatmentIndex], conc = drdata[,concIndex])
		drcmodel <- try(drm(meas ~ conc, treatment, data= subDRData, fct= model), silent=TRUE, ...)
		if(inherits(drcmodel, "try-error")){
			return(subDRData)
		}else{
			# put it into our output datastrucutre
			return(drcmodel)
		}
	})

	names(drmodels) <- names(drdata)[readoutIndices]

    return(drmodels)
}



# This needs to be used if need an easier access to the plot parameters.
calcDRModelsListList <- function(drdata, readoutIndices, treatmentIndex, concIndex, model = LL.4(), constraints = list(NULL, NULL), ...){

    conditions <- unique(drdata[, treatmentIndex])
	#browser()
	drmodels <- lapply(conditions, FUN = function(con){

		drmodel <- lapply(readoutIndices, FUN = function(readoutIndex){
    	    # create a temporary dataset
			subDRData <- drdata[ drdata[, treatmentIndex] == con,c(readoutIndex,concIndex)]
    	    names(subDRData) <- c("meas", "conc")
			
            ## did not work as expected so we just disabled it
            # lowerParConstr = constraints[[1]]
            # upperParConstr = constraints[[2]]
            # drcmodel <- try(drm(meas ~ conc, data= subDRData, fct= model, lowerl=c(10, 50,-Inf,-Inf), upperl=upperParConstr, ...))

			drcmodel <- tryCatch( { 
				drcmodel <- drm(meas ~ conc, data= subDRData, fct= model, ...)
				predict(drcmodel)
				drcmodel
			}, error = function(e) { NA } )

		})

		names(drmodel) <- names(drdata)[readoutIndices]
		return(drmodel)
    })

    names(drmodels) <- conditions
    return(drmodels)
}



#######
# Inverts the structure of a dr-model table (form conditions in rows and respVars in columns to vv.)
pivotModelTable <- function(drmodels){
	compoundNames <- names(drmodels)
	respVarsNames <- names(drmodels[[1]])


	# iterate over all response variables
	pivotList <- lapply(respVarsNames, FUN = function(resVarName){

		# first gather the list of all affected models
		resVarPlotData <- lapply(compoundNames, FUN = function(cmpndName){
			cmpdModels <- drmodels[[cmpndName]]
			return(cmpdModels[[resVarName]])
		})

		names(resVarPlotData) <- compoundNames

		return(resVarPlotData)
	})
	names(pivotList) <- respVarsNames

	return(pivotList)
}

# calculate standard error of the mean (SEM)
sem <- function(x) sqrt(var(x,na.rm=TRUE)/length(na.omit(x)))


###############################################################
###################### Visualization methods #####################
###############################################################

plotDRModels <- function(drmodels){
	numResponsVars = length(drmodels)
	
	numGridRows = ifelse(numResponsVars > 1, 2, 1)
	par(mfrow = c(numGridRows, ceiling(numResponsVars/ numGridRows)))
	
	lapply(1: numResponsVars, FUN = function(drmodelIndex){
		responseVarName <- names(drmodels)[drmodelIndex]
		drmodel = drmodels[[drmodelIndex]]
		# TODO extract the doseName from the drmodels objects
		if(!inherits(drmodel, 'drc')){
			# plot the raw data instead
			plot(1:10, main= paste('Fitting failed for ', responseVarName))
					
		}else{
			plot(drmodel, type='bars',  col=TRUE, ylab= responseVarName, xlab='Concentration')
		}
	})
} 
#plotDRModels(drmodels)




visualizeDrModels <- function(plotdata, plotOpts) {
	numResVars <- length(plotdata)
	
	#grid with ratio 3:2 (rows:columns)
	numGridRows <- ceiling(sqrt(1.5*numResVars))
	numGridCols <- ceiling(numResVars/numGridRows)
	
	doPlotDots <- plotOpts$plotOptions == "Data points"
	xLab <- plotOpts$plotLabels["xLab"]
	legendLab <- plotOpts$plotLabels["legendLab"]
	title <- plotOpts$plotLabels["title"]
	mName <- plotOpts$mName

	# reshape data
	rawData <- lapply(names(plotdata), FUN = function(respVar) {

		if(!is.logical(plotdata[[respVar]]$rawdata)) plotdata[[respVar]]$rawdata$respVarName <- respVar
		if(!is.logical(plotdata[[respVar]]$smoothfitfuns)) {
			plotdata[[respVar]]$smoothfitfuns$respVarName <- respVar
			plotdata[[respVar]]$rawdata$plotDots <- doPlotDots
			} else {
			plotdata[[respVar]]$rawdata$plotDots <- TRUE
		}
		if(!is.logical(plotdata[[respVar]]$errorbardata)) plotdata[[respVar]]$errorbardata$respVarName <- respVar

		plotdata[[respVar]]
	})

	rVarNames <- names(plotdata)
	
	# data frame containing raw data
	rawDF <- lapply(rawData,"[[", "rawdata")
	rawDF <- do.call("rbind", rawDF)
	rawDF$respVarName <- factor(rawDF$respVarName, levels = plotOpts$varOrder)
	
	uniqueConc <- unique(rawDF$conc)
	#rawDF <- replaceZeroConc(rawDF)

	# data frame containing fitted data
	fitDF <- lapply(rawData,"[[", "smoothfitfuns")
	fitDF <- do.call("rbind", fitDF)
	if(ncol(fitDF) > 0) fitDF$respVarName <- factor(fitDF$respVarName, levels = plotOpts$varOrder)
	
	# data frame containing errorbar data
	errorDF <- lapply(rawData,"[[", "errorbardata")
	errorDF <- do.call("rbind", errorDF)
	if(ncol(errorDF) > 0) errorDF$respVarName <- factor(errorDF$respVarName, levels = plotOpts$varOrder)
	
	# new ggplot
	theme_set(theme_bw(base_size = 13))
	p <- ggplot(NULL)

	# if errorbars shall be plotted and any errorbardata for plotting exists
	if( !doPlotDots & ncol(errorDF) > 0) {
		# plot errorbars
		p <- p + geom_errorbar(data = errorDF, mapping = aes(x = conc, y = concPredict, ymin = negY, ymax = posY, colour = compound, group = compound), width=0.05) }
	# plot single data points (if any marked for plotting)
	if( length(which(rawDF$plotDots == TRUE)) > 0) {
	p <- p + geom_point(data = subset(rawDF, plotDots == TRUE), mapping = aes(x = conc, y = resVar, colour = compound, group = compound)) }

	# plot dose response curve (if there is any fitting data)
	if(ncol(fitDF) > 0) {
	p <- p + geom_line(data = fitDF, mapping = aes(x = conc, y = predictResVar, colour = compound, group = compound)) }

	# set log-scale
	p <- p + scale_x_log10(breaks = unique(rawDF$conc), labels = formatC(uniqueConc, digit = 2))
	# one plot per response variable
	p <- p + facet_wrap( ~ respVarName, ncol = numGridCols, scales = "free_y")
	p <- p + ylab("") + ggtitle(paste(title, " (Model: ", mName, ")", sep = ""))
	if(!is.null(xLab) & !is.na(xLab)) p <- p + xlab(xLab)
	if(!is.null(legendLab) & !is.na(legendLab)) p <- p + scale_colour_hue(legendLab)
	print(p) 
}

# method replace 0 concentration values (leave here: in case this is wanted at some point)
# replaceZeroConc <- function(rawDF, concIdx) {
#   
#   # if no zero value concentrations: return
#   if(nrow(rawDF[rawDF[,concIdx] == 0,]) == 0) return(rawDF)
#   
#   # index of zero value concentrations
#   zeroIdx <- which(rawDF[,concIdx] == 0)
#   
#   # sorted unique concentration values without 0
#   conc <- unique(rawDF[-zeroIdx, concIdx])
#   conc <- sort(conc)
#   
#   # log10 of concentrations
#   log10conc <- log10(conc) 
#   
#   # absolute difference between single log10-conectrations
#   diffVec <- abs(log10conc - c(log10conc[2:length(log10conc)], NA))
#   # minimum of this difference vector
#   minDiff <- min(diffVec, na.rm = TRUE)
#   
#   # substract mindiff from first log10-concentration to get a new smaller log10-concentration
#   newLogVal <- log10conc[1] - minDiff
#   # revert the log10 for new value
#   newVal <- 10 ^ newLogVal
#   
#   # replace 0 concentration with this new value
#   rawDF[zeroIdx,concIdx] <- newVal
#   
#   return(rawDF)
# }


#### compile the plot data-structure for a set of componud model for a single response variable
compilePlotData <- function(cpmdModels, drdata, resVarName, concColIndex, cmpndColIndex, plotOptions){

	#browser()
  plotSD <- plotOptions == "SD"
  plotSEM <- plotOptions == "SEM"

  #drdata <- replaceZeroConc(drdata, concColIndex)
  # remove zero concentration
  drdata <- drdata[drdata[,concColIndex] > 0,]
	# 1) create the fit data

	# just select the subset which is of interest
	dataWithFit <- data.frame(conc = drdata[, concColIndex], resVar = drdata[, which(names(drdata) == resVarName)], compound = drdata[, cmpndColIndex])

	# 2) create the smooth model funs

    # sample a set of evaluation points for the predicted function (do it in log-space to get an equal spacing in the visualization later on)
	# in case the smallest concentration value is zero, set it to 0 + very small value for log10 method. comment: plot looks weird as the zero is very far away from the other data pointd
    #if(range[1] == 0) range[1] <- 0 + .Machine$double.eps

    range <- range(drdata[, concColIndex])

	concRange <-log10(range)
	predictConcs <- 10^(seq(concRange[1], concRange[2],  (concRange[2] - concRange[1])/100))
	fitdata <-data.frame(conc = predictConcs)

	#cmpndName  <- names(cpmdModels)[1]
	smoothFitFunList <- lapply(names(cpmdModels), FUN = function(cmpndName){
		drmodel <- cpmdModels[[cmpndName]]
		if(!inherits(drmodel, 'drc')){
			return(NA)
		}
		drPrediction <- data.frame(conc = predictConcs, predictResVar = predict(drmodel, fitdata), compound = cmpndName)
		return(drPrediction)
	})
	smoothFits <- do.call("rbind", smoothFitFunList)
	smoothFits <- smoothFits[complete.cases(smoothFits),]  # drop rows with na values

	# DEBUG
	#library(ggplot2)
	#qplot(smoothFits, aes(x= conc, y= predictResVar, group=compound, color=compound), geom="line")
	#qplot(conc, predictResVar, data=smoothFits,group=compound, color=compound, geom="line")

	#####
	# 3) create the error bar data
	errorBarDataList <- lapply(names(cpmdModels), FUN = function(cmpndName){

		drmodel <- cpmdModels[[cmpndName]]
		if(!inherits(drmodel, 'drc')){
			return(NA)
		}

		drConcs <- unique(drdata[, concColIndex])

		errorDF <- data.frame(conc = drConcs)
		errorDF$concPredict <- predict(drmodel, errorDF)

		ret <- lapply(errorDF$conc, FUN = function(co) {
			subData <- drmodel$data$meas[which(drmodel$data$conc %in% co)]
			c(concRespMean = mean(subData), concRespSD = sd(subData), concRespSEM = sem(subData))
			})
		ret <- do.call("rbind", ret)

		errorDF <- cbind(errorDF, ret)
		errorDF$compound <- cmpndName
		if(plotSD)
		  errorDF <- transform(errorDF, compound = cmpndName, posY = concPredict + concRespSD, negY = concPredict - concRespSD)
		else
		  errorDF <- transform(errorDF, compound = cmpndName, posY = concPredict + concRespSEM, negY = concPredict - concRespSEM)

		errorDF <- errorDF[complete.cases(errorDF),]
		if(nrow(errorDF) == 0) return(NA)

		return(errorDF)
	})

	errorBarData <- do.call("rbind", errorBarDataList)
	errorBarData <- errorBarData[complete.cases(errorBarData),] # drop rows with na values

	plotdata <- list(dataWithFit, smoothFits, errorBarData)
	names(plotdata) <- c('rawdata', 'smoothfitfuns', 'errorbardata')

	return(plotdata)
}


####
# reshapes the data to create a list with one sublist for each readout. The sublist will contain 3 datastrucutres (datawithfit, smoothfitfuns, errorbardata). This can than be used to create some nice graphics with ggplot2
compileDRData4Vis <- function(drmodels, drdata, concIndex, cmpndColIndex, plotOptions){
	pivModels <- pivotModelTable(drmodels)
	
	plotdata <- lapply(names(pivModels), FUN <- function(resVarName){
		return(compilePlotData(pivModels[[resVarName]], drdata, resVarName, concIndex, cmpndColIndex, plotOptions))
	})
	names(plotdata) <- names(pivModels)

	return(plotdata)
}



############# playground  ### (make sure to disable before redeloying the node) #####


#drmodels <- calcDRModels(drdata, readoutIndices, treatmentIndex, concIndex) 
#names(drmodels[[1]])
#names(drmodels)

#
#library(ggplot2)
#m <- ggplot(subDRData, aes(y= meas, x=conc, color = treatment)) + geom_point()
#m + geom_smooth()
#
#qplot(meas, data=subDRData, geom='line')