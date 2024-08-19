(
SynthDef(\vosimMix, {
	arg gate=0, vosim0, vosim1, amp=1, monoBus, bus, pedal, busAmp, out=0;
	var sig, in0, in1, pedalAmp, pedalBusAmp;

	in0 = In.ar(vosim0, 2);
	in1 = In.ar(vosim1, 2);

	sig = Mix.new([in0, in1]);

	sig = sig *  EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	pedalAmp = pedal.linexp(0.0, 1.0, 0.001, amp);
	pedalAmp = Select.kr(BinaryOpUGen('==', 0.001, pedalAmp), [pedalAmp, 0]).lag(0.005);

	pedalBusAmp = pedal.linexp(0.0, 1.0, 0.001, amp);
	pedalBusAmp = Select.kr(BinaryOpUGen('==', 0.001, pedalBusAmp), [pedalBusAmp, 0]).lag(0.005);

	Out.ar(out, sig*pedalAmp);
	Out.ar(bus, sig*pedalBusAmp);
	Out.ar(monoBus, Mix.new(sig*pedalBusAmp ));
}).add;
);





