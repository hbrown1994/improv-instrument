(
SynthDef(\longGrains, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, pointerSampleDelay=20000, minPointerDelay=1000, pointerRandSamples=50000, bufLength=12;
	var in, sig, buf, trig, buffer, bufDur, recTrig, recPhasor, dur, pointer, randPoint, totalDelay, rate, maxGrainDur, rms, delRand;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);
	rms = RMS.kr(in);
	delRand = LFBrownNoise1.kr(
		LFBrownNoise1.kr(0.5).linlin(-1.0, 1.0, 0.5, 2)
	).linexp(-1.0, 1.0, 4, 0.3);
	delRand = Gate.kr(delRand, state.linlin(0, 1, 1, 0));
	rms = DelayL.kr(rms, 4, delRand);

	buf = LocalBuf(SampleRate.ir*bufLength, 1);
	recPhasor = Phasor.ar(0, 1, 0, SampleRate.ir*bufLength);
	recPhasor = Gate.ar(recPhasor, state.linlin(0, 1, 1, 0));
	BufWr.ar(in, buf, recPhasor);

	trig = Dust.kr(rms.linexp(0, 0.2, 2, 100).lag(delRand));

	dur = TBrownRand.kr(rms.linlin(0, 0.2, 0.005, 0.05), 0.2, 1, 0, trig);
	rate = TBrownRand.kr(1, rms.linlin(0, 0.2, 1, 3), 1, 0, trig);

	randPoint = TBrownRand.kr(0, pointerRandSamples, 0.7, 0, trig).bipolar;

	totalDelay = max(pointerSampleDelay - randPoint, minPointerDelay);
	pointer = recPhasor - totalDelay;
	pointer = pointer / BufFrames.kr(buf);

	maxGrainDur = (totalDelay/rate) / SampleRate.ir;
	dur = min(dur, maxGrainDur);

	sig = BufGrain.ar(trig, dur, buf, rate, pointer, 2);
	sig = DelayL.ar(sig, 4, delRand);

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

GlobalSynths.processors.add(\longGrains);
