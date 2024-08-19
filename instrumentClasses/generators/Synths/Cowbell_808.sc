
Cowbell_808 {
	*ar {
		arg freq1=811.16, freq2=538.75, decay=9.5, amp=1, trig;
		var sig, pul1, pul2, env, atk, atkenv, datk;

		atkenv = EnvGen.kr(Env.perc(0, 1, 1, -215), trig, doneAction:0);
		env = EnvGen.kr(Env.asr(0.01, 1.0, decay, -90), trig, doneAction:2);
		pul1 = LFPulse.ar(freq1);
		pul2 = LFPulse.ar(freq2);
		atk = (pul1 + pul2) * atkenv * 6;
		datk = (pul1 + pul2) * env;
		sig = (atk + datk) * amp;
		sig = HPF.ar(sig, 250);
		sig = LPF.ar(sig, 4500);
		sig = sig*0.15;


		^sig;
	}
}


