(
SynthDef(\pitchShift, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, thresh;
	var in, sig;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	sig = [PitchShift.ar(in, 0.19, x.linlin(0, 127, 0, 4), 0, 0.001), PitchShift.ar(in, 0.21, y.linlin(0, 127, 0, 4), 0, 0.001)];

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);

	sig = PanX.ar(GlobalPresets.numChannels, sig[0], LFBrownNoise2.kr(pres.linlin(0, 127, 0.5, 20)).range(0, 1)) + PanX.ar(GlobalPresets.numChannels, sig[1], LFBrownNoise2.kr(pres.linlin(0, 127, 0.5, 20)).range(0, 1));

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\pitchShift);
