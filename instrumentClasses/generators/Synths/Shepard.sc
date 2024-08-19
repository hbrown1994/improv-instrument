Shepard {
	*ar {
		arg phase, relPhase, dropoff, pitch, interval, mul;
		var osc, env, sig, phaseExpr, freq;
		phaseExpr = ((phase+relPhase)%10000) * 0.0002-1;
		freq = ((phaseExpr * interval) + pitch).midicps.lag(0.05);
		osc = LeakDC.ar(SinOsc.ar(freq, mul:0.3));
		env = ((phaseExpr*(-1))*phaseExpr*dropoff).exp.lag(0.05);
		sig = osc * env * mul;
		^sig;
	}
}


