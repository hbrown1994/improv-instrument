(
SynthDef(\iceSynth, {
	arg iceX, iceAmp, iceAmpGate, out, iceGate, iceFreeze, iceTrig;
	var sig, dust, outforce, ogSig;

	sig = {Dust.ar(iceX.linlin(0, 1, 0, 3)+iceTrig, 20)}!8;
	sig = Gate.ar(sig,  iceFreeze+iceTrig);

	sig = BMoog.ar(sig, {TRand.ar(3000, 17000, sig).lag(TExpRand.ar(0.0001, 5, sig))}, {TRand.ar(0.9, 0.999, sig)}, 3, {TRand.ar(0.95, 0.999, sig)});
	sig = Fold.ar(sig, -1.0, 1.0);
	ogSig = Limiter.ar(sig, 0.7)*0.9;

	sig = SplayAz.ar(GlobalPresets.numChannels, sig);

	EnvGen.kr(Env.asr(0.005, 1, 0.005, [4, -4]), iceGate, doneAction: 2);

	sig = sig * iceAmp.lag(0.005);

	sig = LPF.ar(sig, 8000);

	Out.ar(out, sig);
}).add;
);
