GlobalSynths {/*-----> A Singleton class that manages the synths running on the server at any given time*/
	classvar <>synthsDict, <>synthsNums, <>outputSynth, <>areRunning=false, <>processors, <>onsetThreshs, <>needsOnsetData, <>guis;

	/*____________Constructors____________*/
	*new {arg maxSynths; ^super.new.init(maxSynths)}
	*synths {^super.new.synths}
	*synthNums {arg synthsDict; ^super.new.synthNums}
	*freeSynths {^super.new.freeSynths}

	//Make a dictionary to store the synth instances
	//Each synth has unique index that tells the system:
	//1: where it is in this dictionary (synthsDict)
	//2: what the synth's node index is (for GlobalNodes.nodes)
	//3: what the synth's output bus index is (for GlobalBusses.monoBusses/stereoBusses)
	init { arg maxSynths;
		synthsDict = Dictionary.new;
		synthsNums = List.new(0);
		processors = List.new(0);
	    (maxSynths+1).do{|i| if((i-1)==(-1), {onsetThreshs=Dictionary.new}, {onsetThreshs.put(i.asSymbol, 0.7)})};
		guis = List.newClear(2);

	}

	synths{^synthsDict} //Return dictionary with each synth instance

	synthNums{          //Return number of synths in the dictionary
		synthsDict.keysDo{|keys| synthsNums.add(keys)}
		^synthsNums
	}

	freeSynths {        //Free all synths from server
		synthsDict.keysDo{|i| if(synthsDict[i].isPlaying, {synthsDict[i].free})}
	}

}


