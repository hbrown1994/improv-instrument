(
SynthDef(\onsetTrigStep, {
	arg loopBuf, loopStart, loopEnd, deltas, bus, stepperHold=0, stepperFreeze=0, rel, gate, loopChange, masterBuf;
	var loopTrig, trig, loopStepper, loopStepperReset, indexStepper, readBuf, phasor, loopRange, bufFrames;

	loopRange = Control.names([\loopRange]).kr([0, 1]);

	bufFrames = BufFrames.kr(loopBuf);

	loopStart = loopRange[0].linlin(0, 1, 0, BufFrames.kr(masterBuf)).round;
	loopEnd = loopRange[1].linlin(0, 1, 0, BufFrames.kr(masterBuf)).round;

	loopStepperReset = LocalIn.ar(1);
	loopTrig =  BinaryOpUGen('==', loopStepperReset, loopEnd-1)+Changed.kr(loopStart)+Changed.kr(loopEnd)+Changed.kr(loopChange);

	trig = K2A.ar(ListTrig2.kr(loopBuf, loopTrig)); //Get Bounds

	loopStepper = Stepper.ar(trig, loopTrig, loopStart-1, loopEnd, 1,loopStart-1);
	EnvGen.kr(Env.asr(0.005, 1, rel.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180)), gate, doneAction: 2);
	LocalOut.ar(loopStepper);

	Out.ar(bus, [trig, loopStepper]);
}).add;
);