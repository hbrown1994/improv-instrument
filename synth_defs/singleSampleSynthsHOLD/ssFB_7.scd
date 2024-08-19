(
SynthDef(\ssFB_7, {
	arg trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn, dust;
	n = 3;
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;
    //sig = Disintegrator.ar(sig, probability: {TRand.kr(0.0, 1.0, trig)!n }, multiplier: {TRand.kr(-1.0, 1.0, trig)!n }, mul: 1.0, add: 0);
	sig = GlitchBPF.ar(sig, Latch.ar(sig.linlin(-1, 1, 9000, 20000), Dust.ar({Rand(0.01, 1.0)})+Impulse.ar(0)), Squine.ar({TRand.kr(0, 0.02, trig)}!n, {TRand.kr(0.0, 1.0, trig)}, {TRand.kr(-1.0, 1.0, trig)}, {TRand.kr(0.0, 2.0, trig)}, {TRand.kr(0.0, 100.0, trig)}));
	sig = Integrator.ar(sig, {TRand.kr(0.2, 1.0, trig!n) });
	sig = sig * ({ { {TRand.kr(-1.0, 1.0, trig)} * vel.linlin(0, 127, 10, 10000) } ! n } ! n);
	sig = LeakDC.ar(sig);
	sig = sig.clip2;
	LocalOut.ar(sig);
	//sig = sig.select{|item, i| i<2}*0.3;
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 12000)*0.2;


	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
