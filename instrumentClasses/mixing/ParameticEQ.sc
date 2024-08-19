ParameticEQ {
	*ar {
		arg in, lpfGate=0, lpfFreq=16000, hpfGate=0, hpfFreq=20, highshelfGate=0, highshelfFreq=16000, highShelAmp=1.0 , lowshelfGate=0, lowshelfFreq=60, lowShelAmp=1, bpf0Gate=0, bpf0Freq=440, bpf0RQ=10, bpf0Amp=1, bpf1Gate=0, bpf1Freq=440, bpf1RQ=10, bpf1Amp=1, bpf2Gate=0, bpf2Freq=440, bpf2RQ=10, bpf2Amp=1, bpf3Gate=0, bpf3Freq=440, bpf3RQ=10, bpf3Amp=1, bpf4Gate=0, bpf4Freq=440, bpf4RQ=10, bpf4Amp=1, bpf5Gate=0, bpf5Freq=440, bpf5RQ=10, bpf5Amp=1;
		var sig;

		sig = in;
		sig = LinSelectX.ar(lpfGate, [sig, LPF.ar(sig, lpfFreq.clip(20, 20000).lag(0.05))]);
		sig = LinSelectX.ar(hpfGate, [sig, HPF.ar(sig, hpfFreq.clip(20, 20000).lag(0.05))]);
		sig = LinSelectX.ar(highshelfGate, [sig, BHiShelf.ar(sig, highshelfFreq.clip(20, 20000).lag(0.05), 1.0, highShelAmp.clip(-120, 18).lag(0.05))]);
		sig = LinSelectX.ar(lowshelfGate, [sig, BLowShelf.ar(sig, lowshelfFreq.clip(20, 20000).lag(0.05), 1.0, lowShelAmp.clip(-120, 18).lag(0.05))]);

		sig = LinSelectX.ar(bpf0Gate, [sig, BPeakEQ.ar(sig, bpf0Freq.clip(20, 20000).lag(0.05), bpf0RQ.clip(0.01, 10).lag(0.05), bpf0Amp.clip(-120, 18).lag(0.05))]);
		sig = LinSelectX.ar(bpf1Gate, [sig, BPeakEQ.ar(sig, bpf1Freq.clip(20, 20000).lag(0.05), bpf1RQ.clip(0.01, 10).lag(0.05), bpf1Amp.clip(-120, 18).lag(0.05))]);
		sig = LinSelectX.ar(bpf2Gate, [sig, BPeakEQ.ar(sig, bpf2Freq.clip(20, 20000).lag(0.05), bpf2RQ.clip(0.01, 10).lag(0.05), bpf2Amp.clip(-120, 18).lag(0.05))]);
		sig = LinSelectX.ar(bpf3Gate, [sig, BPeakEQ.ar(sig, bpf3Freq.clip(20, 20000).lag(0.05), bpf3RQ.clip(0.01, 10).lag(0.05), bpf3Amp.clip(-120, 18).lag(0.05))]);
		sig = LinSelectX.ar(bpf4Gate, [sig, BPeakEQ.ar(sig, bpf4Freq.clip(20, 20000).lag(0.05), bpf4RQ.clip(0.01, 10).lag(0.05), bpf4Amp.clip(-120, 18).lag(0.05))]);
		sig = LinSelectX.ar(bpf5Gate, [sig, BPeakEQ.ar(sig, bpf5Freq.clip(20, 20000).lag(0.05), bpf5RQ.clip(0.01, 10).lag(0.05), bpf5Amp.clip(-120, 18).lag(0.05))]);

		^sig;
	}
}
