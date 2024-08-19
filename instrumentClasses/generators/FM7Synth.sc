FM7Synth : OSCModule {
	var <>xyData, <>synthParams, <>regressor, <>fm7Presets, path, grainBuf;


	/*____________Constructors____________*/
	*new {arg synthNum, lemur, lemurContainer, dataPath; ^super.new.init(synthNum, lemur, lemurContainer, dataPath)}
	*makeSynth {^super.new.makeSynth}
	*run {^super.new.run}
	*updateSynth {^super.new.updateSynth}
	*deletePreset {arg thisPath, presetName; ^super.new.save(thisPath, presetName)}

	//Assign each instance an index for its trigger synth, synth, lemur controller,
	//which container in the lemur interface will control this instance and an index
	//for syncing one instance's trigger synth to another
	init { arg synthNum, lemur, lemurContainer, dataPath;

		path = dataPath;

		grainBuf = Buffer.loadCollection(Server.local, Env([0, 1, 1, 0], [0.008, 1, 0.008], \lin).asSignal(1024));

		xyData = FluidDataSet.new(Server.local);
		synthParams = FluidDataSet.new(Server.local);
		regressor = FluidMLPRegressor.new(Server.local, hiddenLayers: [130], activation: 0, outputActivation: 0, maxIter: 50, learnRate: 0.1, momentum: 0, batchSize: 1, validation: 0);

		SynthDef(\FM7, {
			arg monoBus, trig=0, state=0, pointX, pointY, add, loopGate, busAmp, amp, out, bus, ampGate, loopSel, size, rate, scale, unGate, hpf, gate, loopFreeze, pointerDelay=2000,  minPointerDelay=0.05, pointerRandSamples=50000, shardRateMod, shardFreq, shardTrig, shardGate, shardSize, modsOscSel=0, ctlOscSel=0, count;
			var ctls, mods, sig, envs, onOffSwitch, val, randVal, point, paramsBuf, xyBuf, regressBuf, loopSig, phasor, recBuf, onsets, sweep, recSig, shardPhasor, shardClock, pointer, shardDur, shardRate, grainPhasor, shardSig, shardBuf, preGatesig, params, ctlArray, modsArray;

			paramsBuf = LocalBuf(218);
			regressBuf = LocalBuf(218);
			xyBuf = LocalBuf(2);

			loopGate = Select.kr(loopFreeze, [loopGate, 1]);

			ctlArray = NamedControl.kr(\ctlArray, ({{{1.5.linrand.round([0.125, (1/3)].choose)}!4}!3}!6));
			modsArray = NamedControl.kr(\modsArray, ({{{1.5.linrand.round([0.125, (1/3)].choose)}!4}!6}!6));

			params = ((ctlArray.flatten.flatten)++(modsArray.flatten.flatten)++ctlOscSel++modsOscSel);

			FluidKrToBuf.kr(params, paramsBuf);
			FluidKrToBuf.kr([pointX, pointY], xyBuf);
			FluidDataSetWr.kr(synthParams, idNumber: count, buf: paramsBuf, trig: add);
			FluidDataSetWr.kr(xyData, idNumber: count, buf: xyBuf, trig: add);

			regressor.kr(Impulse.kr(ControlRate.ir), xyBuf, regressBuf);

			ctlArray = Select.kr(state, [ctlArray, FluidBufToKr.kr(regressBuf)[0..71].clump(4).clump(3)]);
			modsArray = Select.kr(state, [modsArray, FluidBufToKr.kr(regressBuf)[72..215].clump(4).clump(6)]);
			ctlOscSel = Select.kr(state, [ctlOscSel, FluidBufToKr.kr(regressBuf)[216]]);
			modsOscSel = Select.kr(state, [modsOscSel, FluidBufToKr.kr(regressBuf)[217]]);

			ctls= ctlArray.collect{|a| a.collect{|b| Select.kr(ctlOscSel, [LFSaw.kr(*b), SinOsc.kr(*b), LFPar.kr(*b), Pulse.kr(*b), LFCub.kr(*b), LFTri.kr(*b), SinOscFB.kr(*b)])}};
			mods= modsArray.collect{|a| a.collect{|b| Select.kr(modsOscSel, [LFSaw.kr(*b), SinOsc.kr(*b), LFPar.kr(*b), Pulse.kr(*b), LFCub.kr(*b), LFTri.kr(*b), SinOscFB.kr(*b)])}};

			sig =  Clip.ar(Splay.ar(FM7.ar(ctls, mods), 1)*0.4, -1, 1);

			sig = Normalizer.ar(sig, 0.9);

			sig = Limiter.ar(LeakDC.ar(sig), 0.9)*0.8;
			preGatesig = sig;

			sig = Select.ar(ampGate, [sig, sig*unGate.lag(0.005)]);

			onsets = Gate.kr(Onsets.kr(FFT(LocalBuf(512), HPF.ar(sig, 100)),  0.2), (loopGate).linlin(0, 1, 1, 0));
			onsets = onsets[0] + onsets[1];

			recBuf = LocalBuf(SampleRate.ir/2, 2);
			sweep = Sweep.ar(onsets);
			recSig = sig * EnvGen.ar(Env.asr(0.005, 1, 0.005), SetResetFF.ar(Delay1.ar(K2A.ar(onsets)), K2A.ar(onsets)+(sweep>0.495)));
			RecordBuf.ar(recSig, recBuf, 0, 1, 0, 1, 0, onsets);

			rate = rate.linlin(0, 1, 1, Latch.kr(TRand.kr(5, 20, onsets), loopGate));
			size = size.linlin(0, 1, SampleRate.ir/4, Latch.kr(TRand.kr(SampleRate.ir/16, 2, onsets), loopGate));

			phasor = Phasor.ar(onsets, rate, 0, size);
			loopSig = BufRd.ar(2, recBuf, phasor, 0);

			EnvGen.kr(Env.asr(0.005, 1, 0.005, [4, -4]), gate, doneAction: 2);

			preGatesig = SelectX.ar((loopGate+loopSel).lag(0.005), [preGatesig, loopSig]);
			sig = SelectX.ar((loopGate+loopSel).lag(0.005), [sig, loopSig]);

			sig = HPF.ar(sig, hpf);

			//SHARDS
			shardBuf = LocalBuf(SampleRate.ir*2, 1);
			shardPhasor = Phasor.ar(0, 1, 0, SampleRate.ir*2);
			grainPhasor = Wrap.ar((shardPhasor-pointerDelay)/(SampleRate.ir*2));
			BufWr.ar(Mix.new(preGatesig), shardBuf, shardPhasor);

			shardClock =  Dust.kr(shardFreq.linlin(0.0, 1.0, 0.0, 50))+Trig.kr(shardTrig, 0.005);
			pointer = TRand.kr(Wrap.ar(grainPhasor-0.3), grainPhasor, shardClock);
			shardDur = TRand.kr(0.0005, shardSize.linlin(0.0, 1.0, 0.0005, 0.2), shardClock);
			shardRate = TRand.kr(0.8, shardRateMod.linlin(0.0, 1.0, 1.0, 5.0), shardClock)*Select.kr(Sweep.kr(Impulse.kr(0))>2, [1, TChoose.kr(shardClock, [-1, 1])]);

			shardSig = BufGrainB.ar(shardClock, shardDur, shardBuf, shardRate, pointer, grainBuf);
			sig = Select.ar(K2A.ar(shardTrig+shardGate).lag(0.005), [sig, PanX.ar(GlobalPresets.numChannels, shardSig, TRand.kr(0, 1, shardClock))]);

			sig = LeakDC.ar(sig);
			sig = HPF.ar(sig, 50);

			Out.ar(out, (sig*amp.lag(0.005)));
			Out.ar(bus, (sig*busAmp.lag(0.005)));
			Out.ar(monoBus, Mix.new(sig*busAmp.lag(0.005)));
		}).add;

		synthIndex=synthNum;           //Assign synthNum to class var synthIndex
		containerNum = lemurContainer; //Assigns lemur container number to class var
		controller = lemur;            //Assigns lemur instance to class var

		if(Object.readArchive(dataPath+/+"fm7Datapresets").isCollection,          //Read a file containing the preset names as a List and assign to a global class variable;
			{fm7Presets = Object.readArchive(dataPath+/+"fm7Datapresets")}, //If Object from disk is a list, assign to class variable;
			{"No FM7 Presets".postln; fm7Presets=List.new(0)}           //If Object from disk is NOT a list, make new List and assign to class variable;
		);

		rec = Recorder(Server.local);

		vars = Dictionary.newFrom([
			\gate, 0, \synthNum, synthNum, \env, 0,
			\amp, 1, \out, 0, \busAmp, 1, \ampRoute, 0,
			\hardwareOutMon, 0, \hardwareOut, 0,
			\unGate, 0, \ampGate, 0,  \scale, 1, \size, 0, \rate, 0, \state, 0,
			\pointX, 0, \pointY, 0, 'preset', 0, \shardFreq, 0, \shardGate, 0, \loopFreeze, 0
		]);

		controlVars = [\synthNum, \amp,  \out, \hardwareOutMon, \hardwareOut, \ampRoute, \busAmp,  \gate, \scale, \ampGate, \size, \rate, \state, \pointX, \pointY, \preset, \shardFreq, \shardGate, \loopFreeze];

		synthVars = [\gate, \amp, \busAmp, \env, \out, \scale, \ampGate, \size, \rate, \pointX, \pointY, \shardFreq, \shardGate, \loopFreeze];

	}

	deletePreset {arg thisPath, presetName;                                         //Deletes a preset from disk & name of fm7Presets from array/disk
		File.deleteAll(thisPath+/+presetName);                                          //Delete File on Disk
		fm7Presets.removeAt(fm7Presets.asArray.find([presetName])); //Find presetName in fm7Presets and remove it
	}

	//Update synth parameters
	updateSynth{
		if(synth.isPlaying,
			{if(vars[\gate]==1, {synthVars.do{|i| synth.set(i, vars[i])}}, {synth.set(\gate, 0)})},
			{if(vars[\gate]==1, {this.makeSynth}, {nil})});
	}


	makeSynth{
		synth = Synth(\FM7, [
			\scale, vars[\scale], \ampGate, vars[\ampGate],
			\size, vars[\size], \rate, vars[\rate], \state, vars[\state],
			\gate, 1,
			\amp, vars[\amp], \busAmp, vars[\busAmp], \env, vars[\env],
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

		{var presetGuiMain, text, menu, numTrains, continuously_train=true, train, count=0;                  //a Gui method in which all the above methods are executed.


			presetGuiMain = Window.new("FM7_Presets"++synthIndex, Rect(0, 60, 410, 300)).front;              //Make window
			text = TextField(presetGuiMain, Rect(10, 10, 150, 20));                                   //add a text field for typing the preset name
			menu = PopUpMenu(presetGuiMain, Rect(170, 10, 170, 20)).items = fm7Presets.asArray;

			GlobalSynths.guis.add(presetGuiMain);

			Button(presetGuiMain, Rect(10, 40, 75, 75/2))
			.states_([["Save", Color.black, Color.grey]])
			.mouseDownAction_({
				File.makeDir(path+/+text.value);
				fm7Presets = fm7Presets.add(text.value);
			})
			.mouseUpAction_({
				xyData.write(path+/+text.value++"/xyData.json");
				synthParams.write(path+/+text.value++"/synthParams.json");
				regressor.write(path+/+text.value++"/regressor.json");
				menu.items=fm7Presets.asArray;
				fm7Presets.asArray.writeArchive(path+/+"fm7Datapresets");
			});

			Button(presetGuiMain, Rect(90, 40, 75, 75/2)) //Delete preset on disk on mouseDown & update the popUpMenu items on mouseUp
			.states_([["Delete", Color.black, Color.red]])
			.mouseDownAction_({this.deletePreset(path, menu.item)})
			.mouseUpAction_({menu.items=fm7Presets.asArray; fm7Presets.writeArchive(path+/+"fm7Datapresets")});

			Button(presetGuiMain, Rect(170, 40, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
			.states_([["Load", Color.black, Color.grey]])            //each synth/control class via their individual .load methods
			.mouseDownAction_({
				xyData = xyData.read(path+/+menu.item++"/xyData.json");
				synthParams = synthParams.read(path+/+menu.item++"/synthParams.json");
				regressor = regressor.read(path+/+menu.item++"/regressor.json");
			});

			Button(presetGuiMain, Rect(170, 85, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
			.states_([["Add", Color.black, Color.grey]])           //each synth/control class via their individual .load methods
			.mouseDownAction_({synth.set(\add, 1, \count, count)})
			.mouseUpAction_({synth.set(\add, 0); xyData.print; synthParams.print; regressor.fit(xyData, synthParams, {|info| info.postln}).maxIter_(10); count = count +1});

			Button(presetGuiMain, Rect(10, 85, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
			.states_([["Clear", Color.black, Color.red]])            //each synth/control class via their individual .load methods
			.mouseDownAction_({
				xyData.clear;
				synthParams.clear;
				regressor.clear;
				count=0;
			});

			Button(presetGuiMain, Rect(250, 130, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
			.states_([["Train", Color.black, Color.green], ["Stop", Color.black, Color.white]])            //each synth/control class via their individual .load methods
			.action = { |i|
				if(i.value == 1,
					{
						continuously_train = true;
						train = {
							regressor.fit(xyData, synthParams, {
								arg error;
								"current error: % ".format(error.round(0.0001)).postln;
								"".postln;
								if(continuously_train){train.()};
							});
						};
						train.();
					},
					{continuously_train = false}
				);
			};

			Button(presetGuiMain, Rect(250, 70, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
			.states_([["Train 1x", Color.black, Color.yellow]])           //each synth/control class via their individual .load methods
			.mouseDownAction_({regressor.fit(xyData, synthParams, {|info| info.postln}).maxIter_(10)});

			//validation
			TextField(presetGuiMain, Rect(10, 160, 75, 75/4)).string_("0.2").action = {arg i; regressor.validation_(i.value.asFloat)};
			StaticText(presetGuiMain, Rect(10, 140, 75, 75/4)).string = "Validation";

			TextField(presetGuiMain, Rect(10, 200, 75, 75/4)).string_("0.0001").action = {arg i; regressor.learnRate_(i.value.asFloat)};
			StaticText(presetGuiMain, Rect(10, 180, 75, 75/4)).string = "learnRate";

			TextField(presetGuiMain, Rect(100, 160, 75, 75/4)).string_("1000").action = {arg i; regressor.maxIter_(i.value.asFloat)};
			StaticText(presetGuiMain, Rect(100, 140, 75, 75/4)).string = "maxIter";

			TextField(presetGuiMain, Rect(100, 200, 75, 75/4)).string_("50").action = {arg i; regressor.batchSize_(i.value.asFloat)};
			StaticText(presetGuiMain, Rect(100, 180, 75, 75/4)).string = "batchSize";

			TextField(presetGuiMain, Rect(200, 200, 75, 75/4)).string_("[ 3, 3 ]").action = {arg i; regressor.hiddenLayers_(i.value.interpret)};
			StaticText(presetGuiMain, Rect(200, 180, 80, 75/4)).string = "hiddenLayers";

			TextField(presetGuiMain, Rect(10, 240, 75, 75/4)).string_("2").action = {arg i; regressor.activation_(i.value.asInteger)};
			StaticText(presetGuiMain, Rect(10, 220, 75, 75/4)).string = "activation";

			TextField(presetGuiMain, Rect(100, 240, 75, 75/4)).string_("0").action = {arg i; regressor.outputActivation_(i.value.asInteger)};
			StaticText(presetGuiMain, Rect(100, 220, 100, 75/4)).string = "outputActivation";

			TextField(presetGuiMain, Rect(210, 240, 75, 75/4)).string_("0.9").action = {arg i; regressor.momentum_(i.value.asFloat)};
			StaticText(presetGuiMain, Rect(210, 220, 100, 75/4)).string = "momentum";

		}.value;

		{var paramsWindow, ctlText, modsText;                  //a Gui method in which all the above methods are executed.


			paramsWindow = Window.new("FM7_CTLS_MODS", Rect(410, 600, 600, 250)).front;              //Make paramsWindow
			ctlText = TextField(paramsWindow, Rect(10, 10, 500, 40)).string = "({{{1.5.linrand.round([0.125, (1/3)].choose)}!4}!3}!6)";
			modsText = TextField(paramsWindow, Rect(10, 150, 500, 40)).string = "({{{1.5.linrand.round([0.125, (1/3)].choose)}!4}!6}!6)";

			GlobalSynths.guis.add(paramsWindow);

			Button(paramsWindow, Rect(510, 10, 75, 75/2))
			.states_([["CTLS", Color.black, Color.red]])
			.mouseDownAction_({ctlText.value.interpret.postcs; if(synth.isPlaying, {synth.set(\ctlArray, ctlText.value.interpret)})});

			["LFSaw", "SinOsc", "LFPar", "Pulse", "LFCub", "LFTri", "SinOscFB"].do{
				|i, index|

				Button(paramsWindow, Rect(index*80, 60, 75, 75/2))
				.states_([[i, Color.black, Color.green]])
				.mouseDownAction_({if(synth.isPlaying, {synth.set(\ctlOscSel, index)})});
			};

			Button(paramsWindow, Rect(510, 150, 75, 75/2))
			.states_([["MODS", Color.black, Color.red]])
			.mouseDownAction_({modsText.value.interpret.postcs; if(synth.isPlaying, {synth.set(\modsArray, modsText.value.interpret)})});

			["LFSaw", "SinOsc", "LFPar", "Pulse", "LFCub", "LFTri", "SinOscFB"].do{
				|i, index|

				Button(paramsWindow, Rect(index*80, 200, 75, 75/2))
				.states_([[i, Color.black, Color.green]])
				.mouseDownAction_({if(synth.isPlaying, {synth.set(\modsOscSel, index)})});
			};
		}.value;

		["trig", "state", "pointX", "pointY", "scale", "size", "rate", "loopGate", "ampGate", "unGate", "hpf",  "loopFreeze", "shardRateMod", "shardFreq", "shardTrig", "shardGate", "shardSize"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg; vars[param.asSymbol]=msg[0]; if(synth.isPlaying, {synth.set(param.asSymbol, msg[0])})})
		};

		controller.sendMsg(["/container"++containerNum+/+"preset", "@items"]++fm7Presets.asArray);

		controller.addResponder("/container"++containerNum++"/amp/x", {arg msg;
			if(vars['ampRoute']==1.0,
				{if(synth.isPlaying, {synth.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(synth.isPlaying, {synth.set(\amp, msg[0])}); vars['amp'] = msg[0]})
		});

		controller.addResponder("/container"++containerNum+/+"presetSlide"+/+"x", {arg msg;
			vars['preset'] = msg[0].linlin(0, 1,  0, fm7Presets.size).asInteger;
			controller.sendMsg(["/container"++containerNum+/+"preset"+/+"x"]++[msg[0].linlin(0, 1,  0, fm7Presets.size).asInteger]);
		});

		controller.addResponder("/container"++containerNum+/+"presetSlide"+/+"z", {arg msg;
			if(msg[0]==0, {
				xyData = xyData.read(path+/+fm7Presets[vars['preset']]++"/xyData.json");
				synthParams = synthParams.read(path+/+fm7Presets[vars['preset']]++"/synthParams.json");
				regressor = regressor.read(path+/+fm7Presets[vars['preset']]++"/regressor.json");
			});
		});

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
				{this.makeSynth},
				{if(synth.isPlaying, {synth.set(\gate, 0, \env, 0.0001)})}
			);
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