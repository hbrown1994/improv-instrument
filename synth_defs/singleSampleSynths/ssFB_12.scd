(
SynthDef(\ssFB_12, {
	arg trig=0, vel, gate=1, bus, loopRoute=0, out, amp, freq=#[0, 0, 0, 0, 0], rq=#[0, 0, 0, 0, 0], integrator=#[0, 0, 0, 0, 0], mult=#[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
	var sig, n, locIn, dust;
	n = 5;
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;

	sig = DiodeRingMod.ar(sig*Line.kr(1000, 1, 0.005), BRF.ar(sig.scramble, freq, rq));
	sig = Integrator.ar(sig, integrator);
	sig = sig * mult.clump(5);
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
