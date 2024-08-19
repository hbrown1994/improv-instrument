// noteNums: 36 - 48 [51 is a toggle for state control]
// chan: 3

//49 = MIDI num for param hold

(
{

	var synthCollect=[\ssFB_0, \ssFB_1, \ssFB_2, \ssFB_3, \ssFB_4, \ssFB_5, \ssFB_6, \ssFB_7, \ssFB_8, \ssFB_9, \ssFB_10, \ssFB_11, \ssFB_12], synth=nil, loopSynth=nil, latch=0, synthHistory=[nil, nil], numNoteOn=0, bus=Bus.audio(~server_2,  2), amp=1, paramHold=0;
	var monitorButtons=List.new;
	var ssNode = Group.new(~server_2);
	var synthVars = Dictionary.newFrom(
		[
			\ssFB_0, Dictionary.newFrom([\freq, rrand(0.1, 1.0), \bwr, rrand(0, 7), \noiseFreq, rrand(0.01, 0.5)]),
			\ssFB_1, Dictionary.newFrom([\freqs, {rrand(0.001, 0.4)}!3, \skew, rrand(-1.0, 1.0), \clip, rrand(0.7, 1.0), \sync, rrand(-7.0, 7.0), \mul, rrand(0.0, 2.0), \iminsweep,  rrand(4.0, 20.0), \integrator, {rrand(0.99, 1.0)}!3, \mult, rrand(-1.0, 1.0)]),
			\ssFB_2, Dictionary.newFrom([\freqs, {rrand(100, 10000)}!8, \bwr, {rrand(0.01, 5)}!8, \integrator, {rrand(0.98, 1)}!8, \noiseMult, {rrand(-1.0, 1.0)}]),
			\ssFB_3, Dictionary.newFrom([\freqs, rrand(0.0001, 15000)!8, \bwr, rrand(0.01, 5)!8, \integrator, rrand(0.98, 1)!8, \mult, {rrand(0.001, 0.4)}]),
			\ssFB_4, Dictionary.newFrom([\dustFreq, {rrand(0.01, 1)}, \integrator, {rrand(0.01, 1.0)}!7, \mult, {rrand(1, 10000)}!7, \a0, {rrand(0.0, 1.0)}!7, \a1, {rrand(0.0, 1.0)}!7, \b1, {rrand(0.0, 1.0)}!7, \multAlt, {rrand(-1.0, 1.0)}!7]),
			\ssFB_5, Dictionary.newFrom([\dustFreq, {rrand(0.01, 1)}, \integrator, {rrand(0.01, 1.0)}!7, \mult, rrand(1, 10000)]),
			\ssFB_6, Dictionary.newFrom([\integrator, {rrand(0.1, 0.99)}!9, \mult, {rrand(-1.0, 1.0)}!9]),
			\ssFB_7, Dictionary.newFrom([\multNeg, {rrand(-1.0, 1.0)}!3, \multAlt, rrand(10, 10000), \integrator, {rrand(0.2, 1.0)}!3, \dustFreq, rrand(0.01, 1.0), \freq, {rrand(0, 0.02)}!3, \clip, {rrand(0.0, 1.0)}!3, \skew, {rrand(-1.0, 1.0)}!3, \sync, {rrand(0.0, 2.0)}!3, \mul, {rrand(0.0, 100.0)}!3]),
			\ssFB_8, Dictionary.newFrom([\freq, {rrand(0, 0.8)}!3, \clip, {rrand(0.0, 1.0)}!3, \skew, {rrand(-2.0, -2.0)}!3, \sync, {rrand(0.0, 2.0)}!3, \iminsweep, {rrand(0.0, 100.0)}!3, \mulSel, 2.rand, \integrator, {rrand(0.99, 1.0)}!3, \mult, {rrand(-10000.0, 10000.0)}!9, \sigLow, rrand(0.05, 0.5), \sigHigh, rrand(0.6, 2.0)]),
			\ssFB_9, Dictionary.newFrom([\mult, {rrand(-1.0, 1.0)}!81, \vel, rrand(10.0, 10000.0), \integrator, {rrand(0.98, 1.0)}!9, \freq, {rrand(20000.0, 10000.0)}, \pulseFreq, {rrand(0.0, 0.1)}!9, \width, {rrand(0.0, 2pi)}!9, \mul, {rrand(0.0, 20.0)}, \add, {rrand(0.0, 20.0)}]),
			\ssFB_10, Dictionary.newFrom([\freq, {rrand(1, 2000)}!3, \integrator, {rrand(0.1, 1.0)}!3, \mult, {rrand(-10000.0, 10000.0)}!9]),
			\ssFB_11, Dictionary.newFrom([\freq, {0.01.exprand(10000.0)}!5, \rq, {rrand(0.01, 5)}!5, \integrator, {rrand(0.1, 1.0)}!5, \mult, {rrand(-100000.0, 100000.0)}!25]),
			\ssFB_12, Dictionary.newFrom([\freq, {rrand(0, 1.0)}!5, \rq, {rrand(0.1, 1.0)}!5, \integrator, {rrand(0.1, 1.0)}!5, \mult, {rrand(-10000.0, 10000.0)}!25])
	]);

	PathName(GlobalPaths.synthDefs++"singleSampleSynths").entries.do{|i| thisProcess.interpreter.executeFile(i.fullPath)};

	loopSynth = Synth(\ssFB_looper, [\amp, amp, \bus, bus, \out, ~server2Out, \gate, 1], ssNode, 'addToTail').register;

	//paramhold

	MIDIFunc.noteOn({paramHold=1; {monitorButtons[0].value=1}.defer}, 49, 3);
	MIDIFunc.noteOff({paramHold=0; {monitorButtons[0].value=0}.defer}, 49, 3);

	//latch
	MIDIFunc.noteOn({latch=1; {monitorButtons[1].value=1}.defer}, 51, 3);
	MIDIFunc.noteOff({latch=0; {monitorButtons[1].value=0}.defer; if(synth.isPlaying, {synth.set(\gate, 0); synth=nil})}, 51, 3);

	//failSafe
	MIDIFunc.noteOn({ssNode.freeAll; {monitorButtons[2].value=1}.defer}, 50, 3);
	MIDIFunc.noteOff({{monitorButtons[2].value=0}.defer; loopSynth = Synth(\ssFB_looper, [\amp, amp, \bus, bus, \out, ~server2Out, \gate, 1], ssNode, 'addToTail').register}, 50, 3);


	synthCollect.do{|i, j|

		MIDIFunc.noteOn({arg ...args;
			numNoteOn = numNoteOn + 1;
			synthHistory = synthHistory.swap(0, 1).put(1, i);
			//randomize or hold params here
			if(paramHold==1, {nil}, {

				if(i==\ssFB_0, {synthVars[i] = Dictionary.newFrom([\freq, rrand(0.1, 1.0), \bwr, rrand(0, 7), \noiseFreq, rrand(0.01, 0.5)])});
				if(i==\ssFB_1, {synthVars[i] = Dictionary.newFrom([\freqs, {rrand(0.001, 0.4)}!3, \skew, rrand(-1.0, 1.0), \clip, rrand(0.7, 1.0), \sync, rrand(-7.0, 7.0), \mul, rrand(0.0, 2.0), \iminsweep,  rrand(4.0, 20.0), \integrator, {rrand(0.99, 1.0)}!3, \mult, rrand(-1.0, 1.0)])});
				if(i==\ssFB_2, {synthVars[i] = Dictionary.newFrom([\freqs, {rrand(100, 10000)}!8, \bwr, {rrand(0.01, 5)}!8, \integrator, {rrand(0.98, 1)}!8, \noiseMult, {rrand(-1.0, 1.0)}])});
				if(i==\ssFB_3, {synthVars[i] = Dictionary.newFrom([\freqs, rrand(0.0001, 15000)!8, \bwr, rrand(0.01, 5)!8, \integrator, rrand(0.98, 1)!8, \mult, {rrand(0.001, 0.4)}])});
				if(i==\ssFB_4, {synthVars[i] = Dictionary.newFrom([\dustFreq, {rrand(0.01, 1)}, \integrator, {rrand(0.01, 1.0)}!7, \mult, {rrand(1, 10000)}!7, \a0, {rrand(0.0, 1.0)}!7, \a1, {rrand(0.0, 1.0)}!7, \b1, {rrand(0.0, 1.0)}!7, \multAlt, {rrand(-1.0, 1.0)}!7])});
				if(i==\ssFB_5, {synthVars[i] = Dictionary.newFrom([\dustFreq, {rrand(0.01, 1)}, \integrator, {rrand(0.01, 1.0)}!7, \mult, rrand(1, 10000)])});
				if(i==\ssFB_6, {synthVars[i] = Dictionary.newFrom([\integrator, {rrand(0.1, 0.99)}!9, \mult, {rrand(-1.0, 1.0)}!9])});
				if(i==\ssFB_7, {synthVars[i] = Dictionary.newFrom([\multNeg, {rrand(-1.0, 1.0)}!3, \multAlt, rrand(10, 10000), \integrator, {rrand(0.2, 1.0)}!3, \dustFreq, rrand(0.01, 1.0), \freq, {rrand(0, 0.02)}!3, \clip, {rrand(0.0, 1.0)}!3, \skew, {rrand(-1.0, 1.0)}!3, \sync, {rrand(0.0, 2.0)}!3, \mul, {rrand(0.0, 100.0)}!3])});
				if(i==\ssFB_8, {synthVars[i] = Dictionary.newFrom([\freq, {rrand(0, 0.8)}!3, \clip, {rrand(0.0, 1.0)}!3, \skew, {rrand(-2.0, -2.0)}!3, \sync, {rrand(0.0, 2.0)}!3, \iminsweep, {rrand(0.0, 100.0)}!3, \mulSel, 2.rand, \integrator, {rrand(0.99, 1.0)}!3, \mult, {rrand(-10000.0, 10000.0)}!9, \sigLow, rrand(0.05, 0.5), \sigHigh, rrand(0.6, 2.0)])});
				if(i==\ssFB_9, {synthVars[i] = Dictionary.newFrom([\mult, {rrand(-1.0, 1.0)}!81, \vel, rrand(10.0, 10000.0), \integrator, {rrand(0.98, 1.0)}, \freq, {rrand(20000.0, 10000.0)}!9, \pulseFreq, {rrand(0.0, 0.1)}!9, \width, {rrand(0.0, 2pi)}!9, \mul, {rrand(0.0, 20.0)}, \add, {rrand(0.0, 20.0)}])});
				if(i==\ssFB_10, {synthVars[i] = Dictionary.newFrom([\freq, {rrand(1, 2000)}!3, \integrator, {rrand(0.1, 1.0)}!3, \mult, {rrand(-10000.0, 10000.0)}!9])});
				if(i==\ssFB_11, {synthVars[i] = Dictionary.newFrom([\freq, {0.01.exprand(10000.0)}!5, \rq, {rrand(0.01, 5)}!5, \integrator, {rrand(0.1, 1.0)}!5, \mult, {rrand(-100000.0, 100000.0)}!25])});
				if(i==\ssFB_12, {synthVars[i] = Dictionary.newFrom([\freq, {rrand(0, 1.0)}!5, \rq, {rrand(0.1, 1.0)}!5, \integrator, {rrand(0.1, 1.0)}!5, \mult, {rrand(-10000.0, 10000.0)}!25])});

			});

			if(synthHistory[1]==nil, {
				synth = Synth(i, [\amp, amp, \gate, 1, \bus, bus, \out, ~server2Out, \t_trig, 1]++synthVars[i].asPairs, ~server_2, 'addToHead').register;
			},
			{

				if((synthHistory[0]==synthHistory[1]),
					{
						if(synth.isPlaying,
							{synth.set(\t_trig, 1, \vel, args[0])},
							{synth = Synth(i, [\amp, amp, \gate, 1, \bus, bus, \out, ~server2Out, \t_trig, 1]++synthVars[i].asPairs, ssNode, 'addToHead').register})
					},
					{
						if(synth.isPlaying,
							{synth.set(\gate, 0); synth = Synth(i, [\amp, amp, \gate, 1, \bus, bus, \out, ~server2Out, \t_trig, 1]++synthVars[i].asPairs, ssNode, 'addToHead').register},
							{synth = Synth(i, [\amp, amp, \gate, 1, \bus, bus, \out, ~server2Out, \t_trig, 1]++synthVars[i].asPairs, ssNode, 'addToHead').register})
					}
				);
			}
			);


		}, j+36, 3);

		MIDIFunc.noteOff({arg ...args;
			numNoteOn = numNoteOn - 1;
			if(latch==0,
				{if(numNoteOn<1, {if(synth.isPlaying, {synth.set(\gate, 0)})})},
				{if(synth.isPlaying, {synth.set(\gate, 1, \t_trig, 0)})}
			);
		}, j+36, 3);
	};


	//LOOPER_________________________
	MIDIFunc.cc({arg ...args; var val;
		val = args[0].linlin(0, 127, 0, 1);
		if(synth.isPlaying, {synth.set(\loopRoute, val)});
		if(loopSynth.isPlaying, {loopSynth.set(\cut, val)});
	}, 2, 3);

	MIDIFunc.cc({arg ...args; var val;
		val = args[0];
		if(loopSynth.isPlaying, {loopSynth.set(\size, val)});
	}, 0, 3);

	MIDIFunc.cc({arg ...args; var val;
		val = args[0];
		if(loopSynth.isPlaying, {loopSynth.set(\rate, val)});
	}, 1, 3);

	//MONITOR_________________________
	(
		{
			var window;

			window = Window.new("NanoPad Monitor", Rect(0, 388, 400, 400)).front;
			GlobalSynths.guis.add(window);

			3.do{|i|
				monitorButtons.add(
					Button(window, Rect([0, 200, 200][i], [0, 0, 200][i], 200, 200))
					.font_(Font("Geeza Pro", 48))
					.states_([[["NO Hold", "NO Latch", "Reset"][i], Color.black, [Color.white, Color.white, Color.black][i]], [["Hold", "Latch", "Reset"][i], Color.black, [Color.red, Color.green, Color.white][i]]]);
				);
			};
		}.value;
	);
}.value;
);
