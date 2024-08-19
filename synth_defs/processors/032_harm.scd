(
SynthDef(\harm, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, thresh;
	var in, sig, phs0, phs1, del0, del1, sigEnv0, sigEnv1, rate0, rate1, maxdel=0.05;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	rate0 = x.linlin(0, 127, 0, Select.kr(state, [12, 24])).round(Select.kr(state, [1, 0.001])).midiratio - 1 /maxdel;
	rate1 = y.linlin(0, 127, 0, Select.kr(state, [-12, -24])).round(Select.kr(state, [1, 0.001])).midiratio - 1 /maxdel;

	phs0 = LFSaw.ar(rate0.neg, [1, 0]).range(0, maxdel);
	phs1 = LFSaw.ar(rate1.neg, [1, 0]).range(0, maxdel);

	sigEnv0 = SinOsc.ar(rate0, [3pi/2, pi/2]).range(0, 1).sqrt;
	sigEnv1 = SinOsc.ar(rate1, [3pi/2, pi/2]).range(0, 1).sqrt;

	del0 = DelayC.ar(in, maxdel, phs0) * sigEnv0;
	del1 = DelayC.ar(in, maxdel, phs1) * sigEnv1;

	sig = Mix.new([del0, del1])!GlobalPresets.numChannels;

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr (pres>0, [0, 1])]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\harm);
