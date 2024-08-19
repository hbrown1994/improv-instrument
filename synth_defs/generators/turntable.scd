(
SynthDef.new(\turntable, {
	arg buf0, buf1, out, location0, pres0, location1, pres1, quneoLeftAmp=1, quneoLeftBusAmp=1, bus, pauseGate0, pauseGate1;
	var sig0, pointer0, lag_amount0, lag_amount1, sig1, pointer1, sig, pauseGate;

	lag_amount0 = pres0.linexp(0, 127, 0.5, 300);
	lag_amount1 = pres1.linexp(0, 127, 0.5, 300);

	pointer0 = Lag.ar(K2A.ar(location0.linlin(0.0, 127, 0.0, BufFrames.kr(buf0))), lag_amount0);
	pointer1 = Lag.ar(K2A.ar(location1.linlin(0.0, 127, 0.0, BufFrames.kr(buf1))), lag_amount1);

	sig0 = LeakDC.ar(BufRd.ar(1, buf0, pointer0, 1, 4));
	sig1 = LeakDC.ar(BufRd.ar(1, buf1, pointer1, 1, 4));

	sig0 = PanX.ar(2, sig0, Wrap.kr(location0.linlin(0, 127, 0.0, 1.0).lag(lag_amount0), TRand.kr(trig: Changed.kr(location0)), TRand.kr(trig: Changed.kr(location0))));
	sig1 = PanX.ar(2, sig1, Wrap.kr(location1.linlin(0, 127, 0.0, 1.0).lag(lag_amount1), TRand.kr(trig: Changed.kr(location1)), TRand.kr(trig: Changed.kr(location1))));

	sig0 = sig0 * (pres0>0).lag(0.005);
	sig1 = sig1 * (pres1>0).lag(0.005);

	sig = Mix([sig0, sig1]);

	Out.ar(out, sig);
}).add;
)