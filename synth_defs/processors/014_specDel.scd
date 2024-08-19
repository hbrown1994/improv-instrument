(
{
	var fb0Buf, dels0Buf, dels1Buf;
	dels0Buf = Buffer.loadCollection(s, Array.interpolation(1024, 0.05, 0.4));
	dels1Buf = Buffer.loadCollection(s, Array.interpolation(1024, 0.05, 0.4));
	fb0Buf = Buffer.loadCollection(s, Array.fill(1024, { rrand(0.05,0.7) }));

	SynthDef(\specDel, {
		arg busMono, gate, env, x=0, y=0, pres=0, amp, busAmp, bus, thresh, state, ampGate, pauseGate=1, inBus, out=0;
		var sig, in, chain, chain1;

		in = In.ar(inBus, GlobalPresets.numChannels);
		in = Mix.new(in) * ampGate.linlin(0, 1, 1, 0).lag(0.05);

		in = LPF.ar(in, Select.kr(state, [18000, x.linlin(0, 127, 18000, 3000)]));
		in = HPF.ar(in, y.linlin(0, 127, 150, 2000));



		chain = FFT(LocalBuf(2048), in, 0.25, -1);
		chain = PV_PartialSynthF(chain, 5, 6, 0);

		chain1 = PV_Copy(chain, LocalBuf(2048));

		chain = PV_EvenBin(chain);
		chain1 = PV_OddBin(chain1);

		1023.do{|i|
			BufWr.kr(Select.kr(state, [x.linlin(0, 127, 0.005, 0.95), 0.9]), fb0Buf, i);
		};

		chain = PV_BinDelay(chain, 5, dels0Buf, fb0Buf, 0.25);
		chain1 = PV_BinDelay(chain1, 5, dels1Buf, fb0Buf, 0.25);

		sig = [IFFT(chain), IFFT(chain1)];

		env = env.linlin(0, 1, 0.005, 180);
		sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
	PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));
		sig = sig * EnvGen.kr(Env.asr(env, 1, env), gate, doneAction: 2);

		sig = sig.clip(-1.0, 1.0);

		Out.ar(out, sig*amp.lag(0.005));
		Out.ar(bus, sig*busAmp.lag(0.005));
		Out.ar(busMono, Mix.new(sig*busAmp.lag(0.05)));
	}).add;
}.value;
);

GlobalSynths.processors.add(\specDel);