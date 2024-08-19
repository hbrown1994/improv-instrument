SynthDef(\inputSynth, {
	arg monoBus, inBus, bus, out, amp=0, busAmp=0, gate=1, pan=1/((GlobalPresets.numChannels-1)*2),

	lpfGate=0, lpfFreq=20000,
	hpfGate=0, hpfFreq=20,
	highshelfGate=0, highshelfFreq=16000, highShelfAmp=1,
	lowshelfGate=0,  lowshelfFreq=500,    lowShelfAmp=1,
	bpf0Gate=0,      bpf0Freq=5000,       bpf0Amp=1,     bpf0RQ=10,
	bpf1Gate=0,      bpf1Freq=5000,       bpf1Amp=1,     bpf1RQ=10,
	bpf2Gate=0,      bpf2Freq=5000,       bpf2Amp=1,     bpf2RQ=10,
	bpf3Gate=0,      bpf3Freq=5000,       bpf3Amp=1,     bpf3RQ=10,
	bpf4Gate=0,      bpf4Freq=5000,       bpf4Amp=1,     bpf4RQ=10,
	bpf5Gate=0,      bpf5Freq=5000,       bpf5Amp=1,     bpf5RQ=10;

	var sig;

	sig = SoundIn.ar(inBus);
	Out.ar(monoBus, (sig*busAmp));

	EnvGen.kr(Env.asr(0, 1, 0), gate, doneAction: 2);

	sig = PanX.ar(GlobalPresets.numChannels, sig, pan, 1.0, 2.0);

	sig = ParameticEQ.ar(sig,
		lpfGate,  lpfFreq,
		hpfGate,  hpfFreq,
		highshelfGate,  highshelfFreq,  highShelfAmp,
		lowshelfGate,  lowshelfFreq,  lowShelfAmp,

		bpf0Gate, bpf0Freq, bpf0RQ, bpf0Amp,
		bpf1Gate, bpf1Freq, bpf1RQ, bpf1Amp,
		bpf2Gate, bpf2Freq, bpf2RQ, bpf2Amp,
		bpf3Gate, bpf3Freq, bpf3RQ, bpf3Amp,
		bpf4Gate, bpf4Freq, bpf4RQ, bpf4Amp,
		bpf5Gate, bpf5Freq, bpf5RQ, bpf5Amp
	);

	Out.ar(bus, (sig*busAmp));
	Out.ar(out, (sig*amp));
}).add;

