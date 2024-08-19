(
SynthDef(\onsetLoopStereo, {
	arg fbSel, offsetsBuf0, offsetsBuf1, offsetsBuf2, offsetsBuf3, offsetsBuf4, offsetsBuf5, offsetsBuf6, offsetsBuf7, offsetsBuf8, offsetsBuf9, onsetsBuf0, onsetsBuf1, onsetsBuf2, onsetsBuf3, onsetsBuf4, onsetsBuf5, onsetsBuf6, onsetsBuf7, onsetsBuf8, onsetsBuf9, buf0, buf1, buf2, buf3, buf4, buf5, buf6, buf7, buf8, buf9, sourceSelect, markovOnOff, busAmp, syncBus, bus, randScaleEndSamp, randScale, rand, out, reverse=0, onsetThresh=0.5, panTrigSel=0, freeze=0, cut, rate, size, amp, sliceLoop, loopHold, hardLoop, atk=0.005, rel=0.005, gate=1;
	var selector, sourceMarkov, busSig, reverseSig, randTrig, longPhasor, panTrig, env, clock, phasor, source, noLoop, startSamp, endSamp, loop, trig, sig, indexStepper, onsetsBuf, offsetsBuf, buf, markovs;

	/*____Onset Trigge From Buffer__________________________________________________*/
	busSig = Select.ar(fbSel, [In.ar(syncBus, 2), InFeedback.ar(syncBus, 2)]);

	trig = busSig[0]; //Get Trigger
    indexStepper = busSig[1]; //Get Buffer Index

	randTrig = TIRand.kr(0, 1, trig); //coin flip on trig

	markovs = Control.names([\markovs]).kr([1, 1, 1, 1, 1/*, 1, 1, 1, 1, 1*/]);

	sourceMarkov = TWindex.kr(trig, markovs, 1); //Set Markov weights
	selector = Select.kr(markovOnOff, [sourceSelect, sourceMarkov]); //Markov or Manual Select

	//select audio and audio file bounds
	buf = Select.kr(selector, [buf0, buf1, buf2, buf3, buf4, buf5, buf6, buf7, buf8, buf9]);
	onsetsBuf = Select.kr(selector, [onsetsBuf0, onsetsBuf1, onsetsBuf2, onsetsBuf3, onsetsBuf4, onsetsBuf5, onsetsBuf6, onsetsBuf7, onsetsBuf8, onsetsBuf9]);
	offsetsBuf = Select.kr(selector, [offsetsBuf0, offsetsBuf1, offsetsBuf2, offsetsBuf3, offsetsBuf4, offsetsBuf5, offsetsBuf6, offsetsBuf7, offsetsBuf8, offsetsBuf9]);

	buf = Gate.kr(buf, loopHold.linlin(0, 1, 1, 0));
	onsetsBuf = Gate.kr(onsetsBuf, loopHold.linlin(0, 1, 1, 0));
	offsetsBuf = Gate.kr(offsetsBuf, loopHold.linlin(0, 1, 1, 0));

	buf = Gate.kr(buf, freeze.linlin(0, 1, 1, 0));
	onsetsBuf = Gate.kr(onsetsBuf, freeze.linlin(0, 1, 1, 0));
	offsetsBuf = Gate.kr(offsetsBuf, freeze.linlin(0, 1, 1, 0));

	//select Lemur control or stochastics
	loopHold = Select.kr(rand, [loopHold, randTrig]);
	sliceLoop = Select.kr(rand, [sliceLoop, randTrig]);
	reverseSig = Select.ar(rand, [K2A.ar(reverse), TIRand.ar(0, 1, trig)]);

	/*____Get Audio Bounds From Buffer________________________________________________*/
	indexStepper = Gate.ar(indexStepper, loopHold.linlin(0, 1, 1, 0));
	indexStepper = Gate.ar(indexStepper, freeze.linlin(0, 1, 1, 0));

	startSamp = Index.ar(onsetsBuf, indexStepper);
	endSamp = Index.ar(offsetsBuf, indexStepper);

	/*____Loop Phasor thru Audio Buffer to retrieve current sample index for "hardloop"__________________________________________*/
	longPhasor = Phasor.ar(1, 1, 0, BufFrames.kr(buf));
	longPhasor = Gate.ar(longPhasor, (hardLoop+rand).clip(0,1).linlin(0, 1, 1, 0));

	startSamp = Select.ar(hardLoop, [startSamp, longPhasor]);
	endSamp = Select.ar(hardLoop, [size.linlin(0, 1, endSamp, startSamp+256), size.linlin(0, 1, startSamp+(SampleRate.ir*2), startSamp+64)]);
	endSamp = Select.ar(rand, [endSamp, TRand.ar(0.0, 1.0, Select.kr(freeze, [trig, 0])).linlin(0.0, 1.0, randScaleEndSamp.linlin(0.0, 1.0, endSamp, startSamp+256), endSamp)]);

	rate = Select.kr(BinaryOpUGen('==', 0.5, rate), [Select.kr(rate<0.5, [rate.linlin(0.5, 1, 1, 5), rate.linlin(0, 0.5, 0.01, 1)]), 1]);

	rate = Select.kr(rand, [rate, TRand.kr(randScale.linlin(0.0, 1.0, 1.0, 0.1), randScale.linlin(0.0, 1.0, 1.0, 5.0), Select.kr(freeze, [trig, 0])).lag(0.05)]);
	rate = rate * Select.kr(reverseSig, [1, -1]);

	clock = Impulse.ar(1/((endSamp-startSamp)/SampleRate.ir));

	phasor = Phasor.ar(1, rate, Select.ar(reverseSig, [startSamp, endSamp]), Select.ar(reverseSig, [endSamp, startSamp]), Select.ar(reverseSig, [startSamp, endSamp]));
	loop = BufRd.ar(2, buf, phasor, 0);
	loop = loop * EnvGen.ar(Env.linen(0.005, ((endSamp-startSamp)/SampleRate.ir)-0.01, 0.005), clock);

	/*____Playback each sample without loop___________________________________________*/
	noLoop = PlayBuf.ar(2, buf, rate, trig, Select.kr(reverseSig, [startSamp, endSamp]), 0)*EnvGen.kr(Env([0, 1, 1, 0], [0.005, (endSamp-startSamp)/SampleRate.ir, 0.005]), trig, doneAction: 0);

	/*____Loop On/Off _______________________________________________________÷_________*/
	source = LinSelectX.ar((sliceLoop+loopHold+freeze).clip(0, 1), [noLoop, loop]);

	/*____Env ________________________________________________________________________*/
	env = EnvGen.kr(Env.asr(atk.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), 1, rel.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), [4, -4]), gate, doneAction: 2);

	/*____Pans _______________________________________________________________________*/
	panTrig = Select.kr(panTrigSel,
		[
			1/((GlobalPresets.numChannels-1)*2),
			TBrownRand.kr(-1.0, 1.0, 1, 0, Gate.kr(trig, (loopHold+randTrig).clip(0, 1).linlin(0, 1, 1, 0))),
			TBrownRand.kr(-1.0, 1.0, 1, 0, FluidOnsetSlice.ar(HPF.ar(source[0]+source[1], 70), 0, onsetThresh))
		]
	);

	sig =  PanX.ar(GlobalPresets.numChannels, source[0]+source[1], panTrig, 1.0, 2.0);

	/*____Mults ______________________________________________________________________*/
	Out.ar(out, (sig*amp*(cut.linlin(0, 1, 1, 0))*env));
	Out.ar(bus, (sig*(cut.linlin(0, 1, 1, 0))*env*busAmp));
}).add;
);


