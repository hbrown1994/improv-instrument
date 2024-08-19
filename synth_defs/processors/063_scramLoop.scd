(
SynthDef(\scramLoop, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, lag_amount=(-200), freqLow=200, freqHigh=1, window_size=5;
	var in, sig, buf, buffer, bufDur, recTrig, trig;
	var phasor, env, read_point, chunk, mouse_change, rate, clock, freq, env_trig;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*5, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Clip.kr(Gate.kr(Sweep.kr(recTrig), recTrig), 0, 5);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	freq = y.linlin(0, 127, freqLow, freqHigh); //warp: 1 -> exponential mapping
	window_size = x.linlin(0, 127, 1, 200) * (SampleRate.ir/100);
	clock = Dust.kr(Gate.kr(pres.linexp(0, 127, 0.1, 60), ampGate));

	read_point = TBrownRand.kr(
		0,
		(bufDur*SampleRate.ir) - window_size,
		0.3,
		{IRand(0, 5)},
		clock
	).asInteger;

	//here: chunk is a size of the chunk from read_point, as oppossed to just 0.
	chunk = read_point+window_size;
	rate = (window_size * (freq/SampleRate.ir));
	mouse_change = Changed.kr(read_point);
	phasor = Phasor.ar(trig: mouse_change, rate: rate, start: read_point, end: chunk, resetPos: read_point);

	//adding lag to the phasor signal to remove clicks from the shifting read_point also introduces a doppler effect.
	sig = BufRd.ar(1, buf, phasor.lag(0.05), loop: 0);

	//another method of syncing the envelope with the phasor via Impulse.ar
	env_trig = Impulse.ar(freq);
	env = EnvGen.ar(Env.sine((1/freq), 1), env_trig);
	sig = sig * env;
	sig = LeakDC.ar(sig); //remove potential DC Offset
	sig = Limiter.ar(sig);

	sig = PanX.ar(GlobalPresets.numChannels, sig, TBrownRand.kr(
		0.0, 1.0, pres.linexp(0, 127, 0.6, 1), 1,
		clock//Onsets.kr(FFT(LocalBuf(512), HPF.ar(sig, 100.0)), 0.3, \rcomplex, 2, 0.3)
	);
	);

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

GlobalSynths.processors.add(\scramLoop);