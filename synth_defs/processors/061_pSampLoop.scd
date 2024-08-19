(
SynthDef(\pLoop, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, loop_length=4;
	var in, sig, buf, buffer, bufDur, recTrig, pointer, lag_amount, trig, atk, rel, loop, durLow, durHigh, temp, count, mul, rate, dur, startPos=0;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*loop_length, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Clip.kr(Gate.kr(Sweep.kr(recTrig), recTrig), 0, loop_length);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	temp=0;
	sig=0;

	trig = pres>0;
	count = Stepper.kr(trig, 0, 0, 7);

	loop = 1;
	atk = 0.01;
	rel = 0.01;
	//durLow = 0.5;
	//durHigh = x.linlin(0, 127);

	//Random Rate upon trig (only effecting subsequently triggerd instances pf PolySampler.ar)
	rate =  y.linexp(0, 127, 0.25, 4);
	mul = TRand.kr(0.6, 0.8, trig); //Random amplitude upon trig
	//dur = TRand.kr(durLow, durHigh, trig); //Random duration upon trig
	dur = x.linlin(0, 127, 0.5, bufDur*3);

	8.do{ //Create 8 instances of  PolySampler.ar
		arg i;
		temp = PolySampler.ar(1, buf, rate, startPos, loop, atk, dur, rel, mul, BinaryOpUGen('==', count, i)+Impulse.kr(0));
		temp = PanX.ar(GlobalPresets.numChannels, temp, 8.collect{|j| (j/8)+(0.0625)}.scramble[i]);
		sig = sig + temp;
	};

	//sig = sig * (-3).dbamp;

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0), 0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, 0]).lag(0.005);
	sig = SelectX.ar(state.lag(0.005), [Silent.ar, sig]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\pLoop);