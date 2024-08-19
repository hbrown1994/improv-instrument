(
SynthDef(\sand, {
	arg busMono, inBus, gate, env, x=0, y=0, pres=0, amp, busAmp, bus, out, thresh, state, ampGate, pauseGate=1, lowRand=0.0001, hiRand=0.01;
	var inA, inB, inC, chainA1, chainA2, chainB, chain1, chain2, chain3, chain4, out1, out2, sig, inA1, inA2, ampTrack;

	inB = In.ar(inBus, GlobalPresets.numChannels);
	inB = Mix.new(inB);

	ampTrack = LagUD.kr(Amplitude.kr(inB), 0.01, LFNoise2.kr(0.5, 1, 1.75));

	#inA1, inA2 = Dust.ar(Select.kr(state, [[x.linlin(0, 127, 0, 300), y.linlin(0, 127, 0, 300)]*ampTrack, [3000,3000]*ampTrack]), 0.5);

	inC = Dust.ar(Select.kr(state, [[x.linlin(0, 127, 0, 300), y.linlin(0, 127, 0, 300)]*ampTrack, [300,300]*ampTrack]), 0.5)*ampTrack;

	inC = BPF.ar(inC, LFNoise2.kr(0.3, 1000, 8000), 0.2).dup.dup;

	inB = AllpassC.ar(inB, 0.5, LFNoise2.kr(1, 0.05, 0.1), 1.0)+AllpassC.ar(inB, 0.5, LFNoise2.kr(1, 0.05, 0.1), 1.0);

	chainA1 = FFT(LocalBuf(2048), inA1);
	chainA2 = FFT(LocalBuf(2048), inA2);
	chainB = FFT(LocalBuf(2048), inB);

	chain1 = PV_MagMul(chainA1, chainB);
	chain2 = PV_MagMul(chainA2, chainB);

	chain3 = PV_Copy(chain1, LocalBuf(2048));
	chain4 = PV_Copy(chain2, LocalBuf(2048));

	chain1 = PV_BrickWall(chain1, 0.05);
	chain2 = PV_BrickWall(chain2, 0.05);

	chain3 = PV_MagSmear(PV_BrickWall(chain3, -0.05), 10);
	chain4 = PV_MagSmear(PV_BrickWall(chain4, -0.05), 10);

	out1 = 0.5 * [IFFT(chain1), IFFT(chain2)];

	out2 = 0.5 * [IFFT(chain3), IFFT(chain4)];

	sig = out1+DelayC.ar(out1, 0.05, LFNoise2.kr(1).range(0.5, 0.8))+DelayC.ar(out2, 0.05, LFNoise2.kr(1).range(0.5, 0.8));

	sig = LPF.ar((sig+(inC*0.5)), 18000);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = PanX.ar(GlobalPresets.numChannels, LeakDC.ar(sig), LFNoise0.kr(ampTrack.linlin(0, 0.2, 0.01, 20)).range(0, 1));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)])*0.2;

	sig = sig * Select.kr(state, [1, pres.linexp(0, 127, 0.000001, 1.0)]);

	Out.ar(out, sig*amp.lag(0.005));
	Out.ar(bus, sig*busAmp.lag(0.005));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);


GlobalSynths.processors.add(\sand);
