ServerTreeDict {
	
	classvar <>dict;

	*initClass { dict = () }

	*put {|object, key, server|
		this.remove(key);
		dict.put(key, object);
		ServerTree.add(object, server);
	}

	*remove {|key, server|
		var old = dict.at(key);
		server = server ? \all;
		old !? { 
			ServerTree.objects.at(server).performAt(old, {|self, obj, i|
				self.postln;
				self.removeAt(i)
			})
		}
	}
}

ServerTreeFunc {

	var <>object, <>server;

	*put {|object, server|
		^super.new.putOnTree(object, server);
	}

	putOnTree {|object, server|
		if (this.object.notNil) { this.remove };
		this.object = (object ? this.object);
		this.server = (server ? this.server ? \all);
		ServerTree.add(this.object, this.server);
	}

	remove {
		ServerTree.objects.at(this.server).performAt(this.object, {|self, obj, i|
			self.postln;
			self.removeAt(i)
		})
	}
}