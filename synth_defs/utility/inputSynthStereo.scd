SynthDef(\inputSynthStereo, {
	arg monoBus, inBusLeft, inBusRight, bus, out, amp=0, busAmp=0, gate=1;
	var sigLeft, sigRight;

	sigLeft = SoundIn.ar(inBusLeft);
	sigRight = SoundIn.ar(inBusRight);

	EnvGen.kr(Env.asr(0, 1, 0), gate, doneAction: 2);

	Out.ar(monoBus, (Mix.new([sigLeft, sigRight])*busAmp));
	Out.ar(bus, ([sigLeft, sigRight]*busAmp));
	Out.ar(out, ([sigLeft, sigRight]*amp));
}).add;

