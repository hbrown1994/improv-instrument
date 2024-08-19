WaveSetSynth : OSCModule {
	var <>synths, <>cont, <>wsDict, <>dictSel,
	<>buf2, <>buf5, <>buf10, <>buf30,
	<>ws2, <>ws5, <>ws10, <>ws30,
	<>bus, <>psVars, <>psSel,
	<>contBuses,
	<>path, <>recBus, <>contParams,
	<>filtAmpThresh=0.8;

	/*____________Constructors____________*/
	*new {arg synthNum, lemur, lemurContainer, recPath; ^super.new.init(synthNum, lemur, lemurContainer, recPath)}
	*makeSynth {^super.new.makeSynth}
	*makeSplitSynth {^super.new.makeSplitSynth}
	*makeSines {^super.new.makeSines}
	*run {^super.new.run}

	//Assign each instance an index for its trigger synth, synth, lemur controller,
	//which container in the lemur interface will control this instance and an index
	//for syncing one instance's trigger synth to another
	init { arg synthNum, lemur, lemurContainer, recPath;
		//Init the variable "vars" with a dictionary of all the control parameters used for this class and for the lemur controller

		synthIndex=synthNum;           //Assign synthNum to class var synthIndex
		containerNum = lemurContainer; //Assigns lemur container number to class var
		controller = lemur;            //Assigns lemur instance to class var

		buf2 = Buffer.alloc(Server.local, Server.local.sampleRate*2);
		buf5 = Buffer.alloc(Server.local, Server.local.sampleRate*5);
		buf10 = Buffer.alloc(Server.local, Server.local.sampleRate*10);
		buf30 = Buffer.alloc(Server.local, Server.local.sampleRate*30);

		wsDict=Dictionary.new;
		dictSel=\buf2;
		psSel = \ps0;
		bus=999;
		contBuses = {Bus.control(Server.local, 7)}!32;
		path = recPath;


		psVars = Dictionary.newFrom([
			\ps0, Dictionary.newFrom([
				\trigSel, 0, \trigFreq, 1,
				\indexSel, 0, \indexRangeLow, 0, \indexRangeHigh, 2*Server.local.sampleRate,
				\rateSel, 0, \rateRangeLow, 1, \rateRangeHigh, 1,
				\numRepeatsSel, 0, \numRepeatsRangeLow, 1, \numRepeatsRangeHigh, 1,
				\numWsSel, 0, \numWsRangeLow, 1, \numWsRangeHigh, 1,
				\freeze, 0,
				\trigCvBus, 999, \indexCvBus, 999, \rateCvBus, 999, \numRepeatsCvBus, 999,
				\numWsCvBus, 999,
			]),
			\ps1, Dictionary.newFrom([
				\trigSel, 0, \trigFreq, 1,
				\indexSel, 0, \indexRangeLow, 0, \indexRangeHigh, 2*Server.local.sampleRate,
				\rateSel, 0, \rateRangeLow, 1, \rateRangeHigh, 1,
				\numRepeatsSel, 0, \numRepeatsRangeLow, 1, \numRepeatsRangeHigh, 1,
				\numWsSel, 0, \numWsRangeLow, 1, \numWsRangeHigh, 1,
				\freeze, 0,
				\trigCvBus, 999, \indexCvBus, 999, \rateCvBus, 999, \numRepeatsCvBus, 999,
				\numWsCvBus, 999,
			]),
			\ps2, Dictionary.newFrom([
				\trigSel, 0, \trigFreq, 1,
				\indexSel, 0, \indexRangeLow, 0, \indexRangeHigh, 2*Server.local.sampleRate,
				\rateSel, 0, \rateRangeLow, 1, \rateRangeHigh, 1,
				\numRepeatsSel, 0, \numRepeatsRangeLow, 1, \numRepeatsRangeHigh, 1,
				\numWsSel, 0, \numWsRangeLow, 1, \numWsRangeHigh, 1,
				\freeze, 0,
				\trigCvBus, 999, \indexCvBus, 999, \rateCvBus, 999, \numRepeatsCvBus, 999,
				\numWsCvBus, 999,
			]),
			\ps3, Dictionary.newFrom([
				\trigSel, 0, \trigFreq, 1,
				\indexSel, 0, \indexRangeLow, 0, \indexRangeHigh, 2*Server.local.sampleRate,
				\rateSel, 0, \rateRangeLow, 1, \rateRangeHigh, 1,
				\numRepeatsSel, 0, \numRepeatsRangeLow, 1, \numRepeatsRangeHigh, 1,
				\numWsSel, 0, \numWsRangeLow, 1, \numWsRangeHigh, 1,
				\freeze, 0,
				\trigCvBus, 999, \indexCvBus, 999, \rateCvBus, 999, \numRepeatsCvBus, 999,
				\numWsCvBus, 999,
			]);
		]);

		contParams = [\trigSel, \trigFreq,
			\indexSel, \indexRangeLow, \indexRangeHigh,
			\rateSel,  \rateRangeLow, \rateRangeHigh,
			\numRepeatsSel, \numRepeatsRangeLow, \numRepeatsRangeHigh,
			\numWsSel, \numWsRangeLow, \numWsRangeHigh,
			\freeze];

		SynthDef(\wsCont, {
			arg gate=1, buf, freeze=0, trigMod=0, cut,
			trigFreq, trigSel, trigCvBus,
			indexSel, indexRangeLow, indexRangeHigh, indexCvBus,
			rateSel, rateRangeLow, rateRangeHigh, rateCvBus,
			numRepeatsSel, numRepeatsRangeLow, numRepeatsRangeHigh, numRepeatsCvBus,
			numWsSel, numWsRangeLow, numWsRangeHigh, numWsCvBus,
			trigCvBusFbSel, indexCvBusFbSel, rateCvBusFbSel, numRepeatsCvBusFbSel, numWsCvBusFbSel;
			var trig, trigCv, trigCvDiv;
			var index, indexCv;
			var rate, rateCv;
			var numRepeats, numRepeatsCv;
			var numWs, numWsCv;
			var params, busSel, sig;

			/*----- Triggers -----*/
			trigFreq = trigFreq.lag(0.005);
			trigCv = Select.ar(trigCvBusFbSel, [In.ar(trigCvBus), InFeedback.ar(trigCvBus)]);
			trigFreq = Select.kr(trigMod, [trigFreq, trigFreq*trigCv]);
			trigCvDiv = PulseDivider.ar(trigCv, trigFreq);

			trig = Select.kr(trigSel,
				[
					Impulse.kr(trigFreq),
					Dust.kr(trigFreq),
					GaussTrig.kr(trigFreq),
					A2K.kr(trigCv),
					A2K.kr(trigCvDiv),
			]);

			busSel = Stepper.kr(trig, 0, 0, 15, 1, 0);

			/*----- Index -----*/
			indexCv = A2K.kr(Select.ar(indexCvBusFbSel, [In.ar(indexCvBus), InFeedback.ar(indexCvBus)]));
			index = Select.kr(indexSel, [
				Stepper.kr(trig, indexRangeLow, indexRangeLow, indexRangeHigh),
				Stepper.kr(trig, indexRangeHigh, indexRangeHigh, indexRangeLow),
				TIRand.kr(indexRangeLow, indexRangeHigh, trig),
				TBrownRand.kr(indexRangeLow, indexRangeHigh, 0.7, 0, trig),
				indexRangeLow.asInteger,
				indexCv.linlin(0, 1, indexRangeLow, indexRangeHigh).asInteger
			]).asInteger;

			index = Index.kr(buf, index);

			/*----- Rate -----*/
			rateCv = A2K.kr(Select.ar(rateCvBusFbSel, [In.ar(rateCvBus), InFeedback.ar(rateCvBus)]));
			rate = Select.kr(rateSel, [
				Stepper.kr(trig, rateRangeLow, rateRangeLow, rateRangeHigh, 0.25),
				Stepper.kr(trig, rateRangeHigh, rateRangeHigh, rateRangeLow, 0.25),
				TRand.kr(rateRangeLow, rateRangeHigh, trig),
				TBrownRand.kr(rateRangeLow, rateRangeHigh, 0.7, 0, trig),
				rateRangeLow,
				rateCv.linlin(0, 1, rateRangeLow, rateRangeHigh)
			]);

			/*----- numRepeats -----*/
			numRepeatsCv = A2K.kr(Select.ar(numRepeatsCvBusFbSel, [In.ar(numRepeatsCvBus), InFeedback.ar(numRepeatsCvBus)]));
			numRepeats = Select.kr(numRepeatsSel, [
				Stepper.kr(trig, numRepeatsRangeLow, numRepeatsRangeLow, numRepeatsRangeHigh),
				Stepper.kr(trig, numRepeatsRangeHigh, numRepeatsRangeHigh, numRepeatsRangeLow),
				TIRand.kr(numRepeatsRangeLow, numRepeatsRangeHigh, trig),
				TBrownRand.kr(numRepeatsRangeLow, numRepeatsRangeHigh, 0.7, 0, trig),
				numRepeatsRangeLow,
				numRepeatsCv.linlin(0, 1, numRepeatsRangeLow, numRepeatsRangeHigh)
			]).asInteger;

			/*----- numWs -----*/
			numWsCv = A2K.kr(Select.ar(numWsCvBusFbSel, [In.ar(numWsCvBus), InFeedback.ar(numWsCvBus)]));
			numWs = Select.kr(numWsSel, [
				Stepper.kr(trig, numWsRangeLow, numWsRangeLow, numWsRangeHigh),
				Stepper.kr(trig, numWsRangeHigh, numWsRangeHigh, numWsRangeLow),
				TIRand.kr(numWsRangeLow, numWsRangeHigh, trig),
				TBrownRand.kr(numWsRangeLow, numWsRangeHigh, 0.7, 0, trig),
				numWsRangeLow,
				numWsCv.linlin(0, 1, numWsRangeLow, numWsRangeHigh)
			]).asInteger;

			params = [index, rate, numRepeats, numWs];
			params = Gate.kr(params, freeze.linlin(0, 1, 1, 0));
			params = Latch.kr(params, trig+Impulse.kr(0));
			params = [trig]++params++[(cut.linlin(0, 1, 1, 0))];

			EnvGen.kr(Env.asr(0, 1, 0), gate, doneAction: 2);

			32.do{|i|
				Out.kr(contBuses[i], Gate.kr(params, BinaryOpUGen('==', busSel, i)));
			};
		}).add;

		SynthDef(\wsGen, {
			arg gate=1, in, buf, xingsBuf, nextXingsBuf, amp=1, ampGate=0, out, env, busAmp, bus, pauseGate, inBus;
			var trig, index, rate, numRepeats, numWs;
			var sig, start, length, phasor, grainEnv, sustain;

			in = In.kr(inBus, 7);

			trig = in[0];
			index = in[1];
			rate = in[2];
			numRepeats = in[3];
			numWs = in[4];

			start = Index.kr(xingsBuf, index);
			length = Index.kr(xingsBuf, index+numWs);

			phasor = Phasor.ar(trig, BufRateScale.ir(buf)*rate, start, length);
			sustain = (length-start) * numRepeats / rate / SampleRate.ir;
			grainEnv = EnvGen.kr(Env([1, 1, 0], [sustain, 0]), trig, doneAction: 0);
			sig = BufRd.ar(1, buf, phasor)!2 * grainEnv;

			sig = LeakDC.ar(sig) * in[5];

			EnvGen.kr(Env.asr(0, 1, 0), gate, doneAction: 2);

			OffsetOut.ar(out, sig*amp.lag(0.05));
			Out.ar(bus, sig);
		}).add;

		vars = Dictionary.newFrom([
			\gate, 0, \out, 0, \synthNum, synthNum,
			\ampRoute, 0, \amp, 1,
			'hardwareOut', 0, 'hardwareOutMon', 0,
		]);

		controlVars = [\hardwareOut,\amp, \synthNum];

	}

	//Update synth parameters
	updateSynth {
	}

	makeSynth {
		(
			cont = Synth(\wsCont, [
				\freeze, 0,
				\buf, wsDict[dictSel]['indexNums'],
				\trigFreq, psVars[psSel][\trigFreq], \trigSel, psVars[psSel][\trigSel],
				\indexSel, psVars[psSel][\indexSel],
				\indexRangeLow, psVars[psSel][\indexRangeLow], \indexRangeHigh, psVars[psSel][\indexRangeHigh],
				\rateSel, psVars[psSel][\rateSel],
				\rateRangeLow, psVars[psSel][\rateRangeLow], \rateRangeHigh, psVars[psSel][\rateRangeHigh],
				\numRepeatsSel, psVars[psSel][\numRepeatsSel],
				\numRepeatsRangeLow, psVars[psSel][\numRepeatsRangeLow],
				\numRepeatsRangeHigh, psVars[psSel][\numRepeatsRangeHigh],
				\numWsSel, psVars[psSel][\numWsSel],
				\numWsRangeLow, psVars[psSel][\numWsRangeLow],
				\numWsRangeHigh, psVars[psSel][\numWsRangeHigh],
				\freeze, psVars[psSel][\freeze]
			], GlobalNodes.nodes[synthIndex], addAction: \addToHead).register;

			synths = 32.collect{|i|
				Synth(\wsGen, [
					\buf, wsDict[dictSel]['buf'],
					\xingsBuf, wsDict[dictSel]['xingsBuf'],
					\inBus, contBuses[i],
					\amp, vars[\amp],
					\out, GlobalBusses.outputSynthBus[vars[\out]],
					\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex]
				], GlobalNodes.nodes[synthIndex], addAction: \addToTail).register;
			};
		);

	}



	//Run Lemur OSCFuncs that control Synth
	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels); //A bus that is routed nowhere (a fail safe if hardwareOut == -1)
		this.save; //add vars to global controls on instance
		this.initLemur; //Init lemur interface


		/*__________ ROUTE SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/busRoute/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		controller.addResponder("/container"++containerNum++"/amp/x", {arg msg;

			vars['amp'] = msg[0];
			synths.do{|i|
				if(i.isPlaying, {i.set(\amp, msg[0])});
			};
			/*
			if(vars['ampRoute']==1.0,
			{if(synth.isPlaying, {synth.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
			{if(synth.isPlaying, {synth.set(\amp, msg[0])}); vars['amp'] = msg[0]})
			*/
		});

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
				{
					if(cont.isPlaying, {
						synths.do{|i| i.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])}
					});
				},
				{
					if(cont.isPlaying, {synths.do{|i| i.set(\out, dummyBus)}});
				}
			);
		});

		// Record Buffers and Write File to Disk
		[
			["rec0", 2, buf2, "buf2.wav"],
			["rec1", 5, buf5, "buf5.wav"],
			["rec2", 10, buf10, "buf10.wav"],
			["rec3", 30, buf30, "buf30.wav"]
		].do{|items|
			{
				arg param, time, buffer, fileName;
				controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;
					Routine({
						Synth(\bufWrite, [
							\buf, buffer,
							\inBus, if(recBus==nil, {GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign]}, {recBus}),
						], GlobalNodes.nodes[GlobalNodes.nodes.size-1]);
						time.wait;
						buffer.write(
							path+/+fileName,
							"wav",
							completionMessage: {controller.sendMsg(["/container"++containerNum++"/"++param++"/x"]++[0])}
						);
					}).play(AppClock);
				});
			}.value(items[0], items[1], items[2], items[3]);
		};

		// Process Wavesets
		// Record Buffers and Write File to Disk
		[
			["pro0", wsDict, \buf2, ws2, path+/+"buf2.wav", 'buf2.wav'],
			["pro1", wsDict, \buf5, ws5, path+/+"buf5.wav", 'buf5.wav'],
			["pro2", wsDict, \buf10, ws10, path+/+"buf10.wav", 'buf10.wav'],
			["pro3", wsDict, \buf30, ws30, path+/+"buf30.wav", 'buf30.wav'],
		].do{|items|
			{
				arg param, dict, sym, ws, wsPath, key;
				controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;
					if(msg[0]==1, {

						Routine({
							var  xingsBuf=nil, nextXingsBuf=nil, indexNums=nil, lengths=Dictionary.new;

							Wavesets.all.removeAt(key);
							Server.local.sync;
							ws  = Wavesets.from(wsPath);

							Server.local.sync;

							xingsBuf = Buffer.loadCollection(Server.local, ws.fracXings);
							nextXingsBuf = Buffer.loadCollection(Server.local, ws.xings[1..]);
							indexNums = Buffer.loadCollection(Server.local, Array.series(ws.xings.size));

							Server.local.sync;

							dict.add(sym ->
								Dictionary.newFrom([\buf, ws.buffer, \xingsBuf, xingsBuf, \wavesets, ws, \indexNums, indexNums])
							);

							Server.local.sync;

							dict[sym].add(\amps ->
								Dictionary.newFrom((ws.amps.collect{|item, index| [index.asSymbol, item]}).flatten);
							);

							dict[sym].add(\lengths ->
								Dictionary.newFrom((ws.lengths.collect{|item, index| [index.asSymbol, item]}).flatten);
							);

							dict[sym].add(\xings ->
								Dictionary.newFrom((ws.xings.collect{|item, index| [index.asSymbol, item]}).flatten);
							);

							dict[sym].add(\nextXings ->
								Dictionary.newFrom((ws.xings[1..].collect{|item, index| [index.asSymbol, item]}).flatten);
							);

							Server.local.sync;

							controller.sendMsg(["/container"++containerNum++"/"++param++"/x"]++[0])

						}).play(AppClock);
					});
				});
			}.value(items[0], items[1], items[2], items[3], items[4], items[5]);
		};

		// Buf Sel

		["buf2", "buf5", "buf10", "buf30"].do{|param|
			controller.addResponder("/container"++containerNum+/+param+/+"x", {arg msg;

				dictSel = param.asSymbol;

				if(cont.isPlaying, {
					cont.set(\buf, wsDict[param.asSymbol]['indexNums']);
					synths.do{|i|
						i.set(\buf, wsDict[param.asSymbol]['buf'], \xingsBuf, wsDict[param.asSymbol]['xingsBuf']);
					};
				});
			});
		};

		["ps0", "ps1", "ps2", "ps3"].do{|param|
			controller.addResponder("/container"++containerNum+/+param+/+"x", {arg msg;
				psSel=param.asSymbol;
			});
		};

		["go0", "go1", "go2", "go3"].do{|param, index|
			controller.addResponder("/container"++containerNum+/+param+/+"x", {arg msg;
				if(msg[0]==1, {

					controller.sendMsg(["/container"++containerNum++"/"++"indexRange"++"/x"]++[
						psVars[("ps"++index).asSymbol][\indexRangeLow].linlin(0, wsDict[dictSel]['indexNums'].numFrames, 0.0, 1.0),
						psVars[("ps"++index).asSymbol][\indexRangeHigh].linlin(0, wsDict[dictSel]['indexNums'].numFrames, 0.0, 1.0)
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"indexRangeLowMon"++"/x"]++[
						psVars[("ps"++index).asSymbol][\indexRangeLow]
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"indexRangeHighMon"++"/x"]++[
						psVars[("ps"++index).asSymbol][\indexRangeHigh]
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"rateRange"++"/x"]++[
						psVars[("ps"++index).asSymbol][\rateRangeLow].linlin(0.0, 5.0, 0.0, 1.0),
						psVars[("ps"++index).asSymbol][\rateRangeHigh].linlin(0.0, 5.0, 0.0, 1.0)
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"rateRangeLowMon"++"/x"]++[
						psVars[("ps"++index).asSymbol][\rateRangeLow]
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"rateRangeHighMon"++"/x"]++[
						psVars[("ps"++index).asSymbol][\rateRangeHigh]
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"numRepeatsRange"++"/x"]++[
						psVars[("ps"++index).asSymbol][\numRepeatsRangeLow].linlin(1, 80, 0.0, 1.0),
						psVars[("ps"++index).asSymbol][\numRepeatsRangeHigh].linlin(1, 80, 0.0, 1.0),
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"numRepeatsRangeLowMon"++"/x"]++[
						psVars[("ps"++index).asSymbol][\numRepeatsRangeLow]
					]);
					controller.sendMsg(["/container"++containerNum++"/"++"numRepeatsRangeHighMon"++"/x"]++[
						psVars[("ps"++index).asSymbol][\numRepeatsRangeHigh]
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"numWsRange"++"/x"]++[
						psVars[("ps"++index).asSymbol][\numWsRangeLow].linlin(1, 80, 0.0, 1.0),
						psVars[("ps"++index).asSymbol][\numWsRangeHigh].linlin(1, 80, 0.0, 1.0)
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"numWsRangeLowMon"++"/x"]++[
						psVars[("ps"++index).asSymbol][\numWsRangeLow]
					]);

					controller.sendMsg(["/container"++containerNum++"/"++"numWsRangeHighMon"++"/x"]++[
						psVars[("ps"++index).asSymbol][\numWsRangeHigh]
					]);


					[\trigFreq].do{|i|
						controller.sendMsg(["/container"++containerNum++"/"++i++"/x"]++[psVars[("ps"++index).asSymbol][i].linlin(0, 100, 0, 1)]);
					};

					[\freeze].do{|i|
						controller.sendMsg(["/container"++containerNum++"/"++i++"/x"]++[psVars[("ps"++index).asSymbol][i]]);
					};

					contParams.do{|i|
						if(cont.isPlaying, {
							cont.set(i, psVars[("ps"++index).asSymbol][i]);
						});
					};
				});
			});
		};

		// Gate
		controller.addResponder("/container"++containerNum+/+"gate"+/+"x", {arg msg;
			if(msg[0]==1,
				{
					if(cont.isPlaying, {nil}, {
						(
							cont = Synth(\wsCont, [
								\freeze, 0,
								\buf, wsDict[dictSel]['indexNums'],
								\trigFreq, psVars[psSel][\trigFreq], \trigSel, psVars[psSel][\trigSel],
								\indexSel, psVars[psSel][\indexSel],
								\indexRangeLow, psVars[psSel][\indexRangeLow], \indexRangeHigh, psVars[psSel][\indexRangeHigh],
								\rateSel, psVars[psSel][\rateSel],
								\rateRangeLow, psVars[psSel][\rateRangeLow], \rateRangeHigh, psVars[psSel][\rateRangeHigh],
								\numRepeatsSel, psVars[psSel][\numRepeatsSel],
								\numRepeatsRangeLow, psVars[psSel][\numRepeatsRangeLow],
								\numRepeatsRangeHigh, psVars[psSel][\numRepeatsRangeHigh],
								\numWsSel, psVars[psSel][\numWsSel],
								\numWsRangeLow, psVars[psSel][\numWsRangeLow],
								\numWsRangeHigh, psVars[psSel][\numWsRangeHigh],
								\freeze, psVars[psSel][\freeze]
							], GlobalNodes.nodes[synthIndex], addAction: \addToHead).register;

							synths = 32.collect{|i|
								Synth(\wsGen, [
									\buf, wsDict[dictSel]['buf'],
									\xingsBuf, wsDict[dictSel]['xingsBuf'],
									\inBus, contBuses[i],
									\amp, vars[\amp],
									\out, GlobalBusses.outputSynthBus[vars[\out]],
									\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex]
								], GlobalNodes.nodes[synthIndex], addAction: \addToTail).register;
							};
						);
					});
				},
				{
					if(cont.isPlaying, {
						cont.set(\gate, 0);
						synths.do{|i| i.set(\gate, 0)};
					});
				}
			);
		});

		// Control Params
		[["trigFreq", 0, 90]].do{|items|
			var param=items[0], min=items[1], max=items[2];

			controller.addResponder("/container"++containerNum+/+param+/+"x", {arg msg;
				var val;
				val = msg[0].linlin(0, 1, min, max);

				psVars[psSel][param.asSymbol]=val;
				if(cont.isPlaying, {cont.set(param.asSymbol, val)});

				controller.sendMsg(["/container"++containerNum+/+param++"Mon"+/+"x"]++[val]);
			});
		};

		// ----------- TRIG -----------
		["trigImp", "trigDust", "trigGauss", "trigCv", "trigCvDiv"].do{|item, index|
			controller.addResponder("/container"++containerNum+/+item+/+"x", {arg msg;
				if(msg[0]==1, {
					psVars[psSel][\trigSel]=index;
					if(cont.isPlaying, {cont.set(\trigSel, index)});
					if(item=="trigCv", {

						controller.sendMsg(["/container"++containerNum+/+"trigFreqCvMon"+/+"x"]++[~busAssign]);

						psVars[psSel][\trigCvBus] = ~busAssign;
						if(cont.isPlaying, {
							cont.set(
								\trigCvBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign],
								\trigCvBusFbSel, if(~busAssign>synthIndex, {1}, {0}),
							);
						});
					});

					if(item=="trigCvDiv", {
						psVars[psSel][\trigCvBus] = ~busAssign;
						if(cont.isPlaying, {cont.set(\trigCvBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign])});
					});

				});
			});
		};

		["trigFreqMod"].do{|items|
			controller.addResponder("/container"++containerNum+/+items+/+"x", {arg msg;
				if(cont.isPlaying, {cont.set(\trigMod, msg[0])});
			});
		};

		// ----------- INDEX -----------
		["indexRange"].do{|items|
			controller.addResponder("/container"++containerNum+/+items+/+"x", {arg msg;
				var lowVal = msg[0], highVal = msg[1];
				lowVal = lowVal.linlin(0, 1, 0, wsDict[dictSel]['indexNums'].numFrames);
				highVal = highVal.linlin(0, 1, 0, wsDict[dictSel]['indexNums'].numFrames);

				psVars[psSel][\indexRangeLow] = lowVal;
				psVars[psSel][\indexRangeHigh] = highVal;

				if(cont.isPlaying, {
					cont.set(\indexRangeLow, lowVal, \indexRangeHigh, highVal);
				});

				controller.sendMsg(["/container"++containerNum+/+"indexRangeLowMon"+/+"x"]++[lowVal]);
				controller.sendMsg(["/container"++containerNum+/+"indexRangeHighMon"+/+"x"]++[highVal]);
			});
		};

		["indexStepUp", "indexStepDw", "indexRand", "indexBrown", "indexHold", "indexCV"].do{|item, index|
			controller.addResponder("/container"++containerNum+/+item+/+"x", {arg msg;
				if(msg[0]==1, {
					psVars[psSel][\indexSel]=index;
					if(cont.isPlaying, {cont.set(\indexSel, index)});

					if(item=="indexCV", {
						controller.sendMsg(["/container"++containerNum+/+"indexCvMon"+/+"x"]++[~busAssign]);
						psVars[psSel][\indexCvBus] = ~busAssign;
						if(cont.isPlaying, {
							cont.set(
								\indexCvBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign],
								\indexCvBusFbSel, if(~busAssign>synthIndex, {1}, {0}),
							);
						});
					});

				});
			});
		};

		// ----------- RATE -----------

		["rateRange"].do{|items|
			controller.addResponder("/container"++containerNum+/+items+/+"x", {arg msg;
				var lowVal = msg[0], highVal = msg[1];
				lowVal = lowVal.linlin(0, 1, 0.001, 5);
				highVal = highVal.linlin(0, 1, 0.001, 5);

				psVars[psSel][\rateRangeLow] = lowVal;
				psVars[psSel][\rateRangeHigh] = highVal;

				if(cont.isPlaying, {
					cont.set(\rateRangeLow, lowVal, \rateRangeHigh, highVal);
				});

				controller.sendMsg(["/container"++containerNum+/+"rateRangeLowMon"+/+"x"]++[lowVal]);
				controller.sendMsg(["/container"++containerNum+/+"rateRangeHighMon"+/+"x"]++[highVal]);
			});
		};

		["rateStepUp", "rateStepDw", "rateRand", "rateBrown", "rateHold", "rateCV"].do{|item, rate|
			controller.addResponder("/container"++containerNum+/+item+/+"x", {arg msg;
				if(msg[0]==1, {
					psVars[psSel][\rateSel]=rate;
					if(cont.isPlaying, {cont.set(\rateSel, rate)});

					if(item=="rateCV", {
						controller.sendMsg(["/container"++containerNum+/+"rateCvMon"+/+"x"]++[~busAssign]);
						psVars[psSel][\rateCvBus] = ~busAssign;
						if(cont.isPlaying, {
							cont.set(
								\rateCvBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign],
								\rateCvBusFbSel, if(~busAssign>synthIndex, {1}, {0}),
							);
						});
					});
				});
			};
		)};

		// numRepeats
		["numRepeatsRange"].do{|items|
			controller.addResponder("/container"++containerNum+/+items+/+"x", {arg msg;
				var lowVal = msg[0], highVal = msg[1];
				lowVal = lowVal.linlin(0, 1, 1, 80).asInteger;
				highVal = highVal.linlin(0, 1, 1, 80).asInteger;

				psVars[psSel][\numRepeatsRangeLow] = lowVal;
				psVars[psSel][\numRepeatsRangeHigh] = highVal;

				if(cont.isPlaying, {
					cont.set(\numRepeatsRangeLow, lowVal, \numRepeatsRangeHigh, highVal);
				});

				controller.sendMsg(["/container"++containerNum+/+"numRepeatsRangeLowMon"+/+"x"]++[lowVal]);
				controller.sendMsg(["/container"++containerNum+/+"numRepeatsRangeHighMon"+/+"x"]++[highVal]);
			});
		};

		["numRepeatsStepUp", "numRepeatsStepDw", "numRepeatsRand", "numRepeatsBrown", "numRepeatsHold", "numRepeatsCV"].do{|item, numRepeats|
			controller.addResponder("/container"++containerNum+/+item+/+"x", {arg msg;
				if(msg[0]==1, {
					psVars[psSel][\numRepeatsSel]=numRepeats;
					if(cont.isPlaying, {cont.set(\numRepeatsSel, numRepeats)});

					if(item=="numRepeatsCV", {
						controller.sendMsg(["/container"++containerNum+/+"numRepeatsCvMon"+/+"x"]++[~busAssign]);
						psVars[psSel][\numRepeatsBus] = ~busAssign;
						if(cont.isPlaying, {
							cont.set(
								\numRepeatsCvBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign],
								\numRepeatsCvBusFbSel, if(~busAssign>synthIndex, {1}, {0}),
							);
						});
					});
				});
			};
		)};

		//numWs

		["numWsRange"].do{|items|
			controller.addResponder("/container"++containerNum+/+items+/+"x", {arg msg;
				var lowVal = msg[0], highVal = msg[1];
				lowVal = lowVal.linlin(0, 1, 1, 80).asInteger;
				highVal = highVal.linlin(0, 1, 1, 80).asInteger;

				psVars[psSel][\numWsRangeLow] = lowVal;
				psVars[psSel][\numWsRangeHigh] = highVal;

				if(cont.isPlaying, {
					cont.set(\numWsRangeLow, lowVal, \numWsRangeHigh, highVal);
				});

				controller.sendMsg(["/container"++containerNum+/+"numWsRangeLowMon"+/+"x"]++[lowVal]);
				controller.sendMsg(["/container"++containerNum+/+"numWsRangeHighMon"+/+"x"]++[highVal]);
			});
		};

		["numWsStepUp", "numWsStepDw", "numWsRand", "numWsBrown", "numWsHold", "numWsCV"].do{|item, numWs|
			controller.addResponder("/container"++containerNum+/+item+/+"x", {arg msg;
				if(msg[0]==1, {
					psVars[psSel][\numWsSel]=numWs;
					if(cont.isPlaying, {cont.set(\numWsSel, numWs)});

					if(item=="numWsCV", {
						controller.sendMsg(["/container"++containerNum+/+"numWsCvMon"+/+"x"]++[~busAssign]);
						psVars[psSel][\numWsBus] = ~busAssign;
						if(cont.isPlaying, {
							cont.set(
								\numWsCvBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign],
								\numWsCvBusFbSel, if(~busAssign>synthIndex, {1}, {0}),
							);
						});
					});
				});
			});
		};

		["freeze", "cut"].do{|item, numWs|
			controller.addResponder("/container"++containerNum+/+item+/+"x", {arg msg;
				psVars[psSel][item.asSymbol]=msg[0];
				if(cont.isPlaying, {cont.set(item.asSymbol, msg[0])});
			});
		};

		// Routing
		controller.addResponder("/container"++containerNum+/+"busAdd"+/+"x", {arg msg;
			bus=~busAssign;
			recBus = GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign];
			controller.sendMsg(["/container"++containerNum+/+"busIn0"+/+"x"]++[~busAssign]);
		});
	}
}


