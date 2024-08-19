(
SynthDef(\looper, {
	arg busMono, inBus, gate, env, x=0, y=0, pres=0, amp, busAmp, bus, out, thresh=0.7, state, ampGate, pauseGate=1;
	var in, fft, onsets, buf, sliceLength, phasor, sig, clock, rate, size, dust, one, cut, tRand, sweep;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	thresh = Clip.kr(thresh, 0.7, 2.0);

	buf = LocalBuf(SampleRate.ir*2);

	onsets = Onsets.kr(FFT(LocalBuf(512), HPF.ar(in, 150.0)), thresh, \rcomplex, 2, 0.3, 50)+Impulse.kr(0);
	dust = Select.kr(pres>0, [onsets, Dust.kr(pres.linlin(0, 127, 0.2, 30))+onsets]);

	sweep = Sweep.ar(onsets);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), SetResetFF.ar(DelayN.ar(K2A.ar(onsets), 0.01, 0.01), K2A.ar(onsets)+(sweep>1.994)));

	RecordBuf.ar(in, buf, loop: 0, trigger: onsets);

	rate = Select.kr(state,
		[
			y.linlin(0, 127, 0.5, 5),
			TRand.kr(y.linlin(0, 127, 0.2, 2), y.linlin(0, 127, 1.0, 5), dust),
	]).lag(0.05);

	size = Select.kr(state,
		[
			x.linexp(0, 127, SampleRate.ir*2, 2048).lag(0.05),
			TRand.kr(x.linexp(0, 127, SampleRate.ir*2, SampleRate.ir/2), x.linexp(0, 127, SampleRate.ir/2, 2048), dust)
	]).lag(0.0);

	phasor = Phasor.ar(
		Select.kr(state, [Impulse.kr(0), dust]),
		rate, 0, size, 0);

	phasor = Delay1.ar(phasor);

	sig = BufRd.ar(1, buf, Delay2.ar(phasor), 0);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	tRand = TRand.kr(0, 1, onsets+(pres>0));
	sig = PanX.ar(GlobalPresets.numChannels, LeakDC.ar(sig),
		Select.kr(state,
			[
			tRand,
			TBrownRand.kr(0.0, 1.0, 0.6, 0, Onsets.kr(FFT(LocalBuf(512), HPF.ar(sig, 200.0)), thresh, \rcomplex, 2, 0.3, 20)+Impulse.kr(0)),
		]));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\looper);
