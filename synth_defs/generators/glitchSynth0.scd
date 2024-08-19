SynthDef(\glitchSynth0, {
	arg fbMult=1500, lpHPSelect=0, xFreezeShift, yFreezeShift, bitDepth, prob, res, cut, glitch0Gate, glitch0Amp, atk=0, out, freezeGate, glitch0Cut=1;
	var sig, fb, noiseFreq, noise, fbFilterFreq, lastFilterFreq, lastRq, env;

	xFreezeShift = xFreezeShift.linlin(0.0, 1.0, 0.0, 100.0);
	yFreezeShift = yFreezeShift.linlin(0.0, 1.0, 0.0, 100.0);

	fbMult = fbMult.linlin(0.0, 1.0, 0.0, 2400.0);
	res = res.linlin(0.0, 1.0, 0.0, 100.0);

	noiseFreq = Gate.kr(LFNoise1.kr(1).range(500, 12000)!GlobalPresets.numChannels, freezeGate);
	noise = LFNoise0.ar(Gate.kr(LFNoise1.kr(1).range(500, 12000)!GlobalPresets.numChannels, freezeGate)*yFreezeShift);

	fb = LocalIn.ar(2);

	fbFilterFreq = Gate.kr((1000+(fb*fbMult)), freezeGate)*xFreezeShift;

	fb = MoogVCF.ar(noise, fbFilterFreq, res);
	LocalOut.ar(fb);

	lastFilterFreq = Gate.kr(LFNoise0.kr(LFNoise0.kr(0.5).range(0.5,100)).exprange(10,
		LFNoise0.kr(LFNoise0.kr(1).range(0.5,30)).range(40, 20000)), freezeGate)*yFreezeShift;

	lastRq = Gate.kr(LFNoise0.kr(10).range(0.5,50), freezeGate)*xFreezeShift;

	env=EnvGen.kr(Env.asr(atk.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), 1, atk.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), [4, -4]), glitch0Gate, doneAction: 2);


	//freq of last RLPF - no control at all
	sig = MoogVCF.ar(fb, lastFilterFreq, lastRq);
	sig = Compander.ar(sig, sig).clip(-0.5,0.5)*0.3;

	Out.ar(out, LeakDC.ar(sig*glitch0Amp*env*glitch0Cut));
}).add;

