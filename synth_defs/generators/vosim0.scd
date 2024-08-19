(
SynthDef(\vosim0, {
	arg amp, monoBus, bus, pedalAmp, busAmp, fbMult, state, numCycles, decay, freqLow, freqHigh, trigPres, trigPres1, out=0;
	var trigLow=0.0, trigHigh=1.0, p,t,f,n,d,a,pan,x,y,z,x_,y_, trig, sig, fb;
	state;
	fb = LocalIn.ar(1);
	trig = fb;
	p = Select.kr(TWindex.ar(trig, [0.3, 0.7, 0.5], 1), [0.5, 1, 2]); //doubles trig speed sometimes
	trigHigh = trigPres.linexp(0, 127, 0.001, 500);
	trigHigh = Select.kr(state, [trigHigh, freqLow.linexp(0, 127, 0.1, 50)]);
	trigLow = Select.kr(state, [0.0, freqLow.linexp(0, 127, 0.2, 50)*Select.kr(state, [1, trigPres.linlin(0, 127, 0.0, 2.0)])]);
	t = Impulse.ar(LFBrownNoise1.ar(5).range(trigLow, trigHigh)*p) + Trig.ar(trigPres>0, 0.05) + Trig.ar(Changed.kr(state), 0.05); //trig rate
	t = Gate.ar(t, trigHigh>0.001);
	freqLow = Select.kr(state, [freqLow.linexp(0, 127, 40, 10000), freqHigh.linexp(0, 127, 20, 6000)]);
	freqHigh = Select.kr(state, [freqHigh.linexp(0, 127, 40, 10000), freqHigh.linexp(0, 127, 40, 9000)]);
	f = TExpRand.ar(freqLow, freqHigh, t); //freq
	f = Select.ar(state, [f, f, Clip.ar(fb.linlin(-1.0, 1.0, 100, 6000), 20, 20000)]);

	n = TExpRand.ar(numCycles.linexp(0, 127, 5, 500), numCycles.linexp(0, 127, 20, 600), t);
	d = TExpRand.ar(decay.linexp(0, 127, 0.2, 1.8), decay.linexp(0, 127, 0.4, 2.5), t);
	a = 1;//d.expexp(0.2, 2.5, 5.0, 0.01).poll; //amplitude
	sig = Mix.new(VOSIM.ar(t, f, n, d) * a);
	sig = LeakDC.ar(sig);
	LocalOut.ar(t);
	sig = Clip.ar(sig, -1.0, 1.0);
	pan = TRand.ar(-0.8, 0.8, t);
	sig = Pan2.ar(sig, pan);

	sig = Compander.ar(sig, sig, 0.015,
		slopeBelow: 1,
        slopeAbove: 0.5,
        clampTime:  0.01,
        relaxTime:  0.1, mul: 5);

	sig = LeakDC.ar(sig);

	Out.ar(out, sig);
}).add;
);




