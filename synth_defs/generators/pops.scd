(
SynthDef(\pops, {
	arg gate, buf, length, out;
	var sig, endLoop;

	endLoop = BufFrames.kr(buf) * length.linexp(0, 127, 1, 0.01);

	sig = LoopBuf.ar(2, buf, BufRateScale.kr(buf), 1, 0, 0, endLoop);

	EnvGen.kr(Env.asr(0, 1, 0), gate, doneAction: 2);

	Out.ar(out, sig*0.7);
}).add;
)