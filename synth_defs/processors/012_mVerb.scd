(
SynthDef(\mVerb, {
	arg busMono, x, y, pres, inBus, bus, amp=1, busAmp, out=0, gate=1, env, state, ampGate, pauseGate=1;
	var in, temp, sig, trig, lag, arr;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	temp = in;
	arr = [];
	sig = 0;

	trig = Dust.kr(Select.kr(state, [y, pres]).linlin(0, 127, 0, Select.kr(state, [100, 500])))+Trig.kr(pres<1);

	lag = Select.kr(state, [0.08, x.linlin(0, 127, 0.08, 15)]);

	temp = DelayN.ar(temp, 0, 2, 0.1, mul: 7);

	19.do{
		temp = AllpassL.ar(temp, 0.02,
			{TRand.kr(0.001, x.linlin(0, 127, 0.02, 0.3), trig).lag2(lag)},
			{TRand.kr(0.01, y.linlin(0, 127, Select.kr(state, [0.05, 0.2]), Select.kr(state, [0.2, 7.0])), trig).lag2(lag)}
		);

		temp = IIRFilter.ar(temp, {TExpRand.kr(9000, 18000, trig).lag2(lag)}, TRand.kr(0.7, 3.0, trig).lag2(lag));
		arr = arr.add(HPF.ar(temp*0.15, 80));
	};

	sig = SplayAz.ar(GlobalPresets.numChannels,  arr);

	sig = Clip.ar(sig, -1.0, 1.0) * EnvGen.kr(Env.asr(env.linlin(0.0, 1.0, 0.005, 180.0), 1, env.linlin(0.0, 1.0, 0.005, 180.0), curve: [4, -4]), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\mVerb);
