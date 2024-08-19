(
PathName(GlobalPaths.analysis2D_datasets_path).entries.do{|i|
	i.fileName.asString.replace(".json", "").asSymbol;
	GlobalData.analysis2D_datasetsDict.add(i.fileName.asString.replace(".json", "").asSymbol
		-> FluidDataSet.new(Server.local).read(i.asAbsolutePath);
	);
};
);

(
PathName(GlobalPaths.playback2D_datasets_path).entries.do{|i|
	i.fileName.asString.replace(".json", "").asSymbol;
	GlobalData.playback2D_datasetsDict.add(i.fileName.asString.replace(".json", "").asSymbol
		-> FluidDataSet.new(Server.local).read(i.asAbsolutePath);
	);
};
);