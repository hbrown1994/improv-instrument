(
SynthDef(\drumRec, {
	arg monoBus, t_trig, in, rate=1, size, toggle=0, out=0, sizeMin=512, bufferSize=5, maxRate=20, recordBypass=1, state=0, recState=0, busAmp, bus, amp;
	var input, sig, env, buf, phasor, impulse;

	toggle = toggle.linlin(0, 127, 0.0, 1.0).lag(0.005);

	t_trig = Gate.kr(t_trig, Select.kr(recState, [1, toggle.linlin(0, 1, 1, 0)])  * recordBypass);

	buf = LocalBuf(SampleRate.ir*bufferSize, 2);

	input = In.ar(in, 2) * EnvGen.kr(Env.linen(0.005, 2, 0.005), t_trig);

	RecordBuf.ar(
		input,
		buf,
		loop: 0,
		trigger:
		Trig.ar(
			t_trig+Trig.kr(recordBypass),
			Select.kr(recState, [0.01, bufferSize])
		)
	);
	phasor = Phasor.ar(
		Trig.ar(toggle, 0.005),
		Gate.kr(size.linexp(0, 127, 1, maxRate), state+Impulse.kr(0)),
		0,
		Gate.kr(size.linexp(0, 127, SampleRate.ir*bufferSize, sizeMin).lag(0.05), state.linlin(0, 1, 1, 0)+Impulse.kr(0).linlin(0, 1, 1, 0))
	);


	sig = BufRd.ar(2, buf, phasor, 0);

	sig = sig * toggle;

	amp = amp.linexp(0.0, 1.0, 0.0001, 1.0);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(monoBus, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

