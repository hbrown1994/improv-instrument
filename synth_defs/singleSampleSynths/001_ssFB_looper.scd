(
SynthDef(\ssFB_looper, {
	arg bus, size, rate, gate=0, amp, out, cut;
	var in, sig, writePhasor, buffer, bufferSize=SampleRate.ir*1, phasor, onsets;

	in = In.ar(bus, 2);

	buffer = LocalBuf(bufferSize, 2);

	onsets = Onsets.kr(FFT(LocalBuf(512, 2), in), 2);
	onsets = Gate.kr(onsets, cut.linlin(0, 1, 1, 0));

	RecordBuf.ar(in, buffer, 0, 1, 0, 1, 0, onsets);

	rate = rate.linexp(0, 127, 1, 5).lag(0.005);
	size = size.linexp(0, 127, bufferSize, 256).lag(0.005);

	phasor = Phasor.ar(amp, rate, 0, size);
	sig = BufRd.ar(2, buffer, phasor, 0);

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);


	Out.ar(out, sig * amp.lag(0.005) * cut.lag(0.005));
}).add;
);



// 