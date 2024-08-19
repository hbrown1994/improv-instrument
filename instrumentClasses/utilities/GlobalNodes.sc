GlobalNodes {/*-----> A Singleton class that manages the server nodes the entire software system*/
	classvar <>nodesArray;

	/*____________Constructors____________*/

	//Tell the class how many nodes you want it to populate "nodesArray" with
	*new {arg numNodes;^super.new.init(numNodes)}

	*nodes {^super.new.nodes}

	//Populate "nodesArray" with an arbitrary number of nodes in Sequential order
	  //This is helpful when routing audio/data around the system, so you
	  //know a synth's exact place in the server's node order. The synth's nodesArray index
	  //does not change even if the node's IDs within nodesArray changes.
	init {arg numNodes;
		nodesArray = Array.fill(numNodes, {Group.new(Server.local, \addToTail)});
	}

	nodes{^nodesArray} //return nodesArray when called
}


