(
SynthDef(\oneShot, {
	arg monoBus, buf, out, amp=0.3, hpf, lpf, lpfFreq, hpfFreq, lpfOn, hpfOn, masterAmp, t_trig=0, loopBus, busAmp, bus, pan;
	var sig, toggle;

	toggle = ToggleFF.kr(t_trig);

	sig =
	(PlayBuf.ar(1, buf, BufRateScale.kr(buf), Trig.kr(toggle), 0, 0)*toggle.lag(0.005))+
	(PlayBuf.ar(1, buf, BufRateScale.kr(buf), Trig.kr(toggle.linlin(0, 1, 1, 0)), 0, 0)*toggle.linlin(0, 1, 1, 0).lag(0.005));

	EnvGen.kr(Env.linen(0, BufFrames.kr(buf)/SampleRate.ir, 0), t_trig, doneAction: 2);

	amp = amp.linexp(0.0, 1.0, 0.0001, 1.0);

	sig = Pan2.ar(sig, pan);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(loopBus, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(monoBus, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

