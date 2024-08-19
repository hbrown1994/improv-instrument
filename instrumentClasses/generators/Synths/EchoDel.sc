EchoDel {
	*ar {
		arg in1, in2, delTime;
		var sig, delSig;

		sig = in1 + in2;
		delSig = in1 - in2;
		delSig = DelayN.ar(delSig, delTime, delTime);


		^[sig, delSig];
	}
}

/*
This network makes two copies of the (stereo) input, one in phase, the other out of phase and delayed. The total frequency response is flat. The total signal power out is exactly twice that of the input, no matter what freqiencies the input contains. This is used to increase echo density, by stacking several of these units with different delay times. Each stage doubles the echo density.
*/