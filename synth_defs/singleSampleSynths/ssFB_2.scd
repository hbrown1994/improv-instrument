(
SynthDef(\ssFB_2, {
	arg freqs=#[ 10301.393897863, 1014.6064498567, 11297.446514018, 13562.439689683, 3185.5113101857, 14046.156412762, 8621.8625732292, 4243.9342262176 ], bwr=#[ 4.5781893718243, 3.9555124890804, 4.0394404900074, 0.3512173807621, 4.9432294797897, 4.8571373987198, 0.78978029727936, 2.4657164132595 ], integrator = #[ 0.99204580307007, 0.98423890829086, 0.98734346389771, 0.98956395864487, 0.98664091110229, 0.98935814142227, 0.98357380390167, 0.99141238689423 ], noiseMult = rrand(-1.0, 1.0), t_trig=0, vel, gate=1, bus, loopRoute=0, out, amp;
	var sig, n, locIn;
	n = 8;

	sig = Impulse.ar(0)!n;
	locIn = LocalIn.ar(n);
	sig = sig + locIn.sum;

	sig = Resonz.ar(sig, freqs, bwr);
	Integrator.ar(sig, integrator);
	sig = sig * ({ { noiseMult * 1000 } ! n } ! n);

	sig = sig.sum;
	sig = LeakDC.ar(sig);
	sig = sig.clip2;

	LocalOut.ar(sig);
	sig = sig.select{|item, i| i<2};

	sig = LPF.ar(LeakDC.ar(Splay.ar(sig)), 12000)*0.3;
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig  = sig * loopRoute.linlin(0, 1, 1, 0).lag(0.005) * amp.lag(0.005);

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);
