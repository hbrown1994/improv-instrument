Dummy {/*-----> A class for the OnsetLooper synth <-----*/
	var <>vars, <>synthIndex=999, controlVars, synthVars;

	/*____________Constructors____________*/
	*new {arg synthNum; ^super.new.init(synthNum)}
	*run {^super.new.run}
	*kill {^super.new.kill}
	*save {^super.new.save}
	*load {^super.new.load}

		//Assign each instance an index for its trigger synth, synth, lemur controller,
		//which container in the lemur interface will control this instance and an index
		//for syncing one instance's trigger synth to another
		init { arg synthNum;

			vars = Dictionary.newFrom([\synthIndex, synthNum]);
			controlVars = [\synthIndex];
			synthVars = [\synthIndex];
		}

		save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum
			GlobalPresets.controls.add(synthIndex.asSymbol -> vars)
		}

		load { //save the parameter dictionary "vars" to the global "controls" dictionary
			vars=GlobalPresets.controls[synthIndex.asSymbol];
		}

		run {
			this.save; //add vars to global controls on instance
		}
	}