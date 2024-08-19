(
SynthDef(\tGrainsAlt, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1;
	var in, sig, buf, trig, buffer, bufDur, recTrig;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*5, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Clip.kr(Gate.kr(Sweep.kr(recTrig), recTrig), 0, 5).linlin(0.0, 5.0, 0.0, 1.0);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	trig = Dust.kr(x.linexp(0, 127, 1, 150));

	sig = Select.ar(state, [
		Silent.ar,
		BufGrain.ar(
			trig,
			TBrownRand.kr(0.008, 0.15, 1, 0, trig),
			buf,
			TBrownRand.kr(1, y.linexp(0, 127, 1.0, 10.0), 1, 0, trig),
			TBrownRand.kr(0, bufDur, 0.7, 0, trig), 0)]
	);

	sig = PanX.ar(GlobalPresets.numChannels, sig, TBrownRand.kr(0.0, 1.0, 1, 0, trig));

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig * Gate.kr(pres.linexp(0, 127, 0.001, 1).lag(0.005), ampGate);
	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\tGrainsAlt);
