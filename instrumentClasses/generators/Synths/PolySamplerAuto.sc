PolySamplerAuto {
	*ar {
		arg numChans, bufnum, rate, startPos, loop, trig;
		var sig;
		sig = PlayBuf.ar(
			numChans,
			Latch.kr(in: bufnum, trig: trig),
			Latch.kr(in: rate, trig: trig), trig,
			Latch.kr(in: startPos, trig: trig),
			Latch.kr(in: loop, trig: trig),
			0);

		^sig;
	}
}
