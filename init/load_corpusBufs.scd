(
GlobalData.audioBuffersDict.add(\nnMFCCbufs -> Dictionary.new);
PathName(GlobalPaths.buffers++"nnMFCC/").entries.do{|i|
	var fileNameSym = i.fileName.asString.replace(".wav", "").asSymbol;
	GlobalData.audioBuffersDict[\nnMFCCbufs].add(fileNameSym
		-> Buffer.read(s, i.asAbsolutePath)
	);
};
);

(
GlobalData.audioBuffersDict.add(\analysis2Dbufs -> Dictionary.new);
PathName(GlobalPaths.buffers++"analysis2D/").entries.do{|i|
	var fileNameSym = i.fileName.asString.replace(".wav", "").asSymbol;
	GlobalData.audioBuffersDict[\analysis2Dbufs].add(fileNameSym
		-> Buffer.read(s, i.asAbsolutePath)
	);
};
);

