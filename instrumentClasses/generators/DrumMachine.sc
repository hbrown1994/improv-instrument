DrumMachine : MIDIModule {
	var  <>controlVars, <>controller, <>containerNum, <>controllerName, <>synthVars, <>routeContainerNum, <>midiChan, <>midiNums, <>midiStateNums, <>midiRecState, <>xyNums, <>synths, <>drumRecSynthTop, <>drumRecSynthBottom, <>drumRecBusTop, <>drumRecBusBottom, <>state, <>recordBypass, <>recState, <>panNums, <>monitorButtons;

	*new {arg synthNum, midiChannel=0, lemur, container, route;
		^super.new.init(synthNum, midiChannel, lemur, container, route)
	}

	*kill {^super.new.kill}
	*save {^super.new.save}
	*load {^super.new.load}
	*run {^super.new.run}
	*makeSynth {^super.new.makeSynth}
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

	init {arg synthNum, midiChannel=0, lemur, container, route;
		midiChan = midiChannel;
		synthIndex = synthNum;
		controller = lemur;
		containerNum = container;
		routeContainerNum = route;
		rec = Recorder(Server.local);
		monitorButtons = List.new;

		vars = Dictionary.newFrom([\hardwareOut, 0, \hardwareOutMon, 0, \out, 0, \amp, 1, \busAmp, 1, \ampRoute, 0]);

		synthVars = [\amp, \busAmp];

		controlVars = [\ampRoute, \amp, \hardwareOut, \hardwareOutMon];

		panNums = Dictionary.newFrom([
			"000_808-1", 0,
			"001_808-0", 0,
			"004_snare-1", -0.1,
			"005_snare-0", 0.1,
			"006_kick", 0,
			"008_openHat-1", -0.3,
			"011_openhat-0", 0.3,
			"010_clap", 0,
			"009_cowbell", 0.2,
			"012_conga", 0.4,
			"013_clave", 0.5,
			"014_rim", -0.7,
			"015_closedHat", 0.5,
		]);

		midiNums = Array.series(13, 52);
		midiStateNums = [66, 67];
		midiRecState = 65;
		xyNums = [0, 1];
		synths = Array.newClear(16);
		drumRecBusTop = Bus.audio(Server.local, 2);
		drumRecBusBottom = Bus.audio(Server.local, 2);
		state=0;
		recordBypass=0;
		recState=0;
	}

	initControllers {
		controlVars.do{|i, index|
			var names = ["drumsAmpRoute", "drumsAmp", "drumsHardwareOut", "drumsHardwareOutMon"];
			controller.sendMsg(["/container"++containerNum+/+names[index]+/+"x"]++[vars[i]].flatten)
		};

	}

	updateControllers {
		controlVars.do{|i, index|
			var names = ["drumsAmpRoute", "drumsAmp", "drumsHardwareOut", "drumsHardwareOutMon"];
			controller.sendMsg(["/container"++containerNum+/+names[index]+/+"x"]++[GlobalPresets.controls[synthIndex.asSymbol][i]].flatten)
		};
	}

	updateSynth {
		if(vars[\gate]==1,
			{if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\out, GlobalBusses.outputSynthBus[vars[\out]]); synthVars.do{|i| synth.set(i, vars[i])}}, {this.makeSynth})},
			{if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\gate, 0)})};
		);

		if(vars[\gate]==1,
			{if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\out, GlobalBusses.outputSynthBus[vars[\out]]); synthVars.do{|i| synth.set(i, vars[i])}}, {this.makeSynth})},
			{if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\gate, 0)})};
		);
	}

	makeSynth {
		if(drumRecSynthTop.isPlaying, {nil}, {
			drumRecSynthTop = Synth(\drumRec,
				[
					\amp, vars[\amp], \busAmp, vars[\busAmp], \in, drumRecBusTop,
					\monoBus, GlobalBusses.allOut[\1][synthIndex],
					\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
					\out, GlobalBusses.outputSynthBus[vars[\out]]
				],
				GlobalNodes.nodes[synthIndex], 'addToTail').register;
			drumRecSynthBottom = Synth(\drumRec,
				[
					\amp, vars[\amp], \busAmp, vars[\busAmp], \in, drumRecBusBottom,
					\monoBus, GlobalBusses.allOut[\1][synthIndex],
					\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
					\out, GlobalBusses.outputSynthBus[vars[\out]]
				],
				GlobalNodes.nodes[synthIndex], 'addToTail').register;
			rec = Recorder(Server.local); //make recorders for each output channel strip
		});
	}

	run {var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		this.save;
		this.initControllers;
		this.makeSynth;

		controller.addResponder("/container"++containerNum++"/drumsAmpRoute/x", {arg msg;
			vars['ampRoute']=msg[0];
			if(msg[0]==1.0, {controller.sendMsg(["/container"++containerNum++"/drumsAmp/x"]++[vars['busAmp']])}, {controller.sendMsg(["/container"++containerNum++"/drumsAmp/x"]++[vars['amp']])})
		});

		controller.addResponder("/container"++containerNum++"/drumsRec/x", {arg msg;
			this.record(msg[0]);
		});

		/*__________ AMP SLIDER ___________________________________*/
		controller.addResponder("/container"++containerNum++"/drumsAmp/x", {arg msg;
			if(vars['ampRoute']==1.0,
				{if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\amp, msg[0])}); vars['amp'] = msg[0]}
			);

			if(vars['ampRoute']==1.0,
				{if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\amp, msg[0])}); vars['amp'] = msg[0]}
			);
		});

		/*__________ ROUTING ___________________________________*/
		controller.addResponder("/container"++routeContainerNum++"/busAssign"++synthIndex++"/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		controller.sendMsg(["/container"++containerNum++"/synthNum8/x"]++[synthIndex]);

		controller.addResponder("/container"++containerNum++"/drumsHardwareOut/x", {arg msg;
			vars['hardwareOutMon']=(msg[0]*8-1);
			vars['hardwareOut']=msg[0];

			controller.sendMsg(["/container"++containerNum++"/drumsHardwareOutMon/x"]++[(msg[0]*8-1)]);
			if((msg[0]*8-1)>(-1), {vars['out'] = (msg[0]*8-1).asInteger}, {vars['out']=dummyBus});

			if((msg[0]*8-1)>(-1),
				{if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\out, dummyBus)})}
			);

			if((msg[0]*8-1)>(-1),
				{if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\out, dummyBus)})}
			);
		});

		//MIDI
		midiNums.do{|midiNum, index|
			if(index>1, {
				MIDIFunc.noteOn({
					if(index%2==0, {drumRecSynthTop.set(\t_trig, 1)}, {drumRecSynthBottom.set(\t_trig, 1)});
					if(synths[index].isPlaying,
						{synths[index].set(\t_trig, 1)},
						{
							synths[index] =Synth(\oneShot, [
								\amp, vars['amp'], \busAmp, vars['busAmp'],
								\buf, GlobalData.audioBuffers[\drums][index],
								\t_trig, 1, \pan, panNums[GlobalData.audioBufferNames[\drums][index]],
								\out, GlobalBusses.outputSynthBus[vars[\out]],
								\monoBus, GlobalBusses.allOut[\1][synthIndex],
								\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
								\loopBus, if(index%2==0, {drumRecBusTop}, {drumRecBusBottom})],
							GlobalNodes.nodes[synthIndex], 'addToHead').register;
						}
					);
				}, midiNum, midiChan);
			},
			{

				MIDIFunc.noteOn({
					if(index%2==0, {drumRecSynthTop.set(\t_trig, 1)}, {drumRecSynthBottom.set(\t_trig, 1)});
					if(synths[index].isPlaying,
						{synths[index].set(\t_trig, 1)},
						{synths[index] = Synth(\loop,
							[
								\amp, vars['amp'], \busAmp, vars['busAmp'],
								\buf, GlobalData.audioBuffers[\drums][index],
								\gate, 1, \pan, panNums[GlobalData.audioBufferNames[\drums][index]],
								\out, GlobalBusses.outputSynthBus[vars[\out]],
								\monoBus, GlobalBusses.allOut[\1][synthIndex],
								\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
								\loopBus, if(index%2==0, {drumRecBusTop}, {drumRecBusBottom})],
							GlobalNodes.nodes[synthIndex], 'addToHead').register;
						}
					);
				}, midiNum, midiChan);

				MIDIFunc.noteOff({
					if(synths[index].isPlaying, {synths[index].set(\gate, 0)});
				}, midiNum, midiChan);
			});
		};

		xyNums.do{|midiNum, index|
			MIDIFunc.cc({arg msg;
				if(index==0, {drumRecSynthTop.set(\size, msg)}, {drumRecSynthBottom.set(\size, msg)})
			}, midiNum, midiChan);
		};

		MIDIFunc.cc({arg msg;
			drumRecSynthTop.set(\toggle, msg);
			drumRecSynthBottom.set(\toggle, msg);
		}, 2, midiChan);

		MIDIFunc.noteOn({arg msg;
			state = 1;
			{monitorButtons[2].value=1}.defer;
			2.do{|index|
				if(synths[index].isPlaying, {synths[index].set(\gate, 0)});
			};

			if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\state, 1)});
			if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\state, 1)});
		}, midiStateNums[0], midiChan);

		MIDIFunc.noteOff({arg msg;
			state = 0;
			{monitorButtons[2].value=0}.defer;
			2.do{|index|
				if(synths[index].isPlaying, {synths[index].set(\gate, 0)});
			};

			if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\state, 0)});
			if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\state, 0)});
		}, midiStateNums[0], midiChan);

		MIDIFunc.noteOn({arg msg;
			recordBypass = 1;
			{monitorButtons[1].value=1}.defer;
			if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\recordBypass, 1)});
			if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\recordBypass, 1)});

		}, midiStateNums[1], midiChan);

		MIDIFunc.noteOff({arg msg;
			recordBypass = 0;
			{monitorButtons[1].value=0}.defer;
			if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\recordBypass, 0)});
			if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\recordBypass, 0)});

		}, midiStateNums[1], midiChan);

		MIDIFunc.noteOn({arg msg;
			recState = 1;
			{monitorButtons[0].value=1}.defer;
			if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\recState, 1)});
			if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\recState, 1)});
		}, midiRecState, midiChan);

		MIDIFunc.noteOff({arg msg;
			recState = 0;
			{monitorButtons[0].value=0}.defer;
			if(drumRecSynthTop.isPlaying, {drumRecSynthTop.set(\recState, 0)});
			if(drumRecSynthBottom.isPlaying, {drumRecSynthBottom.set(\recState, 0)});
		}, midiRecState, midiChan);

		(
			{
				var window;

				window = Window.new("Drum Monitor", Rect(0, 388, 400, 400)).front;
				GlobalSynths.guis.add(window);

				3.do{|i|
					monitorButtons.add(
						Button(window, Rect([0, 200, 200][i], [0, 0, 200][i], 200, 200))
						.font_(Font("Geeza Pro", 48))
						.states_([[["Latch", "Bypass", "Rhythm"][i], Color.black, [Color.white, Color.green, Color.white][i]], [["Hold", "Thru", "Pitch"][i], Color.black, [Color.red, Color.white, Color.green][i]]]);
					);
				};
			}.value;
		);
	}
}