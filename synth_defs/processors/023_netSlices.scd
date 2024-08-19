(
Routine{
	var buf, netBounds, onsets=List.new, offsets=List.new, bounds, onsetBuf, offsetBuf;

	bounds = FluidJSON.new(GlobalPaths.datasets++"internet-1_bounds.json").asDict;
	buf = GlobalData.audioBuffers[\internet][0];

	s.sync;

	(
		bounds[\data].keys.do{|i|
			onsets.add(bounds[\data][i][0]);
			offsets.add(bounds[\data][i][1]);
		};
	);

	s.sync;

	onsetBuf = Buffer.loadCollection(s, onsets.sort);
	offsetBuf = Buffer.loadCollection(s, offsets.sort);

	s.sync;

	SynthDef(\netSlices, {
		arg busMono, amp=0.8, repeats, dust, freeze=1, ampGate=0, out, env, gate, busAmp, bus, inBus, pauseGate, thresh=0.7, pres=0, x=0, y=0, state;
		var in, onsets, sig, trig, rate, index, sliceOnset,  sliceOffset, phasor, envLength;

		in = In.ar(inBus, GlobalPresets.numChannels);
		in = Mix.new(in)  * state.linlin(0, 1, 1, 0).lag(0.05);
		thresh = Clip.kr(thresh, 0.6, 2.0);

		onsets = Onsets.kr(FFT(LocalBuf(512), HPF.ar(in, 100.0)), thresh, \rcomplex, 2, 0.3, 20);

		trig  =  onsets;
		index = TIRand.kr(0, BufFrames.kr(onsetBuf),  trig);
		sliceOnset = Index.kr(onsetBuf, index);
		sliceOffset = Index.kr(offsetBuf, index);
		phasor = Phasor.ar(trig, BufRateScale.kr(buf)*y.linlin(0, 127, 1, 5), sliceOnset, pres.linexp(0, 127, sliceOffset, sliceOnset+1024), sliceOnset);
		sig = BufRd.ar(1, buf, phasor);



		envLength = Select.kr(TWindex.kr(onsets, [x.linlin(0, 127, 1.0, 0.0), x.linlin(0, 127, 0.0, 1.0)]),

			[
				((sliceOffset-sliceOnset)/SampleRate.ir)-0.01,
				(((sliceOffset-sliceOnset)/SampleRate.ir)-0.01)*TIRand.kr(x.linlin(0, 127, 2, 10), x.linlin(0, 127, 2, 40), onsets)
			]
		);

		sig = sig * Select.kr(state, [
			Clip.kr((EnvGen.kr(Env.linen(0.005, envLength, 0.005), trig)+(pres>1).lag(0.005))),
			1
		]);

		sig = PanX.ar(GlobalPresets.numChannels, sig, Gate.kr(TRand.kr(0, 1, Onsets.kr(FFT(LocalBuf(512), sig), 0.5)), state.linlin(0, 1, 1, 0)));

		env = env.linlin(0, 1, 0.005, 180);
		sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
		sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
		PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

		sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

		Out.ar(out, sig*amp.lag(0.05));
		Out.ar(bus, sig*busAmp.lag(0.05));
		Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
	}).add;

}.play;
);


GlobalSynths.processors.add(\netSlices);

