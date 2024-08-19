//Get Onsets and Offsets
(
~queryOnsets = [];
~queryOffsets = [];
~queryBounds.do{|i| ~queryOnsets = ~queryOnsets.add(i[0]); ~queryOffsets = ~queryOffsets.add(i[1])};
);

(
~queryBoundsBuf = Buffer.loadCollection(s, (~queryBounds.flatten/s.sampleRate));
~queryOnsetsBufSecs = Buffer.loadCollection(s, (~queryOnsets/s.sampleRate));
~queryOnsetsBuf = Buffer.loadCollection(s, ~queryOnsets);
~queryOffsetsBuf = Buffer.loadCollection(s, ~queryOffsets);
);

(
~sourceOnsets = [];
~sourceOffsets = [];
~nearest_bounds.do{|i| ~sourceOnsets = ~sourceOnsets.add(i[0]); ~sourceOffsets = ~sourceOffsets.add(i[1])};
);

(
~sourceBoundsBuf = Buffer.loadCollection(s, (~sourceBounds.flatten/s.sampleRate));
~sourceOnsetsBufSecs = Buffer.loadCollection(s, (~sourceOnsets/s.sampleRate));
~sourceOnsetsBuf = Buffer.loadCollection(s, ~sourceOnsets);
~sourceOffsetsBuf = Buffer.loadCollection(s, ~sourceOffsets);
);

(
~queryList =[0];
~queryBounds.do{|i| ~queryList = ~queryList.add(i[1]-i[0])};
~queryTestBuf = Buffer.loadCollection(s, ~queryList/s.sampleRate);
);

"Data Buffers Loaded".postln;