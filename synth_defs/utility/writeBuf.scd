(
SynthDef(\bufWrite, {
	arg inBus, buf, gate;
	var in;
	in = In.ar(inBus, GlobalPresets.numChannels);
	RecordBuf.ar(in, buf, loop: 0, doneAction: 2);
}).add;
);