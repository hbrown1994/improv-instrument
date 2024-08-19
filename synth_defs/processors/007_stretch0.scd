(
SynthDef(\stretch0, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, lag_amount=0.5, window_size=0.005, precession_min=0, precession_max=0.5, pitchLow=(-400), pitchHigh=500, loop_length=10;
	var in, sig, buf, buffer, bufDur, recTrig, trig;
	var phasor, env, read_point, chunk, mouse_change, rate, clock, freq, env_trig;
	var precession, pitch, precession_rate, phasor_180, env_phase_0, env_phase_180, sig_phase_0, sig_phase_180;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*loop_length, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Clip.kr(Gate.kr(Sweep.kr(recTrig), recTrig), 0, loop_length);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	precession = x.linlin(0, 127, precession_min, precession_max);
	pitch = y.linlin(0, 127, pitchLow, pitchHigh);
	freq = ((2**(pitch/120))-precession / window_size); //convert semitones/10 to hertz

	loop_length = Select.kr(state,
		[
			loop_length,
			Select.kr(BinaryOpUGen('==', 0, bufDur), [bufDur, loop_length])
		]
	);

	precession_rate = (precession/loop_length);
	precession_rate = (loop_length * precession_rate) / SampleRate.ir;
	read_point = Phasor.ar(rate: precession_rate);
	read_point = read_point * loop_length;

	phasor = Phasor.ar(rate: freq/SampleRate.ir);
	phasor_180 = Wrap.ar(phasor + 0.5);

	//Create envelopes by feeding the Phasors into cosine transfer functions
	env_phase_0 = ((phasor - 0.5) * 0.5); //Have phasor loop only at the portions of the trasnfer function were cos will output 0-1
	env_phase_180 = ((phasor_180 - 0.5) * 0.5);

	env_phase_0 = (env_phase_0 * 2pi).cos; //Convert to radians and send into transfer function
	env_phase_180 = (env_phase_180 * 2pi).cos;

	chunk = Latch.ar(window_size, Impulse.kr(freq)); //Latch is here in case you want to make window_size dymanic via mouseX or mouseY
	phasor = phasor * chunk; //Scale phasor to fit chunk (window_size)
	phasor = phasor + read_point; //add read_point to shift the Phasor's start position
	phasor = phasor * SampleRate.ir; //concert to samples for BufRd.ar
	phasor = phasor + 1; //Offset by one sample so that the cos envelops land exactly on the discontinuities caused by Phasor

	//Apply same processes from above to offset phasor
	phasor_180 = phasor_180 * chunk;
	phasor_180 = phasor_180 + read_point;
	phasor_180 = phasor_180 * SampleRate.ir;
	phasor_180 = phasor_180 + 1;

	sig_phase_0 = BufRd.ar(1, buf, phasor, loop: 0, interpolation: 4) * 0.5;
	sig_phase_180 = BufRd.ar(1, buf, phasor_180, loop: 0, interpolation: 4) * 0.5;

	sig_phase_0 = sig_phase_0 * env_phase_0;
	sig_phase_180 = sig_phase_180 * env_phase_180;

	sig = LeakDC.ar(sig_phase_0 + sig_phase_180);

	sig = PanX.ar(
		GlobalPresets.numChannels,
		sig,
		LFBrownNoise2.kr(RMS.kr(sig).linlin(0.0, 0.2, 1, 15.0)).linlin(-1, 1, 0, 1);
	);

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0), 0)).linlin(0, 1, 1, 0));

	sig = sig * Gate.kr(pres.linexp(0, 127, 0.001, 1).lag(0.005), ampGate);
	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);
	sig = SelectX.ar(state.lag(0.005), [Silent.ar, sig]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\stretch0);