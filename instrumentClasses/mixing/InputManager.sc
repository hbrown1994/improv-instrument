InputManager {/*-----> A Singleton class that manages hardware inputs going into the system*/
	classvar <>synthIndex, <>vars, <>synths, <>guiWindow, <>guis, <>recs=0;

	/*____________Constructors____________*/
	*new {arg classes, synthNum; ^super.new.init(classes, synthNum)}
	*updateGui {^super.new.updateGui}
	*run {^super.new.run}
	*save {^super.new.save}
	*load {^super.new.load}


	save { //save the parameter dictionary "vars" to the global "controls" dictionary with its key equaling its synthNum
		GlobalPresets.controls.add(synthIndex.asSymbol -> vars)
	}

	load { //save the parameter dictionary "vars" to the global "controls" dictionary
		if(GlobalPresets.controls[synthIndex.asSymbol]!=nil, {
			vars=GlobalPresets.controls[synthIndex.asSymbol];
			this.updateGui;
		});
	}

	/*____________Init Singelton Class: (only on instance per server)____________*/
	init {arg classes, synthNum;
		synthIndex = synthNum;
		synths = classes;

		guis = Dictionary.newFrom([\buttons, Dictionary.new, \recordButtons, Dictionary.new, \eqButtons, Dictionary.new]);

		if(guiWindow!=nil, {if(guiWindow.isClosed, {nil}, {guiWindow.close})}); //don't open a new window if one is already open

		vars = Dictionary.newFrom( //variables for gui buttons
			[   \0,   0, \1,   0, \2,   0, \3,   0, \4,   0, \5,   0, \6,   0, \7,   0, \8,   0,
				\eq0, 0, \eq1, 0, \eq2, 0, \eq3, 0, \eq4, 0, \eq5, 0, \eq6, 0, \eq7, 0, \eq8, 0
		]);

		recs = (Recorder(Server.local))!9; //Make recorders for each hardware input
	}

	/*____________Methods____________*/
	updateGui { //Update GUI buttons that control the processing on each hardware input channel (gate & eq)
		9.do{|i|
			guis[\buttons][i.asSymbol].value = vars[i.asSymbol];
			guis[\eqButtons][i.asSymbol].value = vars[("eq"++i).asSymbol];
		};
	}

	run{
		guiWindow = Window.new("InputManager", Rect(0, 388, 410, 150)).front;

		9.do{|i| var space=40;
			//Labels
			StaticText(guiWindow,  Rect(space*(i+1), 5, 40, 10)).string_(i.asString).align_(\center);

			//Gate Buttons
			guis[\buttons][i.asSymbol] = Button(guiWindow, Rect(space*(i+1), 20, 40, 40))
			.states_([["Off", Color.black, Color.gray], ["On", Color.black, Color.red]])
			.action_({arg msg; vars[i.asSymbol]=msg.value; if(msg.value==0.0, {synths[i].off}, {synths[i].on})});

			//Record Buttons
			guis[\recordButtons][i.asSymbol] = Button(guiWindow, Rect(space*(i+1), 60, 40, 40))
			.states_([["Rec", Color.black, Color.gray], ["Off", Color.black, Color.red]])
			.action_({arg msg;
				if(msg.value==1.0,
					{
						if(GlobalPresets.recorders.size==0, {GlobalPaths.updateRecDir});

						GlobalPresets.recorders.add(i.asSymbol ->
							recs[i].prepareForRecord(
								GlobalPaths.recordings+/+"input_"++i++".wav",
								GlobalPresets.numChannels)
						);

					},
					{GlobalPresets.recorders.removeAt(i.asSymbol)}
				);

			});

			//eq Buttons
			guis[\eqButtons][i.asSymbol] = Button(guiWindow, Rect(space*(i+1), 100, 40, 40))
			.states_([["EQ", Color.black, Color.gray], ["Off", Color.black, Color.red]])
			.action_({arg msg;

				synths[i].eq.vars['makeEQ'] = msg.value.asInteger;
				vars[("eq"++i).asSymbol]=msg.value;

				if(synths[i].eq != nil, {
					if(msg.value==1.0,
						{if(synths[i].eq.guiWindow!=nil, {if(synths[i].eq.guiWindow.isClosed, {synths[i].eq.run})}, {synths[i].eq.run})},
						{synths[i].eq.kill});
				});
			});
		};
	}
}


