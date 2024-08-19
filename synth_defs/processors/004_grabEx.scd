(
SynthDef(\grabEx, {
	arg busMono, inBus, gate=1, env, x, y, pres, amp, busAmp, bus, out, state, ampGate, thresh, pauseGate=1;
	var in, buf, chunk, phasor, sig, rate, size, read_point, recTrig, freq, bufDur, start, clock;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*5, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Gate.ar(Phasor.ar(recTrig, 1, 0, SampleRate.ir*5), recTrig)+(SampleRate.ir*0.005);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	size = /*x.linlin(0, 127, 100, 1)*/ 100 * (SampleRate.ir/100);
	read_point = x.linlin(0, 127, 0, bufDur -  size);
	freq = y.linexp(0, 127, 0.5, 10);

	clock = Impulse.kr(freq);
	chunk = Latch.kr(read_point+size, clock); //Latch == Sample & Hold
	read_point = Latch.kr(read_point, clock); //Latch == Sample & Hold

	rate = (size * (freq/SampleRate.ir));
	phasor = Phasor.ar(trig: clock, rate: rate, start: read_point, end: chunk, resetPos: read_point);

	sig = BufRd.ar(1, buf, phasor.lag(0.1), loop: 0)!2;
	sig = LeakDC.ar(sig) * EnvGen.ar(Env.sine((1/freq), 1), clock); //remove potential DC Offset

	sig = Select.ar(state, [Silent.ar, sig]);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = PanX.ar(GlobalPresets.numChannels, sig, TRand.kr(0, 1,  Onsets.kr(FFT(LocalBuf(512), HPF.ar(sig, 200.0)), 0.7, \rcomplex, 2, 0.3)));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\grabEx);