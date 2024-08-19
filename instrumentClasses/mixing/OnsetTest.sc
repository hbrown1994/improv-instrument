OnsetTest : OSCModule {
	var <>threshTest;

	*new {arg synthNum, container, lemur;
		^super.new.init(synthNum, container, lemur)
	}

	*run{^super.new.run}

	*save {^super.new.save}
	*load {^super.new.load}

	init {arg synthNum, container, lemur;
		containerNum = container;
		synthIndex = synthNum;
		controller = lemur;

		lemur.sendMsg(["/container"++container+/+"threshTest8"+/+"x"]++[0]);

		vars = GlobalSynths.onsetThreshs;
	}

	save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum
		GlobalPresets.controls.add(synthIndex.asSymbol -> vars)
	}

	load { //save the parameter dictionary "vars" to the global "controls" dictionary
		vars=GlobalPresets.controls[synthIndex.asSymbol];
		GlobalSynths.onsetThreshs = GlobalPresets.controls[synthIndex.asSymbol];
	}

	run {
		controller.addResponder("/container"++containerNum++"/threshTest8"++"/x", {arg msg;
			controller.sendMsg(["/container"++containerNum+/+"slotMon"+/+"x"]++[~busAssign]);

			if(msg[0]==1, {
				if(threshTest.isPlaying, {nil}, {
					threshTest=Synth(\threshTest,
						[
							\inBus, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][~busAssign],
							\thresh, 0.7, \out, GlobalBusses.outputSynthBus[0]
						], GlobalNodes.nodes[synthIndex], 'addToTail'
					).register;
				});

			}, {if(threshTest.isPlaying, {threshTest.free})});
		});

		controller.addResponder("/container"++containerNum++"/thresh8"++"/x", {arg msg;
			GlobalSynths.onsetThreshs[~busAssign.asSymbol] = msg[0].linlin(0, 1, 0, 2);
			if(threshTest.isPlaying, {threshTest.set(\thresh, msg[0].linlin(0, 1, 0, 2))});
		});
	}
}