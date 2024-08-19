(
SynthDef(\ssFB_4, {
	arg multAlt=#[0, 0, 0, 0, 0, 0, 0], a0=#[0, 0, 0, 0, 0, 0, 0], a1=#[0, 0, 0, 0, 0, 0, 0], b1=#[0, 0, 0, 0, 0, 0, 0], trig=0, trigFreq=rrand(0.01, 3), gate=1, bus, loopRoute=0, out, amp, mult=rrand(0.01, 3), integrator=#[0, 0, 0, 0, 0, 0, 0];
	var sig, n, locIn, sigL, sigR;
	n = 7;


	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn;
	sig = FOS.ar(sig, a0, a1, b1);
	sig = FOS.ar(sig, sig, sig, sig);
	//sig = Integrator.ar(sig, integrator);
	sig = sig * ({ { multAlt * mult} ! n } ! n);
	sig = LeakDC.ar(sig);
	sig = sig.clip2*0.2;
	LocalOut.ar(sig);
	//sig = sig.select{|item, i| i<2}*0.3;
	sig = Mix(sig);
	Splay.ar(Mix(sig));
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 7000)*0.21;


	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
