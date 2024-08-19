GlobalPresets {/*-----> A Singleton class that manages the presets on disk for the entire software system*/
	classvar <>controlsDict, <>presetNamesArray, <>globalPath, <>synths, <>loader, <>classes, <>numChannels=2, <>midiOut, <>recorders, <>outputSynthChan, <>synthLemur, <>mixerLemur, <>stateLemur, <>osc, <>masterAmp=0, <>masterSlider;

	/*____________Constructors____________*/
	*new {arg path, synthPath, oscPath, outputIndex=200, synthLemurIP, mixerLemurIP, stateLemurIP, midiOffOn; ^super.new.init(path, synthPath, oscPath, outputIndex, synthLemurIP, mixerLemurIP, stateLemurIP, midiOffOn)}
	*controls {^super.new.controls}
	*addClasses {arg classList; ^super.new.addClasses(classList)}
	*getClasses {^super.new.getClasses}
	*save {arg path, presetName; ^super.new.save(path, presetName)}
	*deletePreset {arg path, presetName; ^super.new.save(path, presetName)}
	*overwritePreset{arg path, presetName; ^super.new.save(path, presetName)}
	*overwriteSave {arg path, presetName; ^super.new.save(path, presetName)}
	*load {arg path, presetName; ^super.new.load(path, presetName)}
	*presetNames {^super.new.presetNames}
	*gui {^super.new.gui}
	*makeOSC {^super.new.makeOSC}

	/*____________Methods____________*/
	/*____________Init Singelton Class: (only on instance per server)____________*/
	init {arg path, synthPath, oscPath, outputIndex=200, synthLemurIP, mixerLemurIP, stateLemurIP, midiOffOn;
		MIDIClient.init;   //Init midi controllers
		MIDIIn.connectAll; //Concect midi controllers to server
		synthLemur = Lemur.new("synthLemur", synthLemurIP, 8000); //Init and assign a Lemur instance for synths
		mixerLemur = Lemur.new("mixerLemur", mixerLemurIP, 8000); //Init and assign a Lemur instance for mixer
		stateLemur = Lemur.new("stateLemur", stateLemurIP, 8000);
		outputSynthChan = outputIndex;
		synths = synthPath;
		osc = oscPath;
		globalPath = path;                                                //assign path from constuctor as a global class variable
		controlsDict = Dictionary.new;                                    //Create multi dimensional dictionary to store all control parameters for the entire system
		classes = List.new(0);                                            //Create list to store instances of all synth & control classes being used in this system
		if(Object.readArchive(path++"presetNames").isCollection,          //Read a file containing the preset names as a List and assign to a global class variable;
			{presetNamesArray = Object.readArchive(path++"presetNames")}, //If Object from disk is a list, assign to class variable;
			{"No Presets".postln; presetNamesArray=List.new(0)}           //If Object from disk is NOT a list, make new List and assign to class variable;
		);

		if(midiOffOn==1,
			{
				midiOut = Dictionary.newFrom( //MIDIOut instances for sending visual data to MIDI controllers
					[
						\nano, MIDIOut.newByName("nanoKONTROL2",  "CTRL").latency=Server.local.latency,
						\dicer, MIDIOut.newByName("Dicer", "Novation Dicer").latency=Server.local.latency,
						\bopPad, MIDIOut.newByName("BopPad", "BopPad").latency=Server.local.latency]
				);
			},
			{
				midiOut = Dictionary.newFrom([\nano, MIDIOut.newByName("nanoKONTROL2",  "CTRL").latency=Server.local.latency]);
			}
		);
		recorders = Dictionary.new; //To store recorders for each synth instance
	}

	makeOSC {
		//Init all classes that use OSC controllers
		//GlobalGui.new;
		PathName(osc).entries.size.do{|i|
			thisProcess.interpreter.executeFile(osc++PathName(osc).entries.at(i).fileName.asString)
		}.value;
	}

	controls{^controlsDict}        //Return control data dictionary when called
	getClasses{^classes}           //Return list of classes when called
	presetNames{^presetNamesArray} //Return list of preset names when called

	addClasses{arg classList;      //Recieves an array of synth/control class instances, converts to a List, and assigns said List to the class variable "classes"
		classes = List.newUsing(classList)
	}

	save{arg path, presetName, presetExists=0;                        //Saves a preset to disk & name of preset to array/disk
		if(GlobalSynths.areRunning, {
			if(presetName.isString, {                                 //if: preset is a String, Run function | Else: print "Preset Name is Not String"
				(presetNamesArray.size+1).do{|i|                      //Iterate over presets:
					if(i==(presetNamesArray.size) && presetExists==0,   // if: the preset name already exists, stop loop and post "Preset Exists"|Else: save preset
						{controlsDict.writeArchive(path++presetName); presetNamesArray.add(presetName).writeArchive(path++"presetNames")},
						{
							if(presetNamesArray[i]==presetName, {"Preset Exists".postln; presetExists=1});
							if(presetName=="", {"Not Valid".postln; presetExists=1});
						}
					);
				}
			}, {"Preset Name is Not String".postln})
		}, {"Need To Run".postln});

	}

	deletePreset {arg path, presetName;                                         //Deletes a preset from disk & name of presetNamesArray from array/disk
		File.delete(path++presetName);                                          //Delete File on Disk
		presetNamesArray.removeAt(presetNamesArray.asArray.find([presetName])); //Find presetName in presetNamesArray and remove it
	}

	overwritePreset {arg path, presetName;                                      //Deletes a preset from disk
		File.delete(path++presetName);                                          //but keeps preset name in presetNamesArray
	}

	overwriteSave{arg path, presetName;                                         //Saves a preset to disk with same name as current preset (for overwriting)
		controlsDict.writeArchive(path++presetName);
	}

	load{arg path, presetName;                                                  //Loads a preset dictionary from disk by assigning it to controlsDict
		controlsDict = Object.readArchive(path++presetName)
	}

	gui{var presetGuiMain, text, menu, save, load, run, kill, overwrite, delete, channels, synthLoad, ampNum;                  //a Gui method in which all the above methods are executed.
		presetGuiMain = Window.new("PresetManager", Rect(0, 600, 410, 280)).front;              //Make window
		text = TextField(presetGuiMain, Rect(10, 10, 150, 20));                                   //add a text field for typing the preset name
		menu = PopUpMenu(presetGuiMain, Rect(170, 10, 170, 20)).items = presetNamesArray.asArray; //Populate PopUpMenu with presetNamesArray

		save = Button(presetGuiMain, Rect(10, 40, 75, 75/2)) //A button that loops through each class in "classes" and calls their .save methods on mouseDown
		.states_([["Save", Color.black, Color.red]])           //Each class' .save methods adds all of their control patameters to a dictionary
		.mouseDownAction_({classes.do{|i| if(i.isNil, {nil}, {i.save})}})            //On mouseUp, call this classes .save method
		.mouseUpAction_({
			this.save(globalPath, text.value);
			menu.items=presetNamesArray.asArray;
		});

		load = Button(presetGuiMain, Rect(170, 40, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
		.states_([["Load", Color.black, Color.red]])            //each synth/control class via their individual .load methods
		.mouseDownAction_({this.load(globalPath, menu.item)})   //get control dictionary from disk
		.mouseUpAction_({classes.do{|i| if(i.isNil, {nil}, {i.load})}});              //distributes the control data to each class

		Button(presetGuiMain, Rect(250, 40, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
		.states_([["Init Classes", Color.black, Color.red]])            //each synth/control class via their individual .load methods
		.mouseDownAction_({this.makeOSC});

		Button(presetGuiMain, Rect(250, 90, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
		.states_([["Init Run", Color.black, Color.red]])            //each synth/control class via their individual .load methods
		.mouseDownAction_({this.makeOSC})   //get control dictionary from disk
		.mouseUpAction_({classes.do{|i| if(i.isNil, {nil}, {i.run})}});              //distributes the control data to each class

		Button(presetGuiMain, Rect(250, 140, 75, 75/2)) //A button that loads a preset from disk and distributes the control data to
		.states_([["MUTE OFF", Color.black, Color.red], ["MUTE ALL", Color.black, Color.green]])            //each synth/control class via their individual .load methods
		.action_({arg msg;
			if(msg.value==1,
				{8.do{|i| if(GlobalSynths.outputSynth.isPlaying, {GlobalSynths.outputSynth.set(("mute"++i).asSymbol, 0.0)})}},
				{8.do{|i| if(GlobalSynths.outputSynth.isPlaying, {GlobalSynths.outputSynth.set(("mute"++i).asSymbol, 1.0)})}}
			);
		});

		run = Button(presetGuiMain, Rect(10, 90, 75, 75/2))   //Run the entire system via calling each class'
		.states_([["Run", Color.black, Color.red]])             //.run methods (essentially running the class' OSCFuncs/MIDIFuncs)
		.mouseDownAction_({classes.do{|i| if(i.isNil, {nil}, {i.run})}});

		kill = Button(presetGuiMain, Rect(170, 90, 75, 75/2)) //Kill each class' currently running processes
		.states_([["Kill", Color.black, Color.red]])
		.mouseDownAction_({classes.do{|i| if(i.isNil, {nil}, {i.kill})}});

		overwrite = Button(presetGuiMain, Rect(170, 140, 75, 75/2))   //Overwrite the preset currently selected in the pop-up window
		.states_([["Overwrite", Color.black, Color.red]])
		.mouseDownAction_({this.overwritePreset(globalPath, menu.item)}) //Delete preset on mouseDown & Create new one with the same name on mouseUp
		.mouseUpAction_({this.overwriteSave(globalPath, menu.item)});

		delete= Button(presetGuiMain, Rect(10, 140, 75, 75/2)) //Delete preset on disk on mouseDown & update the popUpMenu items on mouseUp
		.states_([["Delete", Color.black, Color.red]])
		.mouseDownAction_({this.deletePreset(globalPath, menu.item)})
		.mouseUpAction_({menu.items=presetNamesArray.asArray; presetNamesArray.writeArchive(globalPath++"presetNames")});

		channels = PopUpMenu(presetGuiMain, Rect(10, 190, (75)*(2/3), 75/2)) //a menu for selectiong number of output channels
		.items = ["1", "2", "4", "6", "8", "10", "12", "14", "16"];

		Button(presetGuiMain, Rect(10+50, 190, (75)*(1/3), 75/2))     //Set number of output channels
		.states_([["Set", Color.black, Color.red]])
		.mouseDownAction_({numChannels = channels.item.asInteger});

		synthLoad = Button(presetGuiMain, Rect(170, 190, 75, 75/2))   //Add synthDefs to server (used when changing number of output channels)
		.states_([["Synth Load", Color.black, Color.red]])
		.mouseDownAction_({numChannels = channels.item.asInteger; GlobalSynths.processors = List.new(0)})
		.mouseUpAction_({
			PathName(synths).entries.do{|i|
				(i.entries).do{|j| thisProcess.interpreter.executeFile(j.fullPath)}
			};
		});

		masterSlider =Slider(presetGuiMain, Rect(350, 40, 50, 200))  //Make a slider/numBox to control whole system's output gain
		.value_({0.0})
		.action_({arg msg;
			masterAmp = msg.value;
			if(GlobalSynths.outputSynth.isPlaying, {GlobalSynths.outputSynth.set(\masterAmp, msg.value)});
			ampNum.value = msg.value.ampdb;
		});
		ampNum = NumberBox(presetGuiMain, Rect(350, 240, 50, 75/2));
	}
}



