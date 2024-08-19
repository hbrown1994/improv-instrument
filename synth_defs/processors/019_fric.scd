(
SynthDef(\fric, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, lpf=10000, hpf=50, lag=0.005;
	var in, sig, ampTrack, fric0, mass0, fric1, mass1;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	ampTrack = Dust.kr(RMS.kr(in, 30).linlin(0, 0.2, 0.001, 10))+Impulse.kr(0);

	fric0 = Select.kr(state, [TRand.kr(0.00625439,0.0177887, ampTrack).lag(lag), x.linlin(0, 127,0.00625439, 0.0177887)]);
	mass0 = Select.kr(state, [TRand.kr(0.2, 0.8214, ampTrack).lag(lag), y.linlin(0, 127, 0.2, 0.8214)]);

	fric1 = Select.kr(state, [TRand.kr(0.00625439,0.0177887, ampTrack).lag(lag), x.linlin(0, 127, 0.0177887, 0.00625439)]);
	mass1 = Select.kr(state, [TRand.kr(0.2, 0.8214, ampTrack).lag(lag), y.linlin(0, 127, 0.8214, 0.2)]);

	sig = [
		Friction.ar(in, friction: fric0, mass: mass0),
		Friction.ar(in, friction: fric1, mass: mass1)
	];

	sig = LeakDC.ar(LPF.ar(HPF.ar(sig, hpf), lpf));

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

GlobalSynths.processors.add(\fric);

