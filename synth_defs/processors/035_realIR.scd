(
// also 4096 works on my machine; 1024 too often and amortisation too pushed, 8192 more high load FFT
{
	var irBufsL = Array.newClear(8), irBufsR = Array.newClear(8), fftsize = 2048;

	(GlobalData.audioBufferNames[\realIR].size+1).do{|i|
		if(i<(GlobalData.audioBufferNames[\realIR].size), {
			{
				var ir, irbufferL, irbufferR;

				(GlobalPaths.buffers++"realIR"+/+GlobalData.audioBufferNames[\realIR][i].asString++".wav");

				irbufferL = Buffer.readChannel(s, (GlobalPaths.buffers++"realIR"+/+GlobalData.audioBufferNames[\realIR][i].asString++".wav"), channels: [0]);
				irbufferR = Buffer.readChannel(s, (GlobalPaths.buffers++"realIR"+/+GlobalData.audioBufferNames[\realIR][i].asString++".wav"), channels: [1]);

				s.sync;

				irBufsL[i] = Buffer.alloc(s, PartConv.calcBufSize(fftsize, irbufferL), 1);
				irBufsL[i].preparePartConv(irbufferL, fftsize);

				irBufsR[i] = Buffer.alloc(s, PartConv.calcBufSize(fftsize, irbufferR), 1);
				irBufsR[i].preparePartConv(irbufferR, fftsize);

				s.sync;

				irbufferL.free; // don't need time domain data anymore, just needed spectral version
				irbufferR.free; // don't need time domain data anymore, just needed spectral version
			}.value;
		}, {

			SynthDef(\realIR, {
				arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, thresh;
				var in, sig, sig0, sig1, sig2, sig3, sig4, sig5, sig6, sig7, irSel, onsets, sigs;

				in = In.ar(inBus, GlobalPresets.numChannels);
				in = Mix.new(in) * ampGate.linlin(0, 1, 1, 0).lag(0.05);

				thresh = Clip.kr(thresh, 0.7, 2.0);
				onsets = Onsets.kr(FFT(LocalBuf(512), HPF.ar(in, 150.0)), thresh, \rcomplex, 2, 0.3, 50)+Impulse.kr(0);

				irSel = Select.kr(state, [x.linlin(0, 127, 0, 7), TRand.kr(0, 7, onsets)]);

				in = HPF.ar(in, 120);

				sig0= [PartConv.ar(in, fftsize, irBufsL[0].bufnum, 0.1), PartConv.ar(in, fftsize, irBufsR[0].bufnum, 0.1)];
				sig1= [PartConv.ar(in, fftsize, irBufsL[1].bufnum, 0.1), PartConv.ar(in, fftsize, irBufsR[1].bufnum, 0.1)];
				sig2= [PartConv.ar(in, fftsize, irBufsL[2].bufnum, 0.1), PartConv.ar(in, fftsize, irBufsR[2].bufnum, 0.1)];
				sig3= [PartConv.ar(in, fftsize, irBufsL[3].bufnum, 0.1), PartConv.ar(in, fftsize, irBufsR[3].bufnum, 0.1)];
				sig4= [PartConv.ar(in, fftsize, irBufsL[4].bufnum, 0.1), PartConv.ar(in, fftsize, irBufsR[4].bufnum, 0.1)];
				sig5= [PartConv.ar(in, fftsize, irBufsL[5].bufnum, 0.1), PartConv.ar(in, fftsize, irBufsR[5].bufnum, 0.1)];
				sig6= [PartConv.ar(in, fftsize, irBufsL[6].bufnum, 0.1), PartConv.ar(in, fftsize, irBufsR[6].bufnum, 0.1)];
				sig7= [PartConv.ar(in, fftsize, irBufsL[7].bufnum, 0.1), PartConv.ar(in, fftsize, irBufsR[7].bufnum, 0.1)];

				sigs = [sig0, sig1, sig2, sig3, sig4, sig5, sig6, sig7];

				sig = SelectX.ar(irSel, sigs);

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

GlobalSynths.processors.add(\realIR);