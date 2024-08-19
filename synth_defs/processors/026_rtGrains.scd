(
SynthDef(\rtGrains, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, pointerSampleDelay=20000, minPointerDelay=1000, pointerRandSamples=50000, bufLength=7;
	var in, sig, buf, trig, buffer, bufDur, recTrig, recPhasor, dur, pointer, randPoint, totalDelay, rate, maxGrainDur;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*bufLength, 1);
	recPhasor = Phasor.ar(0, 1, 0, SampleRate.ir*bufLength);
	BufWr.ar(in, buf, recPhasor);

	trig = Select.kr(state, [
		Dust.kr(x.linexp(0, 127, 2, 100)),
		Dust.kr(RMS.kr(in).linexp(0, 0.2, 2, 100))
	]);

	dur = TBrownRand.kr(
		Select.kr(state, [LFBrownNoise1.kr(2).linexp(-1, 1, 0.005, 0.1), RMS.kr(in).linexp(0, 0.2, 0.005, 0.05)]),
		Select.kr(state, [LFBrownNoise1.kr(2).linexp(-1, 1, 0.008, 0.2),  RMS.kr(in).linexp(0, 0.2, 0.008, 0.2)]),
		1, 0, trig);

	rate = TBrownRand.kr(1, Select.kr(state, [y.linexp(0, 127, 1.0, 10.0), RMS.kr(in).linexp(0, 0.2, 1, 10.0)]), 1, 0, trig);

	randPoint = TBrownRand.kr(0, pointerRandSamples, 0.7, 0, trig).bipolar;

	totalDelay = max(pointerSampleDelay - randPoint, minPointerDelay);
	pointer = recPhasor - totalDelay;
	pointer = pointer / BufFrames.kr(buf);

	maxGrainDur = (totalDelay/rate) / SampleRate.ir;
	dur = min(dur, maxGrainDur);

	sig = BufGrain.ar(trig, dur, buf, rate, pointer, 2);
	sig = DelayL.ar(sig, 1, LFBrownNoise2.kr(0.2).linlin(-1, 1, 0.1, 1.0));

	sig = PanX.ar(GlobalPresets.numChannels, sig, TBrownRand.kr(0.0, 1.0, 1, 0, trig));

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

GlobalSynths.processors.add(\rtGrains);
