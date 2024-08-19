StretchShiftFFT {

	*ar {arg input, amp = 0, durMult, playSpeed, numChannels;
		var trigPeriod, sig, chain, trig, pos, fftSize, jump, in, bufnum, sigFinal;

		in = input;

		bufnum = LocalBuf(SampleRate.ir*4, numChannels);
		RecordBuf.ar(in, bufnum, loop: 0);

		fftSize = 8192;

		trigPeriod = fftSize/SampleRate.ir;
		trig = Impulse.ar(1/trigPeriod);
		pos = Demand.ar(trig, 0, demandUGens: Dseries(0, trigPeriod/durMult))*SampleRate.ir;
		jump = fftSize/4/durMult;

		sig = [
			PlayBuf.ar(numChannels, bufnum, playSpeed, trig, (pos), 1),
			PlayBuf.ar(numChannels, bufnum, playSpeed, trig, (pos+jump), 1),
			PlayBuf.ar(numChannels, bufnum, playSpeed, trig, (pos+(2*jump)), 1),
			PlayBuf.ar(numChannels, bufnum, playSpeed, trig, (pos+(3*jump)), 1),
		];

		sig = sig.collect({ |item, i| //collect just applies this process to each playbuf
			chain = FFT(LocalBuf(fftSize, numChannels), item, hop: 1.0, wintype: 1);
			//chain = PV_Diffuser(chain, 1 - trig);
			item = IFFT(chain, wintype: 1);
		});

		sig[1] = DelayC.ar(sig[1], trigPeriod, trigPeriod/4);
		sig[2] = DelayC.ar(sig[2], trigPeriod, trigPeriod/2);
		sig[3] = DelayC.ar(sig[3], trigPeriod, 3*trigPeriod/4);

		sigFinal = Mix(sig) * amp.dbamp;

		sigFinal = sigFinal * Select.kr(Sweep.kr(Impulse.kr(0)) > 1, [0, 1]).lag(0.05);


		^sigFinal;
	}
}





