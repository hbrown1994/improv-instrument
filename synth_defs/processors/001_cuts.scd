(
SynthDef(\cuts, {
	arg busMono, env, inBus, gate, x=0, y=0, pres=0, amp, busAmp, bus, out, thresh=0.7, state, ampGate, pauseGate=1;
	var in, sig, trate, dur, xPos, clk, point, point0, point1, point2, point3, point4, point5, point6, point7, phasor, pauseEnv, buf, lowRate, highRate, hold, overlap;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	lowRate = x.linlin(0, 127, 0, 50);
	highRate = lowRate+5;
	overlap = y.linexp(0, 127, 0.4, 4);

	hold = Select.kr(state, [1, 0]);

	buf = LocalBuf(SampleRate.ir);

	trate = LFNoise2.kr(LFNoise2.kr(0.5, 1.5, 2)).range(lowRate, highRate);
	dur = (1 / trate)*overlap;
	clk = Impulse.kr(trate);

	phasor = Phasor.ar(0, 1, 0, SampleRate.ir);
	phasor = Gate.ar(phasor, hold);

	in = DelayL.ar(in, 0.2, 0.2);

	BufWr.ar(in, buf, phasor, 1);

	xPos = (phasor/SampleRate.ir);

	point0 = Latch.kr(xPos, PulseDivider.kr(clk, 8, 0));
	point1 = Latch.kr(xPos, PulseDivider.kr(clk, 8, 1));
	point2 = Latch.kr(xPos, PulseDivider.kr(clk, 8, 2));
	point3 = Latch.kr(xPos, PulseDivider.kr(clk, 8, 3));
	point4 = Latch.kr(xPos, PulseDivider.kr(clk, 8, 4));
	point5 = Latch.kr(xPos, PulseDivider.kr(clk, 8, 5));
	point6 = Latch.kr(xPos, PulseDivider.kr(clk, 8, 6));
	point7 = Latch.kr(xPos, PulseDivider.kr(clk, 8, 7));

	point = TWChoose.kr(clk, [point0,point1,point2,point3, point4, point5, point6, point7], [LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1)].normalizeSum);

	sig = BufGrain.ar(clk, dur, buf, TBrownRand.kr(1, 1, 0.6, 0, clk), point);

	sig = PanX.ar(GlobalPresets.numChannels, sig, TBrownRand.kr(0.0, 1.0, 1, 0, clk));

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\cuts);
