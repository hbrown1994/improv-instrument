SynthDef(\fieldFFTSines, {
	arg lpf0=1, hpf0=0, lpf1=1, hpf1=0, panLatch0=0, panLatch1=0, panFreq0=0, panFreq1, rate0, rate1, play0, play1, env0, env1, gate, bus, busAmp=1, sine0 = 0, sine1 = 0, sine2 = 0, sine3 = 0, amp0=1, amp1=1, amp=1, out=0, buf0, buf1, fftBuf0, fftBuf1, fftBuf2, fftBuf3, mix0, mix1, freeze0, freeze1, morph0, morph1, bend0, stretch0, bend1, stretch1, bend2, stretch2, bend3, stretch3;
	var pan0, pan1, envSig0, envSig1, env, in0, in1, sig, busSig, sig0, sig1, chain, chain0, chain1, chain2, chain3;

	in0 = PlayBuf.ar(2, buf0, rate0.linlin(0, 1, 0, 2).clip(0.05, 2), play0, 0, 1, 0);
	in1 = PlayBuf.ar(2, buf1, rate1.linlin(0, 1, 0, 2).clip(0.05, 2), play1, 0, 1, 0);

	panLatch0 = panLatch0.linlin(0, 1, 1, 0);
	panLatch1 = panLatch1.linlin(0, 1, 1, 0);

	panFreq0 = panFreq0.linlin(0, 1, 0.005, 20.0);
	panFreq1 = panFreq1.linlin(0, 1, 0.005, 20.0);

	lpf0=lpf0.linlin(0, 1, 40, 10000).clip(40, 10000).lag(0.1);
	hpf0=hpf0.linlin(0, 1, 40, 10000).clip(40, 10000).lag(0.1);
	lpf1=lpf1.linlin(0, 1, 40, 10000).clip(40, 10000).lag(0.1);
	hpf1=hpf1.linlin(0, 1, 40, 10000).clip(40, 10000).lag(0.1);

	fftBuf0 = LocalBuf(2048);
	chain0 = FFT(fftBuf0, in0[0]);
	chain0 = PV_PartialSynthF(chain0, Select.kr(TWindex.kr(freeze0 < 1, [7, 5, 4, 3, 2, 1], 1), [0.05, 0.1, 0.4, 0.8, 1.3, 2.4]));
	chain0 = PV_MagFreeze(chain0, Trig.kr(freeze0 < 1, 0.1).linlin(0, 1, 1, 0));
	chain0 = PV_BinShift(chain0, stretch0.linlin(0, 1, 0.0, 2).clip(0.1, 2));

	fftBuf1 = LocalBuf(2048);
	chain1 = FFT(fftBuf1, in0[1]);
	chain1 = PV_PartialSynthF(chain1, Select.kr(TWindex.kr(freeze0 > 0, [7, 5, 4, 3, 2, 1], 1), [0.05, 0.1, 0.4, 0.8, 1.3, 2.4]));
	chain1 = PV_MagFreeze(chain1, Trig.kr(freeze0 > 0, 0.1).linlin(0, 1, 1, 0));
	chain1 = PV_BinShift(chain1, stretch1.linlin(0, 1, 0.0, 2).clip(0.1, 2));

	fftBuf2 = LocalBuf(2048);
	chain2 = FFT(fftBuf2, in1[0]);
	chain2 = PV_PartialSynthF(chain2, Select.kr(TWindex.kr(freeze1 > 0, [7, 5, 4, 3, 2, 1], 1), [0.05, 0.1, 0.4, 0.8, 1.3, 2.4]));
	chain2 = PV_MagFreeze(chain2, Trig.kr(freeze1 > 0, 0.1).linlin(0, 1, 1, 0));
	chain2 = PV_BinShift(chain2, stretch2.linlin(0, 1, 0.0, 2).clip(0.1, 2));

	fftBuf3 = LocalBuf(2048);
	chain3 = FFT(fftBuf3, in1[1]);
	chain3 = PV_PartialSynthF(chain3, Select.kr(TWindex.kr(freeze1 > 0, [7, 5, 4, 3, 2, 1], 1), [0.05, 0.1, 0.4, 0.8, 1.3, 2.4]));
	chain3 = PV_MagFreeze(chain3, Trig.kr(freeze1 > 0, 0.1).linlin(0, 1, 1, 0));
	chain3 = PV_BinShift(chain3, stretch3.linlin(0, 1, 0.0, 2).clip(0.1, 2));

	chain0 = PV_Compander(chain0, 50, 1.0, 0.25); //Compress signal -> 0.25 == 4:1 compression ratio
	chain1 = PV_Compander(chain1, 50, 1.0, 0.25);
	chain2 = PV_Compander(chain2, 50, 1.0, 0.25);
	chain3 = PV_Compander(chain3, 50, 1.0, 0.25);

	chain0 = PanX.ar(
		GlobalPresets.numChannels,
		IFFT(chain0),
		Gate.kr(Lag.kr(
			TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq0*panLatch0)),
			TBrownRand.kr(panFreq0.linlin(0.005, 10.0, 5, 0.005),
				panFreq0.linlin(0.005, 10.0, 30, 0.2),
				trig: Dust.kr(panFreq0*panLatch0))),
		panLatch0), 1.0, 2.0);

	chain1 = PanX.ar(
		GlobalPresets.numChannels,
		IFFT(chain1),
		Gate.kr(Lag.kr(
			TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq0*panLatch0)),
			TBrownRand.kr(panFreq0.linlin(0.005, 10.0, 5, 0.005),
				panFreq0.linlin(0.005, 10.0, 30, 0.2),
				trig: Dust.kr(panFreq0*panLatch0))),
		panLatch0), 1.0, 2.0);

	chain2 = PanX.ar(
		GlobalPresets.numChannels,
		IFFT(chain2),
		Gate.kr(Lag.kr(
			TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq1*panLatch1)),
			TBrownRand.kr(panFreq1.linlin(0.005, 10.0, 5, 0.005),
				panFreq1.linlin(0.005, 10.0, 30, 0.2),
				trig: Dust.kr(panFreq1*panLatch1))),
		panLatch1), 1.0, 2.0);

	chain3 = PanX.ar(
		GlobalPresets.numChannels,
		IFFT(chain3),
		Gate.kr(Lag.kr(
			TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq1*panLatch1)),
			TBrownRand.kr(panFreq1.linlin(0.005, 10.0, 5, 0.005),
				panFreq1.linlin(0.005, 10.0, 30, 0.2),
				trig: Dust.kr(panFreq1*panLatch1))),
		panLatch1), 1.0, 2.0);

	chain0 = Mix.new(
		[
			(chain0*sine0.linlin(0, 1, 9, 0)),
			PanX.ar(
				GlobalPresets.numChannels, SinOsc.ar(
					Latch.kr(FluidPitch.kr(in0[0])[0], freeze0 < 1),
					2pi.rand,
					sine0.linlin(0, 1, 0.0, 0.2)),
				Gate.kr(Lag.kr(
					TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq0*panLatch0)),
					TBrownRand.kr(panFreq0.linlin(0.005, 10.0, 5, 0.005),
						panFreq0.linlin(0.005, 10.0, 30, 0.2),
						trig: Dust.kr(panFreq0*panLatch0))), panLatch0),
				1.0, 2.0),
			PanX.ar(GlobalPresets.numChannels, SinOsc.ar(
				Latch.kr(FluidPitch.kr(in0[0])[0], freeze0 < 1)*bend0.linlin(0, 1, 0.5, 1.5),
				2pi.rand,
				sine0.linlin(0, 1, 0.0, 0.2)),
			Gate.kr(Lag.kr(TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq0*panLatch0)),
				TBrownRand.kr(panFreq0.linlin(0.005, 10.0, 5, 0.005),
					panFreq0.linlin(0.005, 10.0, 30, 0.2),
					trig: Dust.kr(panFreq0*panLatch0))), panLatch0),
			1.0, 2.0);
		]
	);

	chain1 = Mix.new(
		[
			(chain1*sine1.linlin(0, 1, 9, 0)),
			PanX.ar(
				GlobalPresets.numChannels, SinOsc.ar(
					Latch.kr(FluidPitch.kr(in0[1])[0], freeze0 > 0),
					2pi.rand,
					sine1.linlin(0, 1, 0.0, 0.2)),
				Gate.kr(Lag.kr(
					TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq0*panLatch0)),
					TBrownRand.kr(panFreq0.linlin(0.005, 10.0, 5, 0.005),
						panFreq0.linlin(0.005, 10.0, 30, 0.2),
						trig: Dust.kr(panFreq0*panLatch0))), panLatch0),
				1.0, 2.0),
			PanX.ar(GlobalPresets.numChannels, SinOsc.ar(
				Latch.kr(FluidPitch.kr(in0[1])[0], freeze0 > 0)*bend1.linlin(0, 1, 0.5, 1.5),
				2pi.rand,
				sine1.linlin(0, 1, 0.0, 0.2)),
			Gate.kr(Lag.kr(TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq0*panLatch0)),
				TBrownRand.kr(panFreq0.linlin(0.005, 10.0, 5, 0.005),
					panFreq0.linlin(0.005, 10.0, 30, 0.2),
					trig: Dust.kr(panFreq0*panLatch0))), panLatch0),
			1.0, 2.0);
		]
	);

	chain2 = Mix.new(
		[
			(chain2*sine2.linlin(0, 1, 9, 0)),
			PanX.ar(
				GlobalPresets.numChannels, SinOsc.ar(
					Latch.kr(FluidPitch.kr(in1[0])[0], freeze1 < 1),
					2pi.rand,
					sine2.linlin(0, 1, 0.0, 0.2)),
				Gate.kr(Lag.kr(
					TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq1*panLatch1)),
					TBrownRand.kr(panFreq1.linlin(0.005, 10.0, 5, 0.005),
						panFreq1.linlin(0.005, 10.0, 30, 0.2),
						trig: Dust.kr(panFreq1*panLatch1))), panLatch1),
				1.0, 2.0),
			PanX.ar(GlobalPresets.numChannels, SinOsc.ar(
				Latch.kr(FluidPitch.kr(in1[0])[0], freeze1 < 1)*bend2.linlin(0, 1, 0.5, 1.5),
				2pi.rand,
				sine2.linlin(0, 1, 0.0, 0.2)),
			Gate.kr(Lag.kr(TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq1*panLatch1)),
				TBrownRand.kr(panFreq1.linlin(0.005, 10.0, 5, 0.005),
					panFreq1.linlin(0.005, 10.0, 30, 0.2),
					trig: Dust.kr(panFreq1*panLatch1))), panLatch1),
			1.0, 2.0);
		]
	);

	chain3 = Mix.new(
		[
			(chain3*sine3.linlin(0, 1, 9, 0)),
			PanX.ar(
				GlobalPresets.numChannels, SinOsc.ar(
					Latch.kr(FluidPitch.kr(in1[1])[0], freeze1 < 1),
					2pi.rand,
					sine3.linlin(0, 1, 0.0, 0.2)),
				Gate.kr(Lag.kr(
					TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq1*panLatch1)),
					TBrownRand.kr(panFreq1.linlin(0.005, 10.0, 5, 0.005),
						panFreq1.linlin(0.005, 10.0, 30, 0.2),
						trig: Dust.kr(panFreq1*panLatch1))), panLatch1),
				1.0, 2.0),
			PanX.ar(GlobalPresets.numChannels, SinOsc.ar(
				Latch.kr(FluidPitch.kr(in1[1])[0], freeze1 < 1)*bend3.linlin(0, 1, 0.5, 1.5),
				2pi.rand,
				sine3.linlin(0, 1, 0.0, 0.2)),
			Gate.kr(Lag.kr(TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq1*panLatch1)),
				TBrownRand.kr(panFreq1.linlin(0.005, 10.0, 5, 0.005),
					panFreq1.linlin(0.005, 10.0, 30, 0.2),
					trig: Dust.kr(panFreq1*panLatch1))), panLatch1),
			1.0, 2.0);
		]
	);

	sig0 = LeakDC.ar(LinSelectX.ar(morph0, [chain0, chain1])); //normalize signal as a form of compression make-up gain
	sig1 = LeakDC.ar(LinSelectX.ar(morph1, [chain2, chain3]));

	in0 = PanX.ar(
		GlobalPresets.numChannels, in0[0],
		Gate.kr(Lag.kr(
			TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq0*panLatch0)),
			TBrownRand.kr(panFreq0.linlin(0.005, 10.0, 5, 0.005),
				panFreq0.linlin(0.005, 10.0, 30, 0.2),
				trig: Dust.kr(panFreq0*panLatch0))), panLatch0),
		1.0, 2.0)+
	PanX.ar(
		GlobalPresets.numChannels, in0[1],
		Gate.kr(Lag.kr(
			TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq0*panLatch0)),
			TBrownRand.kr(panFreq0.linlin(0.005, 10.0, 5, 0.005),
				panFreq0.linlin(0.005, 10.0, 30, 0.2),
				trig: Dust.kr(panFreq0*panLatch0))), panLatch0),
		1.0, 2.0);

	in1 = PanX.ar(
		GlobalPresets.numChannels, in1[0],
		Gate.kr(Lag.kr(
			TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq1*panLatch1)),
			TBrownRand.kr(panFreq1.linlin(0.005, 10.0, 5, 0.005),
				panFreq1.linlin(0.005, 10.0, 30, 0.2),
				trig: Dust.kr(panFreq1*panLatch1))),
		panLatch1), 1.0, 2.0)+
	PanX.ar(GlobalPresets.numChannels, in1[1],
		Gate.kr(Lag.kr(
			TBrownRand.kr(0, 1, 1, 0, Dust.kr(panFreq1*panLatch1)),
			TBrownRand.kr(panFreq1.linlin(0.005, 10.0, 5, 0.005),
				panFreq1.linlin(0.005, 10.0, 30, 0.2),
				trig: Dust.kr(panFreq1*panLatch1))),
		panLatch1), 1.0, 2.0);

	sig0 = ((sig0 * mix0) + (in0*mix0.linlin(0, 1, 1, 0))) * amp0.lag(0.05);
	sig0 = LinSelectX.ar((lpf0>9999).lag(0.05), [LPF.ar(sig0, lpf0), sig0]);
	sig0 = LinSelectX.ar((hpf0<41).lag(0.05), [HPF.ar(sig0, hpf0), sig0]);

	sig1 = ((sig1 * mix1) + (in1*mix1.linlin(0, 1, 1, 0))) * amp1.lag(0.05);

	sig1 = LinSelectX.ar((lpf1>9999).lag(0.05), [LPF.ar(sig1, lpf1), sig1]);
	sig1 = LinSelectX.ar((hpf1<41).lag(0.05), [HPF.ar(sig1, hpf1), sig1]);

	envSig0 = EnvGen.kr(Env.asr(env0.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), 1, env0.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), [4, -4]), play0, doneAction: 0);
	envSig1 = EnvGen.kr(Env.asr(env1.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), 1, env1.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), [4, -4]), play1, doneAction: 0);

	env = EnvGen.kr(Env.asr(env0.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), 1, env0.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), [4, -4]), gate, doneAction: 2);

	sig = (((sig0*envSig0)+(sig1*envSig1))*env);
	busSig = (((sig0*envSig0)+(sig1*envSig1))*env);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, busSig*busAmp.lag(0.05));
}).add;