(
SynthDef(\cross0, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1;
	var in, sig, freq, mratio, mod, onsets, fb;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix(in);

	freq = Select.kr(state, [x.linlin(0, 127, 20, 2000), in.linlin(-1.0, 1.0, 0.125, x.linlin(0, 127, 20, 2000))]);
	mratio = y.linlin(0, 127, 0.125, 4);

	mod = Select.ar(state,
	[
		SinOsc.ar(freq * mratio) * Gate.kr(pres.linlin(0, 127, 0, 2), ampGate) * freq * mratio,
		in * Gate.kr(pres.linlin(0, 127, 0, 2), ampGate) * freq * mratio
	]);


	sig = SinOsc.ar(freq * mod);
	sig = LeakDC.ar(sig);
	sig = Pan2.ar(Clip.ar(sig, -1.0, 1.0), 0, 0.3);

	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\cross0);