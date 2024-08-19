NN_MFCC : OSCModule {
	var <>bufA, <>bufB;
	var <>corpus;
	var <>bufA_mfcc, <>bufB_mfcc;
	var <>bufA_playback, <>bufB_playback;
	var <>bufA_tree, <>bufB_tree;
	var <>buf, mfcc_ds, playback_ds, tree_ds;
	var <>recBus, <>bus, <>freeze;

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

		corpus = GlobalData.audioBufferNames[\nnMFCC][0].asSymbol;
		buf = GlobalData.audioBuffersDict[\nnMFCCbufs][corpus];
		mfcc_ds = GlobalData.nnMFCC_datasetsDict[corpus];
		playback_ds = GlobalData.nnPlayback_datasetsDict[corpus];
		tree_ds = GlobalData.nnTree_datasetsDict[corpus];

		bufA = Buffer.alloc(Server.local, Server.local.sampleRate*5);
		bufB = Buffer.alloc(Server.local, Server.local.sampleRate*10);

		vars = Dictionary.newFrom([
			\gate, 0, \out, 0, \synthNum, synthNum,
			\ampRoute, 0, \amp, 1,
			'hardwareOut', 0, 'hardwareOutMon', 0,
			\trigState, 0, \followState, 0, \loopState, 0, \busAmp, 1,
			\amp, 1, \pan, 0, \trigFreq, 20, \noiseGateThresh, 0.0,
			\inBus, 999,
		]);
		controlVars = [\synthNum, \amp, \out, \hardwareOutMon, \hardwareOut, \ampRoute, \busAmp, \gate, \trigState, \followState, \loopState, \pan, \trigFreq, \noiseGateThresh];
		synthVars = [\amp, \out, \busAmp, \trigFreq, \noiseGateThresh];
	}

	makeSynthDef {

		SynthDef(\nnMFCC, {
			arg t_trig, fft, freeze, loopGate, rate=0, size=0, inBusFbSel, inBus, trigState=0, out=0, followState=0, loopState, bus, busAmp, amp=1, pauseGate=1, gate=1, pan, trigFreq=20, noiseGateThresh=0.0;
			var in, trig, mfccs, phasor, pbEnv, onsets, envFollow, loop, fftSig;
			var start, num, sig;
			var mfccbuf = LocalBuf(13);
			var playbackinfo = LocalBuf(2);

			in = Select.ar(inBusFbSel,
				[
					In.ar(inBus, GlobalPresets.numChannels),
					InFeedback.ar(inBus, GlobalPresets.numChannels)
			]);

			in = Mix.new(in);

			mfccs = FluidMFCC.kr(in, startCoeff:1).lag(0.05);

			onsets = A2K.kr(FluidOnsetSlice.ar(in, 0, 0.7));
			trig = Impulse.kr(trigFreq);

			trig = Select.kr(trigState, [trig, onsets]);
			trig = Gate.kr(trig, (freeze+loopGate).linlin(0, 1, 1, 0));

			FluidKrToBuf.kr(mfccs, mfccbuf);

			// the below line must be executed in synthDef compilation to work
			tree_ds.kr(trig, mfccbuf, playbackinfo, 1, playback_ds);
			# start, num = FluidBufToKr.kr(playbackinfo);

			rate = rate.linlin(0.0, 1.0, 1.0, 10.0);
			size = size.linlin(0.0, 1.0, 1.0, 0.005);

			phasor = Phasor.ar(trig, BufRateScale.kr(buf)*rate, start, (start+(num*size)), start);
			sig = BufRd.ar(1, buf, phasor);
			pbEnv = EnvGen.kr(Env.linen(0.005, ((num*size)/SampleRate.ir/rate)-0.01, 0.005), trig);

			loop = Select.kr(loopState, [pbEnv, 1]);
			sig = sig * Select.kr(freeze+loopGate, [loop, 1]) * Select.kr(followState, [1, EnvFollow.ar(in)]);

			sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), gate, doneAction: 2);
			sig = sig * EnvGen.kr(Env.asr(0.005, 1, 0.005), pauseGate, doneAction: 0);
			PauseSelf.kr((pauseGate+Trig.kr(Impulse.kr(0) ,0)).linlin(0, 1, 1, 0));

			sig = sig.clip(-1.0, 1.0);

			fftSig = FFT(LocalBuf(1024), sig);
			fftSig = PV_Freeze(fftSig, SetResetFF.kr(DelayN.kr(trig, 0.05, 0.05), trig));
			fftSig = IFFT(fftSig);

			sig = Select.ar(fft, [sig, fftSig]);

			sig =  Compander.ar(sig, sig,
				thresh: noiseGateThresh,
				slopeBelow: 10,
				slopeAbove:  1,
				clampTime:   0.01,
				relaxTime:   0.01
			);

			sig = Pan2.ar(
				sig,
				Select.kr(pan, [0, TBrownRand.kr(-1.0, 1.0, 0.7, 0, trig)])
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
		synth = Synth(\nnMFCC, [\gate, 1,
			\amp, vars[\amp], \busAmp, vars[\busAmp],
			\trigState, vars[\trigState], \followState, vars[\followState],
			\loopState, vars[\loopState], \pan, vars[\pan],
			\trigFreq, vars[\trigFreq], \noiseGateThresh, vars[\noiseGateThresh],
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
			msg[0].postln;
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
		].do{|items|
			{
				arg param, buffer;
				controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;

					Routine({
						if(param=="recA",
							{bufA_mfcc = FluidDataSet(Server.local)},
							{bufB_mfcc = FluidDataSet(Server.local)}
						);

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
		].do{|items, index|
			{
				arg param, buf, ds_mfccs, ds_playback, tree;

				controller.addResponder("/container"++containerNum++"/"++param++"/x", {arg msg;

					var indices = Buffer(Server.local);
					var mfccs = Buffer(Server.local);
					var stats = Buffer(Server.local);
					var flat = Buffer(Server.local);
					var playback_info_dict = Dictionary.newFrom([
						"cols",2,
						"data",Dictionary.new;
					]);

					FluidBufOnsetSlice.processBlocking(Server.local, buf, indices: indices, metric:9, threshold:0.7, action:
						{
							indices.loadToFloatArray(action:{
								arg fa;
								fa.doAdjacentPairs{
									arg start, end, i;
									var num = end - start;
									var id = "slice-%".format(i);

									// add playback info for this slice to this dict
									playback_info_dict["data"][id] = [start,num];

									FluidBufMFCC.processBlocking(Server.local,buf,start,num,startCoeff:1,features:mfccs);
									FluidBufStats.processBlocking(Server.local,mfccs,stats:stats,select:[\mean]);
									FluidBufFlatten.processBlocking(Server.local,stats,destination:flat);

									// add analysis info for this slice to this data set
									[bufA_mfcc, bufB_mfcc][index].addPoint(id,flat);
								};

								if(index==0, {
									bufA_playback= FluidDataSet(Server.local).load(playback_info_dict,
										{
											bufA_tree= FluidKDTree(Server.local).fit(bufA_mfcc,
												{
													"MFCC done A".postln;
													controller.sendMsg(["/container"++containerNum+/+param+/+"x"]++[0]);
												}
											);
										}
									);
								},
								{
									bufB_playback= FluidDataSet(Server.local).load(playback_info_dict,
										{
											bufB_tree= FluidKDTree(Server.local).fit(bufB_mfcc,
												{
													"MFCC done B".postln;
													controller.sendMsg(["/container"++containerNum+/+param+/+"x"]++[0]);
												}
											);
										}
									);
								}
								);
							});
						}
					);
				});
			}.value(items[0], items[1]);
		};

		// Buf Sel
		["bufA", "bufB", "bufC"].do{|param, index|
			controller.addResponder("/container"++containerNum+/+param+/+"x", {arg msg;

				if(param!="bufC",
					{
						buf = [bufA, bufB][index];
						mfcc_ds = [bufA_mfcc, bufB_mfcc][index];
						playback_ds = [bufA_playback, bufB_playback][index];
						tree_ds = [bufA_tree, bufB_tree][index];
					},
					{
						buf = GlobalData.audioBuffersDict[\nnMFCCbufs][corpus];
						mfcc_ds = GlobalData.nnMFCC_datasetsDict[corpus];
						playback_ds = GlobalData.nnPlayback_datasetsDict[corpus];
						tree_ds = GlobalData.nnTree_datasetsDict[corpus];
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

		controller.addResponder("/container"++containerNum+/+"contBus"+/+"x", {arg msg;
			vars[\inBus]=~busAssign;
			if(synth.isPlaying,
				{
					synth.set(
						\inBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign],
						\inBusFbSel, if(~busAssign>synthIndex, {1}, {0})
					);
				}
			);
			controller.sendMsg(["/container"++containerNum+/+"contBusIn0"+/+"x"]++[~busAssign]);
		});

		controller.sendMsg(["/container"++containerNum+/+"preset", "@items"]++GlobalData.audioBufferNames[\nnMFCC].collect{|i| i.asString});

		controller.addResponder("/container"++containerNum+/+"presetSlide"+/+"x", {arg msg;
			corpus = GlobalData.audioBufferNames[\nnMFCC][msg[0].linlin(0, 1,  0, GlobalData.audioBufferNames[\nnMFCC].size).asInteger].asSymbol;
			controller.sendMsg(["/container"++containerNum+/+"preset"+/+"x"]++[msg[0].linlin(0, 1,  0, GlobalData.audioBufferNames[\nnMFCC].size).asInteger]);
		});

		["trigState", "loopState", "followState", "pan", "freeze", "fft"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x",
				{arg msg;
					vars[param.asSymbol]=msg[0];
					if(synth.isPlaying, {synth.set(param.asSymbol, msg[0])})
				}
			);
		};

		["rate", "size"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x",
				{arg msg;
					if(synth.isPlaying, {synth.set(param.asSymbol, msg[0])})
				}
			);
		};

		["loopGate"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x",
				{arg msg;
					if(synth.isPlaying, {synth.set(param.asSymbol, msg[0])})
				}
			);
		};

		["t_trig"].do{|param|
			controller.addResponder("/container"++containerNum++"/"++param++"/x",
				{arg msg;
					if(synth.isPlaying, {synth.set(param.asSymbol, msg[0])})
				}
			);
		};

		controller.addResponder("/container"++containerNum++"/"++"noiseGateThresh"++"/x",
			{
				arg msg;
				var val=msg[0].linlin(0, 1, 0.001, 0.4);
				vars[\noiseGateThresh]=val;
				if(synth.isPlaying, {synth.set(\noiseGateThresh, val)});
				controller.sendMsg(["/container"++containerNum+/+"noiseGateThreshMon"+/+"x"]++[val]);
			}
		);

		controller.addResponder("/container"++containerNum++"/"++"trigFreq"++"/x",
			{
				arg msg;
				var val=msg[0].linlin(0, 1, 0.1, 50);
				vars[\trigFreq]=val;
				if(synth.isPlaying, {synth.set(\trigFreq, val)});
				controller.sendMsg(["/container"++containerNum+/+"trigFreqMon"+/+"x"]++[val]);
			}
		);

		controller.addResponder("/container"++containerNum++"/"++"reset"++"/x",
			{arg msg;
				if(msg[0]==1,
					{
						if(synth.isPlaying, {synth.set(\gate, 0)});
						this.makeSynthDef;
					},
					{
						synth = Synth(\nnMFCC, [\gate, 1,
							\inBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign],
							\inBusFbSel, if(~busAssign>synthIndex, {1}, {0}),
							\amp, vars[\amp], \busAmp, vars[\busAmp],
							\trigState, vars[\trigState], \followState, vars[\followState],
							\loopState, vars[\loopState], \pan, vars[\pan],
							\trigFreq, vars[\trigFreq], \noiseGateThresh, vars[\noiseGateThresh],
							\bus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][synthIndex],
							\out, GlobalBusses.outputSynthBus[vars[\out]],
						], GlobalNodes.nodes[synthIndex]).register;
					}
				);
		});
	}
}
