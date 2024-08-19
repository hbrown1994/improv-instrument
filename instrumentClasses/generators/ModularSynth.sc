ModularSynth : MIDIModule {
	var  <>controlVars, <>controller, <>containerNum, <>controllerName, <>synthVars, <>routeContainerNum, <>midiChan, <>midiNums, <>midiStateNums, <>midiRecState, <>xyNums, <>synths, <>state, <>vars, <>recordBypass, <>recState, <>panNums, <>presetSel, <>presetState, <>dummyBusesMono, <>dicerChan, <>pedalChan, <>runSynths, <>vosim0Bus, <>vosim1Bus, <>vosim0, <>vosim1, <>benjolinMidi, <>vosimMidi;

	*new {arg synthNum, midiChannel=0, lemur, container, route, dicer, pedal;
		^super.new.init(synthNum, midiChannel, lemur, container, route, dicer, pedal)
	}

	*kill {^super.new.kill}
	*save {^super.new.save}
	*load {^super.new.load}
	*run {^super.new.run}

	*makeSynth0 {^super.new.makeSynth0}
	*run0 {^super.new.run0}

	*run1 {^super.new.run1}

	*updateControllers {^super.new.updateControllers}
	*initControllers {^super.new.initControllers}
	*updateSynth {^super.new.updateSynth}

	kill{ //Free the synth if it's playing
		if(synth.isPlaying, {synth.free});
	}

	save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum
		GlobalPresets.controls.add(synthIndex.asSymbol -> vars);
	}

	load { //save the parameter dictionary "vars" to the global "controls" dictionary
		vars=GlobalPresets.controls[synthIndex.asSymbol];
		this.updateControllers;
		this.updateSynth;
	}

	init {arg synthNum, midiChannel=0, lemur, container, route, dicer, pedal;
		midiChan = midiChannel;
		synthIndex = synthNum;
		controller = lemur;
		containerNum = container;
		routeContainerNum = route;
		dicerChan = dicer;
		pedalChan = pedal;
		rec = Recorder(Server.local);
		vosim0Bus = Bus.audio(Server.local, 2);
		vosim1Bus = Bus.audio(Server.local, 2);
		benjolinMidi = List.new;
		vosimMidi = List.new;

		vars = Dictionary.newFrom([
			\amp, 0.5, \busAmp, 0.5, \hardwareOut, 0, \hardwareOutMon, 0, \out, 0, \ampRoute, 0, \synthSel, 0, \gate, 1, \pedal, 0,

			\benjolin, Dictionary.newFrom([
				\0, Dictionary.newFrom([\freq1, 13080.exprand(20), \freq2, 14000.0.exprand(0.1), \rungler1, 1.0.rand, \rungler2, 1.0.rand, \scale, 1.0.rand, \loop, 1.0.rand, \filtFreq, 19080.exprand(20), \q, 1.0.rand, \outSignal, 7.rand, \filterType, 4.rand]),
				\1, Dictionary.newFrom([\freq1, 13080.exprand(20), \freq2, 14000.0.exprand(0.1), \rungler1, 1.0.rand, \rungler2, 1.0.rand, \scale, 1.0.rand, \loop, 1.0.rand, \filtFreq, 19080.exprand(20), \q, 1.0.rand, \outSignal, 7.rand, \filterType, 4.rand])
			]),
			\vosim,
			Dictionary.newFrom([
				\top,
				Dictionary.newFrom([
					\0,
					Dictionary.newFrom([
						\trigFreq, 0.1.rrand(2000), \freq, 100.rrand(10000), \numCycles, 1.rrand(200), \decay, 0.3.rrand(1.04),
						\trigSel, 0, \freqSel, 0, \decaySel, 0, \state, 0]),
					\1,
					Dictionary.newFrom([\trigFreq, 0.1.rrand(2000), \freq, 100.rrand(10000), \numCycles, 1.rrand(200), \decay, 0.3.rrand(1.04),
						\trigSel, 0, \freqSel, 0, \decaySel, 0, \state, 0])
				]),
				\bottom, Dictionary.newFrom([
					\trigPres, 0.001, \freqLow, 0.1, \freqHigh, 40, \decay, 0.4, \numCycles, 20, \state, 0
				]);
			]);
		]);


		synthVars = [\amp, \busAmp];

		controlVars = [\ampRoute, \amp, \hardwareOut, \hardwareOutMon];

		presetSel = \0;
		presetState=0;

		dummyBusesMono = Bus.audio(Server.local)!4;
	}

	initControllers {
		controlVars.do{|i, index|
			var names = ["modularAmpRoute", "modularAmp", "modularHardwareOut", "modularHardwareOutMon"];
			controller.sendMsg(["/container"++containerNum+/+names[index]+/+"x"]++[vars[i]].flatten)
		};
	}

	updateControllers {
		controlVars.do{|i, index|
			var names = ["modularAmpRoute", "modularAmp", "modularHardwareOut", "modularHardwareOutMon"];
			controller.sendMsg(["/container"++containerNum+/+names[index]+/+"x"]++[GlobalPresets.controls[synthIndex.asSymbol][i]].flatten)
		};
	}

	updateSynth {
		if(vars[\gate]==1,
			{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[vars[\out]]); synthVars.do{|i| synth.set(i, vars[i])}}, {this.makeSynth0})},
			{if(synth.isPlaying, {synth.set(\gate, 0)})};
		);
	}

	makeSynth0 {
		vosimMidi.do{|i| i.free};

		if(synth.isPlaying, {nil}, {
			synth = Synth(\benjolin,
				[
					\gate, 1,
					\freq1, vars[\benjolin][presetSel][\freq1], \freq2, vars[\benjolin][presetSel][\freq2],
					\rungler1, vars[\benjolin][presetSel][\rungler1], \rungler2, vars[\benjolin][presetSel][\rungler2],
					\scale, vars[\benjolin][presetSel][\scale], \loop, vars[\benjolin][presetSel][\loop], \filtFreq, vars[\benjolin][presetSel][\filtFreq],
					\q, vars[\benjolin][presetSel][\q], \outSignal, vars[\benjolin][presetSel][\outSignal], \filterType, vars[\benjolin][presetSel][\filterType],
					\inBus1, dummyBusesMono[0], 	\inBus2, dummyBusesMono[1], 	\inBus3, dummyBusesMono[2], 	\inBus4, dummyBusesMono[3],
					\amp, vars['amp'] , \busAmp, vars['busAmp'], \pedal, vars['pedal'],
					\out, GlobalBusses.outputSynthBus[vars[\out]],
					\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
					\monoBus, GlobalBusses.allOut[\1][synthIndex],
			], GlobalNodes.nodes[synthIndex]).register;
		});
	}

	makeSynth1 {
		benjolinMidi.do{|i| i.free};

		if(synth.isPlaying, {nil}, {
			synth = Synth(\vosimMix,
				[
					\gate, 1,
					\vosim0, vosim0Bus, \vosim1, vosim1Bus,
					\amp, vars['amp'] , \busAmp, vars['busAmp'], \pedal, vars['pedal'],
					\out, GlobalBusses.outputSynthBus[vars[\out]],
					\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
					\monoBus, GlobalBusses.allOut[\1][synthIndex],
			], GlobalNodes.nodes[synthIndex], 'addToTail').register;
		});

		if(vosim0.isPlaying, {nil}, {
			vosim0 = Synth(\vosim0, [
				\out, vosim0Bus,
				\trigFreq, vars[\vosim][\top][presetSel][\trigFreq], \freq, vars[\vosim][\top][presetSel][\freq],
				\numCycles, vars[\vosim][\top][presetSel][\numCycles], \decay, vars[\vosim][\top][presetSel][\decay],
				\trigSel, vars[\vosim][\top][presetSel][\trigSel], \freqSel, vars[\vosim][\top][presetSel][\freqSel],
				\decaySel, vars[\vosim][\top][presetSel][\decaySel], \state, vars[\vosim][\top][presetSel][\state]
			], GlobalNodes.nodes[synthIndex], 'addToHead').register;
		});

		if(vosim1.isPlaying, {nil}, {
			vosim1 = Synth(\vosim1, [
				\out, vosim1Bus,
				\inBus1, dummyBusesMono[0], 	\inBus2, dummyBusesMono[1], 	\inBus3, dummyBusesMono[2], 	\inBus4, dummyBusesMono[3],
				\trigPres, vars[\vosim][\bottom][\trigPres], \freqLow, vars[\vosim][\bottom][\freqLow],
				\freqHigh, vars[\vosim][\bottom][\freqHigh], \decay, vars[\vosim][\bottom][\decay],
				\numCycles, vars[\vosim][\bottom][\numCycles], \state, vars[\vosim][\bottom][\state]
			], GlobalNodes.nodes[synthIndex], 'addToHead').register
		});
	}

	run0 {
		this.makeSynth0;

		[[54, \freq1, 20.0, 14000.0],
			[58, \freq2, 0.1, 14000.0],
			[62, \rungler1, 0.0, 1.0],
			[66, \rungler2, 0.0, 1.0],
			[73, \scale, 0.0, 1.0],
			[75, \loop, 0.0, 1.0],
			[77, \filtFreq, 20.0, 20000.0],
			[79, \q, 0.0, 1.0]].do{|param, index|

			benjolinMidi.add(
				MIDIFunc.cc({arg msg;
					if(param[2]>0.0,
						{
							if(synth.isPlaying, {synth.set(param[1], msg.linexp(0, 127, param[2], param[3]))});
							if(presetState==0, {vars[\benjolin][presetSel][param[1]] = msg.linexp(0, 127, param[2], param[3])});
						},
						{
							if(synth.isPlaying, {synth.set(param[1], msg.linlin(0, 127, param[2], param[3]))});
							if(presetState==0, {vars[\benjolin][presetSel][param[1]] = msg.linlin(0, 127, param[2], param[3])});
						}
					);
				}, param[0], midiChan);
			);
		};

		[48, 49, 50].do{|midiNum, index|
			benjolinMidi.add(
				MIDIFunc.noteOn({
					if(synth.isPlaying, {synth.set(\outSignal, [1, 3, 5][index])});
					if(presetState==0, {vars[\benjolin][presetSel][\outSignal]  = [1, 3, 5][index]});
				}, midiNum, midiChan);
			);
		};

		[48, 49, 50].do{|midiNum, index|
			benjolinMidi.add(
				MIDIFunc.noteOff({
					if(synth.isPlaying, {synth.set(\outSignal, [0, 2, 4][index])});
					if(presetState==0, {vars[\benjolin][presetSel][\outSignal]  = [0, 2, 4][index]});
				}, midiNum, midiChan);
			);
		};

		[51, 55].do{|midiNum, index|
			benjolinMidi.add(
				MIDIFunc.noteOn({
					if(synth.isPlaying, {synth.set(\filterType, [1, 3][index])});
					if(presetState==0, {vars[\benjolin][presetSel][\filterType]  = [1, 3][index]});
				}, midiNum, midiChan);
			);
		};

		[51, 55].do{|midiNum, index|
			benjolinMidi.add(
				MIDIFunc.noteOff({
					if(synth.isPlaying, {synth.set(\filterType, [0, 2][index])});
					if(presetState==0, {vars[\benjolin][presetSel][\filterType]  = [0, 2][index]});
				}, midiNum, midiChan);
			);
		};

		//PresetSel + Save --------------------------------------------------------
		benjolinMidi.add(
			MIDIFunc.noteOn({
				presetSel = \1;
			}, 59, midiChan);
		);

		benjolinMidi.add(
			MIDIFunc.noteOff({
				presetSel = \0;
			}, 59, midiChan);
		);

		benjolinMidi.add(
			MIDIFunc.noteOn({
				presetState = 1;
			}, 63, midiChan);
		);

		benjolinMidi.add(
			MIDIFunc.noteOff({
				presetState = 0;
			}, 63, midiChan);
		);

		//CV sigs
		[[52, \cvFreq1, \inBus1, \fbSel1], [56, \cvFreq2, \inBus2, \fbSel2], [60, \cvRungler1, \inBus3, \fbSel3], [64, \cvRungler2, \inBus4, \fbSel4]].do{|i|
			benjolinMidi.add(
				MIDIFunc.noteOn({
					synth.set(i[1], 1);
					if(synth.isPlaying, {synth.set(i[2], GlobalBusses.allOut[\1][~busAssign], i[3], if(~busAssign>synthIndex, {1}, {0}))});
				}, i[0], midiChan);
			);

			benjolinMidi.add(
				MIDIFunc.noteOff({
					if(synth.isPlaying, {synth.set(i[1], 0)});
				}, i[0], midiChan);
			);
		};
	}

	run1 {
		this.makeSynth1;
		(
			//VOSIM 0
			[
				[72, \trigPres, 0.001, 500],
				[73, \freqLow, 0.1, 50],
				[75, \freqHigh, 40, 9000],
				[77, \decay, 0.4, 2.5],
				[79, \numCycles, 20, 600]
			].do{|i|
				vosimMidi.add(
					MIDIFunc.cc({arg msg;
						vars[\vosim][\bottom][i[1]]=msg.linexp(0, 127, i[2], i[3]);
						vosim0.set(i[1], msg);
					}, i[0], midiChan);
				);
			};

			//state
			vosimMidi.add(
				MIDIFunc.noteOff({
					vosim0.set(\state, 0);
					vars[\vosim][\bottom][\state]=0;
				}, 48, midiChan);
			);

			vosimMidi.add(
				MIDIFunc.noteOn({
					vosim0.set(\state, 1);
					vars[\vosim][\bottom][\state]=1;
				}, 48, midiChan);
			);
		);

		(
			//VOSIM1
			[
				[54, \trigFreq, 0.1, 3000],
				[58, \freq, 100, 10000],
				[62, \numCycles, 1, 200],
				[66, \decay, 0.3, 1.04]
			].do{|i|
				vosimMidi.add(
					MIDIFunc.cc({arg msg;
						var data = msg.linexp(0, 127, i[2], i[3]).poll;
						vosim1.set(i[1], data);
						vars[\vosim][\top][presetSel][i[1]] = data;
					}, i[0], midiChan);
				);
			};

			[
				[53, \trigFreqPres, 0.1, 1000],
				[57, \trigFreqPresAlt, 0.1, 3000],
			].do{|i|
				vosimMidi.add(
					MIDIFunc.cc({arg msg;
						var data = msg.linexp(0, 127, i[2], i[3]);
						vosim1.set(i[1], data);
						vars[\vosim][\top][presetSel][i[1]] = data;
					}, i[0], midiChan);
				);
			};

			//state
			vosimMidi.add(
				MIDIFunc.noteOn({
					vosim1.set(\state, 1);
					vars[\vosim][\top][presetSel][\state] = 1;
				}, 49, midiChan);
			);

			vosimMidi.add(
				MIDIFunc.noteOff({
					vosim1.set(\state, 0);
					vars[\vosim][\top][presetSel][\state] = 0;
				}, 49, midiChan);
			);

			[[\trigSel, 51], [\freqSel, 55], [\decaySel, 63]].do{|i|
				vosimMidi.add(
					MIDIFunc.noteOn({
						vosim1.set(i[0], 1);
						vars[\vosim][\top][presetSel][i[0]] = 1;
					}, i[1], midiChan);
				);

				vosimMidi.add(
					MIDIFunc.noteOff({
						vosim1.set(i[0], 0);
						vars[\vosim][\top][presetSel][i[0]] = 0;
					}, i[1], midiChan);
				);
			};

			vosimMidi.add(
				MIDIFunc.noteOn({
					presetSel = \1;
				}, 59, midiChan);
			);

			vosimMidi.add(
				MIDIFunc.noteOff({
					presetSel = \0;
				}, 59, midiChan);
			);

			vosimMidi.add(
				MIDIFunc.noteOn({
					presetState = 1;
				}, 50, midiChan);
			);

			vosimMidi.add(
				MIDIFunc.noteOff({
					presetState = 0;
				}, 50, midiChan);
			);

			[[52, \cv1, \inBus1, \fbSel1], [56, \cv2, \inBus2, \fbSel2], [60, \cv3, \inBus3, \fbSel3], [64, \cv4, \inBus4, \fbSel4]].do{|i|
				vosimMidi.add(
					MIDIFunc.noteOn({
						vosim1.set(i[1], 1);
						if(vosim1.isPlaying, {vosim1.set(i[2], GlobalBusses.allOut[\1][~busAssign], i[3], if(~busAssign>synthIndex, {1}, {0}))});
					}, i[0], midiChan);
				);

				vosimMidi.add(
					MIDIFunc.noteOff({
						if(vosim1.isPlaying, {vosim1.set(i[1], 0)});
					}, i[0], midiChan);
				);
			};
		);
	}

	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		var midiData = [
			[60, dicerChan[0]], [61, dicerChan[0]], [62, dicerChan[0]], [63, dicerChan[0]], [64, dicerChan[0]],
			[60, dicerChan[1]], [61, dicerChan[1]], [62, dicerChan[1]], [63, dicerChan[1]], [64, dicerChan[1]],
			[60, dicerChan[2]], [61, dicerChan[2]], [62, dicerChan[2]], [63, dicerChan[2]], [64, dicerChan[2]]
		];
		this.save;
		this.initControllers;

		controller.addResponder("/container"++containerNum++"/modularAmpRoute/x", {arg msg;
			vars['ampRoute']=msg[0];
			if(msg[0]==1.0,
				{controller.sendMsg(["/container"++containerNum++"/modularAmp/x"]++[vars['busAmp']])},
				{controller.sendMsg(["/container"++containerNum++"/modularAmp/x"]++[vars['amp']])}
			);
		});

		controller.addResponder("/container"++containerNum++"/modularRec/x", {arg msg;
			this.record(msg[0]);
		});

		/*__________ AMP SLIDER ___________________________________*/
		controller.addResponder("/container"++containerNum++"/modularAmp/x", {arg msg;
			if(vars['ampRoute']==1.0,
				{if(synth.isPlaying, {synth.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(synth.isPlaying, {synth.set(\amp, msg[0])}); vars['amp'] = msg[0]}
			);

			if(vars['ampRoute']==1.0,
				{if(synth.isPlaying, {synth.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(synth.isPlaying, {synth.set(\amp, msg[0])}); vars['amp'] = msg[0]}
			);
		});

		/*__________ ROUTING ___________________________________*/
		controller.addResponder("/container"++routeContainerNum++"/busAssign"++synthIndex++"/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		controller.sendMsg(["/container"++containerNum++"/synthNum8/x"]++[synthIndex]);

		controller.addResponder("/container"++containerNum++"/modularHardwareOut/x", {arg msg;
			vars['hardwareOutMon']=(msg[0]*8-1);
			vars['hardwareOut']=msg[0];

			controller.sendMsg(["/container"++containerNum++"/modularHardwareOutMon/x"]++[(msg[0]*8-1)]);
			if((msg[0]*8-1)>(-1), {vars['out'] = (msg[0]*8-1).asInteger}, {vars['out']=dummyBus});

			if((msg[0]*8-1)>(-1),
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})}
			);
		});

		//PEDAL
		MIDIFunc.cc({arg msg;
			synth.set(\pedal, msg.linlin(0, 127, 0.0, 1.0));
			vars['pedal'] = msg.linlin(0, 127, 0.0, 1.0);
		}, 83, midiChan);

		//STATE GLOBAL: OFF
		midiData.do{|i|
			MIDIFunc.noteOn({
				if(synth.isPlaying, {synth.set(\gate, 0)});

				[vosim0, vosim1].do{|i|
					if(i.isPlaying, {i.set(\gate, 0)});
				};
			}, i[0], i[1]);
		};

		//STATE SWITCH: 0
		MIDIFunc.noteOff({
			this.run0;
		}, midiData[0][0], midiData[0][1]);

		//STATE SWITCH: 1
		MIDIFunc.noteOff({
			this.run1;
		}, midiData[1][0], midiData[1][1]);

	}
}