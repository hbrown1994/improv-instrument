(
SynthDef(\lpfNoise, {
	arg cutoff, fb, mul=0.6, gate=1, amp, out, bus, maxAmp;
	var noise, sig;

	noise = WhiteNoise.ar() + LocalIn.ar(2);
	sig = LPF.ar(noise, cutoff, 0.6)!2;

	LocalOut.ar(Clip.ar(sig)*fb);
	sig = LeakDC.ar(Clip.ar(sig), mul: mul);
	sig = sig * EnvGen.kr(Env.asr(0.6, 1, 0.005), gate, doneAction: 2) * amp.linlin(0, 127, 0.0, maxAmp);

	Out.ar(out, sig);
}).add;
);