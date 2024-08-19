(
SynthDef(\ssFB_5, {
	arg trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn, dust;
	n = 7;
	dust = {Dust.kr({Rand(0.01, vel.linlin(0, 127, 0.0, 2.0))})};
	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;
	sig = FOS.ar(sig, {TRand.kr(trig: dust)}, {TRand.kr(trig: dust)} , {TRand.kr(trig: dust)});
	sig = FOS.ar(sig, sig, sig , sig);
	sig = Integrator.ar(sig, {TRand.kr(0.01, 1.0, trig)});
	sig = sig * ({ { {TRand.kr(-1.0, 1.0, dust)} * TRand.kr(1, 10000, trig) } ! n } ! n);
	sig = sig;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;
	LocalOut.ar(sig);
	//sig = sig.select{|item, i| i<2}*0.3;
	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 8000)*0.2;
	sig = PanX.ar(2, sig, TBrownRand.kr(0, 1, 1, 0, Onsets.kr(FFT(LocalBuf(512), sig), 0.8)));

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
