SynthDef(\shepard, {
	arg phaseFreq=0.05, incr =(-2), dropoff=10, pitch=60, interval=120, relphase=0, shepsAmp, out, shepsGate, atk, shepsState, shepsCut=1;
	var phase, temp, sum, count, a=[], sig, env;
	temp = 0;
	sum = 0;
	count = 0;

	//Stepper is a simple counting UGen
	phase = Stepper.ar(Impulse.ar(phaseFreq.linlin(0.0, 1.0, 0.001, Select.kr(shepsState, [60.0, 200.0]))), 0, 0, 10000, incr.linlin(0.0, 1.0, 0.0, Select.kr(shepsState, [20.0, 500.0])));

	//Generate 20 instances of the "Shepard.ar" class. To see this code
	//go the the "Shepard.sc" class file in this folder. Remember to
	//save this file in your "Extensions" folder for this code to work.
	a = Array.interpolation(20, 0.0, 1.0).scramble;

	20.do{
		temp = PanX.ar(GlobalPresets.numChannels, Shepard.ar(phase, relphase, dropoff.linlin(0.0, 1.0, 0.1, 20.0), pitch.linlin(0.0, 1.0, 0.1, 140.0), interval.linlin(0.0, 1.0, 0.1, Select.kr(shepsState, [30.0, 500.0])), 0.35), a[count]);
		sum = sum + temp; //sum all instance
		relphase = relphase + 500; //increase relative phase of each Shepard.ar instance
		count = count + 1;
	};

	env=EnvGen.kr(Env.asr(atk.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), 1, atk.linlin(0.0, 1.0, 0.005, 180.0).round.clip(0.005, 180), [4, -4]), shepsGate, doneAction: 2);

	sig = sum*0.87;

	Out.ar(out, (sig*shepsAmp*env*shepsCut));
}).add;


