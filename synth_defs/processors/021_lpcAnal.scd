(
SynthDef(\lpcAnal, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1;
	var in, sig, buf, trig, buffer, bufDur, recTrig, freq, hasFreq;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	x = x.linlin(0, 127, 265, 1);

	#freq,hasFreq= Pitch.kr(in);
	sig = LPCAnalyzer.ar(in, Select.ar(hasFreq, [0.25*WhiteNoise.ar, Impulse.ar(freq)]), 256, Select.kr(state, [pres.linlin(0, 127, 256, 1),  x]), 0, windowtype:1);
	sig = sig * Select.kr(state, [1, hasFreq.lag(0.005)]);

	sig = PanX.ar(GlobalPresets.numChannels, LeakDC.ar(sig), LFBrownNoise1.kr(RMS.kr(in).linlin(0.0, 0.2, 0.1, 10)).range(0, 1));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	Out.ar(out, sig*amp.lag(0.005));
	Out.ar(bus, sig*busAmp.lag(0.005));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\lpcAnal);

