(
SynthDef(\markovSynth, {
	arg markovX, markovY, markovCut, out, markovAmp, markovFreeze, markovAmpGate, markovGate;
	var fedIn = LocalIn.ar(1) * 0.5;
	var sig = {MarkovSynth.ar(fedIn + Saw.ar(markovX.linlin(0, 1, 10, 4000), Select.kr(markovCut, [markovY, markovAmpGate.linlin(0, 1, 0, markovY)])), markovFreeze.linlin(0, 1, 1, 0), 1, 2)}!2;
	LocalOut.ar(sig);
	EnvGen.kr(Env.asr(0.005, 1, 0.005, [4, -4]), markovGate, doneAction: 2);
	Out.ar(out, LPF.ar(HPF.ar(LeakDC.ar(sig*markovAmp*Select.kr(markovCut, [1, markovAmpGate.lag(0.005)])), 60), 17000));
}).add;
);




