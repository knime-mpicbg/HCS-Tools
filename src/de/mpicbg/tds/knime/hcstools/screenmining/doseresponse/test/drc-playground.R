

drmodel <- drmodels[[1]][[3]]

# use the drc-approach
plot(drmodel)

#create another data-strucutre to plot it with ggplot2
fitdata <- drmodel$data
fitdata$fit <- fitted(drmodel)


library(ggplot2)
ggplot(fitdata, aes(x = conc, y = fit)) + geom_line() + scale_x_log10()

fortify.drc <- function(model, data = model$data, ...) { 
	data$.fitted <- predict(model)
	data$.resid <- resid(model) 
	#data$.stdresid <- rstandard(model, infl) 

	data 
}

fdata <- fortify(drmodel)

concRange <-range(fdata$conc) 
predictConcs <- seq(concRange[1], concRange[2],  (concRange[2] - concRange[1])/100)

fitdata <-data.frame(conc = predictConcs) 
fitdata$prediction <- predict(drmodel, fitdata)

m <- ggplot(fdata, aes(conc, meas)) +geom_point() + scale_x_log10() 

# add the smoother
m <- m + geom_line(aes(conc, prediction),  data=fitdata)
m
# add the error bars 
# calculate the error bars

#fdata$concfac <- factor(fdata$conc)  

concPredicts<- predict(drmodel, data.frame(conc = unique(fdata$conc)))
concPredDF <- data.frame(concPredict = concPredicts, conc = unique(fdata$conc))

#boxplot(meas ~ conc, fdata)
concRespMean <- tapply(fdata$meas, list(fdata$conc), mean)
concRespSD <- tapply(fdata$meas, list(fdata$conc), sd)
concs <- as.numeric(rownames(means));
errorPartDF <- data.frame(conc=concs, concRespMean = concRespMean, concRespSD = concRespSD);

errorDF <- merge(errorPartDF, concPredDF, by="conc")
errorDF <- transform(errorDF, posY = concPredict + concRespSD, negY = concPredict - concRespSD)


# create a data strucuture for the 

n <- m + geom_errorbar(aes(x=conc, y= concPredict , ymin=negY, ymax=posY), data=errorDF, width=0.1);

# create the actual plot
plotdataBACKUP <- plotdata

# DO THE ACTUAL VISUALIZTATION
curPlotData <- plotdata[[1]]
names(onePlotData)

m <- ggplot(curPlotData[[2]], aes(conc, predictResVar, color=compound, group=compound)) +geom_line() + scale_x_log10() 
#m <- m + geom_point(aes(conc, resVar), data= curPlotData[[1]])
m <- m + geom_errorbar(aes(x=conc, y= concPredict , ymin=negY, ymax=posY), data=curPlotData[[3]], width=0.1);
#m <- m + facet_wrap( ~ compound)
m # print it!



