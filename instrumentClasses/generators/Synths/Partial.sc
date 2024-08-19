Partial {

	*ar {arg freq=440, dur, amp, relDur, relFreq, detune, trig, mul;
		 var sig;

		sig = SinOsc.ar((freq*relFreq)+detune, {Rand(0.0, 2pi)});
		sig = sig * EnvGen.kr(Env.new(levels: [ 0, (amp*mul), 0 ], times: [ 0.005, dur*relDur ], curve: 'cub'), trig);
		^sig;
	}
}





