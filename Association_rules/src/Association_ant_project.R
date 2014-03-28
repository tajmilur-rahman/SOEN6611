#Set up your directory to read your Data
setwd("/home//shams/Ant_project/data//Data_for_rules")
library(arules)

#Reading all the txt files which contains data
my.path <- list.files(pattern=".txt$") 
my.data <- list()

for (i in 1:length(my.path)){
  #my.data[[i]] <- read.table(my.path[[i]])
  #converting dataset into transaction
  tr<-read.transactions(my.path[[i]],format="basket",sep=",", rm.duplicates=TRUE)
  	#Applying Apriori to get association rule
	rules <- apriori(tr, parameter= list(supp=0.009, conf=0.5,maxlen=2))
	rules.sorted <- sort(rules, by="lift")
	#pruning the rules
  	subset.matrix <- is.subset(rules.sorted, rules.sorted)
  	subset.matrix[lower.tri(subset.matrix, diag=T)] <- NA
  	redundant <- colSums(subset.matrix, na.rm=T) >= 1
  	rules.pruned <- rules.sorted[!redundant]
  	#Writing the rules in file
	file=paste(my.path[[i]],"results", "txt", sep=".")
 	sink(file)
  	inspect(rules.pruned)
 	sink()
}
