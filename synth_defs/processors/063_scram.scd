(
SynthDef(\scram, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, loop_length=4;
	var in, sig, buf, buffer, bufDur, recTrig, pointer, lag_amount;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*loop_length, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Clip.kr(Gate.kr(Sweep.kr(recTrig), recTrig), 0, loop_length);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	lag_amount = y.linexp(0, 127, 10, 0.5);
	pointer = x.linlin(0, 127, 0.0, Select.kr(BinaryOpUGen('==', 0, bufDur), [bufDur*SampleRate.ir, BufFrames.kr(buf)]));

	pointer = TBrownRand.kr(
		0,
		Select.kr(BinaryOpUGen('==', 0, bufDur), [bufDur*SampleRate.ir, BufFrames.kr(buf)]),
		0.3,
		{IRand(0, 5)},
		Dust.kr(Gate.kr(pres.linexp(0, 127, 0.1, 60), ampGate))
	).asInteger;

	sig = BufRd.ar(1, buf, Lag.ar(K2A.ar(pointer), lag_amount), loop: 1);
	sig = LeakDC.ar(sig);
	sig = PanX.ar(GlobalPresets.numChannels, sig, Wrap.kr(pointer.lag(lag_amount), TRand.kr(trig: Changed.kr(pointer)), TRand.kr(trig: Changed.kr(pointer))));

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0), 0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);
	sig = SelectX.ar(state.lag(0.005), [Silent.ar, sig]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\scram);