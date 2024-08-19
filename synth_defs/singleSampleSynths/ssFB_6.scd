(
SynthDef(\ssFB_6, {
	arg integrator=#[0, 0, 0, 0, 0, 0, 0, 0, 0], mult=#[0, 0, 0, 0, 0, 0, 0, 0, 0], trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn, dust;
	n = 9;
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn;
	sig = HPF.ar(sig*Line.kr(1000, 1.0, 0.005), sig.scramble);
	sig = Integrator.ar(sig, integrator);
	sig = sig * ({ { mult * 1000 } ! n });
	sig = sig;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;
	LocalOut.ar(sig);
	sig = Mix(sig);
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig, 0)), 8000);
	sig = (sig*0.08).clip2;
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig = Compander.ar(sig, sig, 0.02,
		slopeBelow: 1,
        slopeAbove: 0.5,
        clampTime:  0.01,
        relaxTime:  0.01, mul: 6);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
