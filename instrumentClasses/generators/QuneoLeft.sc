QuneoLeft : OSCModule {
	var   <>controller, <>turntable, <>turntableVars, <>synthIndex, <>routeContainer, <>midiChan, <>turntableBus, <>noiseBus, <>popBus;
	var <>buf0, <>buf1, <>buf2;
	var <>hold = 0;
	var<> pop0=nil, <>pop1=nil, <>pop2=nil;
	var<> midiNotes =#[80, 81, 84];
	var <>turntableAudioSel = 0;

	*new {arg synthNum, lemur, container, quneoChan, route; ^super.new.init(synthNum, lemur, container, quneoChan, route)}
	*makeTurntable  {^super.new.makeTurntable }
	*makeSynth  {^super.new.makeSynth }
	*updateSynth {^super.new.updateSynth}

	init {arg synthNum, lemur, container, quneoChan, route;
		synthIndex = synthNum;
		containerNum = container;
		controller = lemur;
		midiChan = quneoChan;
		routeContainer = route;
		rec = Recorder(Server.local);

		turntableBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		noiseBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		popBus = Bus.audio(Server.local, GlobalPresets.numChannels);

		vars = Dictionary.newFrom([
			\quneoLeftHardwareOut, 0,
			\quneoLeftHardwareOutMon, 0,
			\out, 0,
			\quneoLeftAmp, 1,
			\quneoLeftBusAmp, 1,
			\quneoLeftAmpRoute, 0,
			\gate, 1]);

		synthVars = [\quneoLeftAmp, \quneoLeftBusAmp];

		controlVars = [\quneoLeftAmpRoute, \quneoLeftAmp, \quneoLeftHardwareOut, \quneoLeftHardwareOutMon];
	}


	updateSynth {
		if(vars[\gate]==1,
			{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[vars[\out]]); synthVars.do{|i| synth.set(i, vars[i])}}, {this.makeSynth})},
			{if(synth.isPlaying, {synth.set(\gate, 0)})};
		);
	}

	makeSynth {
		if(synth.isPlaying, {nil}, {
			synth = Synth(\quneoLeft,
				[
					\gate, 1,
					\turntableBus, turntableBus, \noiseBus, noiseBus, \popBus, popBus,
					\quneoLeftAmp, vars[\quneoLeftAmp], \quneoLeftBusAmp, vars[\quneoLeftBusAmp],
					\monoBus, GlobalBusses.allOut[\1][synthIndex],
					\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
					\out, GlobalBusses.outputSynthBus[vars[\out]],
			], GlobalNodes.nodes[synthIndex], 'addToTail').register;
		});
	}

	makeTurntable {
		if(turntable.isPlaying, {nil}, {
			turntable = Synth(\turntable,
				[
					\buf0, GlobalData.audioBuffers[\vox][0], \buf1, GlobalData.audioBuffers[\vox][1],
					\out, turntableBus
			], GlobalNodes.nodes[synthIndex], 'addToHead').run.register;
		});
	}

	run {var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		this.save;
		this.initLemur;
		this.makeTurntable;
		this.makeSynth;


		controller.addResponder("/container"++containerNum++"/quneoLeftRec/x", {arg msg;
			this.record(msg[0]);
		});

		controller.addResponder("/container"++containerNum++"/quneoLeftAmpRoute/x", {arg msg;
			vars['quneoLeftAmpRoute']=msg[0];

			if(msg[0]==1.0,
				{controller.sendMsg(["/container"++containerNum++"/quneoLeftAmp/x"]++[vars['quneoLeftBusAmp']])},
				{controller.sendMsg(["/container"++containerNum++"/quneoLeftAmp/x"]++[vars['quneoLeftAmp']])})});

		/*__________ AMP SLIDER ___________________________________*/
		controller.addResponder("/container"++containerNum++"/quneoLeftAmp/x", {arg msg;
			if(vars[\quneoLeftAmpRoute]==1.0,
				{if(synth.isPlaying, {synth.set(\quneoLeftBusAmp, msg[0])}); vars['quneoLeftBusAmp'] = msg[0]},
				{if(synth.isPlaying, {synth.set(\quneoLeftAmp, msg[0])}); vars['quneoLeftAmp'] = msg[0]})});

		/*__________ ROUTING ___________________________________*/
		controller.addResponder("/container"++routeContainer++"/busAssign"++synthIndex++"/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		controller.sendMsg(["/container"++containerNum++"/synthNum9/x"]++[synthIndex]);

		controller.addResponder("/container"++containerNum++"/quneoLeftHardwareOut/x", {arg msg;
			vars[\quneoLeftHardwareOutMon]=(msg[0]*8-1);
			vars[\quneoLeftHardwareOut]=msg[0];
			controller.sendMsg(["/container"++containerNum++"/quneoLeftHardwareOutMon/x"]++[(msg[0]*8-1)]);
			if((msg[0]*8-1)>(-1), {vars['out'] = (msg[0]*8-1).asInteger}, {vars['out']=dummyBus});
			if((msg[0]*8-1)>(-1),
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})});
		});

		MIDIFunc.cc({arg msg; turntable.set(\location0, msg)}, 68, midiChan);
		MIDIFunc.cc({arg msg; turntable.set(\pres0, msg)}, 67, midiChan);
		MIDIFunc.cc({arg msg; turntable.set(\location1, msg)}, 70, midiChan);
		MIDIFunc.cc({arg msg; turntable.set(\pres1, msg)}, 69, midiChan);

		MIDIFunc.noteOn({
			arg msg;
			turntable.set(\buf0, GlobalData.audioBuffers[\vox][0], \buf1, GlobalData.audioBuffers[\vox][1]);
		}, 71, midiChan);


		MIDIFunc.noteOff({
			arg msg;
			turntable.set(\buf0, GlobalData.audioBuffers[\vox][2], \buf1, GlobalData.audioBuffers[\vox][3]);
		}, 71, midiChan);

		//POPS---------------
		{
			buf0 = GlobalData.audioBuffers[\pops][GlobalData.audioBuffers[\pops].size.rand];

			MIDIFunc.noteOn({arg msg;

				if(hold==0, {buf0 = GlobalData.audioBuffers[\pops][GlobalData.audioBuffers[\pops].size.rand]});

				if(pop0.isPlaying, {nil}, {
					pop0 = Synth(\pops,
						[
							\out, popBus,
							\gate, 1,
							\buf, buf0
						],
						GlobalNodes.nodes[synthIndex], 'addToHead').register;
				});
			}, midiNotes[0], 1);

			MIDIFunc.noteOff({arg msg; if(pop0.isPlaying, {pop0.set(\gate, 0)})}, midiNotes[0], 1);
			MIDIFunc.cc({arg msg; if(pop0.isPlaying, {pop0.set(\length, msg)})}, midiNotes[0], 1);

			buf1 = GlobalData.audioBuffers[\pops][GlobalData.audioBuffers[\pops].size.rand];

			MIDIFunc.noteOn({arg msg;

				if(hold==0, {buf1 = GlobalData.audioBuffers[\pops][GlobalData.audioBuffers[\pops].size.rand]});

				if(pop1.isPlaying, {nil}, {
					pop1 = Synth(\pops,
						[
							\out, popBus,
							\gate, 1,
							\buf, buf1
						],
						GlobalNodes.nodes[synthIndex], 'addToHead').register;
				});
			}, midiNotes[1], 1);

			MIDIFunc.noteOff({arg msg; if(pop1.isPlaying, {pop1.set(\gate, 0)})}, midiNotes[1], 1);
			MIDIFunc.cc({arg msg; if(pop1.isPlaying, {pop1.set(\length, msg)})}, midiNotes[1], 1);


			buf2 = GlobalData.audioBuffers[\pops][GlobalData.audioBuffers[\pops].size.rand];

			MIDIFunc.noteOn({arg msg;

				if(hold==0, {buf2 = GlobalData.audioBuffers[\pops][GlobalData.audioBuffers[\pops].size.rand]});

				if(pop2.isPlaying, {nil}, {
					pop2 = Synth(\pops,
						[
							\out, popBus,
							\gate, 1,
							\buf, buf2
						],
						GlobalNodes.nodes[synthIndex], 'addToHead').register;
				});
			}, midiNotes[2], 1);

			MIDIFunc.noteOff({arg msg; if(pop2.isPlaying, {pop2.set(\gate, 0)})}, midiNotes[2], 1);
			MIDIFunc.cc({arg msg; if(pop2.isPlaying, {pop2.set(\length, msg)})}, midiNotes[2], 1);


			MIDIFunc.noteOn({arg msg; hold = 1}, 85, 1);
			MIDIFunc.noteOff({arg msg; hold=0}, 85, 1);

		}.value;
	}
}