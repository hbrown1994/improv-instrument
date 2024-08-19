GlobalPaths {/*-----> A Singleton class that manages the paths to disk for the entire software system*/
	classvar <>data_path, <>datasets_path, <>osc_path, <>buffers_path, <>synth_defs_path, <>init_path, <>presets_path, <>corpusLoaders_path, <>recordings_path, <>mainDir, <>nnMFCC_datasets_path, <>nnPlayback_datasets_path, <>nnTree_datasets_path, <>analysis2D_datasets_path, <>playback2D_datasets_path, <>tree2D_datasets_path;

	/*____________Constructors____________*/
	*new {arg root; ^super.new.create(root)} /*Define parent path*/
	*data {^super.new.data}
	*datasets {^super.new.datasets}
	*osc {^super.new.osc}
	*buffers {^super.new.buffers}
	*synthDefs {^super.new.synthDefs}
	*init {^super.new.init}
	*presets {^super.new.presets}
	*corpusLoaders {^super.new.corpusLoaders}
	*recordings {^super.new.recordings}
	*updateRecDir {^super.new.updateRecDir}
	*nnMFCC_datasets {^super.new.nnMFCC_datasets}
	*nnPlayback_datasets {^super.new.nnPlayback_datasets}
	*nnTree_datasets {^super.new.nnTree_datasets}
	*analysis2D_datasets {^super.new.analysis2D_datasets }
	*playback2D_datasets {^super.new.playback2D_datasets}
	*tree2D_datasets {^super.new.tree2D_datasets}

	//Make strings that point to various folders throughout the system' root directory
	create {arg root;
		mainDir = root;
		data_path = root++"data/";
		osc_path = root++"osc/";
		buffers_path = root++"buffers/";
		synth_defs_path = root++"synth_defs/";
		init_path = root++"init/";
		presets_path = root++"presets/";
		corpusLoaders_path = root++"corpusLoaders/";
		recordings_path = root++"recordings/"++Date.getDate.stamp;
		datasets_path =  root++"datasets/";
		nnMFCC_datasets_path =  root++"nnMFCC_datasets/mfcc_datasets/";
		nnPlayback_datasets_path =  root++"nnMFCC_datasets/playback_datasets/";
		nnTree_datasets_path =  root++"nnMFCC_datasets/tree_datatsets/";

		analysis2D_datasets_path =  root++"2Dcorpus_datasets/analysis_datasets/";
		playback2D_datasets_path =  root++"2Dcorpus_datasets/playback_datasets/";
		tree2D_datasets_path =  root++"2Dcorpus_datasets/tree_datatsets/";
	}

	/*____________Methods____________*/

	//Return strings
	nnMFCC_datasets{^nnMFCC_datasets_path}
	nnPlayback_datasets{^nnPlayback_datasets_path}
	nnTree_datasets{^nnTree_datasets_path}
	analysis2D_datasets {^analysis2D_datasets_path}
	playback2D_datasets {^playback2D_datasets_path}
	tree2D_datasets {^tree2D_datasets_path}
	data{^data_path}
	datasets{^datasets_path}
	osc{^osc_path}
	buffers{^buffers_path}
	synthDefs{^synth_defs_path}
	init{^init_path}
	presets{^presets_path}
	corpusLoaders{^corpusLoaders_path}
	recordings{^recordings_path}
	updateRecDir{//made a new directory for recordings being written to disk
		recordings_path = mainDir++"recordings/"++Date.getDate.stamp;
		File.mkdir(mainDir++"recordings/"++Date.getDate.stamp;);
	}
}


