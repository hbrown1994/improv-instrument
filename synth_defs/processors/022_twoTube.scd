(
SynthDef(\twoTube, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, lpf=10000, hpf=50, lag=0.005, hpf_freq=60;
	var in, sig, ampTrack, fric0, mass0, fric1, mass1, rms, sig1;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	rms = RMS.kr(in);

	in = HPF.ar(in, 100);

	sig = SelectX.ar(x.linlin(0, 127, 0.0, 5.0).lag(0.05),
		[
			TwoTube.ar(input: in, k: 0.01, loss: 0.8, d1length: 5, d2length: 20),
			TwoTube.ar(input: in, k: 0.01, loss: 0.85, d1length: 20, d2length: 50),
			TwoTube.ar(input: in, k: 0.01, loss: 0.9, d1length: 60, d2length: 100),
			TwoTube.ar(input: in, k: 0.01, loss: 0.95, d1length: 100, d2length: 200),
			TwoTube.ar(input: in, k: 0.01, loss: 0.98, d1length: 200, d2length: 500),

	])*0.6;


	sig1 = SelectX.ar(y.linlin(0, 127, 0.0, 5.0).lag(0.05),
		[
			TwoTube.ar(input: in, k: 0.01, loss: 0.82, d1length: 4, d2length: 17),
			TwoTube.ar(input: in, k: 0.01, loss: 0.85, d1length: 18, d2length: 46),
			TwoTube.ar(input: in, k: 0.01, loss: 0.9, d1length: 57, d2length: 87),
			TwoTube.ar(input: in, k: 0.01, loss: 0.95, d1length: 87, d2length: 180),
			TwoTube.ar(input: in, k: 0.01, loss: 0.98, d1length: 190, d2length: 528),


	])*0.6;

	sig = LeakDC.ar(sig);
	sig1 = LeakDC.ar(sig1);
	sig = [sig, sig1];



	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig * Select.kr(state, [1, pres.linlin(0, 127, 0, 1).lag(0.005)]);

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\twoTube);

