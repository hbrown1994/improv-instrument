(
SynthDef(\lpcError, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1;
	var in, sig, buf, trig, buffer, bufDur, recTrig, error;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	error = Select.kr(state, [pres, x]).linlin(0, 127, 64, 1);

	sig = LPCError.ar(in, error);

	sig = PanX.ar(GlobalPresets.numChannels, LeakDC.ar(sig), TBrownRand.kr(0.0, 1.0, 1, 0, Onsets.kr(FFT(LocalBuf(512), sig))));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	sig = LeakDC.ar(sig);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	Out.ar(out, sig*amp.lag(0.005));
	Out.ar(bus, sig*busAmp.lag(0.005));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\lpcError);

