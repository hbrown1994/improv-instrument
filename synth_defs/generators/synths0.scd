SynthDef(\synths0, {
	arg monoBus, amp, busAmp, atk, gate, bus, out, shepBus, glitch0Bus, markovBus, iceBus;
	var sig, env, sheps, glitch0, markov, ice;

	glitch0 = In.ar(glitch0Bus, GlobalPresets.numChannels);
	sheps = In.ar(shepBus, GlobalPresets.numChannels);
	markov = In.ar(markovBus, GlobalPresets.numChannels);
	ice = In.ar(iceBus, GlobalPresets.numChannels);

	EnvGen.kr(Env.asr(atk.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), 1, atk.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), [4, -4]), gate, doneAction: 2);

	sig = Mix.new([sheps, glitch0, markov, ice]);

	Out.ar(out, (sig*amp));
	Out.ar(bus, (sig*busAmp));
	Out.ar(monoBus, Mix.new(sig*busAmp));
}).add;



