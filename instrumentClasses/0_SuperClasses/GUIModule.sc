GUIModule {/*-----> A SuperClass for all Synthesis Modules using OSC synth <-----*/
	var <>vars, <>initVars, <>synth, <>synthIndex=0;

	/*____________Constructors____________*/

	*save {^super.new.save}
	*load {^super.new.load}
	*updateSynth{arg variables; ^super.new.updateSynth(variables)}

	save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum
		GlobalPresets.controls.add(synthIndex.asSymbol -> vars)
	}

	load { //save the parameter dictionary "vars" to the global "controls" dictionary
		if(GlobalPresets.controls[synthIndex.asSymbol]!=nil, {
			vars=GlobalPresets.controls[synthIndex.asSymbol];
			this.updateGui;
			this.updateSynth(GlobalPresets.controls[synthIndex.asSymbol]);
		});
	}

	updateSynth {arg variables; //update synth if it's runnign while also filtering out "makeEQ" as it only changes gui states
		if(synth.isPlaying, {
			if(variables['makeEQ']!=nil,
				{
					if(variables['makeEQ']==0,
						{variables.keysDo{|i| synth.set(i, initVars[i])}},
						{variables.keysDo{|i| synth.set(i, variables[i])}}
					);
				},
				{variables.keysDo{|i| synth.set(i, variables[i])}}
			);
		});
	}
}
