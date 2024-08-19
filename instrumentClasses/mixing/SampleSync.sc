SampleSync {
	var <>impulseSync, <>containerNum, <>controller;

	*new {arg container, lemur;
		^super.new.init(container, lemur)
	}

	*run{^super.new.run}

	*save {^super.new.save}
	*load {^super.new.load}

	init {arg container, lemur;
		containerNum = container;
		controller = lemur;
	}

	save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum

	}

	load { //save the parameter dictionary "vars" to the global "controls" dictionary

	}

	run {
		//SampleSync
		controller.addResponder("/container"++containerNum++"/sampleSync/x", {arg msg;
			if(msg[0]==1, {
				impulseSync = {var sig;
					sig = Pan2.ar(Impulse.ar(0));
					Out.ar(
						GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][Array.series(8, GlobalPresets.outputSynthChan)],
						sig);
					Out.ar(Array.series(16), sig);
				}.play;
			}, {impulseSync.free});
		});
	}
}