FluidJSON {

	var <>json, <>dict, <>dataSet;

	*new {arg path; ^super.new.init(path)}
	*asDict {^super.new.asDict}
	*asDataSet {^super.new.asDataSet}

	init {arg path;
		json = File.readAllString(path).parseYAML;

		dict = Dictionary.with(*
			[
				\cols -> json["cols"].interpret,
				\data -> Dictionary.newFrom(
					json["data"].keys.collect{|key|
						[
							key.asSymbol,
							json["data"][key].collect{|item| item.interpret}
		]}.asArray.flatten)]);


	}

	asDict {
		^dict
	}

	asDataSet {
		dataSet = FluidDataSet.new(Server.local).load(dict);
		^dataSet
	}
}

