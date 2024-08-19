SynthDef(\sumBus, {
	arg monoBus, in0, in1, in2, out, bus, gate,  busAmp;
	var sig;

	sig = Mix.new([In.ar(in0, 2), In.ar(in1, 2)]);

	EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	Out.ar(bus, sig);
	Out.ar(monoBus, Mix.new(sig));
	Out.ar(out, sig);
}).add;