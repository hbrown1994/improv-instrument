(
SynthDef(\ssFB_1, {
	arg freqs=#[2.9333065629005, 0.72056504487991, 2.5012403011322], clip, skew, sync, mul, iminsweep, integrator=#[0.99480381846428, 0.9959473323822, 0.99140681624413], mult, n, t_trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, locIn;

	integrator;

	n = 3;
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;
	sig = GlitchBPF.ar(sig, Latch.ar(sig.range(8000, 20000), t_trig+Onsets.kr(FFT(LocalBuf(512), Mix(sig)))),
		Gate.ar(Squine.ar(freqs, clip, skew, sync, mul, iminsweep: iminsweep), t_trig+Impulse.kr(0)) * 5;
	);

	sig = Integrator.ar(sig, integrator);
	sig = sig * ({ { mult * 100000 } ! n } ! n);
	sig = sig.sum;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;
	LocalOut.ar(sig);
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 12000)*0.2;

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
