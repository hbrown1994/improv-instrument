(
SynthDef(\cross1, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1;
	var freq, mratio, inFb, mod, sig, in;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix(in);

	inFb = LocalIn.ar(2);
	freq = x.linlin(0, 127, 20, 4000);
	freq = Select.kr(state, [freq, TBrownRand.ar(1000, 20000, 0.3, 0, Trig.kr(inFb[0]))]); //play with these TBrownRand ranges
	mratio = y.linlin(0, 127, 0.125, 8);
	freq = Select.kr(mratio, [freq, TBrownRand.ar(0.125, 2, 0.3, 0, Trig.kr(inFb[1]))]);
	mod = SinOsc.ar(freq * mratio) * freq * mratio * Gate.kr(pres.linlin(0, 127, 0, 5), ampGate);
	sig = SinOsc.ar([(freq * mod * inFb[1]), (freq + mod + inFb[0])]);
	LocalOut.ar(Select.ar(state, [sig*[10, 5], sig/(in*[10, 5])+Impulse.ar(0)!2]));

	sig = LeakDC.ar(Mix(sig));
	sig = Pan2.ar(sig, 0, 0.3);

	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\cross1);
