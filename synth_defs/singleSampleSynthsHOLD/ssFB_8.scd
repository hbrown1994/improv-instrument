(
SynthDef(\ssFB_8, {
	arg trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn, dust;
	n = 3;
	trig = trig + Impulse.kr(0);
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;
	sig = GlitchBPF.ar(sig, Latch.ar(sig.range(8000, 20000), trig.linlin(0, 1, 1, 0)+Impulse.kr(0)),
		Gate.ar(Squine.ar({TRand.kr(0, 0.8, trig)}!n, {TRand.kr(0.0, 1.0, trig)}, {TRand.kr(-2.0, -2.0, trig)}, {TRand.kr(0.0, 2.0, trig)}, iminsweep: {TRand.kr(0.0, 100.0, trig)}), trig+Impulse.kr(0)) * 5;
	);
	sig = Integrator.ar(sig, {TRand.kr(0.99, 1.0, trig!n) });
	sig = sig * ({ { {TRand.kr(-1.0, 1.0, trig)} * 100000 } ! n } ! n);
	sig = sig.sum;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;
	LocalOut.ar(sig);
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 12000)*0.4;
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
