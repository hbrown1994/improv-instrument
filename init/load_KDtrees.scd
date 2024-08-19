(
PathName(GlobalPaths.nnMFCC_datasets).entries.do{|i|
	var fileNameSym = i.fileName.asString.replace(".json", "").asSymbol;
	GlobalData.nnTree_datasetsDict.add(fileNameSym
		-> FluidKDTree(Server.local).fit(GlobalData.nnMFCC_datasetsDict[fileNameSym])
	);
};
);

(
PathName(GlobalPaths.analysis2D_datasets).entries.do{|i|
	var fileNameSym = i.fileName.asString.replace(".json", "").asSymbol;
	GlobalData.tree2D_datasetsDict.add(fileNameSym
		-> FluidKDTree(Server.local).fit(GlobalData.analysis2D_datasetsDict[fileNameSym])
	);
};
);