FieldRecordings : OSCModule {
	var <>buffers, <>bufferNames, <>minRate, <>maxRate, <>lpfHigh, <>lpfLow, <>hpfHigh, <>hpfLow, <>otherPresets, <>otherPresetsSplit, <>presets, <>bufRandInit, <>sumBus0, <>sumBus1, <>fieldGroup, <>sumBusSynth, <>synthSplit;

	/*____________Constructors____________*/
	*new {arg synthNum, lemur, lemurContainer; ^super.new.init(synthNum, lemur, lemurContainer)}
	*makeSynth {^super.new.makeSynth}
	*makeSplitSynth {^super.new.makeSplitSynth}
	*run {^super.new.run}

	//Assign each instance an index for its trigger synth, synth, lemur controller,
	//which container in the lemur interface will control this instance and an index
	//for syncing one instance's trigger synth to another
	init { arg synthNum, lemur, lemurContainer;
		//Init the variable "vars" with a dictionary of all the control parameters used for this class and for the lemur controller

		synthIndex=synthNum;           //Assign synthNum to class var synthIndex
		containerNum = lemurContainer; //Assigns lemur container number to class var
		controller = lemur;            //Assigns lemur instance to class var

		buffers = GlobalData.audioBuffers[\fieldRecordings0];
		bufferNames = GlobalData.audioBufferNames[\fieldRecordings0];
		minRate = 0.01;
		maxRate = 2;

		lpfHigh=20000;
		lpfLow=60;

		hpfHigh=8000;
		hpfLow=30;

		otherPresets = [
			[1, 2, 3, 4, 5, 6, 7],
			[0, 2, 3, 4, 5, 6, 7],
			[1, 0, 3, 4, 5, 6, 7],
			[1, 2, 0, 4, 5, 6, 7],
			[1, 2, 3, 0, 5, 6, 7],
			[1, 2, 3, 4, 0, 6, 7],
			[1, 2, 3, 4, 5, 0, 7],
			[1, 2, 3, 4, 5, 6, 0],
		];

		otherPresetsSplit = [
			[
				[1, 2, 3],
				[0, 2, 3],
				[1, 0, 3],
				[1, 2, 0],
			],
			([
				[1, 2, 3],
				[0, 2, 3],
				[1, 0, 3],
				[1, 2, 0],
			]+4),
		];

		(
			presets = List.new;
			bufRandInit = Array.series(buffers.size, 0, 1);
			8.do{|i|
				presets.add(Dictionary.newFrom([\buf, bufRandInit[i], \rate, 1, \rangeMin, 0, \rangeMax, 1, \lpf, 1, \hpf, 0, \envSize, 0]));
			};
		);

		sumBus0 = Bus.audio(Server.local,  GlobalPresets.numChannels);
		sumBus1 = Bus.audio(Server.local,  GlobalPresets.numChannels);

		fieldGroup = Group.new(GlobalNodes.nodes[synthIndex], addAction: 'addToHead');

		rec = Recorder(Server.local);

		vars = Dictionary.newFrom([
			\gate, 0, \buf, 0,  \out, 0, \synthNum, synthNum,
			\ampRoute, 0, \amp, 1, \bufAmp, 1,
			\rate, 1, \rangeMin, 0, \rangeMax, 1, \presetIndex, 0, \trigSel, 0,
			\lpf, 1, \hpf, 0, \trigFreq, 0, \rand, 0, \split, 0, \ampSplit, 0, \splitSel, 0,
			\markov0, 0,  \markov1, 0,  \markov2, 0,  \markov3, 0,
			\markov4, 0,  \markov5, 0,  \markov6, 0,  \markov7, 0,
			\rand0, 0,  \rand1, 0,  \rand2, 0,  \rand3, 0,
			\rand4, 0,  \rand5, 0,  \rand6, 0,  \rand7, 0, \envSize, 0, \lag, 0,
			'hardwareOut', 0, 'hardwareOutMon', 0,
		]);

		controlVars = [\hardwareOut, \hardwareOut, \rand, \split, \gate, \ampRoute, \amp, \ampSplit, \lag, \envSize, \synthNum];

	}

	//Update synth parameters
	updateSynth {
		if(synth.isPlaying,
			{if(vars[\gate]==1, {synthVars.do{|i| synth.set(i, vars[i])}}, {synth.set(\gate, 0); sumBusSynth.set(\gate, 0)})},
			{if(vars[\gate]==1, {this.makeSynth}, {nil})});
	}

	makeSynth {
		sumBusSynth = Synth(\sumBus, [
			\in0, sumBus0, \in1, sumBus1, \gate, 1,
			\monoBus, GlobalBusses.allOut[\1][synthIndex],
			\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
			\out, GlobalBusses.outputSynthBus[vars[\out]],
		],  GlobalNodes.nodes[synthIndex], 'addToTail').register;

		synth = Synth(\fieldRecording, [
			\out, sumBus0, \rate, vars[\rate], \buf, buffers[vars['buf']],
			\rangeMin, vars['rangeMin'], \rangeMax, vars['rangeMax'],
			\lpf, vars['lpf'], \hpf, vars['hpf'], \amp, vars['amp'] ,\gate, 1, \rand, vars['rand'],
			\trigSel, vars['trigSel'], \lpfLow, lpfLow, \lpfHigh, lpfHigh, \hpfLow, hpfLow, \hpfHigh, hpfHigh
		],  fieldGroup).register;
	}

	makeSplitSynth {
		synthSplit = Synth(\fieldRecordingSplit, [
			\out, sumBus1, \rate, presets[4][\rate], \buf, buffers[presets[4][\buf]],
			\rangeMin, presets[4][\rangeMin],  \rangeMax, presets[4]['rangeMax'],
			\lpf, presets[4]['lpf'], \hpf, presets[4]['hpf'], \amp, vars[\ampSplit], \gate, 1, \rand, vars['rand'],
			\trigSel, vars['trigSel'], \lpfLow, lpfLow, \lpfHigh, lpfHigh, \hpfLow, hpfLow, \hpfHigh, hpfHigh
		],  fieldGroup).register;

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

		controller.sendMsg(["/container"++containerNum+/+"buf", "@items"]++[bufferNames[vars['buf']].asString]);

		controller.addResponder("/container"++containerNum+/+"bufSlide"+/+"x", {arg msg;
			vars['buf'] = msg[0].linlin(0, 1,  0, bufferNames.size).asInteger;
			vars['bufSlide'] = msg[0].linlin(0, 1,  0, bufferNames.size).asInteger;
			presets[vars[\presetIndex]]['buf'] = msg[0].linlin(0, 1,  0, bufferNames.size).asInteger;

			controller.sendMsg(["/container"++containerNum+/+"buf", "@items"]++[bufferNames[msg[0].linlin(0, 1,  0, bufferNames.size).asInteger].asString]);

			if(vars[\splitSel] == 1, {
				if(vars[\presetIndex]<4,
					{
						if(synth.isPlaying, {synth.set(\buf, buffers[msg[0].linlin(0, 1,  0, bufferNames.size).asInteger])});
					},
					{
						if(synthSplit.isPlaying, {synthSplit.set(\buf, buffers[msg[0].linlin(0, 1,  0, bufferNames.size).asInteger])});
					}
				);
			}, {
				if(synth.isPlaying, {synth.set(\buf, buffers[msg[0].linlin(0, 1,  0, bufferNames.size).asInteger])});
			}
			);
		});

		controller.sendMsg(["/container"++containerNum+/+"rate", "@items"]++[1.0.asString]);
		controller.sendMsg(["/container"++containerNum+/+"rateSlide"+/+"x"]++[0.5]);

		controller.addResponder("/container"++containerNum+/+"rateSlide"+/+"x", {arg msg;
			vars['rate'] = msg[0].linlin(0, 1,  minRate,  maxRate);
			vars['rateSlide'] = msg[0].linlin(0, 1,  minRate,  maxRate);
			presets[vars[\presetIndex]]['rate'] = msg[0].linlin(0, 1,  minRate,  maxRate);
			controller.sendMsg(["/container"++containerNum+/+"rate", "@items"]++[msg[0].linlin(0, 1,  minRate, maxRate).round(0.001).asString]);

			if(vars[\splitSel] == 1, {
				if(vars[\presetIndex]<4,
					{
						if(synth.isPlaying, {synth.set(\rate, msg[0].linlin(0, 1,  minRate,  maxRate))});
					},
					{
						if(synthSplit.isPlaying, {synthSplit.set(\rate, msg[0].linlin(0, 1,  minRate,  maxRate))});
					}
				);
			}, {if(synth.isPlaying, {synth.set(\rate, msg[0].linlin(0, 1,  minRate,  maxRate))})});
		});

		controller.sendMsg(["/container"++containerNum+/+"range"+/+"x"]++[0.0, 1.0]);
		controller.addResponder("/container"++containerNum+/+"range"+/+"x", {arg msg; var range;
			range  = msg[0..1];
			vars['rangeMin'] = range[0]; vars['rangeMax'] = range[1];
			presets[vars[\presetIndex]]['rangeMin']  = range[0];
			presets[vars[\presetIndex]]['rangeMax']  = range[1];

			if(vars[\splitSel] == 1, {
				if(vars[\presetIndex]<4,
					{
						if(synth.isPlaying, {synth.set(\rangeMin, range[0], \rangeMax, range[1])});
					},
					{
						if(synthSplit.isPlaying, {synthSplit.set(\rangeMin, range[0], \rangeMax, range[1])});
					}
				);
			}, {if(synth.isPlaying, {synth.set(\rangeMin, range[0], \rangeMax, range[1])})});
		});


		controller.sendMsg(["/container"++containerNum+/+"lpf", "@items"]++[lpfHigh.asString]);
		controller.sendMsg(["/container"++containerNum+/+"lpfSlide"+/+"x"]++[1.0]);

		controller.addResponder("/container"++containerNum+/+"lpfSlide"+/+"x", {arg msg;
			vars['lpf'] = msg[0];
			vars['lpfSlide'] = msg[0];
			presets[vars[\presetIndex]]['lpf'] = msg[0];
			controller.sendMsg(["/container"++containerNum+/+"lpf", "@items"]++[msg[0].linexp(0, 1,  lpfLow, lpfHigh).round(1).asString]);
			if(vars[\splitSel] == 1, {
				if(vars[\presetIndex]<4,
					{
						if(synth.isPlaying, {synth.set(\lpf, msg[0])});
					},
					{
						if(synthSplit.isPlaying, {synthSplit.set(\lpf, msg[0])});
					}
				);
			},
			{
				if(synth.isPlaying, {synth.set(\lpf, msg[0])})
			});
		});

		controller.sendMsg(["/container"++containerNum+/+"hpf", "@items"]++[hpfLow.asString]);
		controller.sendMsg(["/container"++containerNum+/+"hpfSlide"+/+"x"]++[0]);

		controller.addResponder("/container"++containerNum+/+"hpfSlide"+/+"x", {arg msg;
			vars['hpf'] = msg[0];
			vars['hpfSlide'] = msg[0];
			presets[vars[\presetIndex]]['hpf'] = msg[0];
			controller.sendMsg(["/container"++containerNum+/+"hpf", "@items"]++[msg[0].linexp(0, 1,  hpfLow, hpfHigh).round(1).asString]);
			if(vars[\splitSel] == 1, {
				if(vars[\presetIndex]<4,
					{
						if(synth.isPlaying, {synth.set(\hpf, msg[0])});
					},
					{
						if(synthSplit.isPlaying, {synthSplit.set(\hpf, msg[0])});
					}
				);
			},
			{
				if(synth.isPlaying, {synth.set(\hpf, msg[0])});
			});

		});

		controller.addResponder("/container"++containerNum+/+"amp"+/+"x", {arg msg;
			vars['amp'] = msg[0];
			if(synth.isPlaying, {synth.set(\amp, msg[0])});

		});

		controller.addResponder("/container"++containerNum+/+"ampSplit"+/+"x", {arg msg;
			vars['ampSplit'] = msg[0];
			if(synthSplit.isPlaying, {synthSplit.set(\amp, msg[0])});
		});

		controller.addResponder("/container"++containerNum+/+"rand"+/+"x", {arg msg;
			vars['rand'] = msg[0];

			if(synth.isPlaying, {synth.set(\rand, msg[0])});
			if(synthSplit.isPlaying, {synthSplit.set(\rand, msg[0])});
		});

		controller.addResponder("/container"++containerNum+/+"split"+/+"x", {arg msg;
			vars['splitSel'] = msg[0];

			if(msg[0]==1, {
				if(synthSplit.isPlaying, {nil}, {
					this.makeSplitSynth;
				});
			},
			{
				if(synthSplit.isPlaying, {synthSplit.set(\gate, 0)});
			}
			)

		});


		controller.addResponder("/container"++containerNum+/+"gate"+/+"x", {arg msg;
			vars['gate'] = msg[0];

			if(msg[0]==1, {
				if(synth.isPlaying, {nil}, {

					this.makeSynth;

					OSCFunc({ arg msg, time; var markovSel;
						markovSel = msg[3].asInteger;
						vars['presetIndex']  = msg[3].asInteger;

						8.do{|i|
							if(vars['presetIndex']==i,
								{controller.sendMsg(["/container"++containerNum+/+"preset"++i+/+"x"]++[1])},
								{controller.sendMsg(["/container"++containerNum+/+"preset"++i+/+"x"]++[0])}
							);
						};


						controller.sendMsg(["/container"++containerNum+/+"buf", "@items"]++[bufferNames[presets[markovSel][\buf]].asString]);
						controller.sendMsg(["/container"++containerNum+/+"bufSlide"+/+"x"]++[presets[markovSel][\buf]/bufferNames.size]);

						controller.sendMsg(["/container"++containerNum+/+"rate", "@items"]++[presets[markovSel][\rate].round(0.001).asString]);
						controller.sendMsg(["/container"++containerNum+/+"rateSlide"+/+"x"]++([presets[markovSel][\rate].linlin(minRate, maxRate, 0, 1)]));

						controller.sendMsg(["/container"++containerNum+/+"range"+/+"x"]++[presets[markovSel][\rangeMin], presets[markovSel][\rangeMax]]);

						controller.sendMsg(["/container"++containerNum+/+"lpf", "@items"]++[presets[markovSel][\lpf].linlin(0, 1, lpfLow, lpfHigh).round(1).asString]);
						controller.sendMsg(["/container"++containerNum+/+"lpfSlide"+/+"x"]++[presets[markovSel][\lpf]]);

						controller.sendMsg(["/container"++containerNum+/+"hpf", "@items"]++[presets[markovSel][\hpf].linlin(0, 1, hpfLow, hpfHigh).round(1).asString]);
						controller.sendMsg(["/container"++containerNum+/+"hpfSlide"+/+"x"]++[presets[markovSel][\hpf]]);



						presets[markovSel].keysDo{|keys|
							if(synth.isPlaying, {
								if(keys=='buf', {synth.set('buf', buffers[presets[markovSel]['buf']])}, {synth.set(keys, presets[markovSel][keys])})
							});
						};

					},'/markov', Server.local.addr);

					OSCFunc({ arg msg, numParams=5; var rand, bufferSel;

						rand =  msg[3..8];
						bufferSel = rand[0].linlin(0, 1, 0, buffers.size-1).asInteger;

						controller.sendMsg(["/container"++containerNum+/+"buf", "@items"]++[bufferNames[bufferSel].asString]);
						controller.sendMsg(["/container"++containerNum+/+"bufSlide"+/+"x"]++[rand[0]]);

						controller.sendMsg(["/container"++containerNum+/+"rate", "@items"]++[rand[1].round(0.001).asString]);
						controller.sendMsg(["/container"++containerNum+/+"rateSlide"+/+"x"]++([rand[1].linlin(minRate, maxRate, 0, 1)]));

						controller.sendMsg(["/container"++containerNum+/+"range"+/+"x"]++[rand[2], rand[3]]);

						controller.sendMsg(["/container"++containerNum+/+"trigFreq", "@items"]++[rand[4].round(0.01).asString]);
						controller.sendMsg(["/container"++containerNum+/+"trigFreqSlide"+/+"x"]++([rand[4].linlin(0, 15, 0, 1)]));

						controller.sendMsg(["/container"++containerNum+/+"envSize"+/+"x"]++([rand[5]]));

						if(synth.isPlaying, {synth.set('buf', buffers[bufferSel], \rate, rand[1], \rangeMin, rand[2], \rangeMax, rand[3], \envSize, rand[5])});
					},'/rand', Server.local.addr);


					OSCFunc({ arg msg, numParams=5; var rand, bufferSel;

						rand =  msg[3..8];
						bufferSel = rand[0].linlin(0, 1, 0, buffers.size-1).asInteger;

						if(synthSplit.isPlaying, {synthSplit.set('buf', buffers[bufferSel], \rate, rand[1], \rangeMin, rand[2], \rangeMax, rand[3], \envSize, rand[5])});
					},'/randSplit', Server.local.addr);

				});
			},
			{
				sumBusSynth.set(\gate, 0)});
			if(synth.isPlaying, {synth.set(\gate, 0);
				if(synthSplit.isPlaying, {synthSplit.set(\gate, 0)});
			}
			);
		});



		8.do{|i|
			controller.addResponder("/container"++containerNum+/+"preset"++i+/+"x", {arg msg;
				vars[\presetIndex]=i;

				if(vars[\splitSel] == 0, {if(synth.isPlaying, {synth.set(\t_presetTrig, msg[0])})}, {if(i<4, {if(synth.isPlaying, {synth.set(\t_presetTrig, msg[0])})})});
				if(vars[\splitSel] == 0, {if(synthSplit.isPlaying, {synthSplit.set(\t_presetTrig, msg[0])})}, {if(i>3, {if(synthSplit.isPlaying, {synthSplit.set(\t_presetTrig, msg[0])})})});

				controller.sendMsg(["/container"++containerNum+/+"buf", "@items"]++[bufferNames[presets[i][\buf]].asString]);
				controller.sendMsg(["/container"++containerNum+/+"bufSlide"+/+"x"]++[presets[i][\buf]/bufferNames.size]);

				controller.sendMsg(["/container"++containerNum+/+"rate", "@items"]++[presets[i][\rate].round(0.001).asString]);
				controller.sendMsg(["/container"++containerNum+/+"rateSlide"+/+"x"]++([presets[i][\rate]].linlin(minRate, maxRate, 0, 1)));

				controller.sendMsg(["/container"++containerNum+/+"range"+/+"x"]++[presets[i][\rangeMin], presets[i][\rangeMax]]);

				controller.sendMsg(["/container"++containerNum+/+"lpf", "@items"]++[presets[i][\lpf].linlin(0, 1, lpfLow, lpfHigh).round(1).asString]);
				controller.sendMsg(["/container"++containerNum+/+"lpfSlide"+/+"x"]++[presets[i][\lpf]]);

				controller.sendMsg(["/container"++containerNum+/+"hpf", "@items"]++[presets[i][\hpf].linlin(0, 1, hpfLow, hpfHigh).round(1).asString]);
				controller.sendMsg(["/container"++containerNum+/+"hpfSlide"+/+"x"]++[presets[i][\hpf]]);

				if(vars[\splitSel] == 1, {
					if(vars[\presetIndex]<4, {
						controller.sendMsg(["/container"++containerNum+/+"preset"++i+/+"x"]++[1]);
						otherPresetsSplit[0][i].do{|j|
							controller.sendMsg(["/container"++containerNum+/+"preset"++j+/+"x"]++[0]);
						};
						presets[i].keysDo{|keys|
							vars[keys] = presets[i][keys];
							if(synth.isPlaying, {
								if(keys=='buf', {synth.set('buf', buffers[presets[i]['buf']])}, {synth.set(keys, presets[i][keys])});
							});
						};
					},
					{
						controller.sendMsg(["/container"++containerNum+/+"preset"++i+/+"x"]++[1]);
						otherPresetsSplit[1][i-4].do{|j|
							controller.sendMsg(["/container"++containerNum+/+"preset"++j+/+"x"]++[0]);
						};

						presets[i].keysDo{|keys|
							vars[keys] = presets[i][keys];
							if(synthSplit.isPlaying, {
								if(keys=='buf', {synthSplit.set('buf', buffers[presets[i]['buf']])}, {synthSplit.set(keys, presets[i][keys])});
							});
						};
					});
				},
				{
					controller.sendMsg(["/container"++containerNum+/+"preset"++i+/+"x"]++[1]);
					otherPresets[i].do{|j|
						controller.sendMsg(["/container"++containerNum+/+"preset"++j+/+"x"]++[0]);
					};
					presets[i].keysDo{|keys|
						vars[keys] = presets[i][keys];
						if(synth.isPlaying, {
							if(keys=='buf', {synth.set('buf', buffers[presets[i]['buf']])}, {synth.set(keys, presets[i][keys])})
						});
					};

				}
				);
			});
		};
	}
}