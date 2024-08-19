(
SynthDef(\pAuto, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, state=0, ampGate, pauseGate=1, loop_length=8;
	var in, sig, buf, buffer, bufDur, recTrig, pointer, lag_amount, trig, atk, rel, loop, durLow, durHigh, temp, count, mul, rate, dur, startPos=0, atkRel, envTrack, trigArray, envMult, fbTrig;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	buf = LocalBuf(SampleRate.ir*loop_length, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Clip.kr(Gate.kr(Sweep.kr(recTrig), recTrig), 0, loop_length)+(0.005*2);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger:  Select.kr(recTrig, [DelayN.kr(recTrig, 0.006, 0.005), recTrig]));

	trig = Dust.kr(Gate.kr(pres.linexp(0, 127, 0.5, 100.0).lag(0.1), ampGate));
	fbTrig = LocalIn.kr(8);
	count = TWindex.kr(trig, fbTrig, 1); //Stepper.kr(trig, 0, 0, 7); //

	trigArray = 8.collect{|i| Gate.kr(BinaryOpUGen('==', count, i), fbTrig[i])};

	loop = TWindex.kr(trig, [x.linlin(0, 127, 1.0, 0.0), x.linlin(0, 127, 0.0, 1.0)]);
	atkRel = TRand.kr(x.linlin(0, 127, 0.005, 7.0), x.linlin(0, 127, 0.005, 12.0), trig);
	rate = TRand.kr(1, y.linexp(0, 127, 0.25, 4).round(0.1), trig);

	dur = Select.kr(loop>0, [TRand.kr(0.1, bufDur, trig), TRand.kr(0.5, bufDur*2*x.linlin(0, 127, 1.0, 2.0), trig)]);
	mul = TRand.kr(0.8, 1.0, trig); //Random amplitude upon trig

	envMult = 8.collect{|i| EnvGen.kr(Env.linen(Latch.kr(atkRel, trigArray[i]), Latch.kr(dur, trigArray[i]), Latch.kr(atkRel, trigArray[i]), Latch.kr(mul, trigArray[i]), (-4)), trigArray[i])};
	envTrack = 8.collect{|i| Select.kr(envMult[i]>0, [1, 0])};
	LocalOut.kr(envTrack);

	sig = 8.collect{|i| PolySamplerAuto.ar(1, buf, rate, 0, loop, trigArray[i]+Impulse.kr(0)) * envMult[i]};

	sig = Splay.ar(sig.scramble);

	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0), 0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, pres>0]).lag(0.005);
	sig = SelectX.ar(state.lag(0.005), [Silent.ar, sig]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\pAuto);