//Load Symbol file to array
Routine({
	(
		~808_india_labels = [];
		{var x;
			x = FileReader.read(GlobalPaths.data++"/808_india-matchLabels-1.txt", true, true, delimiter: $,);
			x[0].do{|i| ~808_india_labels = ~808_india_labels.add(i.asSymbol)};
		}.value;
	);

	s.sync;

	(
		~808_india_bounds = [];
		Routine({var x;
			x = FileReader.read(GlobalPaths.data++"/808_india-matchBounds-1.txt", true, true, delimiter: $,);
			x[0].do{|i| ~808_india_bounds = ~808_india_bounds.add(i.asInt)};
			s.sync;
			1.wait;
			~808_india_bounds = ~808_india_bounds.clump(2);
			"Done Loading Labels and Bounds".postln;
			s.sync;

		}).play;
	);

	s.sync;


	(
		~queryLabels = [];
		~queryBounds = [];
		(~querySlices.asSortedArray.size.asInt).do{|i|
			~queryLabels = ~queryLabels.add(("india2_rhythms-1.wav-"++(i+1)).asSymbol);
			~queryBounds = ~queryBounds.add(~querySlices[("india2_rhythms-1.wav-"++(i+1)).asSymbol]['bounds'])
		};
	);

	(
		~sourceLabels = [];
		~sourceBounds = [];
		(~sourceSlices.size*2).do{|i|
			if(i%2==0,
				{~sourceLabels = ~sourceLabels.add(~sourceLoader.index.asSortedArray.flatten(1)[i])},
				{~sourceBounds = ~sourceBounds.add(~sourceLoader.index.asSortedArray.flatten(1)[i]['bounds'])});
		};
	);

	s.sync;

	0.1.wait;
	~bufDataRout.next; //calls load_data_buffers.scd

}).play(SystemClock);