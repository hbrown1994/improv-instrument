SynthDef(\quneoLeft, {
	arg monoBus, quneoLeftAmp, quneoLeftBusAmp, gate=1, bus, out, turntableBus, noiseBus, popBus;
	var sig, env, turntable, noise, pop;

	turntable = In.ar(turntableBus, GlobalPresets.numChannels);
	noise = In.ar(noiseBus, GlobalPresets.numChannels);
	pop = In.ar(popBus, GlobalPresets.numChannels);

	EnvGen.kr(Env.asr(0, 1, 0), gate, doneAction: 2);

	sig = Mix.new([turntable, noise, pop]);

	Out.ar(monoBus, (Mix.new(sig)*quneoLeftBusAmp.lag(0.05)));
	Out.ar(out, (sig*quneoLeftAmp.lag(0.05)));
	Out.ar(bus, (sig*quneoLeftBusAmp.lag(0.05)));
}).add;



