(
SynthDef(\scrub, {
	arg busMono, inBus, out, bus, x=0, y=127, pres=0, amp, busAmp, gate, env, state=0, ampGate, pauseGate=1, bufLength=4;
	var in, trigPeriod, sig, chain, trig, pos, fftSize, jump, buf, bufDur, recTrig, playSpeed=1, durMult=1;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	durMult = Select.kr(state, [1, x.linexp(0, 127, 1.0, 15.0)]);
	playSpeed = y.linlin(0, 127, 0.2, 3.0);

	buf = LocalBuf(SampleRate.ir*bufLength, 1);
	recTrig = Select.kr(state, [pres>1, 0]);
	bufDur = Gate.ar(Phasor.ar(recTrig, 1, 0, SampleRate.ir*bufLength), recTrig);
	in = in * EnvGen.ar(Env.asr(0.005, 1, 0.005), recTrig);
	RecordBuf.ar(in, buf, loop: 0, trigger: recTrig);

	fftSize = 8192;

	trigPeriod = fftSize/SampleRate.ir;
	trig = Impulse.ar(1/trigPeriod);
	pos = Demand.ar(trig, 0, demandUGens: Dseries(0, trigPeriod/durMult))*SampleRate.ir;
	pos = Select.ar(state, [K2A.ar(x.linlin(0, 127, 0, bufDur)), (Demand.ar(trig, 0, demandUGens: Dseries(0, trigPeriod/durMult))*SampleRate.ir)]);
	jump = fftSize/4/durMult;

	sig = [
		PlayBuf.ar(1, buf, playSpeed, trig, (pos), Select.kr(state, [0, 1])),
		PlayBuf.ar(1, buf, playSpeed, trig, (pos+jump), Select.kr(state, [0, 1])),
		PlayBuf.ar(1, buf, playSpeed, trig, (pos+(2*jump)), Select.kr(state, [0, 1])),
		PlayBuf.ar(1, buf, playSpeed, trig, (pos+(3*jump)), Select.kr(state, [0, 1])),
	];

	sig = sig.collect({ |item, i| //collect just applies this process to each playbuf
		chain = FFT(LocalBuf(fftSize), item, hop: 1.0, wintype: 1);
	    //chain = PV_Diffuser(chain, 1 - trig);
		item = IFFT(chain, wintype: 1);
	});

	sig[1] = DelayC.ar(sig[1], trigPeriod, trigPeriod/4);
	sig[2] = DelayC.ar(sig[2], trigPeriod, trigPeriod/2);
	sig[3] = DelayC.ar(sig[3], trigPeriod, 3*trigPeriod/4);

	sig = Mix(sig);

	sig = SelectX.ar(state.lag(0.005), [Silent.ar, sig]);

	sig = PanX.ar(GlobalPresets.numChannels, sig, LFBrownNoise1.kr(Gate.kr(pres.linexp(0, 127, 0.1, 15), ampGate)).range(0, 1));

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig * Select.kr(state, [0, 1]).lag(0.005);
	sig = sig * Gate.kr(pres.linexp(0, 127, 0.001, 1).lag(0.005), ampGate);
	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1])]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);



GlobalSynths.processors.add(\scrub);
