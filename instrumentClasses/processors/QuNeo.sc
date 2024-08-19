Quneo : OSCModule {
	var <>midiController, <>cc, <>slot, <>busMixer, <>mixerBus, <>mixerVars, <>stateController, <>layer, <>prevLayer, <>layerArray, <>synthLayers, <>stateContainer;

	/*____________Constructors____________*/
	*new {arg synthNum, lemur, lemurContainer, stateLemur, quNeoChan, midiIndex, slotNum, gateContainer; ^super.new.init(synthNum, lemur, lemurContainer, stateLemur, quNeoChan, midiIndex, slotNum, gateContainer)}
	*makeSynth {^super.new.makeSynth}
	*makeMixer {^super.new.makeMixer}
	*updateSynth {^super.new.updateSynth}
	*makeSynthUpdate{arg updateLayer; ^super.new.makeSynthUpdate(updateLayer)}
	*updateStateController {^super.new.updateStateController}
	*layerSwitch {arg otherContainer; ^super.new.layerSwitch(otherContainer)}

	layerSwitch {arg otherContainer;

		layer = layerArray[1];
		prevLayer = layerArray[0];

		8.do{|i|
			if(i==layer, {controller.sendMsg(["/container"++otherContainer+/+"layer"++i+/+"x"]++[1])}, {controller.sendMsg(["/container"++otherContainer+/+"layer"++i+/+"x"]++[0])});

		};

		if(synthLayers[layerArray[0]]!=nil, {vars[layerArray[0].asSymbol][\pauseGate]=0; synthLayers[layerArray[0]].set(\pauseGate, 0)});
		if(synthLayers[layerArray[1]]!=nil, {
			vars[layerArray[1].asSymbol][\pauseGate]=1;
			if(vars[layerArray[1].asSymbol][\ampGate]==0, {synthLayers[layerArray[1]].run.set(\pauseGate, 1)})
		});

		synth = synthLayers[layerArray[1]];

		this.lemurLayer;

	}

	init {arg synthNum, lemur, lemurContainer, stateLemur, quNeoChan, midiIndex, slotNum, gateContainer;
		synthIndex = synthNum;
		containerNum = lemurContainer;
		controller = lemur;
		stateController = stateLemur;
		stateContainer = gateContainer;
		midiController = quNeoChan;
		cc = midiIndex;
		slot = slotNum;
		layerArray=[999, 0];
		layer = 0;
		prevLayer = 0;
		synthLayers = Array.newClear(8);
		mixerBus = Dictionary.newFrom([
			\0, Bus.audio(Server.local, GlobalPresets.numChannels),
			\1, Bus.audio(Server.local, GlobalPresets.numChannels),
			\2, Bus.audio(Server.local, GlobalPresets.numChannels),
			\3, Bus.audio(Server.local, GlobalPresets.numChannels),
			\4, Bus.audio(Server.local, GlobalPresets.numChannels),
			\5, Bus.audio(Server.local, GlobalPresets.numChannels),
			\6, Bus.audio(Server.local, GlobalPresets.numChannels),
			\7, Bus.audio(Server.local, GlobalPresets.numChannels),
		]);

		vars = Dictionary.new;

		8.do{|i|
			vars.add(i.asSymbol ->
				Dictionary.newFrom([
					\busCount, 0, \state, 0, \ampGate, 0, \pauseGate, 1, \synthActive, 0,
					\gate, 0,  \synthSel, 0, \out, 0,
					\x, 0, \y, 0, \pres, 0,
					\ampRoute, 1, \amp, 1, \busAmp, 1,
					\synthNum, synthNum+i, \env, 0, \envMon, 0,
					\hardwareOutMon, 0, \hardwareOut, 0,
					\busIn0, 999, \busIn1, 999, \busIn2, 999, \busIn3, 999,
					\busIn4, 999, \busIn5, 999, \busIn6, 999, \busIn7, 999,
					\bus0, 999, \bus1, 999, \bus2, 999, \bus3, 999, \bus4, 999, \bus5, 999, \bus6, 999, \bus7, 999,
					\fbSel0, 999, \fbSel1, 999, \fbSel2, 999, \fbSel3, 999, \fbSel4, 999, \fbSel5, 999, \fbSel6, 999, \fbSel7, 999
				]);
			);
		};

		synthVars = [\synthSel, \amp, \busAmp, \env, \out, \state, \ampGate, \x, \y, \pres];

		controlVars = [\busIn0, \busIn1, \busIn2, \busIn3, \busIn4, \busIn5, \busIn6, \busIn7, \amp, \gate, \hardwareOut, \hardwareOutMon, \env, \envMon, \state];

		mixerVars = [\bus0, \bus1, \bus2, \bus3, \bus4, \bus5, \bus6, \bus7, \fbSel0, \fbSel1, \fbSel2, \fbSel3, \fbSel4, \fbSel5, \fbSel6, \fbSel7];
	}

	updateStateController {

		if(vars[layer.asSymbol][\state]==0,
			{
				stateController.sendMsg(["/container"++stateContainer++"/slot"++slot+/+"state"+/+"x"]++([0]));
		});

		if(vars[layer.asSymbol][\state]==1,
			{
				stateController.sendMsg(["/container"++stateContainer++"/slot"++slot+/+"state"+/+"x"]++([1]));
		});

		if(vars[layer.asSymbol][\ampGate]==0,
			{stateController.sendMsg(["/container"++stateContainer++"/slot"++slot+/+"ampGate"+/+"x"]++([0]))},
			{stateController.sendMsg(["/container"++stateContainer++"/slot"++slot+/+"ampGate"+/+"x"]++([1]))}
		);
	}

	initLemur { //Init lemur interface on boot
		controlVars.do{|i|
			controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+(i.asString)+/+"x"]++([vars[layer.asSymbol][i]].flatten))};

		[\busIn0, \busIn1, \busIn2, \busIn3, \busIn4, \busIn5, \busIn6, \busIn7, \amp, \hardwareOutMon].do{|i|
			controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+(i.asString)+/+"x"]++[vars[layer.asSymbol][i]].flatten);
		};

		this.updateStateController;

		controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[0].asString]);
		controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[0].asString]);
	}

	updateLemur { //Update lemur interface on new preset selection

		controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[GlobalPresets.controls[synthIndex.asSymbol][layer.asSymbol][\synthSel]].asString]);
		controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[GlobalPresets.controls[synthIndex.asSymbol][layer.asSymbol][\synthSel]].asString]);

		controlVars.do{|i|
			controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+(i.asString)+/+"x"]++[GlobalPresets.controls[synthIndex.asSymbol][layer.asSymbol][i]].flatten)};

		[\busIn0, \busIn1, \busIn2, \busIn3, \busIn4, \busIn5, \busIn6, \busIn7, \amp, \hardwareOutMon].do{|i|
			controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+(i.asString)+/+"x"]++[vars[layer.asSymbol][i]].flatten);
		};

		this.updateStateController;
	}

	lemurLayer {
		this.updateStateController;

		controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[vars[layer.asSymbol][\synthSel]].asString]);
		controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[vars[layer.asSymbol][\synthSel]].asString]);

		controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"slotMon"+/+"x"]++[(synthIndex+layer)]);
		controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"slotMon"+/+"x"]++[(synthIndex+layer)]);

		controlVars.do{|i|
			controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+(i.asString)+/+"x"]++[vars[layer.asSymbol][i]].flatten);
		};

		[\busIn0, \busIn1, \busIn2, \busIn3, \busIn4, \busIn5, \busIn6, \busIn7, \amp, \hardwareOutMon].do{|i|
			controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+(i.asString)+/+"x"]++[vars[layer.asSymbol][i]].flatten);
		};

	}

	updateSynth {
		8.do{arg layerCount;

			if(vars[layerCount.asSymbol][\synthActive]==1,
				{
					if(vars[layerCount.asSymbol][\gate]==1,
						{
							this.makeSynthUpdate(layerCount);

							if(vars[layerCount.asSymbol][\bus0]==999,
								{nil},
								{busMixer[layerCount.asSymbol] = BusMixer.makeSynth(vars[layerCount.asSymbol][\bus0], mixerBus[layerCount.asSymbol], (synthIndex+layerCount), vars[layerCount.asSymbol][\busCount])}
							);

							[\bus1, \bus2, \bus3, \bus4, \bus5, \bus6, \bus7].do{|i|
								if(vars[layerCount.asSymbol][i]==999,
									{nil},
									{busMixer[layerCount.asSymbol].synth.set(i, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][vars[layerCount.asSymbol][i]])}
								);
							};

							[\fbSel1, \fbSel2, \fbSel3, \fbSel4, \fbSel5, \fbSel6, \fbSel7].do{|i|
								if(vars[layerCount.asSymbol][i]==999,
									{nil},
									{busMixer[layerCount.asSymbol].synth.set(i, vars[layerCount.asSymbol][i])}
								);
							};
						}
					);
				}
			);
		}
	}

	makeMixer {
		if(vars[layer.asSymbol][\busCount]==0,
			{
				busMixer[layer.asSymbol].makeSynth(~busAssign, mixerBus[layer.asSymbol], (synthIndex+layer), vars[layer.asSymbol][\busCount]);
				vars[layer.asSymbol][("bus"++vars[layer.asSymbol][\busCount].wrap(0, 7)).asSymbol]=~busAssign;
				vars[layer.asSymbol][("busIn"++vars[layer.asSymbol][\busCount].wrap(0, 7)).asSymbol]=~busAssign;
				vars[layer.asSymbol][("fbSel"++vars[layer.asSymbol][\busCount].wrap(0, 7)).asSymbol]=if(~busAssign>(synthIndex+layer), {1}, {0});
			},
			{
				busMixer[layer.asSymbol].addBus(~busAssign);
				vars[layer.asSymbol][("bus"++vars[layer.asSymbol][\busCount].wrap(0, 7)).asSymbol]=~busAssign;
				vars[layer.asSymbol][("busIn"++vars[layer.asSymbol][\busCount].wrap(0, 7)).asSymbol]=~busAssign;
				vars[layer.asSymbol][("fbSel"++vars[layer.asSymbol][\busCount].wrap(0, 7)).asSymbol]=if(~busAssign>(synthIndex+layer), {1}, {0})});

		controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"busIn"++(vars[layer.asSymbol][\busCount])++"/x"]++[~busAssign]);
		controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"busIn"++(vars[layer.asSymbol][\busCount])++"/x"]++[~busAssign]);
	}

	makeSynth {
		if(synth.isPlaying, {nil}, {
			synth = Synth(GlobalSynths.processors[vars[layer.asSymbol]['synthSel']], [
				\x, vars[layer.asSymbol][\x], \y, vars[layer.asSymbol][\y], \pres, vars[layer.asSymbol][\pres],
				\pauseGate, 1, \gate, 1, \amp, vars[layer.asSymbol][\amp], \busAmp, vars[layer.asSymbol][\busAmp],
				\inBus, mixerBus[layer.asSymbol], \env, vars[layer.asSymbol][\env], \state, vars[layer.asSymbol][\state], \ampGate, vars[layer.asSymbol][\ampGate],
				\thresh, GlobalSynths.onsetThreshs[(synthIndex+layer).asSymbol],
				\busMono, GlobalBusses.allOut[\1][(synthIndex+layer)],
				\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][(synthIndex+layer)],
				\out, GlobalBusses.outputSynthBus[vars[layer.asSymbol][\out]]
			], GlobalNodes.nodes[(synthIndex+layer)], 'addToTail').run.register;
		});

		vars[layer.asSymbol][\synthActive] = 1;
		synthLayers[layer] = synth;

		GlobalSynths.synths.add((synthIndex+layer).asSymbol -> synth);
	}

	makeSynthUpdate {arg updateLayer;
		if(synth.isPlaying, {nil}, {
			synth = Synth(GlobalSynths.processors[vars[updateLayer.asSymbol]['synthSel']], [
				\x, vars[updateLayer.asSymbol][\x], \y, vars[updateLayer.asSymbol][\y], \pres, vars[updateLayer.asSymbol][\pres],
				\pauseGate, 0, \gate, 1, \amp, vars[updateLayer.asSymbol][\amp], \busAmp, vars[updateLayer.asSymbol][\busAmp],
				\inBus, mixerBus[updateLayer.asSymbol], \env, vars[updateLayer.asSymbol][\env],
				\state, vars[updateLayer.asSymbol][\state], \ampGate, vars[updateLayer.asSymbol][\ampGate],
				\thresh, GlobalSynths.onsetThreshs[(synthIndex+updateLayer).asSymbol],
				\busMono, GlobalBusses.allOut[\1][(synthIndex+layer)],
				\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][(synthIndex+updateLayer)],
				\out, GlobalBusses.outputSynthBus[vars[updateLayer.asSymbol][\out]]
			], GlobalNodes.nodes[(synthIndex+updateLayer)], 'addToTail').run.register;
		});

		synthLayers[updateLayer] = synth;
		GlobalSynths.synths.add((synthIndex+updateLayer).asSymbol -> synth);
	}


	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels);

		busMixer = Dictionary.newFrom([\0, BusMixer.new, \1, BusMixer.new, \2, BusMixer.new, \3, BusMixer.new, \4, BusMixer.new, \5, BusMixer.new, \6, BusMixer.new, \7, BusMixer.new]);

		this.initLemur;

		MIDIFunc.cc({arg msg;  if(synth.isPlaying, {synth.set(\pres, msg); vars[layer.asSymbol][\pres]=msg})}, cc,   midiController);
		MIDIFunc.cc({arg msg;  if(synth.isPlaying, {synth.set(\x,    msg); vars[layer.asSymbol][\x]=msg})}, cc+1, midiController);
		MIDIFunc.cc({arg msg;  if(synth.isPlaying, {synth.set(\y,    msg); vars[layer.asSymbol][\y]=msg})}, cc+2, midiController);
		MIDIFunc.noteOn({arg msg; synth.run.set(\pauseGate, 1)}, cc, midiController);
		MIDIFunc.noteOff({arg msg; if(vars[layer.asSymbol][\ampGate]==1, {if(synth.isRunning, {synth.set(\pauseGate, 0)})})}, cc, midiController);

		stateController.addResponder("/container"++stateContainer++"/slot"++slot+/+"state/x", {arg msg;
			vars[layer.asSymbol][\state]=msg[0];
			if(synth.isPlaying, {synth.set(\state, msg[0])})
		});

		stateController.addResponder("/container"++stateContainer++"/slot"++slot+/+"ampGate"+/+"x", {arg msg;
			vars[layer.asSymbol][\ampGate]=msg[0];
			if(synth.isPlaying, {synth.set(\ampGate, msg[0])});
			if(msg[0]==0, {if(synth.isRunning, {nil}, {synth.run; synth.set(\pauseGate, 1)})});
		});

		//Populate menu with synths
		controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[0].asString]);
		controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[0].asString]);

		controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"slotMon"+/+"x"]++[(synthIndex+layer)]);
		controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"slotMon"+/+"x"]++[(synthIndex+layer)]);

		controller.addResponder("/container"++containerNum+/+"slot"++slot+/+"gate"+/+"x", {arg msg;
			vars[layer.asSymbol][\gate]=msg[0];
			if(msg[0]==1,
				{if(synth.isPlaying, {synth.free}, {this.makeMixer; this.makeSynth})},
				{

					if(synth.isRunning,
						{if(synth.isPlaying, {synth.set(\gate, 0, \env, vars[layer.asSymbol][\env]); busMixer[layer.asSymbol].synth.set(\gate, 0, \env, vars[layer.asSymbol][\env])})},
						{if(synth.isPlaying, {synth.run.set(\gate, 0, \env, vars[layer.asSymbol][\env]); busMixer[layer.asSymbol].synth.set(\gate, 0, \env, vars[layer.asSymbol][\env])})}
					);

					8.do{|i|
						controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"busIn"++i+/+"x"]++[999]);
						controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"busIn"++i+/+"x"]++[999]);
						vars[layer.asSymbol][("bus"++i).asSymbol]=999;
						vars[layer.asSymbol][("busIn"++i).asSymbol]=999;
						vars[layer.asSymbol][("fbSel"++i).asSymbol]=999;

					};
					vars[layer.asSymbol]['amp']=1; vars[layer.asSymbol]['env']=0; vars[layer.asSymbol]['envMon']=0;
					vars[layer.asSymbol][\busCount]=0; vars[layer.asSymbol][\synthActive] = 0;
					controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"amp"+/+"x"]++[1]);
					controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"amp"+/+"x"]++[1]);
					//synthLayers[layer]=nil;

				}
			);
		});

		controller.addResponder("/container"++containerNum+/+"slot"++slot+/+"synthSelSlide"+/+"x", {arg msg;
			vars[layer.asSymbol]['synthSel'] = msg[0].linlin(0, 1,  0, GlobalSynths.processors.size).asInteger;
			controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[msg[0].linlin(0, 1,  0, GlobalSynths.processors.size).asInteger].asString]);
			controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"synthSel", "@items"]++[GlobalSynths.processors[msg[0].linlin(0, 1,  0, GlobalSynths.processors.size).asInteger].asString]);
		});

		controller.addResponder("/container"++containerNum+/+"slot"++slot+/+"amp"+/+"x", {arg msg;
			controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"amp"+/+"x"]++[msg[0]]);
			vars[layer.asSymbol]['amp']=msg[0];
			if(synth.isPlaying, {synth.set(\amp, msg[0])});
		});

		controller.addResponder("/container"++stateContainer+/+"slot"++slot+/+"amp"+/+"x", {arg msg;
			controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"amp"+/+"x"]++[msg[0]]);
			vars[layer.asSymbol]['amp']=msg[0];
			if(synth.isPlaying, {synth.set(\amp, msg[0])});
		});


		controller.addResponder("/container"++containerNum+/+"slot"++slot+/+"hardwareOut/x", {arg msg;
			vars[layer.asSymbol]['hardwareOutMon']=(msg[0]*8-1);
			vars[layer.asSymbol]['hardwareOut']=msg[0];
			controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"hardwareOutMon/x"]++[(msg[0]*8-1)]);
			controller.sendMsg(["/container"++stateContainer+/+"slot"++slot+/+"hardwareOutMon/x"]++[(msg[0]*8-1)]);
			if((msg[0]*8-1)>(-1), {vars[layer.asSymbol]['out'] = (msg[0]*8-1).asInteger}, {vars[layer.asSymbol]['out']=dummyBus});
			if((msg[0]*8-1)>(-1),
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})});
		});


		controller.addResponder("/container"++containerNum+/+"slot"++slot+/+"busAssign"+/+"x", {arg msg; if(msg[0]==1.0, {~busAssign=(synthIndex+layer)})});
		controller.addResponder("/container"++stateContainer+/+"slot"++slot+/+"busAssign"+/+"x", {arg msg; if(msg[0]==1.0, {~busAssign=(synthIndex+layer)})});


		controller.addResponder("/container"++containerNum+/+"slot"++slot+/+"busAdd"+/+"x", {arg msg;
			vars[layer.asSymbol][\gate]=1;
			if(msg[0]==1.0,
				{
					this.makeMixer;
					if(synthLayers[layer]!=nil,
						{if(synthLayers[layer].isRunning, {nil}, {controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"gate"+/+"x"]++[1])})},
						{controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"gate"+/+"x"]++[1])}
					);
				},
				{
					vars[layer.asSymbol][\busCount]=vars[layer.asSymbol][\busCount]+1;
					if(synthLayers[layer]!=nil, {if(synthLayers[layer].isRunning, {nil}, {this.makeSynth})}, {this.makeSynth});
					//if(synth.isPlaying, {nil}, {this.makeSynth});
			});
		});

		controller.addResponder("/container"++stateContainer+/+"slot"++slot+/+"busAdd"+/+"x", {arg msg;
			vars[layer.asSymbol][\gate]=1;
			if(msg[0]==1.0,
				{
					this.makeMixer;
					if(synthLayers[layer]!=nil,
						{if(synthLayers[layer].isRunning, {nil}, {controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"gate"+/+"x"]++[1])})},
						{controller.sendMsg(["/container"++containerNum+/+"slot"++slot+/+"gate"+/+"x"]++[1])}
					);
				},
				{
					vars[layer.asSymbol][\busCount]=vars[layer.asSymbol][\busCount]+1;
					if(synthLayers[layer]!=nil, {if(synthLayers[layer].isRunning, {nil}, {this.makeSynth})}, {this.makeSynth});
					//if(synth.isPlaying, {nil}, {this.makeSynth});
			});
		});

		/*________ Layer Switch ________________________________________________________________________________________________*/
		8.do{|i|
			controller.addResponder("/container2"+/+"layer"++i+/+"x", {arg msg;
				if(msg[0]==1, {layerArray = layerArray.swap(0, 1).put(1, i); this.layerSwitch(7)})
			});
		};

		8.do{|i|
			controller.addResponder("/container7"+/+"layer"++i+/+"x", {arg msg;
				if(msg[0]==1, {layerArray = layerArray.swap(0, 1).put(1, i); this.layerSwitch(2)})
			});
		};
	}
}
