BusMixer {
	var <>synth, <>busCount=0, <>stereoBusCount=1, <>thisNodeGlobal, <>busNodeGlobal, dummyBus, dummyBusStereo, <>env=0;

	*new {^super.new.new}

	*makeSynth {arg busIn, busOut, thisNode, count;
		^super.new.makeSynth(busIn, busOut, thisNode, count)
	}

	*addBus {arg bus;
		^super.new.addBus(bus)
	}

	new{}

	makeSynth {
		arg busIn, busOut, thisNode, count;

		busCount=count;

		thisNodeGlobal = thisNode;
		busNodeGlobal = busIn;

		dummyBus = Bus.audio(Server.local, GlobalPresets.numChannels);
		busCount=busCount+1;

		synth = Synth(\busMixer, [
			\env, env,
			\bus0, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][busIn],
			\bus1, dummyBus,
			\bus2, dummyBus,
			\bus3, dummyBus,
			\bus4, dummyBus,
			\bus5, dummyBus,
			\bus6, dummyBus,
			\bus7, dummyBus,
			\busOut, busOut, \fbSel0, if(busIn>thisNode, {1}, {0})], GlobalNodes.nodes[thisNode]).register;

	}

	addBus {arg bus;
		if(synth.isPlaying, {
			synth.set(
				("bus"++busCount.wrap(0, 7)).asSymbol, GlobalBusses.allOut[GlobalPresets.numChannels.asSymbol][bus],
				("fbSel"++busCount.wrap(0, 7)).asSymbol, if(bus>thisNodeGlobal, {1}, {0}));

			busCount = busCount +1;

		});
	}

}
