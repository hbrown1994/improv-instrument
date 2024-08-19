(
SynthDef(\eightDel, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, thresh;
	var in, sig, baseFreq = 100, freq, hasFreq, cScale, scale, vocoder, ampSig, toggle;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	toggle = ToggleFF.kr(pres>0).lag(0.05);

	sig = {SwitchDelay.ar(in, 0, 1, TRand.kr(0.05, x.linlin(0, 127, 0.1, 2), BinaryOpUGen('==', toggle, 1)+Impulse.kr(0).lag(0.005)).lag(0.1), y.linlin(0, 127, 0, 0.7).lag(0.15))}!8;

	sig = SplayAz.ar(GlobalPresets.numChannels, sig);

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

GlobalSynths.processors.add(\eightDel);
