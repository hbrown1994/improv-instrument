(
PathName(GlobalPaths.nnMFCC_datasets).entries.do{|i|
	i.fileName.asString.replace(".json", "").asSymbol;
	GlobalData.nnMFCC_datasetsDict.add(i.fileName.asString.replace(".json", "").asSymbol
		-> FluidDataSet.new(Server.local).read(i.asAbsolutePath);
	);
};
);

(
PathName(GlobalPaths.nnPlayback_datasets).entries.do{|i|
	i.fileName.asString.replace(".json", "").asSymbol;
	GlobalData.nnPlayback_datasetsDict.add(i.fileName.asString.replace(".json", "").asSymbol
		-> FluidDataSet.new(Server.local).read(i.asAbsolutePath);
	);
};
);