SynthDef(\fieldRecordingSplit, {
	arg gate, buf, rate, out, rangeMin, rangeMax, lpf, hpf, amp, envSize, t_presetTrig, lpfLow, lpfHigh, hpfLow, hpfHigh;
	var sig, phasor, numFrames, start, end, env, flipFlop, sig0, sig1, gate0, gate1, sineSig;

	numFrames = BufFrames.kr(buf);
	flipFlop = ToggleFF.kr(t_presetTrig);

	start = rangeMin.linlin(0, 1, 0, numFrames);
	end = rangeMax.linlin(0, 1, 0, numFrames);

	gate0 = flipFlop;
	gate1 = flipFlop.linlin(0, 1, 1, 0);

	sig0 = BufRd.ar(
		2,
		buf,
		Phasor.ar(
			Trig.kr(gate0),
			Gate.kr(rate, gate0)*BufRateScale.kr(buf),
			Gate.kr(start, gate0),
			Gate.kr(end, gate0),
			Gate.kr(start, gate0)
		);
	);
	sig0 = sig0 * EnvGen.kr(Env.asr(0.05, 1, 0.05, \lin), gate0);

	sig1 = BufRd.ar(
		2,
		buf,
		Phasor.ar(Trig.kr(gate1),
			Gate.kr(rate, gate1)*BufRateScale.kr(buf),
			Gate.kr(start, gate1),
			Gate.kr(end, gate1),
			Gate.kr(start, gate1)
		);
	);
	sig1 = sig1 * EnvGen.kr(Env.asr(0.05, 1, 0.05, \lin), gate1);


	sig = Mix.new([sig0, sig1]);
	sineSig = Mix.new([sig0, sig1]);

	sig = LPF.ar(sig, lpf.linexp(0, 1, lpfLow, lpfHigh).lag(0.1));
	sig = HPF.ar(sig, hpf.linexp(0, 1, hpfLow, hpfHigh).lag(0.1));

	env = EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);

	sig = sig * Select.kr(BinaryOpUGen('==', envSize, 0.0),
		[
			EnvGen.kr(Env.new([0, 1, 1, 0], [0.005, envSize.linexp(0, 1, numFrames/SampleRate.ir, 0.001), 0.005], [-4, 4]), t_presetTrig),
			1
	]);

	sig = sig * amp * env;

	Out.ar(out, sig);
}).add;