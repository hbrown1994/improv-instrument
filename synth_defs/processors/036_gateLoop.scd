(
SynthDef(\gateLoop, {
	arg busMono, inBus, gate, env, x=0, y=0, pres=0, amp, busAmp, bus, out, thresh=0.7, state, ampGate, pauseGate=1;
	var in, fft, onsets, buf, sliceLength, phasor, sig, clock, rate, size, dust, one, cut, tRand, sweep;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	thresh = Clip.kr(thresh, 0.7, 2.0);µ

	buf = LocalBuf(SampleRate.ir*2);

	onsets = Onsets.kr(FFT(LocalBuf(512), HPF.ar(in, 150.0)), thresh, \rcomplex, 2, 0.3, 50)+Impulse.kr(0);
	onsets = Gate.kr(onsets, state.linlin(0, 1, 1, 0));
	dust = Select.kr(pres>0, [onsets, Dust.kr(pres.linlin(0, 127, 0.2, 30))+onsets]);

	sweep = Sweep.ar(onsets);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), SetResetFF.ar(DelayN.ar(K2A.ar(onsets), 0.01, 0.01), K2A.ar(onsets)+(sweep>3.994)));

	RecordBuf.ar(in, buf, loop: 0, trigger: onsets);

	rate = y.linexp(0, 127, 0.2, 3).lag(0.05);

	size = x.linexp(0, 127, SampleRate.ir*2, 2048).lag(0.05);

	phasor = Phasor.ar(
		Impulse.kr(0),
		rate, 0, size, 0);

	phasor = Delay1.ar(phasor);

	sig = BufRd.ar(1, buf, Delay2.ar(phasor), 0);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	tRand = TRand.kr(0, 1, onsets+Changed.kr(pres+x+y)).lag(0.05);
	sig = PanX.ar(GlobalPresets.numChannels, LeakDC.ar(sig), tRand);

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\gateLoop);
