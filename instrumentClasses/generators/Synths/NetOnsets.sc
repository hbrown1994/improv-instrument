NetOnsets {
	*ar {arg in, trig, bufLength=2, freqLow=1, freqHigh=1, silenceLow=0.3, silenceHigh=0.7, noAlias, alias, lagMax=0.5, decayMax=1.5, grainW=0.2, combW=0.5, waveW=0.3, noLoop=0.7, loop=0.3, noLag=0.3, lag=0.7, mul=1, numChannels=1;
		var sig, phasor, rate, windowSize, windowSizeSel, comb, sigFinal, wave, buffer;

		buffer = LocalBuf(SampleRate.ir*bufLength, numChannels);
		RecordBuf.ar(in, buffer, loop: 0, trigger: trig+Impulse.kr(0));

		windowSize = TRand.kr(0.2, 100.0, trig);
		windowSize = windowSize*(SampleRate.ir/100);

		rate = TBrownRand.kr(0.5, Select.kr(TWindex.kr(trig, [noAlias, alias]), [7.0, 30.0]), 1.0, 1, trig); //warp: 1 -> exponential mapping

		phasor = Phasor.ar(trig: trig, rate: rate, start: 0, end: windowSize, resetPos: 0);

		sig = BufRd.ar(numChannels, buffer, phasor, loop: Select.kr(TWindex.kr(trig, [noLoop, loop]), [0, 1]));

		sig = sig * Select.kr(TWindex.kr(trig, [silenceLow, silenceHigh]), [1, 0]).lag(0.005);

		comb = CombL.ar(sig, 10.0, TRand.kr(0.01, 0.1, trig).lag(Select.kr(TWindex.kr(trig, [noLag, lag]), [0, TRand.kr(0.05, lagMax, trig)])),TRand.kr(0.2, decayMax, trig).lag(0.01));

		wave = WaveLoss.ar(LinSelectX.ar(TWindex.kr(trig, [0.4, 0.6]), [sig, comb]), TExpRand.kr(35.0, 15.0, trig), 40.0);

		sigFinal = LinSelectX.ar(TWindex.kr(trig, [grainW, combW,waveW]), [sig, comb, wave]);

		sigFinal = sigFinal * mul;

		^sigFinal;
	}
}



