/* Sébastien Clara - Janvier 2014 - https://sebastienclara.wordpress.com/

ViewPreset v201401
///////////////////////////////////////////////////////////////////////
	Move or link in a extension directorie :
Typical user-specific extensions directories:
OSX	~/Library/Application Support/SuperCollider/Extensions/
Linux	~/.local/share/SuperCollider/Extensions/
*/

ViewPreset {
	var view;

	var <>interpolationNum;
	var nextButton, nextNum;

	var preset;
	var objectType0=nil, objectType1=nil, sizeAllType1Tab;
	var presetPopUp, presetNameChamp;

	var window, backupFile, trigFunctionFile;

	*new {	arg argWindow, argBackupFile, argTrigFunctionFile=nil;
		^super.new.init(argWindow, argBackupFile, argTrigFunctionFile);
	}

	init { arg argWindow, argBackupFile, argTrigFunctionFile;
		window = argWindow;
		backupFile = argBackupFile;
		trigFunctionFile = argTrigFunctionFile;
	}

	build {
		var saveButton, removeButton, saveFileButton;

		var isIntegerAction, interpolationType0Action, majType1Action;
		var objectIsDifferentAction, updatePresetAction;

		var trigFunctionAction, trigFunctionTab;

		preset=();

		if (trigFunctionFile.notNil, {
			trigFunctionTab = FileReader.read(trigFunctionFile, skipEmptyLines:true, skipBlanks:true, delimiter:$@);
		});
///////////////////////////////////////////////////////////////////////
// Gui
		view = View(window).background_(Color.rand);

		presetPopUp = PopUpMenu(view);
		saveButton = Button(view).states_([["Save"]]);
		removeButton = Button(view).states_([["Remove"]]);

		presetNameChamp = TextField(view).string_("");

		interpolationNum = NumberBox(view).align_(\center)
			.clipLo_(0).clipHi_(0)
			.step_(0.05).scroll_step_(0.05).value_(0);
		saveFileButton = Button(view).states_([["SaveOnFile"]]);

		nextButton = Button(view).states_([[">"]]);
		nextNum = NumberBox(view).align_(\center)
			.clipLo_(0).clipHi_(0).step_(1).scroll_step_(1);

		view.layout = VLayout(
			HLayout(saveFileButton, presetPopUp),
			presetNameChamp,
			HLayout(nextNum, nextButton, interpolationNum),
			HLayout(saveButton, removeButton)
		);
///////////////////////////////////////////////////////////////////////
// Action
		isIntegerAction = { arg valeur;
			(valeur-valeur.asInteger)%1 == 0;
		};

		presetPopUp.action = { arg menu;
			nextNum.value = menu.value;
			presetNameChamp.string = presetPopUp.item.asString;
		};
		nextButton.action = {
			interpolationNum.valueAction = nextNum.value;
		};

		interpolationNum.action = { arg num;
			if( num.clipHi != 0, {
				interpolationType0Action.value;

				if( isIntegerAction.value(num.value), {
					presetPopUp.value = num.value;

					presetNameChamp.string = presetPopUp.item.asString;

					majType1Action.value;

					trigFunctionAction.value;

					nextNum.value = num.value + 1;
				});
			});
		};

		objectIsDifferentAction = { arg indexPreset, indexObject;
			preset[ presetPopUp.items
				[interpolationNum.value] ][indexPreset]
			!= objectType1[indexObject].value;
		};

		updatePresetAction = { arg indexPreset, indexObject;
			if(objectIsDifferentAction.value(indexPreset, indexObject), {
				objectType1[indexObject].valueAction =
					preset[presetPopUp.items
					[interpolationNum.value]][indexPreset];
			});
		};

		majType1Action = {
			if(objectType1.notNil, {
				var cpt;
				sizeAllType1Tab.do({ |i, j|
					cpt = preset
						[presetPopUp.items[interpolationNum.value]].size
						- sizeAllType1Tab.copyToEnd(j).sum;

					if(preset[presetPopUp.items[interpolationNum.value]]
						[cpt] == 0, { // OFF
						i.do({
							updatePresetAction.value(
								cpt, cpt-objectType0.size
							);
							cpt=cpt+1;
						});
					}, { // ON
						cpt = objectType0.size +
							sizeAllType1Tab.copyFromStart(j).sum
							- 1;

						i.do({
							updatePresetAction.value(
								cpt, cpt-objectType0.size
							);
							cpt=cpt-1;
						});
					});
				});
			});
		};

		interpolationType0Action = {
			if((interpolationNum.clipHi > 0).and(interpolationNum.value < interpolationNum.clipHi), {
				blend(
					preset[presetPopUp.items
						[interpolationNum.value.asInteger].asSymbol]
					.copyFromStart(objectType0.size-1),

					preset[presetPopUp.items
						[interpolationNum.value.asInteger +1].asSymbol]
					.copyFromStart(objectType0.size-1),

					interpolationNum.value -interpolationNum.value.asInteger
				).do({ arg valeur, index;
					objectType0[index].valueAction = valeur;
				});
			});

			if(interpolationNum.value == interpolationNum.clipHi, {
				objectType0.do({ arg object, index;
					object.valueAction =
					preset[presetPopUp.items[interpolationNum.value]][index]
				});
			});
		};

		trigFunctionAction = {
			if ( trigFunctionFile.notNil, {
				if(trigFunctionTab.size > interpolationNum.value, {
					if(trigFunctionTab[interpolationNum.value.asInteger][1]
						.asString != "_", {
							trigFunctionTab
							[interpolationNum.value.asInteger]
							[1].compile.value;
					});
				});
			});
		};
///////////////////////////////////////////////////////////////////////
		saveButton.action = {
			if( presetNameChamp.string != "", {
				preset[presetNameChamp.string.asSymbol] =
					objectType0.collect({ |o| o.value });

				if(objectType1.notNil, {
					preset[presetNameChamp.string.asSymbol] =
						preset[presetNameChamp.string.asSymbol]
						++ objectType1.collect({ |o| o.value });
				});

				this.majPresets(presetNameChamp.string.asSymbol);
				("Save presets on" + presetNameChamp.string).postln;
			}, {
				("Preset not save! Enter a preset name.").postln;
			});
		};
		presetNameChamp.action = {
			saveButton.action.value;
		};
		removeButton.action = {
			if( presetNameChamp.string != "", {
				if(preset.order.find([presetNameChamp.string.asSymbol]).notNil, {
					("Remove the preset" + presetNameChamp.string).postln;
					preset.removeAt(presetNameChamp.string.asSymbol);
					this.majPresets;
				}, {
					("The preset --" + presetNameChamp.string + "-- don't exist").postln;
				});
			});
		};

		saveFileButton.action = {
			Archive.global.put(\presets, preset);
			Archive.write(backupFile);

			// Doubloon for backup
			Archive.write(
				PathName(backupFile).pathOnly ++
				Date.getDate.format("%Y-%m-%d_%H-%M-%S_").asString ++
				PathName(backupFile).fileName
			);
			("Save presets on the file" + backupFile).postln;
		};


		^view;
	}
///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////

	interpolation { ^interpolationNum }

	next { nextButton.valueAction = 0 }


	start { arg numeralObjectTab, launchingObjectTab=nil;
		objectType0 = numeralObjectTab;
		sizeAllType1Tab = launchingObjectTab.collect({|t| t.size});
		objectType1 = launchingObjectTab.flat;

		this.loadFilePreset;
	}

	loadFilePreset {
		if( File.exists(backupFile), {
			Archive.read(backupFile);
			preset = Archive.global.at(\presets);
			this.majPresets;
			("Open a presets's file" + backupFile).postln;
		});
	}

	majPresets { arg selectionSymbol=nil;
		if(preset.isEmpty.not, {
			presetPopUp.items = preset.order;
			interpolationNum.clipHi_(preset.size-1);
			nextNum.clipHi_(preset.size-1);
			if(selectionSymbol.isNil, {
				presetPopUp.value = 0;
			}, {
				presetPopUp.value = presetPopUp.items.find( [selectionSymbol] );
			});
			interpolationNum.valueAction = presetPopUp.value;
		}, {
			presetNameChamp.string = "";
			presetPopUp.items = [];
		});
	}



	destroy { view.destroy }

	close { this.destroy }
}
///////////////////////////////////////////////////////////////////////


