Playback : MIDIModule {
	var <>controller, <>controlVars, <>containerNum, <>controllerName, <>synthVars, <>routeContainerNum, <>midiChan, <>pedalAmp;

	*new {arg synthNum, midiChannel=0, lemur, container, route;
		^super.new.init(synthNum, midiChannel, lemur, container, route)
	}

	*kill {^super.new.kill}
	*save {^super.new.save}
	*load {^super.new.load}
	*run {^super.new.run}
	*makeSynth {^super.new.makeSynth}
	*updateSynth {^super.new.updateSynth}
	*updateControllers {^super.new.updateControllers}
	*initControllers {^super.new.initControllers}

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
		pedalAmp=0;

		vars = Dictionary.newFrom([\hardwareOut, 0, \hardwareOutMon, 0, \out, 0, \amp, 1, \busAmp, 1, \ampRoute, 0]);

		synthVars = [\amp, \busAmp];

		controlVars = [\ampRoute, \amp, \hardwareOut, \hardwareOutMon];
	}

	initControllers {
		controlVars.do{|i, index|
			var names = ["playbackAmpRoute", "playbackAmp", "playbackHardwareOut", "playbackHardwareOutMon"];
			controller.sendMsg(["/container"++containerNum+/+names[index]+/+"x"]++[vars[i]].flatten)
		};
	}

	updateControllers {
		controlVars.do{|i, index|
			var names = ["playbackAmpRoute", "playbackAmp", "playbackHardwareOut", "playbackHardwareOutMon"];
			controller.sendMsg(["/container"++containerNum+/+names[index]+/+"x"]++[GlobalPresets.controls[synthIndex.asSymbol][i]].flatten)
		};
	}

	updateSynth {
		if(synth.isPlaying,
			{
				synth.set(\out, GlobalBusses.outputSynthBus[vars[\out]]);
				synthVars.do{|i| synth.set(i, vars[i])};
			},
			{this.makeSynth}
		);
	}


	makeSynth {
		if(synth.isPlaying, {synth.set(\t_trig, 1)},
			{
				synth = Synth(\playback,
					[
						\gate, 1, \buf, GlobalData.audioBuffers[\EM][0], \pedalAmp, pedalAmp,
						\amp, vars['amp'] , \busAmp, vars['busAmp'],
						\out, GlobalBusses.outputSynthBus[vars[\out]],
						\monoBus, GlobalBusses.allOut[\1][synthIndex],
						\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
				], GlobalNodes.nodes[synthIndex]).register

		});
	}


	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		var midiChannels = [10, 11, 12];
		var midiNums = Array.series(5, 60);
		var pedalMIDIchan = 15;
		var pedalMIDINum = 11;

		this.save;
		this.initControllers;
		this.makeSynth;

		//OSC
		controller.addResponder("/container"++containerNum++"/playbackAmpRoute/x", {arg msg;
			vars['ampRoute']=msg[0];
			if(msg[0]==1.0, {controller.sendMsg(["/container"++containerNum++"/playbackAmp/x"]++[vars['busAmp']])}, {controller.sendMsg(["/container"++containerNum++"/playbackAmp/x"]++[vars['amp']])})
		});

		controller.addResponder("/container"++containerNum++"/playbackRec/x", {arg msg;
			this.record(msg[0]);
		});

		/*__________ AMP SLIDER ___________________________________*/
		controller.addResponder("/container"++containerNum++"/playbackAmp/x", {arg msg;
			if(vars['ampRoute']==1.0,
				{if(synth.isPlaying, {synth.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(synth.isPlaying, {synth.set(\amp, msg[0])}); vars['amp'] = msg[0]}
			);
		});

		/*__________ ROUTING ___________________________________*/
		controller.addResponder("/container"++routeContainerNum++"/busAssign"++synthIndex++"/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		controller.addResponder("/container"++containerNum++"/playbackHardwareOut/x", {arg msg;
			vars['hardwareOutMon']=(msg[0]*8-1);
			vars['hardwareOut']=msg[0];

			controller.sendMsg(["/container"++containerNum++"/playbackHardwareOutMon/x"]++[(msg[0]*8-1)]);

			if((msg[0]*8-1)>(-1), {vars['out'] = (msg[0]*8-1).asInteger}, {vars['out']=dummyBus});

			if((msg[0]*8-1)>(-1),
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})}
			);
		});

		//MIDI
		MIDIFunc.cc({arg msg;
			pedalAmp = msg.linlin(0, 127, 0.0, 1.0);
			if(synth.isPlaying, {synth.set(\pedalAmp, msg.linlin(0, 127, 0.0, 1.0))});
		}, pedalMIDINum, pedalMIDIchan);

		GlobalData.audioBuffers[\EM].do{|buf, index|
			MIDIFunc.noteOn({
				if(synth.isPlaying, {synth.set(\buf, buf,  \rateLow, 1,  \rateHigh, 1, \t_trig, 1, \cut, 1)})
			}, midiNums[index], midiChannels[0])
		};

		GlobalData.audioBuffers[\glitchStretch].do{|buf, index|
			MIDIFunc.noteOn({
				if(synth.isPlaying, {synth.set(\buf, buf, \rateLow, 0.1,  \rateHigh, 4.0, \t_trig, 1, \cut, 1)})
			}, midiNums[index], midiChannels[1])
		};

		GlobalData.audioBuffers[\glitchPercs].do{|buf, index|
			MIDIFunc.noteOn({
				if(synth.isPlaying, {synth.set(\buf, buf, \rateLow, 0.9,  \rateHigh, 8.0, \t_trig, 1, \cut, 1)})
			}, midiNums[index], midiChannels[2]);

			MIDIFunc.noteOff({
				if(synth.isPlaying, {synth.set(\cut, 0)});
			}, midiNums[index], midiChannels[2])
		};
	}
}

