/* Group manipulation of Ndefs */
NdefG {
	classvar <>all;
	var <>defs, <>key;
	
	*initClass { all = () }

	*new {|groupKey ... aKeys|
		var result;
		groupKey = groupKey.asSymbol;
		result = all.at(groupKey);

		if (result.isNil)
		{
			result = super.new.init(groupKey, aKeys);
			all[groupKey] = result;
		} 

		^all[groupKey]

	}

	init {|groupKey, aKeys|
		this.key = groupKey;
		this.defs = aKeys.collect{|key|
			Ndef(key);
		};
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

}

