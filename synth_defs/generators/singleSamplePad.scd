(
SynthDef(\singleSamplePad, {
	arg trig, shift, cut, amp=1, out=0, morph=( -0.07), filter, busAmp, bus, gate;
	var sig;

	sig = Impulse.ar(trig);
	sig = sig + LocalIn.ar(8);
	sig = Select.ar(filter, [
		Resonz.ar(sig, {TRand.kr(0.0001, 20000, trig) }, {TRand.kr(0, 5, trig)}),
		RLPF.ar(sig, {TRand.kr(0.0001, 20000, trig) }, {TRand.kr(0, 5, trig)}),
		RHPF.ar(sig, {TRand.kr(0.0001, 20000, trig) }, {TRand.kr(0, 5, trig)})
	]);

	cut = cut.linlin(0, 1, 1, 0);

	sig = Integrator.ar(sig, 0.99) * Select.kr(morph>(-0.004),
		[
			Clip.kr(morph, -0.1, -0.004),
			Clip.kr(morph, 0.004, 0.1)

	]);

	sig = sig * ({ { TRand.kr(-1, 1, (trig+shift)) * 1000 } ! 8 } ! 8);
	sig = sig.sum;

	sig = LeakDC.ar(sig).clip2;
	LocalOut.ar(sig);

	sig = SplayAz.ar(GlobalPresets.numChannels, sig) * 0.1;

	sig = LeakDC.ar(sig * cut);

	EnvGen.kr(Env.asr(0, 1, 0), gate, doneAction: 2);


	Out.ar(out, (sig*amp));
	Out.ar(bus, (sig*busAmp));
}).add;
);