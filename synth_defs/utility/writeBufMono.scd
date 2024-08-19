(
SynthDef(\bufWriteMono, {
	arg inBus, buf, gate;
	var in;
	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix(in);
	RecordBuf.ar(in, buf, loop: 0, doneAction: 2);
}).add;
);