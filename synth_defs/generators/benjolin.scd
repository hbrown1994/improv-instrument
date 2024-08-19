(
SynthDef(\benjolin,{ |gate, fbSel1, fbSel2, fbSel3, fbSel4, pedal, cvFreq1, cvFreq2, cvRungler1, cvRungler2, freq1= 40, freq2=4, scale=1, rungler1=0.16, rungler2=0.0, runglerFilt=9, loop=0, filtFreq=40, q=0.82, gain=1, filterType=0, outSignal=6, amp=1, inBus1, inBus2, inBus3, inBus4, out, busAmp, bus, monoBus |
	var osc1, osc2, tri1, tri2, sh0, sh1, sh2, sh3, sh4, sh5, sh6, sh7, sh8=1, rungler, pwm, filt, sig;
	var sr;
	var osc2freq, buf, bufR;
	var freq1sig, freq2sig, rungler1sig, rungler2sig;
	var in1, in2, in3, in4;

	in1 = Select.ar(fbSel1, [In.ar(inBus1, 1), InFeedback.ar(inBus1, 1)]);
	in2 = Select.ar(fbSel2, [In.ar(inBus2, 1), InFeedback.ar(inBus2, 1)]);
	in3 = Select.ar(fbSel3, [In.ar(inBus3, 1), InFeedback.ar(inBus3, 1)]);
	in4 = Select.ar(fbSel4, [In.ar(inBus4, 1), InFeedback.ar(inBus4, 1)]);

	in1 = EnvFollow.ar(in1).linexp(0.0, 0.7, 20.0, 20000);
	in1 = Select.ar(K2A.ar(EnvFollow.ar(in1)>0), [K2A.ar(freq1), in1]);

	in2 = EnvFollow.ar(in2).linexp(0.0, 0.7, 0.1, 10000);
	in2 = Select.ar(K2A.ar(EnvFollow.ar(in2)>0), [K2A.ar(freq2), in2]);

	in3 = EnvFollow.ar(in3).linlin(0.0, 0.7, 0.0, 1.0);
	in3 = Select.ar(K2A.ar(EnvFollow.ar(in3)>0), [K2A.ar(rungler1), in3]);

	in4 = EnvFollow.ar(in4).linlin(0.0, 0.7, 0.0, 1.0);
	in4 = Select.ar(K2A.ar(EnvFollow.ar(in2)>0), [K2A.ar(rungler2), in4]);

	freq1sig = Select.ar(cvFreq1, [K2A.ar(freq1), in1]);
	freq2sig = Select.ar(cvFreq2, [K2A.ar(freq2), in2]);
	rungler1sig = Select.ar(cvRungler1, [K2A.ar(rungler1), in3]);
	rungler2sig = Select.ar(cvRungler2, [K2A.ar(rungler2), in4]);

	bufR = LocalIn.ar(2,0);
	rungler = bufR.at(0);
	buf = bufR.at(1);

	sr = SampleDur.ir;
	//sr = ControlDur.ir;
	tri1 = LFTri.ar((rungler*rungler1sig)+freq1sig);
	tri2 = LFTri.ar((rungler*rungler2sig)+freq2sig);
	osc1 = PulseDPW.ar((rungler*rungler1sig)+freq1sig);
	osc2 = PulseDPW.ar((rungler*rungler2sig)+freq2sig);

	//pwm = tri1 > tri2;
	pwm = BinaryOpUGen('>', (tri1 + tri2),(0));

	osc1 = ((buf*loop)+(osc1* (loop* -1 +1)));
	sh0 = BinaryOpUGen('>', osc1, 0.5);
	sh0 = BinaryOpUGen('==', (sh8 > sh0), (sh8 < sh0));
	sh0 = (sh0 * -1) + 1;

	sh1 = DelayN.ar(Latch.ar(sh0,osc2),0.01,sr);
	sh2 = DelayN.ar(Latch.ar(sh1,osc2),0.01,sr*2);
	sh3 = DelayN.ar(Latch.ar(sh2,osc2),0.01,sr*3);
	sh4 = DelayN.ar(Latch.ar(sh3,osc2),0.01,sr*4);
	sh5 = DelayN.ar(Latch.ar(sh4,osc2),0.01,sr*5);
	sh6 = DelayN.ar(Latch.ar(sh5,osc2),0.01,sr*6);
	sh7 = DelayN.ar(Latch.ar(sh6,osc2),0.01,sr*7);
	sh8 = DelayN.ar(Latch.ar(sh7,osc2),0.01,sr*8);

	rungler = ((sh1/2.pow(8))+(sh2/2.pow(7))+(sh3/2.pow(6))+(sh4/2.pow(5))+(sh5/2.pow(4))+(sh6/2.pow(3))+(sh7/2.pow(2))+(sh8/2.pow(1)));

	buf = rungler;
	rungler = (rungler * scale.linlin(0,1,0,127));
	rungler = rungler.midicps;

	LocalOut.ar([rungler,buf]);

	filt = SelectX.ar(filterType, [
		RLPF.ar(pwm,(rungler*runglerFilt)+filtFreq,q* -1 +1,gain),
		RHPF.ar(pwm,(rungler*runglerFilt)+filtFreq,q* -1 +1,gain),
		SVF.ar(pwm,(rungler*runglerFilt)+filtFreq,q,1,0,0,0,0,gain),
		DFM1.ar(pwm,(rungler*runglerFilt)+filtFreq,q,gain,1)
	]);

	sig = SelectX.ar(outSignal, [
		osc1, tri2, osc2, pwm, sh0, filt
	]);
	sig = Clip.ar(sig, -1.0, 1.0);

	pedal = pedal.linexp(0.0, 1.0, 0.001, amp);
	pedal = Select.kr(BinaryOpUGen('==', 0.001, pedal), [pedal, 0]);

	sig = sig*pedal.lag(0.005);

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	Out.ar(monoBus, (sig*busAmp.lag(0.05)));

	sig = LeakDC.ar(sig * amp ! 2);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
}).add;
);

