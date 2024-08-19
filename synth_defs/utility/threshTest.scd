SynthDef(\threshTest, {
	arg inBus, out, thresh;
	var in, sig, onsets;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	onsets = Onsets.kr(FFT(LocalBuf(512), HPF.ar(in, 300.0)), thresh, \rcomplex, 2, 0.3);

	sig = WhiteNoise.ar(0.2) * Trig.kr(onsets, 0.05);

	Out.ar(out, sig!2);

}).add;