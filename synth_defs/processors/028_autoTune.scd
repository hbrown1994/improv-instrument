(
SynthDef(\autoTune, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, thresh;
	var in, sig, baseFreq = 100, freq, hasFreq, cScale, scale, vocoder, ampSig;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	# freq, hasFreq = Pitch.kr(in, ampThreshold: 0.02, median: 7);
	baseFreq = freq.cpsmidi.round.midicps;

	cScale = DegreeToKey.kr(LocalBuf.newFrom([0,0,2,2,2,2,4,4,5,5,5,5,7,7,7,7,9,9,9,9,11,11,12,12]), freq.cpsmidi*2, 12, 1);

	baseFreq = Select.kr(state, [baseFreq, cScale.midicps]);

	ampSig = Amplitude.kr(in,0.01,0.1);

	sig = LFTri.ar([baseFreq, baseFreq], 0, Lag.kr(ampSig, 0.3));
	ampSig = Lag.kr((ampSig>thresh),0.1);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));

}).add;
);

GlobalSynths.processors.add(\autoTune);
