(
SynthDef(\ssFB_3, {
	arg freqs=#[0, 0, 0, 0, 0, 0, 0], bwr=#[0, 0, 0, 0, 0, 0, 0], integrator=#[0, 0, 0, 0, 0, 0, 0], mult=rrand(0.001, 0.4), trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn;
	n = 8;

	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;

	sig = Resonz.ar(sig, freqs, bwr*sig*(mult*8));

	sig = Integrator.ar(sig, integrator);
	sig = sig * ({ { TRand.kr(-1.0, 1.0, Dust.kr(mult*2)) * 10000 } ! n } ! n);
	sig = sig.sum;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;

	LocalOut.ar(sig);
	sig = sig.select{|item, i| i<2}*0.4;

	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 12000)*0.8;
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
