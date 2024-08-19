InputSynth : OSCModule {
	var <>synthVars, <>eq, <>eqNum, <>guiVars, <>routeContainerNum, <>onsetContainerNum, <>threshTest, <>input, <>impulseSync;

	*new {arg synthNum, container, lemur, eqSynthNum, routeContainer, onsetContainer, hardwareInput; ^super.new.init(synthNum, container, lemur, eqSynthNum, routeContainer, onsetContainer, hardwareInput)}
	*save {^super.new.save}
	*load {^super.new.load}
	*initLemur {^super.new.initLemur}
	*updateLemur {^super.new.updateLemur}
	*run{^super.new.run}
	*on{^super.new.on}
	*off{^super.new.off}

	save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum
		GlobalPresets.controls.add(synthIndex.asSymbol -> vars);
		if(eq!=nil, {eq.save});
	}

	load { //save the parameter dictionary "vars" to the global "controls" dictionary
		vars=GlobalPresets.controls[synthIndex.asSymbol];
		this.updateLemur;
		this.updateSynth;
		if(eq!=nil, {eq.load}, {if(GlobalPresets.controls[synthIndex.asSymbol][\makeEQ]==1, {eq = ParametericEQGui.new(synth, eqNum, \gui, nil).run})});
		if(GlobalPresets.controls[eqNum.asSymbol]==nil, {if(eq!=nil, {if(eq.guiWindow.isClosed, {nil}, {eq.guiWindow.close})})});
	}

	init {arg synthNum, container, lemur, eqSynthNum, routeContainer, onsetContainer, hardwareInput;

		synthIndex = synthNum;
		containerNum = container;
		routeContainerNum = routeContainer;
		onsetContainerNum = onsetContainer;
		controller = lemur;
		eqNum = eqSynthNum;
		input = hardwareInput;

		lemur.sendMsg(["/container"++container++"/synthNum"++synthNum++"/x"]++[synthNum]);

		vars = Dictionary.newFrom([
			\gate, 0,
			\amp, 0, \busAmp, 1, \ampRoute, 0,
			\pan, 1/((GlobalPresets.numChannels-1)*2),
			\synthNum, synthNum,
			\inBus, hardwareInput, \bus, synthNum,
			\out, 0, 'makeEq', 0, \onsetThresh, 0.7,
		]);

		synthVars = [\gate, \amp, \pan, \busAmp];

		controlVars = [\gate, \amp, \ampRoute, \hardwareOutMon, \hardwareOut, \pan, \synthNum];

		guiVars = [\makeEq];
	}


	initLemur { //Init lemur interface on boot
		controlVars.do{|i|
			controller.sendMsg(["/container"++containerNum+/+(i.asString++synthIndex.asString)+/+"x"]++([vars[i]].flatten))}
	}

	updateLemur{ //Update lemur interface on new preset selection
		controlVars.do{|i|
			controller.sendMsg(["/container"++containerNum+/+(i.asString++synthIndex.asString)+/+"x"]++[GlobalPresets.controls[synthIndex.asSymbol][i]].flatten)}
	}

	updateSynth { //update synth if running, gate synth if not. Create an eq for this channel
		if(synth.isPlaying,
			{
				if(vars['gate']==1, {
					synth.set(
						\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][vars[\bus]],
						\out, GlobalBusses.outputSynthBus[vars[\out]]);

					synthVars.do{|i| synth.set(i, vars[i])};
				},
				{synth.set(\gate, 0)});
			},
			{
				if(vars['gate']==1, {
					synth = Synth(\inputSynth,
						[
							\gate, vars[\gate],
							\amp, vars[\amp], \busAmp, vars[\busAmp],
							\inBus, input,
							\pan, vars[\pan],
							\monoBus, GlobalBusses.allOut[\1][vars[\bus]],
							\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][vars[\bus]],
							\out, GlobalBusses.outputSynthBus[vars[\out]]
						], GlobalNodes.nodes[synthIndex]
					).register;

					GlobalSynths.synths.add(synthIndex.asSymbol -> synth);

					if(eq==nil, {eq = ParametericEQGui.new(synth, eqNum, \gui, nil).run});
				});
		});
	}

	run {var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels); //A bus that is routed nowhere (a fail safe if hardwareOut == -1)

		this.initLemur;

		if(vars[\gate]==1,{
			synth = Synth(\inputSynth,
				[
					\gate, vars[\gate],
					\amp, vars[\amp], \busAmp, vars[\busAmp],
					\inBus, input,
					\pan, vars[\pan],
					\monoBus, GlobalBusses.allOut[\1][vars[\bus]],
					\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][vars[\bus]],
					\out, GlobalBusses.outputSynthBus[vars[\out]]
				], GlobalNodes.nodes[synthIndex]
			).register;

			GlobalSynths.synths.add(synthIndex.asSymbol -> synth);

			if(eq==nil, {eq = ParametericEQGui.new(synth, eqNum, \gui, nil).run});

		});

		//Ampitude Control
		controller.addResponder("/container"++containerNum++"/amp"++synthIndex++"/x", {arg msg;
			if(vars['ampRoute']==1.0,
				{vars['busAmp']=msg[0]; if(synth.isPlaying, {synth.set(\busAmp, msg[0])})},
				{vars['amp']=msg[0]; if(synth.isPlaying, {synth.set(\amp, msg[0])})}
			);
		});

		controller.addResponder("/container"++containerNum++"/ampRoute"++synthIndex++"/x", {arg msg;
			vars['ampRoute'] = msg[0];
			if(msg[0]==1.0,
				{controller.sendMsg(["/container"++containerNum++"/amp"++synthIndex++"/x"]++[vars['busAmp']])},
				{controller.sendMsg(["/container"++containerNum++"/amp"++synthIndex++"/x"]++[vars['amp']])}
			);
		});

		/*Hardware Outs*/
		controller.addResponder("/container"++containerNum++"/hardwareOut"++synthIndex++"/x", {arg msg;
			vars['hardwareOutMon']=(msg[0]*8-1);
			vars['hardwareOut']=msg[0];
			controller.sendMsg(["/container"++containerNum++"/hardwareOutMon"++synthIndex++"/x"]++[(msg[0]*8-1)]);
			if((msg[0]*8-1)>(-1), {vars['out'] = (msg[0]*8-1).asInteger}, {vars['out']=0});
			if((msg[0]*8-1)>(-1),
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})});
		});

		//Panning
		controller.addResponder("/container"++containerNum++"/pan"++synthIndex++"/x", {arg msg;
			vars['pan']=msg[0];
			if(synth.isPlaying, {synth.set(\pan, msg[0])});
		});

		controller.addResponder("/container"++containerNum++"/panReset"++synthIndex++"/x", {arg msg;
			vars['pan']=1/((GlobalPresets.numChannels-1)*2);
			if(synth.isPlaying, {synth.set(\pan, 1/((GlobalPresets.numChannels-1)*2))});

			controller.sendMsg(["/container"++containerNum++"/pan"++synthIndex++"/x"]++[1/((GlobalPresets.numChannels-1)*2)]);
		});

		controller.addResponder("/container"++routeContainerNum++"/busAssign"++synthIndex++"/x", {arg msg;
			~busAssign = synthIndex;
		});

		//Onset Detetction
		controller.addResponder("/container"++onsetContainerNum++"/thresh"++synthIndex++"/x", {arg msg;
			vars[\onsetThresh] = msg[0];
			GlobalSynths.onsetThreshs[synthIndex.asSymbol] = msg[0];
			if(threshTest.isPlaying, {threshTest.set(\thresh, msg[0])});
		});

		controller.addResponder("/container"++onsetContainerNum++"/threshTest"++synthIndex++"/x", {arg msg;
			if(msg[0]==1, {
				if(threshTest.isPlaying, {nil}, {
					threshTest=Synth(\threshTest,
						[
							\inBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][vars[\bus]],
							\thresh, vars[\onsetThresh], \out, GlobalBusses.outputSynthBus[0]
						], GlobalNodes.nodes[synthIndex], 'addToTail'
					).register;
				});

			}, {if(threshTest.isPlaying, {threshTest.free})});
		});
	}

	off {
		vars[\gate]=0;
		if(synth.isPlaying, {synth.set(\gate, 0)});
	}

	on {
		if(synth.isPlaying,
			{vars[\gate]=1},
			{vars[\gate]=1; this.run});
	}
}
