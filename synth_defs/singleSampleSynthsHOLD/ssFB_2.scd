(
SynthDef(\ssFB_2, {
	arg trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn;
	n = 8;

	sig = Impulse.ar(0)!n;

	locIn = LocalIn.ar(n);

	sig = sig + locIn.sum;

	trig = MouseButton.kr(lag:0);

	sig = Resonz.ar(sig, {TExpRand.kr(0.0001, 15000, trig!n) }, {TRand.kr(0.01, 5, trig!n) });

	//sig = MoogVCF.ar(sig, TExpRand.kr(20, 15000, trig!n).lag(0.1), TRand.kr(0.8,0.99!n));

	Integrator.ar(sig, {TRand.kr(0.98, 1, trig!n) });

	sig = sig * ({ { LFNoise1.kr({TRand.kr(0.001, 0.4, trig)}) * 1000 } ! n } ! n);

	sig = sig.sum;

	sig = LeakDC.ar(sig);

	sig = sig.clip2;

	LocalOut.ar(sig);
	sig = sig.select{|item, i| i<2}*0.8;

	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 12000)*0.2;

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
