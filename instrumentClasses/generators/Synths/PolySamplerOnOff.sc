PolySamplerOnOff {
	*ar {
		arg numChans, bufnum, rate, startPos, loop, atk, dur, rel, mul, trig, doneAction=0, off;
		var sig;
		sig = PlayBuf.ar(
			numChans,
			Latch.kr(in: bufnum, trig: trig),
			Latch.kr(in: rate, trig: trig), trig,
			Latch.kr(in: startPos, trig: trig),
			Latch.kr(in: loop, trig: trig),
			doneAction);
		sig = sig * Linen.kr(SetResetFF.kr(Trig.kr(trig, dur), off), atk, mul, rel, doneAction);

		^sig;
	}
}
÷



