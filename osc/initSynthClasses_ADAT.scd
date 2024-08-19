// {
// 	var quneoStart = 32;
// 	var classes = [
// 		InputSynth.new(0, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+20, 1, 4, 2),
// 		InputSynth.new(1, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+21, 1, 4, 4),
// 		InputSynth.new(2, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+22, 1, 4, 5),
// 		InputSynth.new(3, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+23, 1, 4, 6),
// 		InputSynth.new(4, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+24, 1, 4, 7),
// 		InputSynth.new(5, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+25, 1, 4, 8),
// 		InputSynth.new(6, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+26, 1, 4, 0),
// 		InputSynth.new(7, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+27, 1, 4, 1),
// 		/*_____________________ MIDI Devices ________________________*/
// 		InputSynthStereo.new(8, 3, GlobalPresets.mixerLemur, GlobalPresets.outputSynthChan+28, 1, 4, 10, 11),
// 		DrumMachine.new(9, 3, GlobalPresets.mixerLemur, 3, 1),
// 		QuneoLeft.new(10, GlobalPresets.mixerLemur, 3, 1, 1),
// 		Playback.new(11, 11, GlobalPresets.mixerLemur, 3, 1),
// 		ModularSynth.new(12, 1, GlobalPresets.mixerLemur, 3, 1, [13, 14, 15], 15),
// 		Dummy.new(13),
// 		Dummy.new(14),
// 		Dummy.new(15),
// 		/*_____________________ Lemur Left ________________________*/
// 		FieldRecordings.new(16, GlobalPresets.synthLemur, 0),
// 		Synths0.new(17, GlobalPresets.synthLemur, 1),
// 		Dummy.new(18),
// 		Dummy.new(19),
// 		Dummy.new(20),
// 		Dummy.new(21),
// 		Dummy.new(22),
// 		Dummy.new(23),
// 		/*_____________________ Lemur Right ________________________*/
// 		//OnsetLooper.new(24, 12, GlobalPresets.synthLemur, 0, 12, 10, 12),
// 		FM7Synth.new(24, GlobalPresets.synthLemur, 8, GlobalPaths.data++"fm7Data"),
// 		SingleSamplePad.new(25, GlobalPresets.synthLemur, 9),
// 		Dummy.new(26),
// 		Dummy.new(27),
// 		Dummy.new(28),
// 		Dummy.new(29),
// 		Dummy.new(30),
// 		Dummy.new(31),
// 		/*_____________________ Lemur Right ________________________*/
// 		Quneo.new(quneoStart, GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur, 1,  0,  0, 5),
// 		Quneo.new(quneoStart+(8*1), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 3,  1, 5),
// 		Quneo.new(quneoStart+(8*2), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 6,  2, 5),
// 		Quneo.new(quneoStart+(8*3), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 9,  3, 5),
// 		Quneo.new(quneoStart+(8*4), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 12, 4, 5),
// 		Quneo.new(quneoStart+(8*5), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 15, 5, 5),
// 		Quneo.new(quneoStart+(8*6), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 18, 6, 5),
// 		Quneo.new(quneoStart+(8*7), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 21, 7, 5),//
// 		Quneo.new(quneoStart+(8*8), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 24, 8, 5),
// 		Quneo.new(quneoStart+(8*9), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 27, 9, 5),
// 		Quneo.new(quneoStart+(8*10), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 30, 10, 5),
// 		Quneo.new(quneoStart+(8*11), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 33, 11, 5),//
// 		Quneo.new(quneoStart+(8*12), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 36, 12, 5),
// 		Quneo.new(quneoStart+(8*13), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 39, 13, 5),
// 		Quneo.new(quneoStart+(8*14), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur,  1, 42, 14, 5),
// 		Quneo.new(quneoStart+(8*15), GlobalPresets.mixerLemur, 0, GlobalPresets.stateLemur, 1, 45, 15, 5),
// 		OnsetTest.new((quneoStart+(8*15))+1, 4, GlobalPresets.mixerLemur),
// 		OutputMixer.new(2, \nano, GlobalPresets.outputSynthChan, ~isSplit, [0, 0, 0, 0, 1, 1, 1, 1]),
// 		ParametericEQGui.new(GlobalSynths.outputSynth, GlobalPresets.outputSynthChan + 1, \nano, 2),
// 	];
// 	GlobalPresets.addClasses(classes);
// 	{GlobalPresets.getClasses.add(InputManager.new(classes[..8], (GlobalPresets.outputSynthChan + 2)))}.defer;
// 	thisProcess.interpreter.executeFile(GlobalPaths.init++"singleSampleSynths.scd");
// }.value;
