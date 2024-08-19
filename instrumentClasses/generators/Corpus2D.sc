Corpus2D : OSCModule {
	var <>bufA, <>bufB, <>bufC;
	var <>corpus;
	var <>bufA_analysis, <>bufB_analysis, <>bufC_analysis;
	var <>bufA_playback, <>bufB_playback, <>bufC_playback;
	var <>bufA_tree, <>bufB_tree, <>bufC_tree;
	var <>buf, analysis_ds, playback_ds, tree_ds;
	var <>recBus, <>bus, <>freeze;
	var <>normedA, <>ds_griddedA, <>analysesA, <>normedB, <>ds_griddedB, <>analysesB, <>normedC, <>ds_griddedC, <>analysesC;

	/*____________Constructors____________*/
	*new {arg synthNum, lemur, lemurContainer; ^super.new.init(synthNum, lemur, lemurContainer)}
	*makeSynth {^super.new.makeSynth}
	*makeSynthDef {^super.new.makeSynthDef}
	*run {^super.new.run}

	//Assign each instance an index for its trigger synth, synth, lemur controller,
	//which container in the lemur interface will control this instance and an index
	//for syncing one instance's trigger synth to another
	init { arg synthNum, lemur, lemurContainer;
		//Init the variable "vars" with a dictionary of all the control parameters used for this class and for the lemur controller

		synthIndex=synthNum;           //Assign synthNum to class var synthIndex
		containerNum = lemurContainer; //Assigns lemur container number to class var
		controller = lemur;            //Assigns lemur instance to class var

		corpus = GlobalData.audioBufferNames[\analysis2D][0].asSymbol;
		buf = GlobalData.audioBuffersDict[\analysis2Dbufs][corpus];
		analysis_ds = GlobalData.analysis2D_datasetsDict[corpus];
		playback_ds = GlobalData.playback2D_datasetsDict[corpus];
		tree_ds = GlobalData.tree2D_datasetsDict[corpus];

		bufA = Buffer.alloc(Server.local, Server.local.sampleRate*10);
		bufB = Buffer.alloc(Server.local, Server.local.sampleRate*20);
		bufC = Buffer.alloc(Server.local, Server.local.sampleRate*30);

		normedA = FluidDataSet(Server.local);
		ds_griddedA = FluidDataSet(Server.local);
		analysesA = FluidDataSet(Server.local);

		normedB = FluidDataSet(Server.local);
		ds_griddedB = FluidDataSet(Server.local);
		analysesB = FluidDataSet(Server.local);

		normedC = FluidDataSet(Server.local);
		ds_griddedC = FluidDataSet(Server.local);
		analysesC = FluidDataSet(Server.local);

		vars = Dictionary.newFrom([
			\gate, 0, \out, 0, \synthNum, synthNum,
			\ampRoute, 0, \amp, 1,
			'hardwareOut', 0, 'hardwareOutMon', 0,
			\loopState, 0, \busAmp, 1,
			\amp, 1, \pan, 0,
			\inBus, 999,
		]);
		controlVars = [\synthNum, \amp, \out, \hardwareOutMon, \hardwareOut, \ampRoute, \gate, \loopState, \pan];
		synthVars = [\amp, \out, \busAmp];
	}

	makeSynthDef {

		SynthDef(\playCorpus, {
			arg t_trig, xSel, ySel, fft, freeze, loopGate, rate=0, size=0, loopState, bus, busAmp, amp=1, pauseGate=1, gate=1, pan, out;
			var xy, xySum, changedTrig, trig;
			var phasor, pbEnv;
			var start, num, sig;
			var xyBuf=LocalBuf(2);
			var playbackinfo = LocalBuf(2);
			var loop, fftSig;

			xy = [xSel, ySel];
			xySum = xy[0]+xy[1];
			trig = Impulse.kr(ControlRate.ir);

			FluidKrToBuf.kr(xy, xyBuf);

			tree_ds.kr(trig, xyBuf, playbackinfo, 1, playback_ds);

			# start, num = FluidBufToKr.kr(playbackinfo);

			//# start, num = Latch.kr(FluidBufToKr.kr(playbackinfo), trig);

			rate = rate.linlin(0.0, 1.0, 1.0, 10.0);
			size = size.linlin(0.0, 1.0, 1.0, 0.005);

			changedTrig = Changed.kr(start) + t_trig;
			phasor = Phasor.ar(changedTrig, BufRateScale.kr(buf)*rate, start, (start+(num*size)), start);
			sig = BufRd.ar(1, buf, phasor);
			pbEnv = EnvGen.kr(Env.linen(0.005, ((num*size)/SampleRate.ir/rate)-0.01, 0.005), changedTrig);

			loop = Select.kr(loopState, [pbEnv, 1]);
			sig = sig * Select.kr(freeze+loopGate, [loop, 1]);

			sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
			sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
			PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

			sig = sig.clip(-1.0, 1.0);

			fftSig = FFT(LocalBuf(1024), sig);
			fftSig = PV_Freeze(fftSig, SetResetFF.kr(DelayN.kr(changedTrig, 0.05, 0.05), changedTrig));
			fftSig = IFFT(fftSig);

			sig = Select.ar(fft, [sig, fftSig]);

			sig = Pan2.ar(
				sig,
				Select.kr(pan, [0, TBrownRand.kr(-1.0, 1.0, 0.7, 0, changedTrig)])
			);

			Out.ar(out, sig*amp.lag(0.05));
			Out.ar(bus, sig*busAmp.lag(0.05));
		}).add;
	}

	//Update synth parameters
	updateSynth{
		if(synth.isPlaying,
			{if(vars[\gate]==1, {synthVars.do{|i| synth.set(i, vars[i])}}, {synth.set(\gate, 0)})},
			{if(vars[\gate]==1, {this.makeSynth}, {nil})});
	}

	makeSynth {
		synth = Synth(\playCorpus, [\gate, 1,
			\amp, vars[\amp], \busAmp, vars[\busAmp],
			\loopState, vars[\loopState], \pan, vars[\pan],
			\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
			\out, GlobalBusses.outputSynthBus[vars[\out]],
		], GlobalNodes.nodes[synthIndex]).register;
	}

	//Run Lemur OSCFuncs that control Synth
	run {
		var dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels); //A bus that is routed nowhere (a fail safe if hardwareOut == -1)
		this.save; //add vars to global controls on instance
		this.initLemur; //Init lemur interface

		this.makeSynthDef;

		/*__________ ROUTE SYNTH ___________________________________*/
		controller.addResponder("/container"++containerNum++"/busRoute/x", {arg msg; if(msg[0]==1.0, {~busAssign=synthIndex})});

		controller.addResponder("/container"++containerNum++"/gate/x", {arg msg;
			if(msg[0]==1.0,
				{
					vars[\gate] = 1;
					if(synth.isPlaying, {nil}, {this.makeSynth})
				},
				{
					vars[\gate] = 0;
					if(synth.isPlaying, {synth.set(\gate, 0)})
				}
			)
		});

		controller.addResponder("/container"++containerNum++"/amp/x", {arg msg;
			if(vars['ampRoute']==1.0,
				{if(synth.isPlaying, {synth.set(\busAmp, msg[0])}); vars['busAmp'] = msg[0]},
				{if(synth.isPlaying, {synth.set(\amp, msg[0])}); vars['amp'] = msg[0]})
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
				{if(synth.isPlaying, {synth.set(\out, GlobalBusses.outputSynthBus[(msg[0]*8-1).asInteger])})},
				{if(synth.isPlaying, {synth.set(\out, dummyBus)})});
		});

		// Record Buffers and Write File to Disk
		[
			["recA", bufA],
			["recB", bufB],
			["recC", bufC]
		].do{|items|
			{
				arg param, buffer;
				controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;

					Routine({

						if(param=="recA", {bufA_analysis = FluidDataSet(Server.local)});
						if(param=="recB", {bufB_analysis = FluidDataSet(Server.local)});
						if(param=="recC", {bufC_analysis = FluidDataSet(Server.local)});

						Synth(\bufWriteMono, [
							\buf, buffer,
							\inBus, if(recBus==nil, {GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign]}, {recBus}),
						], GlobalNodes.nodes[GlobalNodes.nodes.size-1]);

						(buffer.numFrames/Server.local.sampleRate).wait;

						controller.sendMsg(["/container"++containerNum+/+param+/+"x"]++[0]);

					}).play(AppClock);
				});
			}.value(items[0], items[1]);
		};

		[
			["proA", bufA],
			["proB", bufB],
			["proC", bufC]
		].do{|items, index|
			{
				arg param, buf;

				controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;

					{
						var indices = Buffer(Server.local);
						var normed = FluidDataSet(Server.local);
						var ds_gridded = FluidDataSet(Server.local);
						var analyses;

						FluidBufOnsetSlice.processBlocking(Server.local,buf, metric: 9,threshold:0.2,indices: indices, action:
							{
								indices.loadToFloatArray( action:{
									arg fa;
									var spec = Buffer(Server.local);
									var stats = Buffer(Server.local);
									var stats2 = Buffer(Server.local);
									var loudness = Buffer(Server.local);
									var point = Buffer(Server.local);
									var playback_info_dict = Dictionary.newFrom([
										"cols",2,
										"data",Dictionary.new;
									]);

									fa.doAdjacentPairs{
										arg start, end, i;
										var num = end - start;
										var id = "slice-%".format(i);

										playback_info_dict["data"][id] = [start,num];

										FluidBufSpectralShape.processBlocking(Server.local,buf,start,num,features:spec,select:[\centroid]);
										FluidBufStats.processBlocking(Server.local,spec,stats:stats,select:[\mean]);

										FluidBufLoudness.processBlocking(Server.local,buf,start,num,features:loudness,select:[\loudness]);
										FluidBufStats.processBlocking(Server.local,loudness,stats:stats2,select:[\mean]);

										FluidBufCompose.processBlocking(Server.local,stats,destination:point,destStartFrame:0);
										FluidBufCompose.processBlocking(Server.local,stats2,destination:point,destStartFrame:1);


										if(param=="proA", {bufA_analysis.addPoint(id, point)});
										if(param=="proB", {bufB_analysis.addPoint(id, point)});
										if(param=="proC", {bufC_analysis.addPoint(id, point)});
									};

									if(param=="proA", {bufA_playback = FluidDataSet(Server.local).load(playback_info_dict)});
									if(param=="proB", {bufB_playback = FluidDataSet(Server.local).load(playback_info_dict)});
									if(param=="proC", {bufC_playback = FluidDataSet(Server.local).load(playback_info_dict)});

									Server.local.sync;

									if(param=="proA", {analyses = bufA_analysis});
									if(param=="proB", {analyses = bufB_analysis});
									if(param=="proC", {analyses = bufC_analysis});

									FluidNormalize(Server.local).fitTransform(analyses, normed,
										{
											FluidGrid(Server.local).fitTransform(normed, ds_gridded,
												{
													FluidNormalize(Server.local).fitTransform(ds_gridded, ds_gridded,
														{
															if(param=="proA", {bufA_tree = FluidKDTree(Server.local).fit(ds_gridded, {"Corpus Done".postln})});
															if(param=="proB", {bufB_tree = FluidKDTree(Server.local).fit(ds_gridded, {"Corpus Done".postln})});
															if(param=="proC", {bufC_tree = FluidKDTree(Server.local).fit(ds_gridded, {"Corpus Done".postln})});

															controller.sendMsg(["/container"++containerNum+/+param+/+"x"]++[0]);
														}
													);
												}
											);
										}
									);
								});
						});
					}.value;
				});
			}.value(items[0], items[1]);
		};

		// Buf Sel
		["bufA", "bufB", "bufC", "bufD"].do{|param, index|
			controller.addResponder("/container"++containerNum+/+param+/+"x", {arg msg;

				if(param!="bufD",
					{
						buf = [bufA, bufB, bufC][index];
						analysis_ds = [bufA_analysis, bufB_analysis, bufC_analysis][index];
						playback_ds = [bufA_playback, bufB_playback, bufC_playback][index];
						tree_ds = [bufA_tree, bufB_tree, bufC_tree][index];
					},
					{
						buf = GlobalData.audioBuffersDict[\analysis2Dbufs][corpus];
						analysis_ds = GlobalData.analysis2D_datasetsDict[corpus];
						playback_ds = GlobalData.playback2D_datasetsDict[corpus];
						tree_ds = GlobalData.tree2D_datasetsDict[corpus];
					}
				);
			});
		};

		// Routing
		controller.addResponder("/container"++containerNum+/+"busAdd"+/+"x", {arg msg;
			bus=~busAssign;
			recBus = GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign];
			controller.sendMsg(["/container"++containerNum+/+"busIn0"+/+"x"]++[~busAssign]);
		});

		controller.sendMsg(["/container"++containerNum+/+"preset", "@items"]++GlobalData.audioBufferNames[\analysis2D].collect{|i| i.asString});

		controller.addResponder("/container"++containerNum+/+"presetSlide"+/+"x", {arg msg;
			corpus = GlobalData.audioBufferNames[\analysis2D][msg[0].linlin(0, 1,  0, GlobalData.audioBufferNames[\analysis2D].size).asInteger].asSymbol;
			controller.sendMsg(["/container"++containerNum+/+"preset"+/+"x"]++[msg[0].linlin(0, 1,  0, GlobalData.audioBufferNames[\analysis2D].size).asInteger]);
		});

		["loopState", "pan", "freeze", "fft", "t_trig"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x",
				{arg msg;
					vars[param.asSymbol]=msg[0];
					if(synth.isPlaying, {synth.set(param.asSymbol, msg[0])})
				}
			);
		};

		["rate", "size", "loopGate", "xSel", "ySel"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x",
				{arg msg;
					if(synth.isPlaying, {synth.set(param.asSymbol, msg[0])})
				}
			);
		};

		controller.addResponder("/container"++containerNum++"/"++"reset"++"/x",
			{arg msg;
				if(msg[0]==1,
					{
						if(synth.isPlaying, {synth.set(\gate, 0)});
						this.makeSynthDef;
					},
					{
						if(synth.isPlaying, {nil}, {this.makeSynth});
					}
				);
		});
	}
}
