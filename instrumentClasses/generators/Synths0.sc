Synths0 : OSCModule {/*-----> A class for the OnsetLooper synth <-----*/
	var <>shepBus, <>shepVars, <>sheps;
	var <>glitch0Bus, <>glitch0Vars, <>glitch0;
	//var <>markovBus, <>markovVars, <>markov;
	var <>iceBus, <>iceVars, <>ice;

	/*____________Constructors____________*/
	*new {arg synthNum, lemur, lemurContainer; ^super.new.init(synthNum, lemur, lemurContainer)}
	*makeSynth {^super.new.makeSynth}
	*makeSheps {^super.new.makeSheps}
	//*makeMarkov {^super.new.makeMarkov}
	*makeGlitch0 {^super.new.makeGlitch0}
	*makeIce {^super.new.makeIce}
	*run {^super.new.run}
	*updateSynth {^super.new.updateSynth}

	//Assign each instance an index for its trigger synth, synth, lemur controller,
	//which container in the lemur interface will control this instance and an index
	//for syncing one instance's trigger synth to another
	init { arg synthNum, lemur, lemurContainer;
		//Init the variable "vars" with a dictionary of all the control parameters used for this class and for the lemur controller

		shepBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		//markovBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		glitch0Bus = Bus.audio(Server.local, GlobalPresets.numChannels);
		iceBus = Bus.audio(Server.local, GlobalPresets.numChannels);

		synthIndex=synthNum;           //Assign synthNum to class var synthIndex
		containerNum = lemurContainer; //Assigns lemur container number to class var
		controller = lemur;            //Assigns lemur instance to class var

		rec = Recorder(Server.local);

		vars = Dictionary.newFrom([
			\gate, 0, \synthIndex, synthNum, \atk, 0,
			\amp, 1, \out, 0, \busAmp, 1, \ampRoute, 0,
			\hardwareOutMon, 0, \hardwareOut, 0,
			\phaseFreq, 0, \incr, 0, \dropoff, 10, \pitch, 60, \interval, 120,
			\shepsAmp, 1, \shepsGate, 0, \shepsState, 0, \shepsCut, 0,
			\glitch0Gate, 0, \glitch0Amp, 1,
			\xFreezeShift, 0, \yFreezeShift, 0,
			\fbMult, 0, \res, 0, \freezeGate, 0, \glitch0Cut, 0,
			\markovX, 0, \markovY, 0, \markovCut, 0,
			\markovAmp, 1, \markovFreeze, 0, \markovGate, 0,
			\iceX, 0, \iceY, 0, \iceAmp, 1, \iceGate, 0
		]);

		controlVars = [\synthNum, \amp,  \out, \hardwareOutMon, \hardwareOut, \ampRoute, \busAmp,  \gate, \shepsAmp, \phaseFreq, \incr, \dropoff, \pitch, \interval, \shepsGate, \shepsState, \shepsCut, \glitch0Gate, \glitch0Amp, \glitch0Cut, \markovX, \markovY, \markovCut, \markovAmp, \makovGate, \markovFreeze, \xFreezeShift, \yFreezeShift, \fbMult,  \res, \iceX, \iceY, \iceAmp, \iceGate];

		synthVars = [\gate, \amp, \busAmp, \atk, \out];

		shepVars = [\phaseFreq, \incr, \dropoff, \pitch, \interval, \shepsAmp, \shepsGate, \shepsState, \shepsCut];

		glitch0Vars = [\glitch0Gate, \glitch0Amp, \out, \xFreezeShift, \yFreezeShift, \fbMult,  \res, \freezeGate, \glitch0Cut];

		//markovVars = [\markovX, \markovY, \markovCut, \markovAmp, \markovFreeze, \markovGate];

		//iceVars = [\markovX, \markovY, \markovCut, \markovAmp, \markovFreeze, \markovGate];
	}

	//Update synth parameters
	updateSynth{
		if(synth.isPlaying,
			{if(vars[\gate]==1, {synthVars.do{|i| synth.set(i, vars[i])}}, {synth.set(\gate, 0)})},
			{if(vars[\gate]==1, {this.makeSynth}, {nil})});

		if(sheps.isPlaying,
			{if(vars[\shepsGate]==1, {shepVars.do{|i| sheps.set(i, vars[i])}}, {sheps.set(\shepsGate, 0)})},
			{if(vars[\shepsGate]==1, {this.makeSheps}, {nil})});

		if(glitch0.isPlaying,
			{if(vars[\glitch0Gate]==1, {glitch0Vars.do{|i| glitch0.set(i, vars[i])}}, {glitch0.set(\glitch0Gate, 0)})},
			{if(vars[\glitch0Gate]==1, {this.makeGlitch0}, {nil})});

		/*if(markov.isPlaying,
			{if(vars[\markovGate]==1, {markovVars.do{|i| markov.set(i, vars[i])}}, {markov.set(\markovGate, 0)})},
			{if(vars[\markovGate]==1, {this.makeMarkov}, {nil})});*/

		if(ice.isPlaying,
			{if(vars[\iceGate]==1, {iceVars.do{|i| ice.set(i, vars[i])}}, {ice.set(\iceGate, 0)})},
			{if(vars[\iceGate]==1, {this.makeIce}, {nil})});
	}

	makeGlitch0{
		glitch0 = Synth(\glitchSynth0, [
			\glitch0Gate, vars[\glitch0Gate],
			\glitch0Amp, vars[\glitch0Amp], \out, glitch0Bus,

			\xFreezeShift, vars[\xFreezeShift], \yFreezeShift, vars[\yFreezeShift],
			\fbMult, vars[\fbMult], \res, vars[\res],
			\freezeGate, vars[\freezeGate], \glitch0Cut, vars[\glitch0Cut]

		],  GlobalNodes.nodes[synthIndex]).register;
	}

	makeSheps{
		sheps = Synth(\shepard, [
			\shepsGate, vars[\shepsGate],
			\shepsAmp, vars[\shepsAmp], \out, shepBus,

			\phaseFreq, vars[\phaseFreq], \incr, vars[\incr],
			\dropoff, vars[\dropoff], \pitch, vars[\pitch],
			\interval, vars[\interval]

		],  GlobalNodes.nodes[synthIndex]).register;
	}

	/*makeMarkov{
		markov = Synth(\markovSynth, [
			\out, markovBus,
			\markovGate, vars[\markovGate],
			\markovX, vars[\markovX], \markovY, vars[\markovY], \markovCut, vars[\markovCut],
			\markovAmp, vars[\markovAmp], \markovFreeze, vars[\markovFreeze]
		],  GlobalNodes.nodes[synthIndex]).register;
	}
*/
	makeIce{
		ice = Synth(\iceSynth, [
			\out, iceBus, \iceGate, vars[\iceGate],
			\iceX, vars[\iceX], \iceY, vars[\iceY], \iceAmp, vars[\iceAmp],
		],  GlobalNodes.nodes[synthIndex]).register;
	}


	makeSynth{
		synth = Synth(\synths0, [
			\gate, 1,
			\shepBus, shepBus, \glitch0Bus, glitch0Bus, /*\markovBus, markovBus,*/ \iceBus, iceBus,
			\amp, vars[\amp], \busAmp, vars[\busAmp], \atk, vars[\atk],
			\monoBus, GlobalBusses.allOut[\1][synthIndex],
			\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
			\out, GlobalBusses.outputSynthBus[vars[\out]],

		],  GlobalNodes.nodes[synthIndex], 'addToTail').register;

		GlobalSynths.synths.add(synthIndex.asSymbol -> synth);
	}

	//Run Lemur OSCFuncs that control Synth
	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels); //A bus that is routed nowhere (a fail safe if hardwareOut == -1)
		this.save; //add vars to global controls on instance
		this.initLemur; //Init lemur interface

		/*/*__markov____________________________________*/
		controller.addResponder("/container"++containerNum++"/markovGate/x", {arg msg;
			vars[\markovGate]=msg[0];
			if(msg[0]==1, {this.makeMarkov; if(synth.isPlaying, {nil}, {this.makeSynth})},
				{if(markov.isPlaying, {markov.set(\markovGate, 0)})});
		});

		["markovAmp", "markovX", "markovY", "markovAmpGate",  "markovCut", "markovFreeze"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;
				vars[(param).asSymbol]=msg[0];
				if(markov.isPlaying, {markov.set((param).asSymbol, msg[0])});
			});
		};*/

		/*__glitch0____________________________________*/

		controller.addResponder("/container"++containerNum++"/glitch0Gate/x", {arg msg;
			vars[\glitch0Gate]=msg[0];
			if(msg[0]==1, {this.makeGlitch0; if(synth.isPlaying, {nil}, {this.makeSynth})},
				{if(glitch0.isPlaying, {glitch0.set(\glitch0Gate, 0, \atk, vars[\atk])})});
		});

		controller.addResponder("/container"++containerNum++"/glitch0Cut/x", {arg msg;
			vars['glitch0Cut'] = msg[0];
			glitch0.set(\glitch0Cut, msg[0].linlin(0, 1, 1, 0));
		});

		controller.addResponder("/container"++containerNum++"/glitch0Left/z", {arg msg;
			if(vars['glitch0Cut']==1, {glitch0.set(\glitch0Cut, msg[0])}, {glitch0.set(\glitch0Cut, 1)})
		});

		["glitch0Amp", "xFreezeShift", "yFreezeShift", "fbMult",  "res", "freezeGate"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;
				vars[(param).asSymbol]=msg[0];
				if(glitch0.isPlaying, {glitch0.set((param).asSymbol, msg[0])});
			});
		};

		/*__SHEPS____________________________________*/

		controller.addResponder("/container"++containerNum++"/shepsGate/x", {arg msg;
			vars[\shepsGate]=msg[0];
			if(msg[0]==1, {this.makeSheps; if(synth.isPlaying, {nil}, {this.makeSynth})},
				{if(sheps.isPlaying, {sheps.set(\shepsGate, 0, \atk, vars[\atk])})});
		});

		controller.addResponder("/container"++containerNum++"/shepsAmp/x", {arg msg;
			vars['shepsAmp'] = msg[0];
			if(sheps.isPlaying, {sheps.set(\shepsAmp, msg[0])});
		});

		controller.addResponder("/container"++containerNum++"/shepsState/x", {arg msg;
			vars['shepsState'] = msg[0];
			if(sheps.isPlaying, {sheps.set(\shepsState, msg[0])});
		});

		["phaseFreq", "incr", "dropoff", "pitch", "interval"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;
				vars[(param).asSymbol]=msg[0];
				if(sheps.isPlaying, {sheps.set((param).asSymbol, msg[0])});
			});

		};

		controller.addResponder("/container"++containerNum++"/shepsCut/x", {arg msg;
			vars['shepsCut'] = msg[0];
			sheps.set(\shepsCut, msg[0].linlin(0, 1, 1, 0));
		});

		controller.addResponder("/container"++containerNum++"/phaseIncr/z", {arg msg;
			if(vars['shepsCut']==1, {sheps.set(\shepsCut, msg[0])}, {sheps.set(\shepsCut, 1)})
		});

		/*__Ice____________________________________*/
		["iceX", "iceFreeze", "iceTrig", "iceAmp", "iceAmpGate"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;
				vars[param.asSymbol]=msg[0];
				if(ice.isPlaying, {ice.set((param).asSymbol, msg[0])});
			});
		};

		controller.addResponder("/container"++containerNum++"/iceGate/x", {arg msg;
			vars[\iceGate]=msg[0];
			if(msg[0]==1, {this.makeIce; if(synth.isPlaying, {nil}, {this.makeSynth})},
				{if(ice.isPlaying, {ice.set(\iceGate, 0, \atk, vars[\atk])})});
		});


		/*__MIX____________________________________*/

		/*__________ RECORD SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/record/x", {arg msg; this.record(msg[0])});

		/*__________ ROUTE SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/busRoute/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		/*__________ KILL SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/kill/x", {arg msg; this.kill});

		controller.sendMsg(["/container"++containerNum++"/synthNum/x"]++[synthIndex]);

		controller.addResponder("/container"++containerNum++"/gate/x", {arg msg;
			vars[\gate]=msg[0];
			if(msg[0]==1,
				{
					if(synth.isPlaying, {nil},{ this.makeSynth});
					if(vars[\shepsGate]==1, {if(sheps.isPlaying, {nil}, {this.makeSheps})});
					if(vars[\glitch0Gate]==1, {if(glitch0.isPlaying, {nil}, {this.makeGlitch0})});
					//if(vars[\markovGate]==1, {if(markov.isPlaying, {nil}, {this.makeMarkov})});
					if(vars[\iceGate]==1, {if(ice.isPlaying, {nil}, {this.makeIce})});
				},
				{
					if(synth.isPlaying, synth.set(\gate, 0, \atk, vars[\atk]));
					if(sheps.isPlaying, sheps.set(\shepsGate, 0, \atk, vars[\atk]); controller.sendMsg(["/container"++containerNum+/+"shepsGate"+/+"x"]++[0]));
					if(glitch0.isPlaying, glitch0.set(\glitch0Gate, 0, \atk, vars[\atk]); controller.sendMsg(["/container"++containerNum+/+"glitch0Gate"+/+"x"]++[0]));
					//if(markov.isPlaying, markov.set(\markovGate, 0, \atk, vars[\atk]); controller.sendMsg(["/container"++containerNum+/+"markovGate"+/+"x"]++[0]));
					if(ice.isPlaying, ice.set(\iceGate, 0, \atk, vars[\atk]); controller.sendMsg(["/container"++containerNum+/+"iceGate"+/+"x"]++[0]));
			});
		});


		controller.addResponder("/container"++containerNum++"/atk/x", {arg msg;
			vars['atk']=msg[0];
			vars['rel']=msg[0];
			vars['envMon']=msg[0].linlin(0.0, 1.0, 0.005, 180.0);
			if(synth.isPlaying, {synth.set(\atk, msg[0])});
			if(synth.isPlaying, {synth.set(\rel, msg[0])});
			controller.sendMsg(["/container"++containerNum++"/envMon/x"]++[msg[0].linlin(0.0, 1.0, 0.005, 180.0)]);
		});

		controller.addResponder("/container"++containerNum++"/amp/x", {arg msg;
			if(vars['ampRoute']==1.0,
				{if(synth.isPlaying, {synth.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(synth.isPlaying, {synth.set(\amp, msg[0])}); vars['amp'] = msg[0]})});


		controller.addResponder("/container"++containerNum++"/ampRoute/x", {arg msg;
			vars['ampRoute'] = msg[0];
			if(msg[0]==1.0,
				{controller.sendMsg(["/container"++containerNum++"/amp/x"]++[vars['busAmp']])},
				{controller.sendMsg(["/container"++containerNum++"/amp/x"]++[vars['amp']])})});


		controller.addResponder("/container"++containerNum++"/hardwareOut/x", {arg msg;
			vars['hardwareOutMon']=(msg[0]*8-1);
			vars['hardwareOut']=msg[0];

			controller.sendMsg(["/container"++containerNum++"/hardwareOutMon/x"]++[(msg[0]*8-1)]);
			if((msg[0]*8-1)>(-1), {vars['out'] = (msg[0]*8-1).asInteger}, {vars['out']=dummyBus});
			if((msg[0]*8-1)>(-1),
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})});
		});
	}
}