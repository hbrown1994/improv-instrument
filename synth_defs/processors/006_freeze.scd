(
SynthDef(\freeze, {
	arg busMono, x, y, pres, inBus, bus, amp=1, busAmp, out=0, gate=1, env, state, ampGate, thresh, pauseGate=1;
	var in, chain, sig, trig, hold, pan, onsets, dust, grain, shift, trigSel;

	in = In.ar(inBus, GlobalPresets.numChannels);
	in = Mix.new(in);


	thresh = Clip.kr(thresh, 0.7, 2.0);
	onsets = Onsets.kr(FFT(LocalBuf(512), HPF.ar(in, 200.0)), thresh, \rcomplex, 2, 0.3);

	trig = pres > 0;

	trigSel = Select.kr(state, [trig, onsets]);
	trig = SetResetFF.kr(DelayN.kr(trigSel, 0.05, 0.05), trigSel);

	chain = FFT(LocalBuf(256), in);
	chain = PV_Freeze(chain, trig);
	sig = IFFT(chain);

	env = env.linlin(0, 1, 0.005, 180);

	sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2) * trig.lag(0.05);
	sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

	dust = Dust.kr(y.linexp(0, 127, 0.1, 100));

	grain = GrainIn.ar(1, dust, TRand.kr(0.005, x.linexp(0, 127, 0.008, 0.1), dust), sig);

	sig = SelectX.ar(x.linlin(0, 127, 0.0, 1.0), [sig, grain]);

	pan = TBrownRand.kr(0, 1, 1, 0, dust+trig).lag(0.05);
	sig = PanX.ar(GlobalPresets.numChannels, sig, pan);

	sig = sig.clip(-1.0, 1.0) * Select.kr(ampGate, [1, Select.kr(pres>0, [0, 1]).lag(0.005)]);

	Out.ar(out, sig*amp.lag(0.05));
	Out.ar(bus, sig*busAmp.lag(0.05));
	Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
}).add;
);

GlobalSynths.processors.add(\freeze);



