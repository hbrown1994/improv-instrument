(
SynthDef(\ssFB_13, {
	arg trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn, dust;

	n = 5;
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;
	sig = Decimator.ar(sig, {TRand.kr(1000, SampleRate.ir, trig)}!n, {TIRand.kr(1, 16, trig)}!n);
	sig = sig * ({ { {TRand.kr(-1.0, 1.0, trig)} * vel.linlin(0, 127, 100, 10000) } ! n } ! n);
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
