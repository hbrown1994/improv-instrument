(
SynthDef(\shards, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, pointerDelay=2000, minPointerDelay=0.05, pointerRandSamples=50000;
	var in, sig, buf, trig, buffer, bufDur, recPhasor, combSig, pointer, totalDelay, dur, maxGrainDur, rate, randPoint, grainPhasor;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*2, 1);

	recPhasor = Phasor.ar(0, 1, 0, SampleRate.ir*2);
	grainPhasor = Wrap.ar((recPhasor-pointerDelay)/(SampleRate.ir*2));
	BufWr.ar(in, buf, recPhasor);

	trig = Dust.kr(x.linexp(0, 127, 1, 70));
	pointer = TRand.kr(Wrap.ar(grainPhasor-0.3), grainPhasor, trig);
	dur = TRand.kr(0.005, 0.1, trig);
	rate = TRand.kr(0.9, 1.1, trig)*Select.kr(Sweep.kr(Impulse.kr(0))>2, [1, TChoose.kr(trig, [-1, 1])]);

	sig = BufGrain.ar(trig, dur, buf, rate, pointer);

	combSig = LPF.ar(CombL.ar(sig, 0.1, TRand.kr(0.005, 0.08, trig).lag(TRand.kr(0.0, pres.linlin(0, 127, 0.5, 2.0), trig)), y.linlin(0, 127, 0.01, 3)), 17000);
	combSig = HPF.ar(combSig, 80);

	sig = SelectX.ar(state.lag(0.005), [sig, SelectX.ar(pres.linlin(0, 127, 0, 1), [sig, combSig])]);

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

GlobalSynths.processors.add(\shards);
