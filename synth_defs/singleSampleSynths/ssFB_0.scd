(
SynthDef(\ssFB_0, {
	arg freq=0.1, bwr=7, noiseFreq=0.1, t_trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n;

	n = 8;
	sig = Impulse.ar(0);
	sig = sig + LocalIn.ar(n);
	t_trig = t_trig + Impulse.kr(0);
	sig = Resonz.ar(sig, freq*sig, bwr);
	sig = Integrator.ar(sig, 0.99);
	sig = sig * ({ { LFNoise2.kr(noiseFreq) * 10000 } ! n } ! n);
	sig = sig.sum;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;

	LocalOut.ar(sig);
	sig  = LPF.ar(sig, 12000);
	sig = Splay.ar(sig) * 0.4;

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);