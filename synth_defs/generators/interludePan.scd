(
SynthDef(\interlude, {
	arg out, bus, pan0=0/((GlobalPresets.numChannels-1)*2), pan1=2/((GlobalPresets.numChannels-1)*2), buf;
	var in, sig0, sig1, sig, sweep, onsets, chain, test;

	in = PlayBuf.ar(2, buf, 1, 1, 0, 0, 2);
	chain = FFT(LocalBuf(512), in, wintype: 1);
	onsets = Onsets.kr(chain, 0.7);

	pan0=(0/((GlobalPresets.numChannels-1)*2));
	pan1=(2/((GlobalPresets.numChannels-1)*2));

	sweep = Sweep.kr(Impulse.kr(0));

	pan0 = Select.kr(((sweep > 21.69) * (sweep < 21.82)),     [pan0, LFNoise0.kr(10).range(0, 1)]);
	pan1 = Select.kr(((sweep > 21.69) * (sweep < 21.82)),     [pan1, LFNoise0.kr(11).range(0, 1)]);

	pan0 = Select.kr(((sweep > 50.952) * (sweep < 51.787)),   [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 50.952) * (sweep < 51.787)),   [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 51.788) * (sweep < 51.970)),   [pan0, LFNoise1.kr(11).range(0, 1)]);
	pan1 = Select.kr(((sweep > 51.788) * (sweep < 51.970)),   [pan1, LFNoise1.kr(12).range(0, 1)]);

	pan0 = Select.kr(((sweep > 67.940) * (sweep < 70.516)),   [pan0, TBrownRand.kr(0, 1, trig: onsets[0])]);
	pan1 = Select.kr(((sweep > 67.940) * (sweep < 70.516)),   [pan1, TBrownRand.kr(0, 1, trig: onsets[1])]);

	pan0 = Select.kr(((sweep > 97.823) * (sweep < 98.060)),   [pan0,  TRand.kr(0, 1, onsets[0])]);
	pan1 = Select.kr(((sweep > 97.823) * (sweep < 98.060)),   [pan1,  TRand.kr(0, 1, onsets[1])]);

	pan0 = Select.kr(((sweep > 109.250) * (sweep < 110.356)), [pan0,  TRand.kr(0, 1, onsets[0])]);
	pan1 = Select.kr(((sweep > 109.250) * (sweep < 110.356)), [pan1,  TRand.kr(0, 1, onsets[1])]);

	pan0 = Select.kr(((sweep > 124.175) * (sweep < 125.811)), [pan0,  TRand.kr(0, 1, onsets[0])]);
	pan1 = Select.kr(((sweep > 124.175) * (sweep < 125.811)), [pan1,  TRand.kr(0, 1, onsets[1])]);

	pan0 = Select.kr(((sweep > 125.811) * (sweep < 134.983)), [pan0,  {0.5.rand}]);
	pan1 = Select.kr(((sweep > 125.811) * (sweep < 134.983)), [pan1,  {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 134.983) * (sweep < 135.899)), [pan0,  TBrownRand.kr(0, 1, trig: onsets[0])]);
	pan1 = Select.kr(((sweep > 134.983) * (sweep < 135.899)), [pan1,  TBrownRand.kr(0, 1, trig: onsets[1])]);

	pan0 = Select.kr(((sweep > 135.899) * (sweep < 139.597)), [pan0,  LFBrownNoise1.kr(0.1).range(0, 0.5)]);
	pan1 = Select.kr(((sweep > 135.899) * (sweep < 139.597)), [pan1,  LFBrownNoise1.kr(0.2).range(0.5, 1.0)]);

	pan0 = Select.kr(((sweep > 139.597) * (sweep < 141.052)), [pan0,  EnvGen.kr(Env([0, 0.5], 1.455), ((sweep > 139.597) * (sweep < 141.052)))]);
	pan1 = Select.kr(((sweep > 139.597) * (sweep < 141.052)), [pan1,  EnvGen.kr(Env([0.5, 1], 1.455), ((sweep > 139.597) * (sweep < 141.052)))]);

	pan0 = Select.kr(((sweep > 141.052) * (sweep < 159.005)), [pan0,  LFBrownNoise1.kr(0.2).range(0, 0.5)]);
	pan1 = Select.kr(((sweep > 141.052) * (sweep < 159.005)), [pan1,  LFBrownNoise1.kr(0.32).range(0.5, 1.0)]);

	pan0 = Select.kr(((sweep > 159.005) * (sweep < 161.422)), [pan0,  LFBrownNoise1.kr(20).range(0, 0.5)]);
	pan1 = Select.kr(((sweep > 159.005) * (sweep < 161.422)), [pan1,  LFBrownNoise1.kr(22).range(0.5, 1.0)]);

	pan0 = Select.kr(((sweep > 161.422) * (sweep < 166.900)), [pan0,  TBrownRand.kr(0, 1, trig: onsets[0])]);
	pan1 = Select.kr(((sweep > 161.422) * (sweep < 166.900)), [pan1,  TBrownRand.kr(0, 1, trig: onsets[1])]);

	pan0 = Select.kr(((sweep > 175.918) * (sweep < 176.419)), [pan0,  LFNoise0.kr(10).range(0, 1)]);
	pan1 = Select.kr(((sweep > 175.918) * (sweep < 176.419)), [pan1,  LFNoise0.kr(11).range(0, 1)]);

	pan0 = Select.kr(((sweep > 176.419) * (sweep < 176.806)), [pan0,  EnvGen.kr(Env([0, 1], 0.387), ((sweep > 176.419) * (sweep < 176.806)))]);
	pan1 = Select.kr(((sweep > 176.419) * (sweep < 176.806)), [pan1,  EnvGen.kr(Env([1, 0], 0.387), ((sweep > 176.419) * (sweep < 176.806)))]);

	pan0 = Select.kr(((sweep > 187.448) * (sweep < 187.673)), [pan0,  {0.5.rand}]);
	pan1 = Select.kr(((sweep > 187.448) * (sweep < 187.673)), [pan1,  {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 197.287) * (sweep < 198.236)), [pan0,  TBrownRand.kr(0, 1, trig: onsets[0])]);
	pan1 = Select.kr(((sweep > 197.287) * (sweep < 198.236)), [pan1,  TBrownRand.kr(0, 1, trig: onsets[1])]);

	pan0 = Select.kr(((sweep > 199.141) * (sweep < 200.969)), [pan0,  {0.5.rand}]);
	pan1 = Select.kr(((sweep > 199.141) * (sweep < 200.969)), [pan1,  {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 200.969) * (sweep < 202.693)), [pan0,  {0.5.rand}]);
	pan1 = Select.kr(((sweep > 200.969) * (sweep < 202.693)), [pan1,  {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 204.335) * (sweep < 204.505)), [pan0,  LFNoise0.kr(43).range(0, 1)]);
	pan1 = Select.kr(((sweep > 204.335) * (sweep < 204.505)), [pan1,  LFNoise0.kr(29).range(0, 1)]);

	pan0 = Select.kr(((sweep > 217.195) * (sweep < 219.177)), [pan0,  TRand.kr(0, 1, onsets[0])]);
	pan1 = Select.kr(((sweep > 217.195) * (sweep < 219.177)), [pan1,  TRand.kr(0, 1, onsets[1])]);

	pan0 = Select.kr(((sweep > 221.339) * (sweep < 241.260)), [pan0,  LFBrownNoise1.kr(0.2).range(0, 0.5)]);
	pan1 = Select.kr(((sweep > 221.339) * (sweep < 241.260)), [pan1,  LFBrownNoise1.kr(0.32).range(0.5, 1.0)]);

	pan0 = Select.kr(((sweep > 241.260) * (sweep < 242.500)), [pan0,  LFBrownNoise1.kr(0.2).range(0, 1)]);
	pan1 = Select.kr(((sweep > 241.260) * (sweep < 242.500)), [pan1,  LFBrownNoise1.kr(0.32).range(0, 1)]);

	pan0 = Select.kr(((sweep > 242.500) * (sweep < 243.520)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 242.500) * (sweep < 243.520)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 243.520) * (sweep < 243.588)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 243.520) * (sweep < 243.588)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 243.588) * (sweep < 243.684)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 243.588) * (sweep < 243.684)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 246.617) * (sweep < 246.800)), [pan0,  LFBrownNoise1.kr(5).range(0, 1)]);
	pan1 = Select.kr(((sweep > 246.617) * (sweep < 246.800)), [pan1,  LFBrownNoise1.kr(6.1).range(0, 1)]);

	pan0 = Select.kr(((sweep > 242.800) * (sweep < 247.536)), [pan0, {0.5+0.5.rand}]);
	pan1 = Select.kr(((sweep > 242.800) * (sweep < 247.536)), [pan1, {0.5.rand}]);

	pan0 = Select.kr(((sweep > 247.536) * (sweep < 248.660)), [pan0,  LFBrownNoise1.kr(1.8,  0.2).range(0, 1)]);
	pan1 = Select.kr(((sweep > 247.536) * (sweep < 248.660)), [pan1,  LFBrownNoise1.kr(1.93, 0.2).range(0, 1)]);

	pan0 = Select.kr(((sweep > 254.553) * (sweep < 254.720)), [pan0, {0.5+0.5.rand}]);
	pan1 = Select.kr(((sweep > 254.553) * (sweep < 254.720)), [pan1, {0.5.rand}]);

	pan0 = Select.kr(((sweep > 258.190) * (sweep < 258.596)), [pan0, {0.5+0.5.rand}]);
	pan1 = Select.kr(((sweep > 258.190) * (sweep < 258.596)), [pan1, {0.5.rand}]);

	pan0 = Select.kr(((sweep > 258.596) * (sweep < 259.003)), [pan0, {0.5+0.5.rand}]);
	pan1 = Select.kr(((sweep > 258.596) * (sweep < 259.003)), [pan1, {0.5.rand}]);

	pan0 = Select.kr(((sweep > 264.697) * (sweep < 265.150)), [pan0,  LFBrownNoise1.kr(2.8,  0.2).range(0, 1)]);
	pan1 = Select.kr(((sweep > 264.697) * (sweep < 265.150)), [pan1,  LFBrownNoise1.kr(2.93, 0.2).range(0, 1)]);

	pan0 = Select.kr(((sweep > 269.741) * (sweep < 272.202)), [pan0,  EnvGen.kr(Env([{1.0.rand}, {1.0.rand}], 2.461), ((sweep > 269.741) * (sweep < 272.202)))]);
	pan1 = Select.kr(((sweep > 269.741) * (sweep < 272.202)), [pan1,  EnvGen.kr(Env([{1.0.rand}, {1.0.rand}], 2.461), ((sweep > 269.741) * (sweep < 272.202)))]);

	pan0 = Select.kr(((sweep > 276.250) * (sweep < 278.248)), [pan0,  TRand.kr(0, 1, onsets[0])]);
	pan1 = Select.kr(((sweep > 276.250) * (sweep < 278.248)), [pan1,  TRand.kr(0, 1, onsets[1])]);

	pan0 = Select.kr(((sweep > 280.830) * (sweep < 282.731)), [pan0,  EnvGen.kr(Env([{1.0.rand}, {1.0.rand}], 2.461), ((sweep > 280.830) * (sweep < 282.731)))]);
	pan1 = Select.kr(((sweep > 280.830) * (sweep < 282.731)), [pan1,  EnvGen.kr(Env([{1.0.rand}, {1.0.rand}], 2.461), ((sweep > 280.830) * (sweep < 282.731)))]);

	pan0 = Select.kr(((sweep > 282.731) * (sweep < 286.257)), [pan0, {0.5+0.5.rand}]);
	pan1 = Select.kr(((sweep > 282.731) * (sweep < 286.257)), [pan1, {0.5.rand}]);

	pan0 = Select.kr(((sweep > 286.257) * (sweep < 287.750)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 286.257) * (sweep < 287.750)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 289.890) * (sweep < 290.522)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 289.890) * (sweep < 290.522)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 290.522) * (sweep < 291.154)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 290.522) * (sweep < 291.154)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 291.154) * (sweep < 291.786)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 291.154) * (sweep < 291.786)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 291.786) * (sweep < 292.418)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 291.786) * (sweep < 292.418)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 296.531) * (sweep < 299.206)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 296.531) * (sweep < 299.206)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 301.680) * (sweep < 301.945)), [pan0,  LFBrownNoise1.kr(20).range(0, 0.5)]);
	pan1 = Select.kr(((sweep > 301.680) * (sweep < 301.945)), [pan1,  LFBrownNoise1.kr(22).range(0.5, 1.0)]);

	pan0 = Select.kr(((sweep > 308.855) * (sweep < 309.879)), [pan0,  TRand.kr(0, 1, onsets[0])]);
	pan1 = Select.kr(((sweep > 308.855) * (sweep < 309.879)), [pan1,  TRand.kr(0, 1, onsets[1])]);

	pan0 = Select.kr(((sweep > 323.750) * (sweep < 324.059)), [pan0, {0.5.rand}]);
	pan1 = Select.kr(((sweep > 323.750) * (sweep < 324.059)), [pan1, {0.5+0.5.rand}]);

	pan0 = Select.kr(((sweep > 338.874) * (sweep < 338.806)), [pan0,  LFBrownNoise1.kr(0.2).range(0, 1)]);
	pan1 = Select.kr(((sweep > 338.874) * (sweep < 338.806)), [pan1,  LFBrownNoise1.kr(0.32).range(0, 1)]);

	sig0 = PanX.ar(GlobalPresets.numChannels, in[0], pan0, 1.0, 2.0);
	sig1=  PanX.ar(GlobalPresets.numChannels, in[1], pan1, 1.0, 2.0);

	sig = Mix.new([sig0, sig1]) * 0.9;

	Out.ar(out, sig);
	Out.ar(bus, sig);
}).add;
);