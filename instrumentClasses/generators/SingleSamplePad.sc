SingleSamplePad : OSCModule {/*-----> A class for the OnsetLooper synth <-----*/

	/*____________Constructors____________*/
	*new {arg synthNum, lemur, lemurContainer; ^super.new.init(synthNum, lemur, lemurContainer)}
	*makeSynth {^super.new.makeSynth}
	*run {^super.new.run}

	//Assign each instance an index for its trigger synth, synth, lemur controller,
	//which container in the lemur interface will control this instance and an index
	//for syncing one instance's trigger synth to another
	init { arg synthNum, lemur, lemurContainer;
		//Init the variable "vars" with a dictionary of all the control parameters used for this class and for the lemur controller
		synthIndex=synthNum;           //Assign synthNum to class var synthIndex
		containerNum = lemurContainer; //Assigns lemur container number to class var
		controller = lemur;            //Assigns lemur instance to class var

		vars = Dictionary.newFrom([
			\gate, 0, \synthIndex, synthNum,
			\amp, 1, \out, 0, \busAmp, 1, \ampRoute, 0,
			\hardwareOutMon, 0, \hardwareOut, 0,
		]);

		controlVars = [\synthNum, \amp,  \out, \hardwareOutMon, \hardwareOut, \ampRoute, \busAmp,  \gate];

	}

	//Update synth parameters
	updateSynth{
		if(synth.isPlaying,
			{if(vars[\gate]==1, {synthVars.do{|i| synth.set(i, vars[i])}}, {synth.set(\gate, 0)})},
			{if(vars[\gate]==1, {this.makeSynth}, {nil})});
	}

	makeSynth{
		synth = Synth(\singleSamplePad, [
			\gate, 1, \amp, vars[\amp], \busAmp, vars[\busAmp],
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

		["trig", "shift", "cut", "morph"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg; synth.set(param.asSymbol, msg[0])});
		};

		3.do{|i|
			controller.addResponder("/container"++containerNum++"/"++["res", "rlpf", "rhpf"][i]++"/x", {arg msg; synth.set(\filter, i)});
		};

		/*__________ ROUTE SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/busRoute/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		/*__________ KILL SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/kill/x", {arg msg; this.kill});

		controller.sendMsg(["/container"++containerNum++"/synthNum/x"]++[synthIndex]);

		controller.addResponder("/container"++containerNum++"/gate/x", {arg msg;
			vars[\gate]=msg[0];
			if(msg[0]==1, {if(synth.isPlaying, {nil},{ this.makeSynth})}, {if(synth.isPlaying, synth.set(\gate, 0))});
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