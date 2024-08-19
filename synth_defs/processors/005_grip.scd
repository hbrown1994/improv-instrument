(
SynthDef(\grip, {
	arg busMono, inBus, env, x=0, y=0, pres=0, amp, busAmp, bus, out, thresh, state, ampGate, trigRateDust=5, trigRateImpulse=12, inDelay = 0.02, t_trig = 0, gate = 1, pauseGate = 1, localEnvGate = 0, releaseTime = 1;
	var trig, div0, div1, switch0, switch1, in, local, delay, delayTime, line,  onsets;
	var triga, div0a, div1a, switch0a, switch1a, destFreq, lpfLine, hpfLine, lpfDest, hpfDest, sig;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);

	trig = Select.kr(state, [Dust.kr(x.linlin(0, 127, 0, 30)), pres>0]);

	div0 = PulseDivider.kr(trig, 2, 0);
	div1 = PulseDivider.kr(trig, 2, 1);
	switch0 = SetResetFF.kr(div0,div1);
	switch1 = SetResetFF.kr(div1,div0);

	t_trig = Select.kr(state, [pres > 0, pres > 0]);
	div0a = Trig.kr(t_trig, 0.01);
	div1a = Trig.kr(TDelay.kr(t_trig, inDelay), 0.01);
	switch0a = SetResetFF.kr(div0a,div1a);
	switch1a = SetResetFF.kr(div1a,div0a);

	switch0 = Select.kr(state, [switch0, switch0a]);
	switch1 = Select.kr(state, [switch1, switch1a]);

	delayTime = Select.kr(state, [TRand.kr(32/44100, 1024/44100, trig), inDelay*pres.linexp(0, 127, 1.0, 3.0)]);

	delay = DelayN.ar(LocalIn.ar(2), 8192/44100, delayTime);

	delay = Compander.ar((switch1*delay), (switch1*delay), 1, 1, 0.5, 0.01, 0.01).distort.clip2(0.8);

	local = Mix.new([(switch0*in),delay]);

	LocalOut.ar(local.reverse*1.2);

	destFreq = TGaussRand.kr(7000, 21000, Select.kr(state, [trig, t_trig]));

	lpfLine =  destFreq.lag(0.2);

	sig = LPF.ar(HPF.ar(local,60), lpfLine);

	env = env.linlin(0, 1, 0.005, 180);
	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	sig = PanX.ar(GlobalPresets.numChannels, LeakDC.ar(sig), TGaussRand.kr(trig: trig));

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	sig = sig * Select.kr(state, [1, 1, LagUD.kr(Clip.kr(Amplitude.kr(in).linlin(0, 0.2, 0, 0.7)), 0.01)]);

	Out.ar(out, sig*amp.lag(0.005));
	Out.ar(bus, sig*busAmp.lag(0.005));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);
GlobalSynths.processors.add(\grip);