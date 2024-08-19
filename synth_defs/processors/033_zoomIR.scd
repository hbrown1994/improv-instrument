(
// also 4096 works on my machine; 1024 too often and amortisation too pushed, 8192 more high load FFT
{
	var irBufs = Array.newClear(6), fftsize = 2048;
	var regressor = FluidMLPRegressor.new(Server.local, hiddenLayers: [ 5, 5, 5, 5], activation: 1, outputActivation: 1, maxIter: 1000, learnRate: 0.01, momentum: 0, batchSize: 1, validation: 0).read("/Users/hb/Sync/1_instrument/data/midiXYdata/regressor.json");

	(GlobalData.audioBufferNames[\zoomIR].size+1).do{|i|
		if(i<(GlobalData.audioBufferNames[\zoomIR].size), {
			{
				var ir, irbuffer;

				(GlobalPaths.buffers++"zoomIR"+/+GlobalData.audioBufferNames[\zoomIR][i].asString++".wav");

				irbuffer = Buffer.readChannel(s, (GlobalPaths.buffers++"zoomIR"+/+GlobalData.audioBufferNames[\zoomIR][i].asString++".wav"), channels: [0]);
				s.sync;

				irBufs[i] = Buffer.alloc(s, PartConv.calcBufSize(fftsize, irbuffer), 1);
				irBufs[i].preparePartConv(irbuffer, fftsize);

				s.sync;

				irbuffer.free; // don't need time domain data anymore, just needed spectral version
			}.value;
		}, {

			SynthDef(\zoomIR, {
				arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, thresh;
				var in, sig, sig0, sig1, sig2, sig3, sig4, sig5, irSel, onsets, stereo0, stereo1, stereo2, stereo3, stereo4, sigs, randSel;

				in = In.ar(inBus, GlobalPresets.numChannels);
				in = Mix.new(in) * ampGate.linlin(0, 1, 1, 0).lag(0.05);

				thresh = Clip.kr(thresh, 0.7, 2.0);
				onsets = Onsets.kr(FFT(LocalBuf(512), HPF.ar(in, 150.0)), thresh, \rcomplex, 2, 0.3, 50)+Impulse.kr(0);

				irSel = Select.kr(state, [x.linlin(0, 127, 0, 3), 4]);

				in = HPF.ar(in, 120);

				sig0= PartConv.ar(in, fftsize, irBufs[0].bufnum, 0.1);
				sig1= PartConv.ar(in, fftsize, irBufs[1].bufnum, 0.1);
				sig2= PartConv.ar(in, fftsize, irBufs[2].bufnum, 0.1);
				sig3= PartConv.ar(in, fftsize, irBufs[3].bufnum, 0.1);
				sig4= PartConv.ar(in, fftsize, irBufs[4].bufnum, 0.1);
				sig5= PartConv.ar(in, fftsize, irBufs[5].bufnum, 0.1);

				sigs = [sig0, sig1, sig2, sig3, sig4, sig5];

				stereo0 = [sig0, sig1];
				stereo1 = [sig2, sig3];
				stereo2 = [sig4, sig5];
				stereo3 = [sig1, sig4];

				randSel = {Select.kr(state, [5.rand, TIRand.kr(0, 5, onsets)])}!2;
				stereo4 = [Select.ar(randSel[0].lag(0.05), sigs), Select.ar(randSel[1].lag(0.05), sigs)];

				sig = SelectX.ar(irSel, [stereo0, stereo1, stereo2, stereo3, stereo4]);

				sig = sig * EnvGen.kr(Env.asr(0, 1, 0), gate, doneAction: 2);
				sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
				PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

				Out.ar(out, sig*amp.lag(0.05));
				Out.ar(bus, sig*busAmp.lag(0.05));
				Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
			}).add;
		});
	};
}.fork;
);

GlobalSynths.processors.add(\zoomIR);