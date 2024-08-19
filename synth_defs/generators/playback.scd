(
SynthDef(\playback, {
	arg monoBus ,buf, out=0, amp, gate=0, t_trig, bus, pedalAmp, busAmp, rateLow, rateHigh, cut;
	var sig;

	sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf)*TRand.kr(rateLow, rateHigh, t_trig), Trig.kr(gate)+t_trig, 1.0, 1.0);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig = sig * cut.lag(0.005);

	Out.ar(out, sig*pedalAmp.linlin(0.0, 1.0, 0.0, amp));
	Out.ar(bus, sig*pedalAmp.linlin(0.0, 1.0, 0.0, busAmp));
	Out.ar(monoBus, Mix.new(sig*pedalAmp.linlin(0.0, 1.0, 0.0, busAmp)));
}).add;
);

