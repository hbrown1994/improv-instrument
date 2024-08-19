(
SynthDef(\shot_2, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, loop_length=2;
	var in, sig, buf, buffer, bufDur, recTrig, trig;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*loop_length, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Clip.kr(Gate.kr(Sweep.kr(recTrig), recTrig), 0, loop_length);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	trig = ampGate.linlin(0, 1, 1, 0) + pres>0;
	sig = PlayBuf.ar(1, buf, 1, trig, 0, 0)!2;
	sig = sig * Linen.kr(trig, 0.005, 1, 0.005);
	sig = LeakDC.ar(sig);

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0), 0)).linlin(0, 1, 1, 0));

	sig = SelectX.ar(state.lag(0.005), [Silent.ar, sig]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\shot_2);