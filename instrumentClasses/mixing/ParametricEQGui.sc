ParametericEQGui : GUIModule {/*-----> A class for using a gui to control the parameteric eq ugen  <-----*/
	var <>guiWindow, <>params, <>gates, <>midiOut, <>channelNum, <>controller, <>guis, <>controlVars;

	/*____________Constructors____________*/
	*new {arg synthInstance, synthNum, controllerName=\nano, chan; ^super.new.init(synthInstance, synthNum, controllerName, chan)}
	*run{^super.new.run}
	*kill{^super.new.kill}
	*updateGui{^super.new.updateGui}
	*isOff{^super.new.isOff}
	*initMIDI{^super.new.initGui}

	initMIDI{ //engage EQ via midi controller or gui nutton
		if(controller!=\gui, {
			MIDIFunc.cc({|msg| vars['makeEQ'] = msg;
				if(msg==1,
					{if(guiWindow!=nil, {if(guiWindow.isClosed, {this.run})}, {this.run})},
					{this.kill});

				midiOut.control(channelNum, ctlNum: 27, val: msg)}, 27, channelNum);
		});
	}

	init {arg synthInstance, synthNum, controllerName=\nano, chan;

		if(guiWindow!=nil, {if(guiWindow.isClosed, {nil}, {guiWindow.close})});
		controller=controllerName;
		synth=synthInstance;
		synthIndex=synthNum;
		if(controllerName!=\gui, {midiOut = GlobalPresets.midiOut[controllerName]});
		channelNum = chan;

		guis = Dictionary.newFrom([\slider, Dictionary.new, \numBox, Dictionary.new, \button, Dictionary.new, \sliderScale, Dictionary.new]);

		initVars = Dictionary.newFrom(
			[
				'lpfGate', 0, 'lpfFreq', 20000, 'hpfGate',  0, 'hpfFreq', 20,
				'highshelfGate', 0, 'highshelfFreq', 16000, 'highshelfAmp', 0,
				'lowshelfGate', 0, 'lowshelfFreq', 250, 'lowshelfAmp', 0,
				'bpf0Gate', 0, 'bpf0Freq', 250, 'bpf0RQ', 1, 'bpf0Amp', 0,
				'bpf1Gate', 0, 'bpf1Freq', 250, 'bpf1RQ', 1, 'bpf1Amp', 0,
				'bpf2Gate', 0, 'bpf2Freq', 1000, 'bpf2RQ', 1, 'bpf2Amp', 0,
				'bpf3Gate', 0, 'bpf3Freq', 1000,'bpf3RQ', 1, 'bpf3Amp', 0,
				'bpf4Gate', 0, 'bpf4Freq', 4000, 'bpf4RQ', 1, 'bpf4Amp', 0,
				'bpf5Gate', 0, 'bpf5Freq', 4000, 'bpf5RQ', 1, 'bpf5Amp', 0,
				'makeEQ', 0
		]);

		vars = Dictionary.newFrom(
			[
				'lpfGate', 0, 'lpfFreq', 20000, 'hpfGate',  0, 'hpfFreq', 20,
				'highshelfGate', 0, 'highshelfFreq', 16000, 'highshelfAmp', 0,
				'lowshelfGate', 0, 'lowshelfFreq', 250, 'lowshelfAmp', 0,
				'bpf0Gate', 0, 'bpf0Freq', 250, 'bpf0RQ', 1, 'bpf0Amp', 0,
				'bpf1Gate', 0, 'bpf1Freq', 250, 'bpf1RQ', 1, 'bpf1Amp', 0,
				'bpf2Gate', 0, 'bpf2Freq', 1000, 'bpf2RQ', 1, 'bpf2Amp', 0,
				'bpf3Gate', 0, 'bpf3Freq', 1000,'bpf3RQ', 1, 'bpf3Amp', 0,
				'bpf4Gate', 0, 'bpf4Freq', 4000, 'bpf4RQ', 1, 'bpf4Amp', 0,
				'bpf5Gate', 0, 'bpf5Freq', 4000, 'bpf5RQ', 1, 'bpf5Amp', 0,
				'makeEQ', 0
		]);

		gates = Dictionary.newFrom(
			[
				'lpfGate', 0, 'hpfGate',  0,
				'highshelfGate', 0, 'lowshelfGate', 0,
				'bpf0Gate', 0, 'bpf1Gate', 0,
				'bpf2Gate', 0, 'bpf3Gate', 0,
				'bpf4Gate', 0, 'bpf5Gate', 0,
		]);

		params = Dictionary.newFrom(
			[
				'lpfFreq', 20000,  'hpfFreq', 20,
				'highshelfFreq', 16000, 'highshelfAmp', 0,
				'lowshelfFreq', 250, 'lowshelfAmp', 0,
				'bpf0Freq', 250,     'bpf0RQ', 1, 'bpf0Amp', 0,
				'bpf1Freq', 250,     'bpf1RQ', 1, 'bpf1Amp', 0,
				'bpf2Freq', 1000,    'bpf2RQ', 1, 'bpf2Amp', 0,
				'bpf3Freq', 1000,    'bpf3RQ', 1, 'bpf3Amp', 0,
				'bpf4Freq', 4000,    'bpf4RQ', 1, 'bpf4Amp', 0,
				'bpf5Freq', 4000,    'bpf5RQ', 1, 'bpf5Amp', 0,
				'makeEQ', 0,
		]);

		this.initMIDI;
	}

	updateGui {

		if(GlobalPresets.controls[synthIndex.asSymbol]==nil, {
			InputManager.vars.keysDo{|i|
				InputManager.guis[\buttons][i].value = InputManager.vars[i];
				InputManager.guis[\eqButtons][i].value = InputManager.vars;
			};
		});

		if(guis[\slider].size==0, {this.run}, {
			gates.keysDo{|i| guis[\button][i].value = vars[i]};

			params.keysDo{|i|
				if(i=='makeEQ',
					{
						if(vars['makeEQ']==1,
							{if(this.isOff, {this.run})},
							{if(this.isOff, {nil}, {this.kill})};
						);

					},
					{
						guis[\numBox][i].value = vars[i];
						guis[\slider][i].value = vars[i].linlin(guis[\sliderScale][i][0], guis[\sliderScale][i][1], 0, 1);
					}
			)};
		});
	}

	isOff {
		^if(guiWindow!==nil, {guiWindow.isClosed}, {true});
	}

	kill {
		if(synth.isPlaying, {gates.keysDo{|i| synth.set(i, 0)}});
		if(guiWindow!=nil, {if(guiWindow.isClosed, {nil}, {{guiWindow.close}.defer})}, {nil});
	}


	run {arg buttonLength=40, sliderLength=250, height = 17;
		var channel, buttons0, buttonNames0, sliders0, buttons1, buttonNames1, sliders1, buttons2, buttonNames2, sliders2, buttons3, buttonNames3, sliders3, freqRanges=[[20, 500], [20, 500], [500, 2000], [500, 2000], [2000, 16000], [2000, 16000]];

		if(synth.isPlaying, {this.updateSynth(vars)}, {this.initMIDI});
		this.save;

		{
			guiWindow = Window.new("ParametricEQ"++"_"++synthIndex, Rect(0, 0, 1350, 105)).front;
			guiWindow.view.decorator_(FlowLayout(guiWindow.bounds, 0@0, 0@0));
			channel = CompositeView(guiWindow, 1450@105).background_(Color.black);

			buttons0 = ["lpfGate", "hpfGate", "highshelfGate", "highshelfAmp", "lowshelfGate", "lowshelfAmp"];
			buttonNames0 = ["lpf", "hpf", "High", "Amp", "Low", "Amp"];
			sliders0 = ["lpfFreq", "hpfFreq", "highshelfFreq", "highshelfAmp", "lowshelfFreq", "lowshelfAmp"];

			buttons1 = ["bpf0Gate", "bpf0Amp", "bpf0RQ", "bpf1Gate", "bpf1Amp", "bpf1RQ"];
			buttonNames1 = ["bpf0", "Amp",  "RQ", "bpf1", "Amp",  "RQ"];å
			sliders1 = ["bpf0Freq", "bpf0Amp", "bpf0RQ", "bpf1Freq", "bpf1Amp", "bpf1RQ"];

			buttons2 = ["bpf2Gate", "bpf2Amp", "bpf2RQ", "bpf3Gate", "bpf3Amp", "bpf3RQ"];
			buttonNames2 = ["bpf2", "Amp",  "RQ", "bpf3", "Amp",  "RQ"];
			sliders2 = ["bpf2Freq", "bpf2Amp", "bpf2RQ", "bpf3Freq", "bpf3Amp", "bpf3RQ"];

			buttons3 = ["bpf4Gate", "bpf4Amp", "bpf4RQ", "bpf5Gate", "bpf5Amp", "bpf5RQ"];
			buttonNames3 = ["bpf3", "Amp",  "RQ", "bpf4", "Amp",  "RQ"];
			sliders3 = ["bpf4Freq", "bpf4Amp", "bpf4RQ", "bpf5Freq", "bpf5Amp", "bpf5RQ"];


			buttons0.do{|item, index|
				channel.decorator_(FlowLayout(channel.bounds, 0@(height*index), 0@0));

				guis[\button][(item).asSymbol] = Button(channel, buttonLength@height).font_(Font(size: 10))
				.states_([[buttonNames0[index], Color.black, Color.gray(0.8)],[buttonNames0[index], Color.black, Color.red]])
				.action_({ arg msg;
					vars[(item).asSymbol]=msg.value;
					if(synth.isPlaying, {synth.set((item).asSymbol, msg.value)});
					msg.value;
				});
			};

			sliders0.do{|item, index|
				var min, max;
				case
				{index  == 0}{min=8000;  max=20000}
				{index  == 1}{min=20;    max=500}
				{index  == 2}{min=8000;  max=20000}
				{index  == 3}{min=(-30); max=18}
				{index  == 4}{min=20;    max=500}
				{index  == 5}{min=(-30); max=18};

				guis[\sliderScale][(item).asSymbol] = [min, max];

				channel.decorator_(FlowLayout(channel.bounds, buttonLength@(height*index), 0@0));

				guis[\slider][(item).asSymbol] = Slider(channel, sliderLength@height)
				.action_({arg msg;
					var sliderVal = msg.value.linlin(0, 1, min, max).round(0.01);

					vars[(item).asSymbol]=sliderVal;
					if(synth.isPlaying, {synth.set((item).asSymbol, sliderVal)});
					guis[\numBox][(item).asSymbol].value = sliderVal;

				});

				channel.decorator_(FlowLayout(channel.bounds, (sliderLength+buttonLength)@(height*index), 0@0));
				guis[\numBox][(item).asSymbol] = NumberBox(channel, buttonLength@height).font_(Font(size: 12))
			};

			/*_________________________1_________________________________________________*/
			buttons1.do{|item, index|
				channel.decorator_(FlowLayout(channel.bounds, ((buttonLength*2)+sliderLength)@(height*index), 0@0));

				guis[\button][(item).asSymbol] = Button(channel, buttonLength@height).font_(Font(size: 10))
				.states_([[buttonNames1[index], Color.black, Color.gray(0.8)],[buttonNames1[index], Color.black, Color.red]])
				.action_({ arg msg;
					vars[(item).asSymbol]=msg.value;
					if(synth.isPlaying, {synth.set((item).asSymbol, msg.value)});
				});
			};


			sliders1.do{|item, index, count=0|
				var min, max;

				case
				{index%3 == 0}{min=freqRanges[index.wrap(0, 1)][0]; max=freqRanges[index.wrap(0, 1)][1];}
				{index%3 == 1}{min=(-30); max=18}
				{index%3 == 2}{min=0.01; max=10};

				guis[\sliderScale][(item).asSymbol] = [min, max];

				channel.decorator_(FlowLayout(channel.bounds, ((buttonLength*3)+sliderLength)@(height*index), 0@0));

				guis[\slider][(item).asSymbol] = Slider(channel, sliderLength@height)
				.action_({arg msg;
					var sliderVal = msg.value.linlin(0, 1, min, max).round(0.01);
					vars[(item).asSymbol]=sliderVal;
					if(synth.isPlaying, {synth.set((item).asSymbol, sliderVal)});
					guis[\numBox][(item).asSymbol].value = sliderVal;
				});

				channel.decorator_(FlowLayout(channel.bounds, ((sliderLength*2)+(buttonLength*3))@(height*index), 0@0));
				guis[\numBox][(item).asSymbol] = NumberBox(channel, buttonLength@height).font_(Font(size: 12));
			};

			/*_________________________2_________________________________________________*/

			buttons2.do{|item, index|

				channel.decorator_(FlowLayout(channel.bounds, ((buttonLength*4)+(sliderLength*2))@(height*index), 0@0));

				guis[\button][(item).asSymbol] = Button(channel, buttonLength@height).font_(Font(size: 10))
				.states_([[buttonNames2[index], Color.black, Color.gray(0.8)],[buttonNames2[index], Color.black, Color.red]])
				.action_({ arg msg;
					vars[(item).asSymbol]=msg.value;
					if(synth.isPlaying, {synth.set((item).asSymbol, msg.value)});
				});
			};

			sliders2.do{|item, index, count=0, add=2|
				var min, max;

				case
				{index%3 == 0}{min=freqRanges[index.wrap(0, 1)+add][0]; max=freqRanges[index.wrap(0, 1)+add][1]}
				{index%3 == 1}{min=(-30); max=18}
				{index%3 == 2}{min=0.01; max=10};

				guis[\sliderScale][(item).asSymbol] = [min, max];

				channel.decorator_(FlowLayout(channel.bounds, ((buttonLength*5)+(sliderLength*2))@(height*index), 0@0));

				guis[\slider][(item).asSymbol] = Slider(channel, sliderLength@height)
				.action_({arg msg;
					var sliderVal = msg.value.linlin(0, 1, min, max).round(0.01);
					vars[(item).asSymbol]=sliderVal;
					if(synth.isPlaying, {synth.set((item).asSymbol, sliderVal)});
					guis[\numBox][(item).asSymbol].value = sliderVal;
				});

				channel.decorator_(FlowLayout(channel.bounds, ((sliderLength*3)+(buttonLength*5))@(height*index), 0@0));
				guis[\numBox][(item).asSymbol] = NumberBox(channel, buttonLength@height).font_(Font(size: 12));
			};


			/*_________________________3_________________________________________________*/

			buttons3.do{|item, index|
				channel.decorator_(FlowLayout(channel.bounds, ((buttonLength*6)+(sliderLength*3))@(height*index), 0@0));

				guis[\button][(item).asSymbol] = Button(channel, buttonLength@height).font_(Font(size: 10))
				.states_([[buttonNames3[index], Color.black, Color.gray(0.8)],[buttonNames3[index], Color.black, Color.red]])
				.action_({ arg msg;
					vars[(item).asSymbol]=msg.value;
					if(synth.isPlaying, {synth.set((item).asSymbol, msg.value)});
				});
			};

			sliders3.do{|item, index, count=0, add=4|
				var min, max;

				case
				{index%3 == 0}{min=freqRanges[index.wrap(0, 1)+add][0]; max=freqRanges[index.wrap(0, 1)+add][1]}
				{index%3 == 1}{min=(-30); max=18}
				{index%3 == 2}{min=0.01; max=10};

				guis[\sliderScale][(item).asSymbol] = [min, max];

				channel.decorator_(FlowLayout(channel.bounds, ((buttonLength*7)+(sliderLength*3))@(height*index), 0@0));

				guis[\slider][(item).asSymbol] = Slider(channel, sliderLength@height)
				.action_({arg msg;
					var sliderVal = msg.value.linlin(0, 1, min, max).round(0.01);
					vars[(item).asSymbol]=sliderVal;
					if(synth.isPlaying, {synth.set((item).asSymbol, sliderVal)});
					guis[\numBox][(item).asSymbol].value = sliderVal;
				});

				channel.decorator_(FlowLayout(channel.bounds, ((sliderLength*4)+(buttonLength*7))@(height*index), 0@0));
				guis[\numBox][(item).asSymbol] = NumberBox(channel, buttonLength@height).font_(Font(size: 12));
			};

			this.updateGui;

		}.defer;
	}
}