
(
/*______________________________________________ 1. Server Configuration / Controller Init _________________________*/
s = Server.local; //Define server
s.options.blockSize = 64;           //server blocksize (as small as possible, mainly for latency and InFeedback.kr/ar)`
//s.options.hardwareBufferSize = 512;
s.newBusAllocators;                 //allocate busses on the server
s.latency = 0.05;                   //as little latency as possible between the language and the server
s.options.numOutputBusChannels = 16; //8 inputs and outputs
s.options.numInputBusChannels = 12;
s.options.memSize = 2.pow(21);
s.options.numAudioBusChannels = 100000;
s.options.maxNodes = 11000;
s.options.recHeaderFormat = "wav";
s.options.numWireBufs = 128 * 128;
GlobalData.new; //make data dictionarys
GlobalSynths.new(100); //make synth instance dictionary
GlobalPaths.new(PathName(thisProcess.nowExecutingPath).parentPath); //make Paths
GlobalPresets.new(GlobalPaths.presets, GlobalPaths.synthDefs, GlobalPaths.osc, 200, "192.168.0.131","192.168.0.159", "192.168.0.159", 0).gui;//make preset manager ''
Buffer.freeAll;

//server 2
(
~server_2 = Server.new(\server_2, NetAddr("127.0.0.1", 57130));
~server_2.options.numOutputBusChannels = 18;
~server_2.options.numInputBusChannels = 0;
~server_2.options.blockSize = 1;           //server blocksize (as small as possible, mainly for latency and InFeedback.kr/ar)
~server_2.latency = 0.05;                   //as little latency as possible between the language and the server
~server_2.options.memSize = 2.pow(21);
~server_2.options.recHeaderFormat = "wav";
~server_2.options.numWireBufs = 128 * 128;
~server2Out=16;
);

//
//Clean up server just in case something is running
ServerBoot.removeAll;
ServerTree.removeAll;
ServerQuit.removeAll;
//

/*______________________________________________ 2. Global Variables/Init Singeltons ______________________________________________*/
~oscWait = 0;   //a variable uses to wait for all buffers to load before executing osc files
~busAssign = 0; //a global variable that holds the synth/node/bus index of a synth that s being routed to another synth
~featuresAssign = 0;

/*______________________________________________ 3. Server. Cleanup ______________________________________________*/

//Cleanup server on command period/Server quit
~cleanup = {
	s.newBusAllocators;
	ServerBoot.removeAll;
	ServerTree.removeAll;
	ServerQuit.removeAll;
};


ServerQuit.add(~cleanup);

~cmdPeriodCleanup = {
	GlobalSynths.areRunning=false;
	if(InputManager.guiWindow!=nil, {if(InputManager.guiWindow.isClosed, {nil}, {InputManager.guiWindow.close})});
	GlobalPresets.recorders = Dictionary.new;
	InputManager.synths.do{|i| if(i.eq!=nil, {if(i.eq.guiWindow.isClosed, {nil}, {i.eq.guiWindow.close})})};
	GlobalSynths.guis.do{|i| if(i!=nil, {i.close})};

};

CmdPeriod.add(~cmdPeriodCleanup);


/*______________________________________________ 4. Executing on Server Boot ______________________________________________*/
s.waitForBoot({ //Wait to boot server once contained functions have been evaluated.
	var rout;

	~server_2.boot;

	GlobalBusses.new(GlobalPresets.outputSynthChan+8, GlobalPresets.outputSynthChan);

	ServerTree.add({GlobalNodes.new(GlobalPresets.outputSynthChan+8)});

	PathName(GlobalPaths.synthDefs).entries.do{|i|
		(i.entries).do{|j| thisProcess.interpreter.executeFile(j.fullPath)}
	};

	thisProcess.interpreter.executeFile(GlobalPaths.init++"make_buffers.scd"); //Make buffers via "make_buffers.scd" in thie init folder in this directory

	s.sync;
	s.freeAll;
	s.sync;
	s.meter;
	s.plotTree;
	"done".postln;
});
);