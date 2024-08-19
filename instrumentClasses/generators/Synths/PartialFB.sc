PartialFB {

	*ar {arg freq=440, dur, amp, relDur, relFreq, detune, trig, mul, fb, curve;
		 var sig;

		sig = SinOscFB.ar((freq*relFreq)+detune, fb);
		sig = sig * EnvGen.kr(Env.new(levels: [ 0, (amp*mul), 0 ], times: [ 0.005, dur*relDur ], curve: curve), trig);
		^sig;
	}
}






