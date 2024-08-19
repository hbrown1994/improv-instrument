MIDIModule {/*-----> A SuperClass for all Synthesis Modules using OSC synth <-----*/
	var <>vars, <>synthIndex=0, <>midiOut, <>channelNum, <>eq;
	var <>synth, <>controlVarsNums;
	var <>rec=0;

	/*____________Constructors____________*/
	*kill {^super.new.kill}
	*initMIDI {^super.new.initMIDI}
	*updateMIDI {^super.new.updateMIDI}
	*updateSynth {^super.new.updateSynth}
	*save {^super.new.save}
	*load {^super.new.load}
	*run {^super.new.run}
	*record {arg recordOnOff; ^super.new.record(recordOnOff)}

	kill{ //Free the synth if it's playing
		if(synth.isPlaying, {synth.free});
		GlobalSynths.areRunning=false;
	}

	initMIDI { //Init MIDI controller on boot
		controlVarsNums.keysDo{|i|
			midiOut.control(channelNum, ctlNum: controlVarsNums[i], val: 0);
		}
	}

	updateMIDI { //Update lemur interface on new preset selection
		controlVarsNums.keysDo{|i|
			midiOut.control(channelNum, ctlNum: controlVarsNums[i], val: vars[i]);
		}
	}

	updateSynth { //Update synth parameters if it is running on the server
		if(synth.isPlaying, {
			vars.keysDo{|i|
				synth.set(i, vars[i]);
			};
		});
	}

	save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum
		GlobalPresets.controls.add(synthIndex.asSymbol -> vars);
	}

	load { //save the parameter dictionary "vars" to the global "controls" dictionary
		vars=GlobalPresets.controls[synthIndex.asSymbol];
		this.updateMIDI;
		this.updateSynth;
	}

	record {arg recordOnOff; //Record this synth
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