(
SynthDef(\squiz, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1;
	var in, sig, onsets;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	sig = Squiz.ar(in, x.linlin(0, 127, 1, 100), zcperchunk: y.linlin(0, 127, 1, 30), mul:0.8);
	sig = PanX.ar(GlobalPresets.numChannels, sig, LFBrownNoise1.kr(Gate.kr(pres.linlin(0, 127, 0.1, 70), state.linlin(0, 1, 1, 0))).range(0, 1));

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

GlobalSynths.processors.add(\squiz);
