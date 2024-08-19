(
SynthDef(\vosim1, {
	arg amp, monoBus, out, bus, pedalAmp, busAmp, cv1, cv2, cv3, cv4, fbSel1, fbSel2, fbSel3, fbSel4, decaySel, freqSel, trigSel, inBus1, inBus2, inBus3, inBus4, trigFreqPresAlt, trigFreq, freq, numCycles, decay, trigFreqPres, state, numCyclesPres;
	var trigFreqSigAlt, trigFreqSig, trigLow, trigHigh, p,t,f,n,d,a,l,x,y,z,x_,y_, trig, sig;
	var in1, in2, in3, in4;

	in1 = Select.ar(fbSel1, [In.ar(inBus1, 1), InFeedback.ar(inBus1, 1)]);
	in2 = Select.ar(fbSel2, [In.ar(inBus2, 1), InFeedback.ar(inBus2, 1)]);
	in3 = Select.ar(fbSel3, [In.ar(inBus3, 1), InFeedback.ar(inBus3, 1)]);
	in4 = Select.ar(fbSel4, [In.ar(inBus4, 1), InFeedback.ar(inBus4, 1)]);

	in1 = EnvFollow.ar(in1).linexp(0.0, 0.7, 0.1, 2000);
	in2 = EnvFollow.ar(in2).linexp(0.0, 0.7, 100, 10000);
	in3 = EnvFollow.ar(in3).linlin(0.0, 0.7, 1, 200);
	in4 = EnvFollow.ar(in4).linlin(0.0, 0.7, 0.0, 1.0);

	trigFreqSig = Select.kr(state, [
		trigFreq.lag(0.05),
		trigFreqPres.lag(0.05)
	]);

	trigFreqSigAlt = trigFreqPresAlt.lag(0.05);
	trigFreqSig = Select.kr(state, [[trigFreqSig, 0], [trigFreqSig, trigFreqSigAlt]]);

	in1 = Select.ar(K2A.ar(EnvFollow.ar(in1)>0), [K2A.ar(trigFreqSig), in1]);
	trigFreqSig = Select.ar(cv1, [K2A.ar(trigFreqSig), in1]);

	t = SelectX.ar(trigSel, [Impulse.ar(trigFreqSig), Dust.ar(trigFreqSig)]); //trig rate
	t = Gate.ar(t, trigFreqSig>0.1);

	f = Select.ar(state, [
		SelectX.ar(freqSel, [K2A.ar(freq), TBrownRand.ar(100, freq, 0.3, 0, t)]),
		SelectX.ar(freqSel, [K2A.ar(trigFreq.linlin(0.1, 3000, 100, 10000)), TBrownRand.ar(100, K2A.ar(trigFreq.linlin(0.1, 3000, 100, 10000)), 0.3, 0, t)])
	]);

	f[1] = Select.ar(state, [DC.ar(0), K2A.ar(freq)]);
	in2 = Select.ar(K2A.ar(EnvFollow.ar(in2)>0), [f, in2]);
	f = Select.ar(cv2, [f, in2]);

	n = K2A.ar(numCycles);
	in3 = Select.ar(K2A.ar(EnvFollow.ar(in3)>0), [n, in3]);
	n = Select.ar(cv3, [n, in3]);

	d = SelectX.ar(decaySel, [K2A.ar(decay), TBrownRand.ar(0.3, decay, 0.3, 0, t)]);
	in4 = Select.ar(K2A.ar(EnvFollow.ar(in4)>0), [d, in4]);
	d = Select.ar(cv4, [d, in4]);

	//a = d.linexp(0.3, 1.04, 3.0, 0.4); //amplitude
	a=5;
	sig = Select.ar(state, [Mix.new(VOSIM.ar(t+Impulse.ar(0), f, n, d, a)), Splay.ar(VOSIM.ar(t+Impulse.ar(0), f, n, d, a))]);
	sig = Clip.ar(sig, -1.0, 1.0);
	sig = LeakDC.ar(sig);

	Out.ar(out, sig);
}).add;
);

