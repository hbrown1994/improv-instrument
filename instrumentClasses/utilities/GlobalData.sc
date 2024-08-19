GlobalData {/*-----> A Singleton class that manages the audio buffers, audio feature data, and other forms of data throughout the entire software system*/
	classvar <>audioBuffersDict, <>audioBufferNamesDict, <>featuresBuffersDict, <>offsetBuffersDict, <>labelsDict, <>slicesDict, <>boundsDict, <>internetDict, <>controlsDict, <>nnMFCC_datasetsDict, <>nnPlayback_datasetsDict, <>nnTree_datasetsDict, <>analysis2D_datasetsDict, <>playback2D_datasetsDict, <>tree2D_datasetsDict;

	/*____________Constructors____________*/
	*new {^super.new.init}
    *audioBuffers {^super.new.audioBuffers}
	*audioBufferNames {^super.new.audioBufferNames}
	*featuresBuffers {^super.new.featuresBuffers}
	*labels {^super.new.labels}
	*slices {^super.new.slices}
	*bounds {^super.new.bounds}
	*internet {^super.new.bounds}
	*controls {^super.new.controls}
	*nnMFCC_datasets {^super.nnMFCC_datasets}
	*nnPlayback_datasets {^super.nnPlayback_datasets}
	*nnTree_datasets {^super.nnTree_datasets}
	*analysis2D_datasets {^super.analysis2D_datasets}
	*playback2D_datasets {^super.playback2D_datasets}
	*tree2D_datasets {^super.tree2D_datasets}

	//Make dictionarys for storing all the data. These arrays are populated outisde of this class
	//and within other functions throughout the system via the below methods
	init {
		audioBuffersDict = Dictionary.new;
		audioBufferNamesDict = Dictionary.new;
		featuresBuffersDict = Dictionary.new;
		labelsDict = Dictionary.new;
		slicesDict = Dictionary.new;
		boundsDict = Dictionary.new;
		internetDict = Dictionary.new;
		controlsDict = Dictionary.new;
		nnMFCC_datasetsDict = Dictionary.new;
		nnPlayback_datasetsDict = Dictionary.new;
		nnTree_datasetsDict = Dictionary.new;

		analysis2D_datasetsDict = Dictionary.new;
		playback2D_datasetsDict = Dictionary.new;
		tree2D_datasetsDict = Dictionary.new;
	}

	/*____________Methods____________*/
	nnMFCC_datasets {^nnMFCC_datasetsDict}
	nnPlayback_datasets {^nnPlayback_datasetsDict}
	nnTree_datasets {^nnTree_datasetsDict}

	analysis2D_datasets {^analysis2D_datasetsDict}
	playback2D_datasets {^playback2D_datasetsDict}
	tree2D_datasets {^tree2D_datasetsDict}

	audioBuffers{^audioBuffersDict}
	audioBufferNames{^audioBufferNamesDict}
	featuresBuffers{^featuresBuffersDict}
	labels{^labelsDict}
	slices{^slicesDict}
	bounds{^boundsDict}
	internet{^internetDict}
	controls{^controlsDict}
}

