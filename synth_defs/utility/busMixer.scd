(
SynthDef(\busMixer, {
	arg gate=1, env, busOut, bus0, bus1, bus2, bus3, bus4, bus5, bus6, bus7, fbSel0=0, fbSel1=0, fbSel2=0, fbSel3=0, fbSel4=0, fbSel5=0, fbSel6=0, fbSel7=0;
	var mix;

	mix = Mix.new([
		Select.ar(fbSel0, [In.ar(bus0, GlobalPresets.numChannels), InFeedback.ar(bus0, GlobalPresets.numChannels)]),
		Select.ar(fbSel1, [In.ar(bus1, GlobalPresets.numChannels), InFeedback.ar(bus1, GlobalPresets.numChannels)]),
		Select.ar(fbSel2, [In.ar(bus2, GlobalPresets.numChannels), InFeedback.ar(bus2, GlobalPresets.numChannels)]),
		Select.ar(fbSel3, [In.ar(bus3, GlobalPresets.numChannels), InFeedback.ar(bus3, GlobalPresets.numChannels)]),
		Select.ar(fbSel4, [In.ar(bus4, GlobalPresets.numChannels), InFeedback.ar(bus4, GlobalPresets.numChannels)]),
		Select.ar(fbSel5, [In.ar(bus5, GlobalPresets.numChannels), InFeedback.ar(bus5, GlobalPresets.numChannels)]),
		Select.ar(fbSel6, [In.ar(bus6, GlobalPresets.numChannels), InFeedback.ar(bus6, GlobalPresets.numChannels)]),
		Select.ar(fbSel7, [In.ar(bus7, GlobalPresets.numChannels), InFeedback.ar(bus7, GlobalPresets.numChannels)])
	]);

	EnvGen.kr(Env.asr(env.linlin(0.0, 1.0, 0.005, 180.0), 1, env.linlin(0.0, 1.0, 0.005, 180.0)), gate, doneAction: 2);

	Out.ar(busOut, mix);
}).add;
);

