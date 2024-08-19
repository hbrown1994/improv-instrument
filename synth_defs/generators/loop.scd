(
SynthDef(\loop, {
	arg monoBus, buf, out, amp=1, hpf, lpf, lpfFreq, hpfFreq, lpfOn, hpfOn, masterAmp, gate, bus, busAmp, loopBus, pan;
	var sig, env;

	sig = PlayBuf.ar(1, buf, BufRateScale.kr(buf), 1, 0, 1, 2);

	env = EnvGen.kr(Env.asr(0.005, 1, 0.04), gate, doneAction: 2);

	sig = sig * env;

	amp = amp.linexp(0.0, 1.0, 0.0001, 1.0);

	sig = Pan2.ar(sig, pan);

	Out.ar(out, sig!2*amp.lag(0.05));
	Out.ar(loopBus, sig!2*amp.lag(0.05));
	Out.ar(bus, sig!2*busAmp.lag(0.05));
	Out.ar(monoBus, sig*busAmp.lag(0.05));
}).add;
);

