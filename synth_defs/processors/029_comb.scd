(
SynthDef(\comb, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, thresh;
	var in, sig, baseFreq = 100, freq, hasFreq, cScale, scale, vocoder, ampSig;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	sig = [
		CombC.ar(in, 0.05, x.linlin(0, 127, 30.reciprocal, 9000.reciprocal), y.linlin(0, 127, 0.15, 4.7), Select.kr(state, [1, pres.linlin(0, 127, 0, 1).lag(0.005)])),
		CombC.ar(in, 0.05, x.linlin(0, 127, 40.reciprocal, 8000.reciprocal), y.linlin(0, 127, 0.1, 5.0), Select.kr(state, [1, pres.linlin(0, 127, 0, 1).lag(0.005)]))
	];

	sig = LeakDC.ar(sig);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\comb);
