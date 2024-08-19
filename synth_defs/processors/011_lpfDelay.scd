(
SynthDef(\lpfDelay, {
	arg busMono, inBus, out, bus, x, y, pres=0, amp, busAmp, gate, env, state, ampGate, pauseGate=1;
	var in, sig, fb, delayTime, lpf;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in) * ampGate.linlin(0, 1, 1, 0).lag(0.05);


	fb = LocalIn.ar(1) * y.linlin(0, 127, Select.kr(state, [0.92, 0]), Select.kr(state, [0.7, 0.95]));
	fb = fb + in;

	delayTime = Latch.kr(
			y.linexp(0, 127, Select.kr(state, [0.02, 0.5]), Select.kr(state, [0.5, 0.02])),
			Changed.kr(pres)
	);

	sig = DelayL.ar(fb, 0.5, delayTime.lag(0.05));

	lpf = Select.kr(state, [x, pres]).linlin(0, 127, 127, 0);
	sig = LPF.ar(sig, lpf.linlin(0, 127, 1000, 16000).lag2(0.05));

	LocalOut.ar(sig);

	sig = sig * lpf.linexp(0, 127, 1.5, 1);

	sig = PanX.ar(GlobalPresets.numChannels, sig, TRand.kr(0.2, 0.8, pres>0));

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\lpfDelay);
