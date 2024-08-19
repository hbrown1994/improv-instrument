(
SynthDef(\ssFB_5, {
	arg trig=0, dustFreq=rrand(0.01, 3), gate=1, bus, loopRoute=0, out, amp, mult=rrand(0.01, 3), integrator=#[0, 0, 0, 0, 0, 0, 0];
	var sig, n, locIn, dust, sigL, sigR;
	n = 7;

//	\dustFreq, rrand(0.01, 3), \mult, rrand(1, 10000)

	dust = {Dust.kr(dustFreq)};
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;
	sig = FOS.ar(sig, {TRand.kr(trig: dust)}, {TRand.kr(trig: dust)} , {TRand.kr(trig: dust)});
	sig = FOS.ar(sig, sig, sig , sig);
	sig = Integrator.ar(sig, integrator);
	sig = sig * ({ { {TRand.kr(-1.0, 1.0, dust)} * mult} ! n } ! n);
	sig = sig;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;
	LocalOut.ar(sig);
	//sig = sig.select{|item, i| i<2}*0.3;
	sig = Mix(sig);
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 7000)*0.15;
	sigL = PanX.ar(2, sig[0], TBrownRand.kr(0, 1, 1, 0, Onsets.kr(FFT(LocalBuf(512), sig[0]), 0.8)));
	sigR = PanX.ar(2, sig[1], TBrownRand.kr(0, 1, 1, 0, Onsets.kr(FFT(LocalBuf(512), sig[1]), 0.8)));

	sig = Mix([sigL, sigR]);

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
