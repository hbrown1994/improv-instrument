(
SynthDef(\ssFB_8, {
	arg  sigLow, sigHigh, mult=#[0, 0, 0, 0, 0, 0, 0, 0, 0], freq=#[0, 0, 0], clip=#[0, 0, 0], skew=#[0, 0, 0], sync=#[0, 0, 0], iminsweep=#[0, 0, 0], mulSel, integrator=#[0, 0, 0], trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var multClump, test, sig, n, locIn, dust;
	n = 3;
	trig = trig + Impulse.kr(0);
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;
	sig = GlitchBPF.ar(sig*Line.kr(10000, 1.0, 0.05), Latch.ar(sig.linlin(-1.0, 1.0, sigLow, sigHigh), trig.linlin(0, 1, 1, 0)+Impulse.kr(0)),
		Gate.ar(Squine.ar(
			freq,
			clip,
			skew,
			sync,
			iminsweep: iminsweep),
		trig+Impulse.kr(0)) * 5,
		Select.ar(mulSel, [sig, sig.scramble])
	);
	sig = Integrator.ar(sig, integrator);
	mult = mult.clump(3);
	sig = sig * mult;
	//sig = sig * ({ { {TRand.kr(-1.0, 1.0, trig)} * 10000 } ! n } ! n);
//	sig;
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