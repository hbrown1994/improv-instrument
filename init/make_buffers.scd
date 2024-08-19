//Adds all buffers in the "buffers" folder to a dictionary array.

//Stereo Buffers

PathName(GlobalPaths.buffers).entries.do{
	arg subfolder;
	GlobalData.audioBuffers.add(
		subfolder.folderName.asSymbol ->
		Array.fill(
			subfolder.entries.size,
			{
				arg i;
				Buffer.read(s, subfolder.entries[i].fullPath);
			}
		)
	);
};

PathName(GlobalPaths.buffers).entries.do{
	arg subfolder;
	GlobalData.audioBufferNames.add(
		subfolder.folderName.asSymbol ->
		Array.fill(
			subfolder.entries.size,
			{
				arg i;
				subfolder.entries[i].fileName.asString.replace(".wav", "");
			}
		)
	);
};




