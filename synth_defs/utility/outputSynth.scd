(
SynthDef(\outputSynth, {
	arg amp0=0, amp1=0,amp2=0, amp3=0, amp4=0, amp5=0, amp6=0, amp7=0, masterAmp=0,

	pan0, pan1, pan2, pan3, pan4, pan5, pan6, pan7,
	mute0, mute1, mute2, mute3, mute4, mute5, mute6, mute7,
	solo0, solo1, solo2, solo3, solo4, solo5, solo6, solo7, center, buf0, run0, makeEQ=1,
	busIn0, busIn1, busIn2, busIn3, busIn4, busIn5, busIn6, busIn7,

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

	var in0, in1, in2, in3, in4, in5, in6, in7, sig, soloSelect, eq;

	amp0 = amp0.linlin(0, 127, 0.0, 1.0).lag(0.05);
	amp1 = amp1.linlin(0, 127, 0.0, 1.0).lag(0.05);
	amp2 = amp2.linlin(0, 127, 0.0, 1.0).lag(0.05);
	amp3 = amp3.linlin(0, 127, 0.0, 1.0).lag(0.05);
	amp4 = amp4.linlin(0, 127, 0.0, 1.0).lag(0.05);
	amp5 = amp5.linlin(0, 127, 0.0, 1.0).lag(0.05);
	amp6 = amp6.linlin(0, 127, 0.0, 1.0).lag(0.05);
	amp7 = amp7.linlin(0, 127, 0.0, 1.0).lag(0.05);

	pan0 = pan0.linlin(0, 127, 0.0, 1.0).lag(0.05);
	pan1 = pan1.linlin(0, 127, 0.0, 1.0).lag(0.05);
	pan2 = pan2.linlin(0, 127, 0.0, 1.0).lag(0.05);
	pan3 = pan3.linlin(0, 127, 0.0, 1.0).lag(0.05);
	pan4 = pan4.linlin(0, 127, 0.0, 1.0).lag(0.05);
	pan5 = pan5.linlin(0, 127, 0.0, 1.0).lag(0.05);
	pan6 = pan6.linlin(0, 127, 0.0, 1.0).lag(0.05);
	pan7 = pan7.linlin(0, 127, 0.0, 1.0).lag(0.05);

	mute0 = mute0.linlin(0.0, 1.0, 1.0, 0.0).lag(0.05);
	mute1 = mute1.linlin(0.0, 1.0, 1.0, 0.0).lag(0.05);
	mute2 = mute2.linlin(0.0, 1.0, 1.0, 0.0).lag(0.05);
	mute3 = mute3.linlin(0.0, 1.0, 1.0, 0.0).lag(0.05);
	mute4 = mute4.linlin(0.0, 1.0, 1.0, 0.0).lag(0.05);
	mute5 = mute5.linlin(0.0, 1.0, 1.0, 0.0).lag(0.05);
	mute6 = mute6.linlin(0.0, 1.0, 1.0, 0.0).lag(0.05);
	mute7 = mute7.linlin(0.0, 1.0, 1.0, 0.0).lag(0.05);

	in0 = In.ar(busIn0, GlobalPresets.numChannels);
	in1 = In.ar(busIn1, GlobalPresets.numChannels);
	in2 = In.ar(busIn2, GlobalPresets.numChannels);
	in3 = In.ar(busIn3, GlobalPresets.numChannels);
	in4 = In.ar(busIn4, GlobalPresets.numChannels);
	in5 = In.ar(busIn5, GlobalPresets.numChannels);
	in6 = In.ar(busIn6, GlobalPresets.numChannels);
	in7 = In.ar(busIn7, GlobalPresets.numChannels);

	soloSelect = [solo0, solo1, solo2, solo3, solo4, solo5, solo6, solo7];

	sig = Select.ar(soloSelect.sum>0,
		[
			[
				PanX.ar(GlobalPresets.numChannels, (in0*amp0*mute0), Select.kr(center, [pan0, 1/((GlobalPresets.numChannels-1)*2)])),
				PanX.ar(GlobalPresets.numChannels, (in1*amp1*mute1), Select.kr(center, [pan1, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in2*amp2*mute2), Select.kr(center, [pan2, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in3*amp3*mute3), Select.kr(center, [pan3, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in4*amp4*mute4), Select.kr(center, [pan4, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in5*amp5*mute5), Select.kr(center, [pan5, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in6*amp6*mute6), Select.kr(center, [pan6, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in7*amp7*mute7), Select.kr(center, [pan7, 1/((GlobalPresets.numChannels-1)*2)]);)
			],

			[
				PanX.ar(GlobalPresets.numChannels, (in0*amp0*mute0*soloSelect[0]), Select.kr(center, [pan0, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in1*amp1*mute1*soloSelect[1]), Select.kr(center, [pan1, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in2*amp2*mute2*soloSelect[2]), Select.kr(center, [pan2, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in3*amp3*mute3*soloSelect[3]), Select.kr(center, [pan3, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in4*amp4*mute4*soloSelect[4]), Select.kr(center, [pan4, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in5*amp5*mute5*soloSelect[5]), Select.kr(center, [pan5, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in6*amp6*mute6*soloSelect[6]), Select.kr(center, [pan6, 1/((GlobalPresets.numChannels-1)*2)]);),
				PanX.ar(GlobalPresets.numChannels, (in7*amp7*mute7*soloSelect[7]), Select.kr(center, [pan7, 1/((GlobalPresets.numChannels-1)*2)]);)
			]
		]
	);

	8.do{|i| Out.ar(GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][GlobalPresets.outputSynthChan+i], sig[i])}; //for recording

	sig = sig[0]+sig[1]+sig[2]+sig[3]+sig[4]+sig[5]+sig[6]+sig[7];

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

	sig = sig * masterAmp;

	Out.ar(0, sig);
}).add;
);


