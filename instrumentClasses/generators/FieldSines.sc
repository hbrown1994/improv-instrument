FieldSines : OSCModule {/*-----> A class for the OnsetLooper synth <-----*/

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

		vars = Dictionary.newFrom([
			\gate, 0,
			\mix0, 0, \mix1, 0,
			\buf0, 0, \buf1, 1, \amp, 1, \out, 0,
			\morph0, 0, \morph1, 0,
			\bend0, 0.5, \stretch0, 0.5,
			\bend1, 0.5, \stretch1, 0.5,
			\bend2, 0.5, \stretch2, 0.5,
			\bend3, 0.5, \stretch3, 0.5,
			\ampRoute, 0,
			\busAmp, 1, \amp0, 1, \amp1, 1,
			\play0, 0, \play1, 0,
			\rate0, 0.5, \rate1, 0.5,
			\env0, 0, \envMon0, 0, \env1, 0, \envMon1, 0,
			\lpf0, 1, \hpf0, 0, \lpf1, 1, \hpf1, 0,
			\panFreq0, 0, \panFreq1, 0, \panLatch0, 0, \panLatch1, 0,
			\sine0, 0, \sine1, 0, \sine2, 0, \sine3, 0,
			\synthNum, synthNum, \filterSel0, 0, \filterSel1, 0,
			\filterFreq0, 1, \filterFreq1, 1,
			\filterMon0, 10000, \filterMon1, 10000,
			\hardwareOutMon, 0, \hardwareOut, 0
		]);

		controlVars = [\filterMon0, \filterMon1, \filterFreq0, \filterFreq1, \synthNum, \mix0, \mix1, \buf0,  \buf1,  \amp,  \out, \hardwareOutMon, \hardwareOut, \morph0, \morph1, \bend0, \stretch0, \bend1, \stretch1, \bend2, \stretch2, \bend3, \stretch3, \ampRoute, \busAmp, \amp0, \amp1, \play0, \play1, \rate0, \rate1, \env0, \envMon0, \env1, \envMon1, \lpf0, \hpf0,  \lpf1,  \hpf1, \panFreq0, \panFreq1, \panLatch0, \panLatch1, \sine0, \sine1, \sine2, \sine3, \filterSel0, \filterSel1, \gate];

		synthVars = [\mix0, \mix1, \buf0,  \buf1,  \amp,  \out, \hardwareOutMon, \hardwareOut, \morph0, \morph1, \bend0, \stretch0, \bend1, \stretch1, \bend2, \stretch2, \bend3, \stretch3, \ampRoute, \busAmp, \amp0, \amp1, \play0, \play1, \rate0, \rate1, \env0, \envMon0, \env1, \envMon1, \lpf0, \hpf0,  \lpf1,  \hpf1, \panFreq0, \panFreq1, \panLatch0, \panLatch1, \sine0, \sine1, \sine2, \sine3];
	}

	//Update synth parameters
	updateSynth{
		if(synth.isPlaying,
			{if(vars[\gate]==1, {synthVars.do{|i| synth.set(i, vars[i])}}, {synth.set(\gate, 0)})},
			{if(vars[\gate]==1, {this.makeSynth}, {nil})});
	}

	makeSynth{
		synth = Synth(\fieldFFTSines, [
			\gate, 1,
			\buf0, GlobalData.audioBuffers['1_fieldRecordings'][vars[\buf0]],
			\buf1, GlobalData.audioBuffers['1_fieldRecordings'][vars[\buf1]],
			\amp, vars[\amp],
			\mix0, vars[\mix0], \mix1, vars[\mix1],
			\morph0, vars[\morph0], \morph1, vars[\morph1],
			\bend0, vars[\bend0], \stretch0, vars[\stretch0],
			\bend1, vars[\bend1], \stretch1, vars[\stretch1],
			\bend2, vars[\bend2], \stretch2, vars[\stretch2],
			\bend3, vars[\bend3], \stretch3, vars[\stretch3],
			\busAmp, vars[\busAmp], \amp0, vars[\amp0], \amp1, vars[\amp1],
			\play0, vars[\play0], \play1, vars[\play1],
			\env0, vars[\env0], \env1, vars[\env1],
			\rate0, vars[\rate0], \rate1, vars[\rate1],
			\lpf0, vars[\lpf0], \hpf0, vars[\hpf0], \lpf1, vars[\lpf1], \hpf1, vars[\hpf1],
			\panFreq0, vars[\panFreq0], \panFreq1, vars[\panFreq1], \panLatch0, vars[\panLatch0], \panLatch1, vars[\panLatch1],
			\sine0, vars[\sine0], \sine1, vars[\sine1], \sine2, vars[\sine2], \sine3, vars[\sine3],
			\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
			\out, GlobalBusses.outputSynthBus[vars[\out]]
		],  GlobalNodes.nodes[synthIndex]).register;

		GlobalSynths.synths.add(synthIndex.asSymbol -> synth);
	}

	//Run Lemur OSCFuncs that control Synth
	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels); //A bus that is routed nowhere (a fail safe if hardwareOut == -1)
		this.save; //add vars to global controls on instance
		this.initLemur; //Init lemur interface

		//populate menus
		{
			arg names=GlobalData.audioBufferNames['1_fieldRecordings'];
			var strings=List.new(0);

			(names.size+1).do{|i| if(i==(names.size),
				{2.do{|i| controller.sendMsg(["/container"++containerNum++"/buf"++i, "@items"]++strings.asArray)}},
				{strings.add(names[i].asString)});
			};
		}.value;

		/*__________ RECORD SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/record/x", {arg msg; this.record(msg[0])});

		/*__________ ROUTE SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/busRoute/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		/*__________ KILL SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/kill/x", {arg msg; this.kill});

		controller.addResponder("/container"++containerNum++"/gate/x", {arg msg;
			vars[\gate]=msg[0];
			if(msg[0]==1, {this.makeSynth},
				{if(synth.isPlaying, synth.set(\gate, 0, \env, vars[\env]))});
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

		controller.addResponder("/container"++containerNum++"/kill/x", {arg msg; synth.free});

		controller.addResponder("/container"++containerNum++"/hardwareOut/x", {arg msg;
			vars['hardwareOutMon']=(msg[0]*8-1);
			vars['hardwareOut']=msg[0];

			controller.sendMsg(["/container"++containerNum++"/hardwareOutMon/x"]++[(msg[0]*8-1)]);
			if((msg[0]*8-1)>(-1), {vars['out'] = (msg[0]*8-1).asInteger}, {vars['out']=dummyBus});
			if((msg[0]*8-1)>(-1),
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})});
		});

		["rate", "panFreq", "panLatch", "play", "amp", "mix", "freeze", "morph"].do{|param|
			2.do{|i|

				controller.addResponder("/container"++containerNum++"/"++param++i++"/x", {arg msg;
					vars[(param++i).asSymbol]=msg[0];
					if(synth.isPlaying, {synth.set((param++i).asSymbol, msg[0])});
				});
			};
		};

		["stretch", "bend", "sine"].do{|param|
			4.do{|i|
				controller.addResponder("/container"++containerNum++"/"++param++i++"/x", {arg msg;
					vars[(param++i).asSymbol]=msg[0];
					if(synth.isPlaying, {synth.set((param++i).asSymbol, msg[0])});
				});
			};
		};

		//mix
		2.do{|i|

			controller.addResponder("/container"++containerNum++"/env"++i++"/x", {arg msg;
				controller.sendMsg(["/container"++containerNum++"/envMon"++i++"/x"]++[msg[0].linlin(0.0, 1.0, 0.005, 180.0)]);
				vars[("env"++i).asSymbol]=msg[0];
				vars[("envMon"++i).asSymbol]=msg[0].linlin(0.0, 1.0, 0.005, 180.0);
				if(synth.isPlaying, {synth.set(("env"++i).asSymbol, msg[0])});
			});

			controller.addResponder("/container"++containerNum++"/buf"++i++"/x", {arg msg;
				vars[("buf"++i).asSymbol]=msg[0].asInteger;
				if(synth.isPlaying, {synth.set(("buf"++i).asSymbol, GlobalData.audioBuffers['1_fieldRecordings'][msg[0].asInteger])});
			});

			controller.addResponder("/container"++containerNum++"/buf"++i++"/x", {arg msg;
				vars[("buf"++i).asSymbol]=msg[0].asInteger;
				if(synth.isPlaying, {synth.set(("buf"++i).asSymbol, GlobalData.audioBuffers['1_fieldRecordings'][msg[0].asInteger])});
			});

			controller.addResponder("/container"++containerNum++"/filterSel"++i++"/x", {arg msg;
				vars[("filterSel"++i).asSymbol]=msg[0].asInteger;
				if(msg[0]==0,
					{
						controller.sendMsg(["/container"++containerNum++"/filterMon"++i++"/x"]++[vars[("lpf"++i).asSymbol].linlin(0.0, 1.0, 40, 10000)]);
						controller.sendMsg(["/container"++containerNum++"/filterFreq"++i++"/x"]++[vars[("lpf"++i).asSymbol]]);

					},
					{
						controller.sendMsg(["/container"++containerNum++"/filterMon"++i++"/x"]++[vars[("hpf"++i).asSymbol].linlin(0.0, 1.0, 40, 10000)]);
						controller.sendMsg(["/container"++containerNum++"/filterFreq"++i++"/x"]++[vars[("hpf"++i).asSymbol]]);
					}
				);
			});

			controller.addResponder("/container"++containerNum++"/filterFreq"++i++"/x", {arg msg;
				vars[("filterFreq"++i).asSymbol];
				if(vars[("filterSel"++i).asSymbol]==0,
					{
						controller.sendMsg(["/container"++containerNum++"/filterMon"++i++"/x"]++[vars[("lpf"++i).asSymbol].linlin(0.0, 1.0, 40, 10000)]);
						vars[("lpf"++i).asSymbol]=msg[0];
						if(synth.isPlaying, {synth.set(("lpf"++i).asSymbol, msg[0])})
					}, //LPF
					{
						controller.sendMsg(["/container"++containerNum++"/filterMon"++i++"/x"]++[vars[("hpf"++i).asSymbol].linlin(0.0, 1.0, 40, 10000)]);
						vars[("hpf"++i).asSymbol]=msg[0];
						if(synth.isPlaying, {synth.set(("hpf"++i).asSymbol, msg[0])})}
				);

				vars[("filterFreq"++i).asSymbol]=msg[0].asInteger;
			});

		};
	}
}