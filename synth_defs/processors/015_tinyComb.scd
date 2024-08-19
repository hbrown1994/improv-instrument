(
SynthDef(\tinyComb, {
	arg busMono, inBus, gate, env, x=0, y=0, pres=0, amp, busAmp, bus, out, thresh, state, ampGate, pauseGate=1, lowRand=0.0004, hiRand=0.01;
	var in, onsets, rms, sig, delayTime;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	onsets = Onsets.kr(FFT(LocalBuf(512), HPF.ar(in, 200.0)), thresh, \rcomplex, 2, 0.3);
	rms = RMS.kr(in);

	delayTime = TRand.kr(
			x.linlin(0, 127, hiRand, lowRand),
			hiRand,
			Select.kr(state, [onsets, Impulse.kr(pres.linlin(0, 127, 0, 50))]));

	sig = CombC.ar(in, 0.01, delayTime, 0.2);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = PanX.ar(GlobalPresets.numChannels, LeakDC.ar(sig), SinOsc.kr(rms.linlin(0, 0.2, 0.01, 50)).range(0, 1));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	sig = LPF.ar(sig, y.linlin(0, 127, 8000, 19000));

	Out.ar(out, sig*amp.lag(0.005));
	Out.ar(bus, sig*busAmp.lag(0.005));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\tinyComb);
