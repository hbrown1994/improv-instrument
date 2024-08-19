(
SynthDef(\ssFB_7, {
	arg multNeg=#[0, 0, 0], multAlt=rrand(10, 10000), integrator = #[0, 0, 0], clip=#[0, 0, 0], skew=#[0, 0, 0], sync=#[0, 0, 0], mul=#[0, 0, 0], freq = #[0, 0, 0], dustFreq, trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn, dust;
	n = 3;
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn;

	sig = GlitchBPF.ar(
		sig,
		Latch.ar(sig.linlin(-1, 1, 9000, 20000), Dust.kr(dustFreq)+Impulse.ar(0)),
		Squine.ar(freq, clip, skew, sync, mul);
	);

	sig = Integrator.ar(sig, integrator);
	sig = sig * ({ { multNeg * multAlt } ! n });

	sig = LeakDC.ar(sig);
	sig = sig.clip2;
	LocalOut.ar(sig);

	sig = Mix(sig);
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig, 0.3)), 9000)*0.2;
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	sig = Compander.ar(sig, sig, 0.02,
		slopeBelow: 1,
        slopeAbove: 0.5,
        clampTime:  0.01,
        relaxTime:  0.01, mul: 3.0);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
