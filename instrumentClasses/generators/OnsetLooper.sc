OnsetLooper : OSCModule {/*-----> A class for the OnsetLooper synth <-----*/
	var <>deltaBuf=0, <>audioBuf=0, <>onsetsBuf=0,  <>offsetsBuf=0,  <>syncBus=0,  <>loopBuf=0;
	var <>macroLoopStart = 0, <>macroLoopEnd = 0;
	var <>class0, <>class1;

	/*____________Constructors____________*/
	*new {arg trigNum, synthNum, lemur, lemurContainer, sync, thisClass, otherClass; ^super.new.init(trigNum, synthNum, lemur, lemurContainer, sync, thisClass, otherClass)}
	*makeSynth {^super.new.makeSynth}
	*run {^super.new.run}

	//Assign each instance an index for its trigger synth, synth, lemur controller,
	//which container in the lemur interface will control this instance and an index
	//for syncing one instance's trigger synth to another
	init { arg trigNum, synthNum, lemur, lemurContainer, sync, thisClass, otherClass;
		//Init the variable "vars" with a dictionary of all the control parameters used for this class and for the lemur controller
		class0 = thisClass;
		class1 = otherClass;
		vars = Dictionary.newFrom([
			'trigSelect', "indiaGlitch", 'sourceSelect', 0, 'synthNum', synthNum, 'trigNum', trigNum, 'syncBusNum', trigNum,
			'audioBufSym', 'drums_808_1',  'onsetsBufSym', 'indiaGlitch-drums_808_1',  'offsetsBufSym', 'indiaGlitch-drums_808_1',
			'amp', 1.0, 'cut', 0, 'freeze', 0, 'out', 0, 'atk', 0, 'rel', 0, 'size', 0, 'rate', 0.5, 'rand', 0, 'busAmp', 1.0, 'fbSel', 0,
			'reverse', 0, 'hardLoop', 0, 'sliceLoop', 0, 'randScaleEndSamp', 0, 'randScale', 0, 'gate', 0, 'panTrigSel', 0,
			'markovOnOff', 0,  'markovs', [1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
			'presetNum', 0,  'loopPresets', [[0, 1, 0.5], [0, 1, 0.5], [0, 1, 0.5], [0, 1, 0.5], [0, 1, 0.5], [0, 1, 0.5], [0, 1, 0.5]],
			'tempo', 0.5, 'loopRange', [0, 1],
			'ampRoute', 0.0, 'trigSelectIndex', 0, 'envMon', 0.005, 'controlType', 0, 'hardwareOutMon', 0, 'hardwareOut', 0.1, 'sliceLoop', 0,
			'xyReset', 0, 'savePreset', 0,
			'soundSources', ["drums_808_1", "analogSynth", "digiGlitch", "chiFieldHack", "indiaGlitch"],
		]);

		//Construct arrays that parse the above dictionary for specfic patameters from the lemur controller, synth instance, and trigger synth instance; respectively
		controlVars = [
			'loopRange', 'trigSelectIndex', 'controlType', 'loopPreset', 'atk', 'envMon', 'hardwareOutMon', 'hardwareOut', 'tempo',
			'size', 'rate', 'amp', 'ampRoute', 'cut', 'reverse', 'sliceLoop', 'hardLoop', 'freeze', 'rand', 'gate', 'markovs', 'markovOnOff',
			'synthNum', 'trigNum', 'savePreset', 'sliceLoop', 'xyReset'
		];
		synthVars = [\fbSel, 'sourceSelect', \markovOnOff, \busAmp, \randScaleEndSamp, \randScale, \rand, \reverse,
			\panTrigSel, \freeze, \cut, \rate, \size, \amp, \sliceLoop, \hardLoop, \atk, \rel, \markovs, \sliceLoop, \size, \gate
		];
		trigVars = ['trigSelect', \loopRange];

		trigIndex=trigNum;             //Assign trigNum to class var trigIndex
		synthIndex=synthNum;           //Assign synthNum to class var synthIndex
		syncIndex=sync;                //Assign sync to class var syncIndex
		syncBusNum = trigNum;          //defines bus numbers connecting trigger to synth
		containerNum = lemurContainer; //Assigns lemur container number to class var
		controller = lemur;            //Assigns lemur instance to class var

		//Init class varibles that hold buffers
		deltaBuf = GlobalData.featuresBuffers['indiaGlitch'][\deltas];
		audioBuf = GlobalData.audioBuffers['drums_808_1'];
		onsetsBuf = GlobalData.featuresBuffers['indiaGlitch-drums_808_1']['onsets'];
		offsetsBuf = GlobalData.featuresBuffers['indiaGlitch-drums_808_1']['offsets'];
		loopBuf = Buffer.new(Server.local);

		//Define bus that Syncs the trigger synth and synth itself
		syncBus = GlobalBusses.stereoOut[trigNum];

		//Global variables that hold the buffer of the macro loop that is performing the concatenative synthesis the source
		macroLoopStart = 0;
		macroLoopEnd = GlobalData.featuresBuffers['indiaGlitch'][\deltas].numFrames.round;

		rec = Recorder(Server.local);
	}

	//Update synth parameters
	updateSynth{
		//Set deltaBuf
		deltaBuf=GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas];

		if(synth.isPlaying,
			{//if Synth is running, set all the parameters

				Routine({
					var macroLoopStartLocal = vars['loopPresets'][vars['presetNum']][0].linlin(0, 1, 0, GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas].numFrames).round;
					var macroLoopEndLocal = vars['loopPresets'][vars['presetNum']][1].linlin(0, 1, 0, GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas].numFrames).round;

					loopBuf.free({loopBuf = Buffer.new(Server.local)});

					Server.local.sync;

					FluidBufCompose.process( //Composer new Buffer (loopBuf) based on deltaBuf and macro loop bounds
						Server.local,
						GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas],
						macroLoopStartLocal,
						macroLoopEndLocal-macroLoopStartLocal,
						0, 1, vars['loopPresets'][vars['presetNum']][2].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);

					Server.local.sync;

					if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, { //If loopBuf exists, set trig and synth parameters
						trig.set(
							\masterBuf, GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas],
							\loopBuf, loopBuf,
							\gate, vars['gate'],
							\loopRange, vars['loopRange'],
							\bus, GlobalBusses.stereoOut[trigIndex]);

						if(synth.isPlaying, {synthVars.do{|i| synth.set(i, GlobalPresets.controls[synthIndex.asSymbol][i])}}); //Non Buffer paramaters
						if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[vars[\out]])});

						vars['soundSources'].do{|i, j| synth.set(\bus, GlobalBusses.stereoOut[vars['syncBusNum']],          //Buffer-related parameters
							("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++i).asSymbol]['onsets'],
							("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++i).asSymbol]['offsets']);
						};
					});
				}).play(SystemClock);
			},
			{if(vars['gate']==1, {this.makeSynth})}
		);
	}


	makeSynth{
		var macroLoopStartLocal = vars['loopPresets'][vars['presetNum']][0].linlin(0, 1, 0, GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas].numFrames).round;
		var macroLoopEndLocal = vars['loopPresets'][vars['presetNum']][1].linlin(0, 1, 0, GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas].numFrames).round;

		this.updateLemur;

		if(vars['freeze'] == 1 && vars['rand']==0,
			{
				vars['freeze'] = 0; vars[\size]=0; vars[\rate]=0.5; vars['xyReset']=0;
				controller.sendMsg(["/container"++containerNum++"/freeze/x"]++[0]);
				controller.sendMsg(["/container"++containerNum++"/rate/x"]++[0.5]);
				controller.sendMsg(["/container"++containerNum++"/size/x"]++[0]);
			},
			{vars['freeze'] = 0; controller.sendMsg(["/container"++containerNum++"/freeze/x"]++[0])});

		Routine({
			loopBuf.free({loopBuf = Buffer.new(Server.local)});
			Server.local.sync;
			FluidBufCompose.process(
				Server.local,
				GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas],
				macroLoopStartLocal,
				macroLoopEndLocal-macroLoopStartLocal,
				0, 1, vars['loopPresets'][vars['presetNum']][2].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);

			Server.local.sync;

			if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {nil}, {
				trig = Synth(\onsetTrigStep, [\gate, 1,
					\masterBuf, GlobalData.featuresBuffers[vars['trigSelect'].asSymbol][\deltas],
					\loopBuf, loopBuf,
					\loopRange, vars['loopRange'],
					\bus, GlobalBusses.stereoOut[trigIndex]
				], GlobalNodes.nodes[trigIndex]).register;

				synth = Synth(\onsetLoopStereo, [\gate, 1,
					\syncBus, GlobalBusses.stereoOut[vars['syncBusNum']],
					\sourceSelect, vars['sourceSelect'],
					\buf0, GlobalData.audioBuffers[vars['soundSources'][0].asSymbol], \buf1, GlobalData.audioBuffers[vars['soundSources'][1].asSymbol],
					\buf2, GlobalData.audioBuffers[vars['soundSources'][2].asSymbol], \buf3, GlobalData.audioBuffers[vars['soundSources'][3].asSymbol],
					\buf4, GlobalData.audioBuffers[vars['soundSources'][4].asSymbol],
					\buf5, nil, \buf6, nil, \buf7, nil, \buf8, nil, \buf9, nil,
					\onsetsBuf0, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][0]).asSymbol]['onsets'],
					\onsetsBuf1, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][1]).asSymbol]['onsets'],
					\onsetsBuf2, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][2]).asSymbol]['onsets'],
					\onsetsBuf3, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][3]).asSymbol]['onsets'],
					\onsetsBuf4, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][4]).asSymbol]['onsets'],
					\onsetsBuf5, nil, \onsetsBuf6, nil, \onsetsBuf7, nil, \onsetsBuf8, nil, \onsetsBuf9, nil,
					\offsetsBuf0, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][0]).asSymbol]['offsets'],
					\offsetsBuf1, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][1]).asSymbol]['offsets'],
					\offsetsBuf2, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][2]).asSymbol]['offsets'],
					\offsetsBuf3, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][3]).asSymbol]['offsets'],
					\offsetsBuf4, GlobalData.featuresBuffers[(vars['trigSelect']++"-"++vars['soundSources'][4]).asSymbol]['offsets'],
					\offsetsBuf5, nil, \offsetsBuf6, nil, \offsetsBuf7, nil, \offsetsBuf8, nil, \offsetsBuf9, nil,
					\sliceLoop, vars[\sliceLoop], \rate, if(vars['freeze'] == 1, {0.5}, {vars[\rate]}), \rand, vars[\rand],
					\amp, vars[\amp], \cut, vars[\cut], \atk, vars[\atk], \rel, vars[\rel], \freeze, 0,
					\busAmp, vars[\busAmp], \randScale, vars[\randScale],
					\fbSel, vars[\fbSel], \randScaleEndSamp, vars[\randScaleEndSamp],
					\reverse, vars[\reverse], \hardLoop, vars[\hardLoop],
					\markovOnOff, vars[\markovOnOff],
					\markovs, vars[\markovs], \panTrigSel, vars[\panTrigSel],
					\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
					\out, GlobalBusses.outputSynthBus[vars[\out]]
				], GlobalNodes.nodes[synthIndex]).register;

				GlobalSynths.synths.add(trigIndex.asSymbol -> trig); //Add synths to dictionary of currenting running synths
				GlobalSynths.synths.add(synthIndex.asSymbol -> synth);
			})});
		}).play(SystemClock);
	}


	//Run Lemur OSCFuncs that control Synth
	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels); //A bus that is routed nowhere (a fail safe if hardwareOut == -1)
		this.save; //add vars to global controls on instance
		this.initLemur; //Init lemur interface

		/*__________ RECORD SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/record/x", {arg msg; this.record(msg[0])});

		/*__________ ROUTE SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/busRoute/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		/*__________ KILL SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/kill/x", {arg msg; this.kill});

		/*__________ GATE SYNTH ___________________________________*/
		//Instantiate and gate synth instances on toggle
		controller.addResponder("/container"++containerNum++"/gate/x", {arg msg;

			vars['gate']=msg[0];

			if(msg[0]==1.0,
				{this.makeSynth},
				{
					if(synth.isPlaying, {synth.set(\gate, 0, \rel, vars['rel']); trig.set(\gate, 0, \rel, vars['rel'])});
					if(trigIndex==class0 && GlobalPresets.getClasses[class1].synth.isPlaying,
						{GlobalPresets.getClasses[class1].synth.set(\fbSel, 0, \syncBus, GlobalBusses.stereoOut[syncIndex])});
					if(trigIndex==class1 && GlobalPresets.getClasses[class0].synth.isPlaying,
						{GlobalPresets.getClasses[class0].synth.set(\fbSel, 0, \syncBus, GlobalBusses.stereoOut[syncIndex])});
				}
			);
		});

		/*__________ AMP ROUTE ___________________________________*/
		//A toggle that selects whether the amp slider controls the hardware out ampitude or the amplitude of the bus sends
		controller.addResponder("/container"++containerNum++"/ampRoute/x", {arg msg;
			vars['ampRoute'] = msg[0];
			if(msg[0]==1.0, {controller.sendMsg(["/container"++containerNum++"/amp/x"]++[vars['busAmp']])}, {controller.sendMsg(["/container"++containerNum++"/amp/x"]++[vars['amp']])})});

		/*__________ AMP SLIDER ___________________________________*/
		controller.addResponder("/container"++containerNum++"/amp/x", {arg msg;
			if(vars['ampRoute']==1.0,
				{if(synth.isPlaying, {synth.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(synth.isPlaying, {synth.set(\amp, msg[0])}); vars['amp'] = msg[0]})});

		/*__________ AMP CUT ___________________________________*/
		controller.addResponder("/container"++containerNum++"/cut/x", {arg msg; vars['cut'] = msg[0]; if(synth.isPlaying, {synth.set(\cut, msg[0])})});

		/*__________ LOOP SLICE ___________________________________*/
		controller.addResponder("/container"++containerNum++"/sliceLoop/x", {arg msg; vars['sliceLoop'] = msg[0]; if(synth.isPlaying, {synth.set(\sliceLoop, msg[0])})});

		/*__________ HARD LOOP ___________________________________*/
		//Loop slice from 2*SAMPLERATE - 256 SAMPLES
		controller.addResponder("/container"++containerNum++"/hardLoop/x", {arg msg; vars['hardLoop']=msg[0]; if(synth.isPlaying, {synth.set(\hardLoop, msg[0])})});

		/*__________ REVERSE LOOPING SLICE ___________________________________*/
		controller.addResponder("/container"++containerNum++"/reverse/x", {arg msg; vars['reverse'] = msg[0].asInteger; if(synth.isPlaying, {synth.set(\reverse, msg[0].asInteger)})});

		/*__________ SLICE SIZE/RATE ___________________________________*/
		//XY Pad that controls a looping slice's size and rate
		controller.addResponder("/container"++containerNum++"/size/x", {arg msg;
			vars['size']=msg[0];
			vars['randScaleEndSamp'] = msg[0];
			if(synth.isPlaying, {synth.set(\size, msg[0], \randScaleEndSamp, msg[0])})});

		controller.addResponder("/container"++containerNum++"/rate/x", {arg msg;
			vars['rate']=msg[0];
			vars['randScale']=msg[0];
			if(synth.isPlaying, {synth.set(\rate, msg[0], \randScale, msg[0])})});

		//Wen the XY pad is released, reset synth parameters or hold them if freeze is engaged
		controller.addResponder("/container"++containerNum++"/sizeRate/z", {arg msg;
			if(synth.isPlaying, {synth.set(\sliceLoop, msg[0])});
			if(synth.isPlaying,{synth.set(\loopHold, msg[0])});
			if(msg[0]==0.0,
				{
					if(vars['xyReset']==0, {vars['size']=0.0; vars['rate'] = 0.5;
						controller.sendMsg(["/container"++containerNum++"/size/x"]++[0]); controller.sendMsg(["/container"++containerNum++"/rate/x"]++[0.5]);
						if(synth.isPlaying,{synth.set(\size, 0.0, \rate, 0.5)})});
			});
		});

		/*__________ FREEZE SYNTH PARAMETERS ___________________________________*/
		controller.addResponder("/container"++containerNum++"/freeze/x", {arg msg;
			vars['freeze']=msg[0];
			if(vars['rand']==0, {vars['xyReset']=msg[0]});
			if(synth.isPlaying, {synth.set(\freeze, msg[0])});
			if(msg[0].asInteger==0, {
				if(vars['rand']==0, {vars['size']=0.0; vars['rate'] = 0.5});
				if(vars['rand']==0, {if(synth.isPlaying,{synth.set(\size, 0)})});
				if(vars['rand']==0, {if(synth.isPlaying, {synth.set(\rate, 0.5)})});
				if(vars['rand']==0, {controller.sendMsg(["/container"++containerNum++"/size/x"]++[0])});
				if(vars['rand']==0, {controller.sendMsg(["/container"++containerNum++"/rate/x"]++[0.5])});
			});
		});

		/*__________ PAN FUNCTION SELECT ___________________________________*/
		controller.addResponder("/container"++containerNum++"/pan/x", {arg msg;
			case
			{ msg[0] == 1 } { if(synth.isPlaying, {synth.set(\panTrigSel, 0)}); controller.sendMsg(["/container"++containerNum++"/pan/x"]++[0, 0, 0]); vars['panTrigSel']=0}
			{ msg[1] == 1 } { if(synth.isPlaying, {synth.set(\panTrigSel, 1)}); controller.sendMsg(["/container"++containerNum++"/pan/x"]++[0, 0, 0]); vars['panTrigSel']=1}
			{ msg[2] == 1 } { if(synth.isPlaying, {synth.set(\panTrigSel, 2)}); controller.sendMsg(["/container"++containerNum++"/pan/x"]++[0, 0, 0]); vars['panTrigSel']=2};
		});

		/*__________ SYNTH ENVELOPES  ___________________________________*/
		controller.addResponder("/container"++containerNum++"/atk/x", {arg msg;
			vars['atk']=msg[0];
			vars['rel']=msg[0];
			vars['envMon']=msg[0].linlin(0.0, 1.0, 0.005, 180.0);
			if(synth.isPlaying, {synth.set(\atk, msg[0])});
			if(synth.isPlaying, {synth.set(\rel, msg[0])});
			controller.sendMsg(["/container"++containerNum++"/envMon/x"]++[msg[0].linlin(0.0, 1.0, 0.005, 180.0)]);
		});

		/*__________ HARDWARE OUT  ___________________________________*/
		controller.addResponder("/container"++containerNum++"/hardwareOut/x", {arg msg;
			vars['hardwareOutMon']=(msg[0]*8-1);
			vars['hardwareOut']=msg[0];
			controller.sendMsg(["/container"++containerNum++"/hardwareOutMon/x"]++[(msg[0]*8-1)]);
			if((msg[0]*8-1)>(-1), {vars['out'] = (msg[0]*8-1).asInteger}, {vars['out']=dummyBus});
			if((msg[0]*8-1)>(-1),
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})});
		});

		/*__________ MARKOV SOURCE SELECT STATE  ___________________________________*/
		//The three functions below decide whether this synth's slice manipulations and sound sources are
		//being controlled dircectly by the lemur interface or by a markov chain that sets weights for how likely
		//a certain sound source is going to be choosen to play back
		controller.addResponder("/container"++containerNum++"/rand/x", {arg msg;
			vars['rand']=msg[0]; //vars['freeze']=msg[0].asInteger;
			vars['xyReset']=msg[0];
			if(synth.isPlaying, {synth.set(\rand, msg[0].asInteger)});
			if(msg[0].asInteger==0, {
				controller.sendMsg(["/container"++containerNum++"/size/x"]++[0]); controller.sendMsg(["/container"++containerNum++"/rate/x"]++[0.5]);
				vars['size']=0.0; vars['rate'] = 0.5;
				if(synth.isPlaying,{synth.set(\size, 0.0, \rate, 0.5)})});
		});

		controller.addResponder("/container"++containerNum++"/markovs/x", {arg msg; vars['markovs']=msg; if(synth.isPlaying, {synth.set(\markovs, msg)})});
		controller.addResponder("/container"++containerNum++"/markovOnOff/x", {arg msg; vars['markovOnOff']=msg[0]; if(synth.isPlaying, {synth.set(\markovOnOff, msg[0])})});

		/*__________ TRIG SELECT ___________________________________*/
		//A Menu that chooses which audio file to use as the concatenative onset triggers
		controller.addResponder("/container"++containerNum++"/trigSelectIndex/x", {arg msg;
			case
			{ msg[0].asInteger == 0 } {/*_____0________________________________________*/
				vars['fbSel'] = 0;
				vars['trigSelectIndex'] = 0;
				vars['trigSelect'] = "indiaGlitch";
				vars['syncBusNum'] = trigIndex;
				deltaBuf=GlobalData.featuresBuffers[\indiaGlitch][\deltas];

				if(trig.isPlaying, {trig.set(\loopChange, 0, \bus, GlobalBusses.stereoOut[vars['syncBusNum']], \loopBuf, GlobalData.featuresBuffers[\indiaGlitch][\deltas], \loopStart, 0, \loopEnd, 1)});

				if(trigIndex==class0,
					{if(GlobalPresets.getClasses[class1].vars['trigSelectIndex']==2, {
						GlobalPresets.getClasses[class1].vars['trigSelect'] = "indiaGlitch";
						vars['soundSources'].do{|i, j| GlobalPresets.getClasses[class1].synth.set(
							("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("indiaGlitch-"++i).asSymbol]['onsets'],
							("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("indiaGlitch-"++i).asSymbol]['offsets']);
						};
					})},
					{if(GlobalPresets.getClasses[class0].vars['trigSelectIndex']==2, {
						GlobalPresets.getClasses[class0].vars['trigSelect'] = "indiaGlitch";
						vars['soundSources'].do{|i, j| GlobalPresets.getClasses[class0].synth.set(
							("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("indiaGlitch-"++i).asSymbol]['onsets'],
							("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("indiaGlitch-"++i).asSymbol]['offsets']);
						};
					};
				)});

				if(synth.isPlaying, {
					vars['soundSources'].do{|i, j| synth.set(
						\fbSel, 0,
						\syncBus, GlobalBusses.stereoOut[vars['syncBusNum']],
						("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("indiaGlitch-"++i).asSymbol]['onsets'],
						("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("indiaGlitch-"++i).asSymbol]['offsets']);
					};
				});
			}
			{ msg[0].asInteger == 1 } {/*_____1________________________________________*/
				vars['fbSel'] = 0;
				vars['trigSelectIndex'] = 1;
				vars['trigSelect'] = "chiFieldHack";
				vars['syncBusNum'] = trigIndex;
				deltaBuf=GlobalData.featuresBuffers[\chiFieldHack][\deltas];
				if(trig.isPlaying, {trig.set(\loopChange, 1, \bus, GlobalBusses.stereoOut[vars['syncBusNum']], \loopBuf, GlobalData.featuresBuffers[\chiFieldHack][\deltas], \loopStart, 0, \loopEnd, 1)});

				if(trigIndex==class0,
					{if(GlobalPresets.getClasses[class1].vars['trigSelectIndex']==2, {GlobalPresets.getClasses[class1].vars['trigSelect'] = "chiFieldHack";
						vars['soundSources'].do{|i, j| GlobalPresets.getClasses[class1].synth.set(
							("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("chiFieldHack-"++i).asSymbol]['onsets'],
							("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("chiFieldHack-"++i).asSymbol]['offsets']);
						};
					});
					},
					{if(GlobalPresets.getClasses[class0].vars['trigSelectIndex']==2, {GlobalPresets.getClasses[class0].vars['trigSelect'] = "chiFieldHack";
						vars['soundSources'].do{|i, j| GlobalPresets.getClasses[class0].synth.set(
							("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("chiFieldHack-"++i).asSymbol]['onsets'],
							("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("chiFieldHack-"++i).asSymbol]['offsets']);
						};
					});
					}
				);

				if(synth.isPlaying, {
					vars['soundSources'].do{|i, j| synth.set(
						\fbSel, 0,
						\syncBus, syncBus,
						("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("chiFieldHack-"++i).asSymbol]['onsets'],
						("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[("chiFieldHack-"++i).asSymbol]['offsets']);
					};
				});
			}
			{ msg[0].asInteger == 2 } {/*_____2*________________________________________*/
				vars['syncBusNum'] = syncIndex;
				vars['trigSelectIndex'] = 2;
				if(trigIndex==class0,
					{vars['trigSelect'] = GlobalPresets.getClasses[class1].vars['trigSelect']; vars['fbSel']=1;
						if(synth.isPlaying, {synth.set(\fbSel, 1, \syncBus, GlobalBusses.stereoOut[syncIndex]);
							vars['soundSources'].do{|i, j| synth.set(
								("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[(GlobalPresets.getClasses[class1].vars['trigSelect']++"-"++i).asSymbol]['onsets'],
								("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[(GlobalPresets.getClasses[class1].vars['trigSelect']++"-"++i).asSymbol]['offsets']);
							};
						});
					},
					{vars['trigSelect'] = GlobalPresets.getClasses[class0].vars['trigSelect']; vars['fbSel']=0;
						if(synth.isPlaying, {synth.set(\fbSel, 0, \syncBus, GlobalBusses.stereoOut[syncIndex]);
							vars['soundSources'].do{|i, j| synth.set(
								("onsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[(GlobalPresets.getClasses[class0].vars['trigSelect']++"-"++i).asSymbol]['onsets'],
								("offsetsBuf"++j).asSymbol, GlobalData.featuresBuffers[(GlobalPresets.getClasses[class0].vars['trigSelect']++"-"++i).asSymbol]['offsets']);
							};
						});
					}
				);
			};
		});

		/*__________ SELECT SOURCE AUDIO ___________________________________*/
		//Case function to select concatenative looper sound source
		controller.addResponder("/container"++containerNum++"/sourceSelect/x", {arg msg;

			//Reset control interface
			controller.sendMsg(["/container"++containerNum++"/sourceSelect/x"]++[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
			case
			{ msg[0] == 1 } { vars['sourceSelect']=0;  if(synth.isPlaying, {synth.set('sourceSelect', 0)})}
			{ msg[1] == 1 } { vars['sourceSelect']=1;  if(synth.isPlaying, {synth.set('sourceSelect', 1)})}
			{ msg[2] == 1 } { vars['sourceSelect']=2;  if(synth.isPlaying, {synth.set('sourceSelect', 2)})}
			{ msg[3] == 1 } { vars['sourceSelect']=3;  if(synth.isPlaying, {synth.set('sourceSelect', 3)})}
			{ msg[4] == 1 } { vars['sourceSelect']=4;  if(synth.isPlaying, {synth.set('sourceSelect', 4)})}
			{ msg[5] == 1 } { vars['sourceSelect']=5;  if(synth.isPlaying, {synth.set('sourceSelect', 5)})}
			{ msg[6] == 1 } { vars['sourceSelect']=6;  if(synth.isPlaying, {synth.set('sourceSelect', 6)})}
			{ msg[7] == 1 } { vars['sourceSelect']=7;  if(synth.isPlaying, {synth.set('sourceSelect', 7)})}
			{ msg[8] == 1 } { vars['sourceSelect']=8;  if(synth.isPlaying, {synth.set('sourceSelect', 8)})}
			{ msg[9] == 1 } { vars['sourceSelect']=9;  if(synth.isPlaying, {synth.set('sourceSelect', 9)})};
		});

		/*__________ DELTABUF SCRUB  ___________________________________*/
		//Scrub through the macro loop (deltaBuf)
		//Due to the nature of ListTrig.kr, to access specfic bounds within a deltaBuf
		//you need to recompose the buffer based on the bounds you desire
		controller.addResponder("/container"++containerNum++"/loopRange/x", {arg msg;
			var loopStartLocal, loopEndLocal;

			loopStartLocal = msg[0].linlin(0, 1, 0, deltaBuf.numFrames).round;
			loopEndLocal = msg[1].linlin(0, 1, 0, deltaBuf.numFrames).round;

			macroLoopStart = msg[0].linlin(0, 1, 0, deltaBuf.numFrames).round;
			macroLoopEnd = msg[1].linlin(0, 1, 0, deltaBuf.numFrames).round;

			vars['loopRange'] = msg;

			Routine({
				loopBuf.free({loopBuf = Buffer.new(Server.local)});
				Server.local.sync;
				FluidBufCompose.process(Server.local, deltaBuf, loopStartLocal, loopEndLocal-loopStartLocal, 0, 1, vars['tempo'].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);
				Server.local.sync;
				if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf, \loopRange, msg)})});
			}).play(SystemClock);
		});

		/*__________ DELTABUF SPEED/TEMPO  ___________________________________*/
		//Control how fast to proceed though a deltaBuf. Since ListTrig.kr accepts delta times, or onsets
		//a buffer's gain setting will lengthen or compress these delta times, which is what is happening here.
		controller.addResponder("/container"++containerNum++"/tempo/x", {arg msg;
			vars['tempo'] = msg[0];
			vars['loopPresets'][vars['presetNum']][2]=msg[0];
			Routine({
				loopBuf.free({loopBuf = Buffer.new(Server.local)});
				Server.local.sync;
				FluidBufCompose.process(Server.local, deltaBuf, macroLoopStart, macroLoopEnd-macroLoopStart, 0, 1, msg[0].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);
				Server.local.sync;
				if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf)})});
			}).play(SystemClock);

		});

		/*__________ RESET DELTABUF SPEED/TEMPO ON BUTTON  ___________________________________*/
		controller.addResponder("/container"++containerNum++"/tempoReset/x", {arg msg;
			vars['tempo'] = 0.5; controller.sendMsg(["/container"++containerNum++"/tempo/x"]++[0.5]);
			if(msg[0].asInteger==1, {
				Routine({
					loopBuf.free({loopBuf = Buffer.new(Server.local)});
					Server.local.sync;
					FluidBufCompose.process(Server.local, deltaBuf, macroLoopStart, macroLoopEnd-macroLoopStart, 0, 1, 1, loopBuf);
					Server.local.sync;
					if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf)})});
				}).play(SystemClock);
			});
		});

		/*__________ SAVE MACRO LOOP PRESET  ___________________________________*/
		//A toggle that changes whether you are saving a macroLoop preset, or retrieving a macroLoop preset
		controller.addResponder("/container"++containerNum++"/savePreset/x", {arg msg; vars[\savePreset] = msg[0]});

		/*__________ EXECUTING MACROLOOP PRESETS  ___________________________________*/
		controller.addResponder("/container"++containerNum++"/loopPreset/x", {arg msg; controller.sendMsg(["/container"++containerNum++"/loopPreset/x"]++[0, 0, 0, 0, 0, 0, 0]);
			case
			{ msg[0] == 1 } { //PRESET 0 IS ALWAYS THE WHOLE LOOP AT THE NORMAL TEMPO
				vars['presetNum']=0;
				vars['tempo'] = 0.5;
				macroLoopStart = 0.linlin(0, 1, 0, deltaBuf.numFrames).round;
				macroLoopEnd = 1.linlin(0, 1, 0, deltaBuf.numFrames).round;
				Routine({
					loopBuf.free({loopBuf = Buffer.new(Server.local)});
					Server.local.sync;
					FluidBufCompose.process(Server.local, deltaBuf, 0, deltaBuf.numFrames, 0, 1, 1, loopBuf);
					Server.local.sync;
					if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf, \loopRange, [0, 1])})});
					controller.sendMsg(["/container"++containerNum++"/loopRange/x"]++[0, 1]);
				}).play(SystemClock);

			}
			{ msg[1] == 1 } {
				var loopStartLocal= vars['loopPresets'][1][0].linlin(0, 1, 0, deltaBuf.numFrames).round, loopStartEnd=vars['loopPresets'][1][1].linlin(0, 1, 0, deltaBuf.numFrames).round;
				vars['presetNum']=1;

				if(vars[\savePreset] == 0.0,
					{
						vars['loopPresets'][1]=vars['loopRange']++vars['tempo'];
					},
					{
						macroLoopStart = vars['loopPresets'][0].linlin(0, 1, 0, deltaBuf.numFrames).round;
						macroLoopEnd = vars['loopPresets'][1].linlin(0, 1, 0, deltaBuf.numFrames).round;

						Routine({
							loopBuf.free({loopBuf = Buffer.new(Server.local)});
							Server.local.sync;
							FluidBufCompose.process(Server.local, deltaBuf, loopStartLocal, loopStartEnd-loopStartLocal, 0, 1, vars['loopPresets'][1][2].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);
							Server.local.sync;
							if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf, \loopRange, vars['loopPresets'][1][0..1])})});
							controller.sendMsg(["/container"++containerNum++"/loopRange/x"]++vars['loopPresets'][1][0..1]);
							controller.sendMsg(["/container"++containerNum++"/tempo/x"]++[vars['loopPresets'][1][2]]);
						}).play(SystemClock);
					}
				);
			}
			{ msg[2] == 1 } {
				var loopStartLocal= vars['loopPresets'][2][0].linlin(0, 1, 0, deltaBuf.numFrames).round, loopStartEnd=vars['loopPresets'][2][1].linlin(0, 1, 0, deltaBuf.numFrames).round;
				vars['presetNum']=1;

				if(vars[\savePreset] == 0.0,
					{
						vars['loopPresets'][2]=vars['loopRange']++vars['tempo'];
					},
					{
						macroLoopStart = vars['loopPresets'][0].linlin(0, 1, 0, deltaBuf.numFrames).round;
						macroLoopEnd = vars['loopPresets'][2].linlin(0, 1, 0, deltaBuf.numFrames).round;

						Routine({
							loopBuf.free({loopBuf = Buffer.new(Server.local)});
							Server.local.sync;
							FluidBufCompose.process(Server.local, deltaBuf, loopStartLocal, loopStartEnd-loopStartLocal, 0, 1, vars['loopPresets'][2][2].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);
							Server.local.sync;
							if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf, \loopRange, vars['loopPresets'][2][0..1])})});
							controller.sendMsg(["/container"++containerNum++"/loopRange/x"]++vars['loopPresets'][2][0..1]);
							controller.sendMsg(["/container"++containerNum++"/tempo/x"]++[vars['loopPresets'][2][2]]);
						}).play(SystemClock);
					}
				);
			}
			{ msg[3] == 1 } {
				var loopStartLocal= vars['loopPresets'][3][0].linlin(0, 1, 0, deltaBuf.numFrames).round, loopStartEnd=vars['loopPresets'][3][1].linlin(0, 1, 0, deltaBuf.numFrames).round;
				vars['presetNum']=1;

				if(vars[\savePreset] == 0.0,
					{
						vars['loopPresets'][3]=vars['loopRange']++vars['tempo'];
					},
					{
						macroLoopStart = vars['loopPresets'][0].linlin(0, 1, 0, deltaBuf.numFrames).round;
						macroLoopEnd = vars['loopPresets'][3].linlin(0, 1, 0, deltaBuf.numFrames).round;

						Routine({
							loopBuf.free({loopBuf = Buffer.new(Server.local)});
							Server.local.sync;
							FluidBufCompose.process(Server.local, deltaBuf, loopStartLocal, loopStartEnd-loopStartLocal, 0, 1, vars['loopPresets'][3][2].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);
							Server.local.sync;
							if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf, \loopRange, vars['loopPresets'][3][0..1])})});
							controller.sendMsg(["/container"++containerNum++"/loopRange/x"]++vars['loopPresets'][3][0..1]);
							controller.sendMsg(["/container"++containerNum++"/tempo/x"]++[vars['loopPresets'][3][2]]);
						}).play(SystemClock);
					}
				);
			}
			{ msg[4] == 1 } {
				var loopStartLocal= vars['loopPresets'][4][0].linlin(0, 1, 0, deltaBuf.numFrames).round, loopStartEnd=vars['loopPresets'][4][1].linlin(0, 1, 0, deltaBuf.numFrames).round;
				vars['presetNum']=1;

				if(vars[\savePreset] == 0.0,
					{
						vars['loopPresets'][4]=vars['loopRange']++vars['tempo'];
					},
					{
						macroLoopStart = vars['loopPresets'][0].linlin(0, 1, 0, deltaBuf.numFrames).round;
						macroLoopEnd = vars['loopPresets'][4].linlin(0, 1, 0, deltaBuf.numFrames).round;

						Routine({
							loopBuf.free({loopBuf = Buffer.new(Server.local)});
							Server.local.sync;
							FluidBufCompose.process(Server.local, deltaBuf, loopStartLocal, loopStartEnd-loopStartLocal, 0, 1, vars['loopPresets'][4][2].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);
							Server.local.sync;
							if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf, \loopRange, vars['loopPresets'][4][0..1])})});
							controller.sendMsg(["/container"++containerNum++"/loopRange/x"]++vars['loopPresets'][4][0..1]);
							controller.sendMsg(["/container"++containerNum++"/tempo/x"]++[vars['loopPresets'][4][2]]);
						}).play(SystemClock);
					}
				);
			}

			{ msg[5] == 1 } {
				var loopStartLocal= vars['loopPresets'][5][0].linlin(0, 1, 0, deltaBuf.numFrames).round, loopStartEnd=vars['loopPresets'][5][1].linlin(0, 1, 0, deltaBuf.numFrames).round;
				vars['presetNum']=1;

				if(vars[\savePreset] == 0.0,
					{
						vars['loopPresets'][5]=vars['loopRange']++vars['tempo'];
					},
					{
						macroLoopStart = vars['loopPresets'][0].linlin(0, 1, 0, deltaBuf.numFrames).round;
						macroLoopEnd = vars['loopPresets'][5].linlin(0, 1, 0, deltaBuf.numFrames).round;

						Routine({
							loopBuf.free({loopBuf = Buffer.new(Server.local)});
							Server.local.sync;
							FluidBufCompose.process(Server.local, deltaBuf, loopStartLocal, loopStartEnd-loopStartLocal, 0, 1, vars['loopPresets'][5][2].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);
							Server.local.sync;
							if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf, \loopRange, vars['loopPresets'][5][0..1])})});
							controller.sendMsg(["/container"++containerNum++"/loopRange/x"]++vars['loopPresets'][5][0..1]);
							controller.sendMsg(["/container"++containerNum++"/tempo/x"]++[vars['loopPresets'][5][2]]);
						}).play(SystemClock);
					}
				);
			}
			{ msg[6] == 1 } {
				var loopStartLocal= vars['loopPresets'][6][0].linlin(0, 1, 0, deltaBuf.numFrames).round, loopStartEnd=vars['loopPresets'][6][1].linlin(0, 1, 0, deltaBuf.numFrames).round;
				vars['presetNum']=1;

				if(vars[\savePreset] == 0.0,
					{
						vars['loopPresets'][6]=vars['loopRange']++vars['tempo'];
					},
					{
						macroLoopStart = vars['loopPresets'][0].linlin(0, 1, 0, deltaBuf.numFrames).round;
						macroLoopEnd = vars['loopPresets'][6].linlin(0, 1, 0, deltaBuf.numFrames).round;

						Routine({
							loopBuf.free({loopBuf = Buffer.new(Server.local)});
							Server.local.sync;
							FluidBufCompose.process(Server.local, deltaBuf, loopStartLocal, loopStartEnd-loopStartLocal, 0, 1, vars['loopPresets'][6][2].linlin(0, 1, 2, 0).clip(0.15, 2), loopBuf);
							Server.local.sync;
							if((loopBuf.numFrames)!=nil||(loopBuf.numFrames)!=0, {if(trig.isPlaying, {trig.set(\loopBuf, loopBuf, \loopRange, vars['loopPresets'][6][0..1])})});
							controller.sendMsg(["/container"++containerNum++"/loopRange/x"]++vars['loopPresets'][6][0..1]);
							controller.sendMsg(["/container"++containerNum++"/tempo/x"]++[vars['loopPresets'][6][2]]);
						}).play(SystemClock);
					}
				);
			}
		});
	}
}