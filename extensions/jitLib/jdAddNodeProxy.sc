/* Group manipulation of Ndefs */
NGroup {
	var <>defs;

	*new {| ... aKeys|
		^super.new.init(aKeys)
	}
	
	init {|aKeys|
		this.defs = ();
		this.add(aKeys);
	}

	defNames {
		^this.defs.asArray.collect{ |def| def.key }
	}


	// Management
	add {|aDefNames|
		aDefNames.do{|key|
			this.defs[key] = Ndef(key);
		};
	}

	remove {|aDefNames|
		aDefNames.do{|key|
			this.defs.removeAt(key);
		};
	}

	removeAll {
		this.defNames.do{|key|
			this.remove(key)
		}
	}
	
	//Access

	at {| aDefNames|
		var list = List.new;
		aDefNames.do {|aKey|
			if (this.defNames[aKey.asSymbol].notNil)
			{
				list.add(defs[aKey])
			}
		}
	}

	do {|func|
		defs.do{|def|
			func.value(def);
		}
	}

	//Source Setting
	clear {| ... arglist|
		this.defs.do{|def|
			def.clear(*arglist)
		}
	}

	fadeTime_ {| ... arglist|
		this.defs.do{|def|
			def.fadeTime_(*arglist)
		}
	}

	//Initialisation
	ar {| ... arglist|
		this.defs.do{|def|
			def.ar(*arglist)
		}
	}

	kr {| ... arglist|
		this.defs.do{|def|
			def.kr(*arglist)
		}
	}

	// Play Controls
	play {| ... arglist|
		this.defs.do{|def|
			def.play(*arglist)
		}
	}

	stop {| ... arglist|
		this.defs.do{|def|
			def.stop(*arglist)
		}
	}

	//Timing
	clock {| ... arglist|
		this.defs.do{|def|
			def.clock(*arglist)
		}
	}

	quant {| ... arglist|
		this.defs.do{|def|
			def.quant(*arglist)
		}
	}

	printOn {|stream|

		var string = "";

		this.defNames.do {|name|
			string = string + ("\n\t Ndef('" ++ name.asString ++ "')") 
		};
		stream << this.class.asString << ":" << string 

	}

	
}


/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */


NdefG : NGroup {

	classvar <>all;
	var <>key;
	
	*initClass { all = () }

	*new {|groupKey ... aKeys|
		var result;
		groupKey = groupKey.asSymbol;
		result = this.all.at(groupKey);

		if (result.isNil)
		{
			result = super.new.init(groupKey, aKeys.postln);
			all[groupKey] = result;
		} 

		^this.all[groupKey]
	}

	init {|groupKey, aKeys|
		super.init(aKeys);
		// this.key = groupKey;
	}

}


/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

NDict : NGroup {

	add {| aKeyDefnamePairs|
		if (aKeyDefnamePairs.size.even)
		{
			aKeyDefnamePairs.pairsDo {|key, def|
				this.defs[key.asSymbol] = Ndef(def.asSymbol);
			}
		} {
			Error("Array Not Even").throw;
		}
	}

}
