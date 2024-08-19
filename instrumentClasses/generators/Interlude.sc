Interlude : OSCModule {/*-----> A class for the OnsetLooper synth <-----*/
	var <>vars = 0, <>synthIndex=0, <>containerNum=0, <>controller=0;
	var <>synth, <>controlVars = 0, <>synthVars=0;
	var <>rec=0;

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

		rec = Recorder(Server.local);

		vars = Dictionary.newFrom([\gate, 0, \out, 0, \hardwareOutMon, 0, \hardwareOut, 0]);

		controlVars = [\gate, \out, \hardwareOutMon, \hardwareOut]
	}

	updateSynth {
		if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[vars[\out]])});
	}


	makeSynth{
		synth = Synth(\interlude, [
			\buf, GlobalData.audioBuffers[\interlude][0],
			\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
			\out, GlobalBusses.outputSynthBus[vars[\out]]
		], GlobalNodes.nodes[synthIndex]).register;
	}

	//Run Lemur OSCFuncs that control Synth
	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels); //A bus that is routed nowhere (a fail safe if hardwareOut == -1)
		this.save; //add vars to global controls on instance
		this.initLemur; //Init lemur interfaces

		/*__________ RECORD SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/record/x", {arg msg; this.record(msg[0])});


		controller.addResponder("/container"++containerNum++"/gate/x", {arg msg;
			vars[\gate]==msg[0];
			if(msg[0]==1, {this.makeSynth}, {if(synth.isPlaying, {synth.free})});
		});


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