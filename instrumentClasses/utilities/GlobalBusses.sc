GlobalBusses {/*-----> A Singleton class that manages the audio and control Out used for the entire software system*/
	classvar <>monoOutArray, <>stereoOutArray, <>quadOutArray, <>sixOutArray, <>octoOutArray, <>twelveOutArray, <>sixteenOutArray, <>allOut, <>monoInArray, <>stereoInArray, <>quadInArray, <>sixInArray, <>octoInArray, <>twelveInArray, <>sixteenInArray, <>allIn, outputBusNum;

	*new {arg numBusses, outputBus; ^super.new.init(numBusses, outputBus)} //Indicate how many Out you want to create on class instantiation
	*monoOut {^super.new.monoOut}              //construct monoOut
	*stereoOut {^super.new.stereoOut}          //construct stereoOut
	*quadOut {^super.new.quadOut}          //construct stereoOut
	*sixOut {^super.new.sixOut}          //construct stereoOut
	*octoOut {^super.new.octoOut}          //construct stereoOut
	*twelveOut {^super.new.twelveOut}          //construct stereoOut
	*sixteenOut {^super.new.sixteenOut}
	*outputSynthBus {^super.new.outputSynthBus}

	*monoIn {^super.new.monoIn}
	*stereoIn {^super.new.stereoIn}
	*quadIn {^super.new.quadIn}
	*sixIn {^super.new.sixIn}
	*octoIn {^super.new.octoIn}
	*twelveIn {^super.new.twelveIn}
	*sixteenIn {^super.new.sixteenIn}

	init {arg numBusses, outputBus;                             //Make arrays of both mono and stereo Out of size numBusses

		outputBusNum=outputBus;
		monoOutArray = Array.fill(numBusses, {Bus.audio(Server.local,    1)});
		stereoOutArray = Array.fill(numBusses, {Bus.audio(Server.local,  2)});
		quadOutArray = Array.fill(numBusses, {Bus.audio(Server.local,    4)});
		sixOutArray = Array.fill(numBusses, {Bus.audio(Server.local,     6)});
		octoOutArray = Array.fill(numBusses, {Bus.audio(Server.local,    8)});
		twelveOutArray = Array.fill(numBusses, {Bus.audio(Server.local,  12)});
		sixteenOutArray = Array.fill(numBusses, {Bus.audio(Server.local, 16)});

		monoInArray = Array.fill(numBusses, {Bus.audio(Server.local,    1)});
		stereoInArray = Array.fill(numBusses, {Bus.audio(Server.local,  2)});
		quadInArray = Array.fill(numBusses, {Bus.audio(Server.local,    4)});
		sixInArray = Array.fill(numBusses, {Bus.audio(Server.local,     6)});
		octoInArray = Array.fill(numBusses, {Bus.audio(Server.local,    8)});
		twelveInArray = Array.fill(numBusses, {Bus.audio(Server.local,  12)});
		sixteenInArray = Array.fill(numBusses, {Bus.audio(Server.local, 16)});

		allOut = Dictionary.newFrom([ //Dictionaries that return which ever array of busses is needed number of channels
			\1, monoOutArray,
			\2, stereoOutArray,
			\4, quadOutArray,
			\6, sixOutArray,
			\8, octoOutArray,
			\12, twelveOutArray,
			\16, sixteenOutArray,
		]);

		allIn = Dictionary.newFrom([
			\1, monoInArray,
			\2, stereoInArray,
			\4, quadInArray,
			\6, sixInArray,
			\8, octoInArray,
			\12, twelveInArray,
			\16, sixteenInArray,
		]);
	}

	monoOut{^monoOutArray}             //Return arrays of busses depending on how many channels the system is running
	stereoOut{^stereoOutArray}
	quadOut{^stereoOutArray}
	sixOut{^stereoOutArray}
	octoOut{^stereoOutArray}
	twelveOut{^stereoOutArray}
	sixteenOut{^stereoOutArray}

	monoIn{^monoInArray}
	stereoIn{^stereoInArray}
	quadIn{^stereoInArray}
	sixIn{^stereoInArray}
	octoIn{^stereoInArray}
	twelveIn{^stereoInArray}
	sixteenIn{^stereoInArray}

	outputSynthBus {
		^allIn[GlobalPresets.numChannels.asSymbol][outputBusNum..]
	}
}