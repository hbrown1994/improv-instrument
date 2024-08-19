OutputMixer : MIDIModule {
	var <>recs, <>eqClass, <>channelSplitBoolean, <>monoSelect;

	*new {arg channel=2, controllerName=\nano, synthNum, channelSplit, monoArray;
		^super.new.init(channel, controllerName, synthNum, channelSplit, monoArray)
	}

	*save {^super.new.save}
	*load {^super.new.load}
	*run {^super.new.run}
	*makeSynth {^super.new.makeSynth}

	init {arg channel=2, controllerName=\nano, synthNum, channelSplit, monoArray;
		channelNum = channel;
		synthIndex = synthNum;
		midiOut = GlobalPresets.midiOut[controllerName];
		recs = (Recorder(Server.local))!8;
		channelSplitBoolean = channelSplit;
		monoSelect = monoArray;

		vars = Dictionary.newFrom([
			\amp0, 0,  \amp1, 0, \amp2, 0, \amp3, 0, \amp4, 0, \amp5, 0, \amp6, 0, \amp7, 0,
			\pan0, 0,  \pan1, 0, \pan2, 0, \pan3, 0, \pan4, 0, \pan5, 0, \pan6, 0, \pan7, 0,
			\solo0, 0, \solo1, 0, \solo2, 0, \solo3, 0, \solo4, 0, \solo5, 0, \solo6, 0, \solo7, 0,
			\mute0, 0, \mute1, 0,  \mute2, 0, \mute3, 0, \mute4, 0, \mute5, 0, \mute6, 0, \mute7, 0,
			\center, 0
		]);

		controlVarsNums = Dictionary.newFrom([
			\solo0, 32, \solo1, 33, \solo2, 34, \solo3, 35, \solo4, 36, \solo5, 37, \solo6, 38, \solo7, 39,
			\mute0, 48, \mute1, 49, \mute2, 50, \mute3, 51, \mute4, 52, \mute5, 53, \mute6, 54, \mute7, 55,
			\center, 46
		]);

		this.makeSynth;
	}

	makeSynth {
		if(synth.isPlaying, {nil}, {
			GlobalSynths.outputSynth = synth = Synth(if(channelSplitBoolean==true, {\outputSynthRecording}, {\outputSynth}), [
				\busIn0, GlobalBusses.outputSynthBus[0],
				\busIn1, GlobalBusses.outputSynthBus[1],
				\busIn2, GlobalBusses.outputSynthBus[2],
				\busIn3, GlobalBusses.outputSynthBus[3],
				\busIn4, GlobalBusses.outputSynthBus[4],
				\busIn5, GlobalBusses.outputSynthBus[5],
				\busIn6, GlobalBusses.outputSynthBus[6],
				\busIn7, GlobalBusses.outputSynthBus[7],
				\mono0, monoSelect[0], \mono1, monoSelect[1], \mono2, monoSelect[2], \mono3, monoSelect[3],
				\mono4, monoSelect[4], \mono5, monoSelect[5], \mono6, monoSelect[6], \mono7, monoSelect[7],
				\masterAmp, GlobalPresets.masterAmp
			], target: GlobalNodes.nodes[synthIndex]).register;

			recs = (Recorder(Server.local))!8; //make recorders for each output channel strip
		});
	}

	run {
		this.makeSynth;
		this.save;
		GlobalSynths.areRunning=true;

		//Center pan for all output channels
		MIDIFunc.cc({|msg| vars[("center").asSymbol] = msg; synth.set(\center, msg); midiOut.control(2, ctlNum: 46, val: msg)}, 46, channelNum);

		//Run/kill all recorders
		MIDIFunc.cc({|msg|
			if(msg==1,
				{
					midiOut.control(2, ctlNum: 28, val: 1);
					GlobalPresets.recorders.keysValuesDo{|key, value|
						value.record(
							bus: GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][key.asInteger],
							node: GlobalNodes.nodes[synthIndex+1]);
					}
				},
				{
					midiOut.control(2, ctlNum: 28, val: 0);

					8.do{|i| {InputManager.guis[\recordButtons][i.asSymbol].value = 0}.defer};

					(GlobalPresets.recorders.size+1).do{|i|
						if(i==GlobalPresets.recorders.size,
							{GlobalPresets.recorders.clear},
							{
								if(GlobalPresets.recorders.asArray[i].isRecording, {GlobalPresets.recorders.asArray[i].stopRecording});
								midiOut.control(2, ctlNum: 64+GlobalPresets.recorders.keys.asArray.asInteger[i]-synthIndex, val: 0);

						});
					};

				}

			);
		}, 28, channelNum);


        //Assign nanoKontrol sliders/knobs/button to output channel strip parameters
		8.do{|i|

			MIDIFunc.cc({|msg| //arm output channel strip for recording
				if(msg==1,
					{
						midiOut.control(2, ctlNum: 64+i, val: 1);

						//If nothing is armed, make new folder to save all recordings
						if(GlobalPresets.recorders.size==0, {GlobalPaths.updateRecDir});

						GlobalPresets.recorders.add((synthIndex+i).asSymbol -> //name audio file and how many channels to include
							recs[i].prepareForRecord(GlobalPaths.recordings+/+"main"++(i)++".wav",
								GlobalPresets.numChannels));
					},
					{
						//unarm recorder
						midiOut.control(2, ctlNum: 64+i, val: 0);
						GlobalPresets.recorders.removeAt((synthIndex+i).asSymbol);
					}
				);
			}, 64+i, channelNum); //recording

			MIDIFunc.cc({|msg| vars[("amp"++i).asSymbol] = msg; synth.set(("amp"++i).asSymbol,  msg)}, i, 2); //amplitude sliders
			MIDIFunc.cc({|msg| vars[("pan"++i).asSymbol] = msg; synth.set(("pan"++i).asSymbol,  msg)}, i+16, channelNum); //pan knobs

			//Solo and mute functions
			MIDIFunc.cc({|msg| vars[("solo"++i).asSymbol] = msg; synth.set(("solo"++i).asSymbol, msg); midiOut.control(2, ctlNum: i+32, val: msg)}, i+32, channelNum);
			MIDIFunc.cc({|msg| vars[("mute"++i).asSymbol] = msg; synth.set(("mute"++i).asSymbol, msg); midiOut.control(2, ctlNum: i+48, val: msg)}, i+48, channelNum);
		};

		//turn audio on/off with stop and play functins on nano kontrol
		MIDIFunc.noteOn({arg msg;
			if(synth.isPlaying, {synth.set(\masterAmp, 0)});
		}, 26, 2);

		MIDIFunc.noteOn({arg msg;
			if(synth.isPlaying, {synth.set(\masterAmp, GlobalPresets.masterAmp)});
		}, 27, 2);
	}
}