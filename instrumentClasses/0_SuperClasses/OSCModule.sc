OSCModule {/*-----> A SuperClass for all Synthesis Modules using OSC synth <-----*/
	var <>vars, <>trigIndex=0, <>synthIndex=0, <>syncIndex=3, <>containerNum=0, <>controller=0, <>initState;
	var <>synth, <>trig, <>syncBusNum, <>controlVars, <>synthVars, <>trigVars;
	var <>rec=0;

	/*____________Constructors____________*/
	*initLemur {^super.new.initLemur}
	*updateLemur {^super.new.updateLemur}
	*kill {^super.new.kill}
	*save {^super.new.save}
	*load {^super.new.load}
	*run {^super.new.run}
	*record {arg recordOnOff; ^super.new.record(recordOnOff)}

	initLemur { //Init lemur interface on boot
		controlVars.do{|i|
			controller.sendMsg(["/container"++containerNum+/+(i.asString)+/+"x"]++([vars[i]].flatten))}
	}

	updateLemur{ //Update lemur interface on new preset selection
		controlVars.do{|i|
			controller.sendMsg(["/container"++containerNum+/+(i.asString)+/+"x"]++[GlobalPresets.controls[synthIndex.asSymbol][i]].flatten)}
	}

	kill{ //Free the synth if it's playing
		if(synth.isPlaying, {synth.free});
		if(trig.isPlaying, {trig.free});
	}

	save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum
		GlobalPresets.controls.add(synthIndex.asSymbol -> vars)
	}

	load { //save the parameter dictionary "vars" to the global "controls" dictionary
		vars=GlobalPresets.controls[synthIndex.asSymbol];
		this.updateLemur;
		this.updateSynth;
	}

	record {arg recordOnOff; //record synth's direct output
		if(recordOnOff==1.0,
			{
				if(GlobalPresets.recorders.size==0, {GlobalPaths.updateRecDir});
				GlobalPresets.recorders.add(synthIndex.asSymbol ->
					rec.prepareForRecord(GlobalPaths.recordings+/+"synth_"++synthIndex++".wav",
						GlobalPresets.numChannels));

			},
			{
				GlobalPresets.recorders.removeAt(synthIndex.asSymbol)
			}
		);
	}
}