{var sig;
	sig = Pan2.ar(Impulse.ar(0));
	Out.ar(
		GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][Array.series(8, GlobalPresets.outputSynthChan)],
		sig);
	Out.ar(Array.series(16), sig);
}.play;