(
SynthDef(\ssFB_4, {
	arg trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn;
	n = 8;

	sig = Impulse.ar(0)!n;

	locIn = LocalIn.ar(n);

	sig = sig + locIn.sum;

	sig = Integrator.ar(sig, {TRand.kr(0.8, 1, trig!n) });

	sig = sig * ({ { {TRand.kr(-1, 1, trig)} * 100000 } ! n } ! n);

	sig = sig.sum;

	LocalOut.ar(sig);

	sig = LeakDC.ar(sig);
	sig = sig.clip2;

	sig = sig.select{|item, i| i<2}*0.3;

	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 12000)*0.8;

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
