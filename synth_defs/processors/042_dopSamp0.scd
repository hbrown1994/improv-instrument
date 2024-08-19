(
SynthDef(\dopSamp0, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, lag_amount=0.5, freqLow=1, freqHigh=30;
	var in, sig, buf, buffer, bufDur, recTrig, trig;
	var phasor, env, read_point, chunk, window_size, mouse_change, rate, clock, freq;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*5, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Clip.kr(Gate.kr(Sweep.kr(recTrig), recTrig), 0, 5);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	window_size = y.linlin(0, 127, 1, 50) * (SampleRate.ir/100);
	read_point = x.linlin(0, 127, 0, (bufDur*SampleRate.ir) - window_size);
	freq = Gate.kr(pres.linexp(0, 127, freqLow, freqHigh).lag(0.005), ampGate);
	mouse_change = Changed.kr(read_point);

	clock = Impulse.kr(freq);

	chunk = Latch.kr(read_point+window_size, clock); //Latch == Sample & Hold
	read_point = Latch.kr(read_point, clock); //Latch == Sample & Hold

	rate = (window_size * (freq/SampleRate.ir));
	phasor = Phasor.ar(trig: clock, rate: rate, start: read_point, end: chunk, resetPos: read_point);

	sig = BufRd.ar(1, buf, phasor.lag(0.1), loop: 0);
	sig = LeakDC.ar(sig); //remove potential DC Offset

	env = EnvGen.ar(Env.sine((1/freq), 1), clock);
	sig = sig * env;
	sig = Limiter.ar(sig) * 0.7;

	sig = PanX.ar(
		GlobalPresets.numChannels,
		sig,
		TBrownRand.kr(
			0.0, 1.0, pres.linexp(0, 127, 0.6, 1), 1,
			Onsets.kr(FFT(LocalBuf(512), HPF.ar(sig, 100.0)), 0.3, \rcomplex, 2, 0.3)
		);
	);

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);
	sig = SelectX.ar(state.lag(0.005), [Silent.ar, sig]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\dopSamp0);


