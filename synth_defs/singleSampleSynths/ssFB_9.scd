(
SynthDef(\ssFB_9, {
	arg freq, pulseFreq=#[0, 0, 0, 0, 0, 0, 0, 0, 0], width=#[0, 0, 0, 0, 0, 0, 0, 0, 0], mul, add, integrator=#[0, 0, 0, 0, 0, 0, 0, 0, 0], mult = #[ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80 ], trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn, dust;
	n = 9;
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;
	sig = GlitchRHPF.ar(sig, freq, Pulse.kr(pulseFreq, width, mul, add));
	sig = Integrator.ar(sig, integrator);
	sig = sig * (mult.clump(9)*vel);
	sig = sig;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;
	LocalOut.ar(sig);
	sig = Mix(sig);
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 10000)*0.2;

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	sig = Compander.ar(sig, sig, 0.02,
		slopeBelow: 1,
        slopeAbove: 0.5,
        clampTime:  0.01,
        relaxTime:  0.01, mul: 2);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
