
//

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

NdefManager {
	var <>key;
	// var <>originalSource;//Source Control
	var <>ins, <>outs;//Routing
	var  <>mixers;
	/*
		InType:
			/replace -> replace mapped control
			/sum -> sum mapped controls
			/mean
	*/
	classvar <>all;
	*initClass { all = () }

	*postRoutes {|server|
		var string = "";

		Ndef.all[ server ? Server.default.asSymbol ].envir.pairsDo{|defkey, def|

			string = string + ("\n" ++ def.cs++":");

			if (def.ins.size > 0)
			{
				string = string ++ "\n\tIns:";
				def.ins.do {|key, inDef|
					string = string + "\n\t\t" 
						++ (def.cs 
							++ " << \\" 
							++ key.asString 
							++ "."
							++ (inDef.rate==\audio).if(\ar,\kr).asString 
							++ " << " 
							++ inDef.cs
						);
				};
			};

			if (def.outs.size > 0)
			{
				string = string ++ "\n\tOuts:";
					def.outs.do{|key, outDef|
					string = string 
						++ "\n\t\t" 
						++ (def.cs 
							++ " >> \\" 
							++ key.asString
							++ "."++ (outDef.rate==\audio).if(\ar,\kr).asString
							++ " >> "
							++ outDef.cs
						);
				};
			}
		};

		string.postln;
	}

	*new {|aKey|
		var ndefManager, key;
		key = aKey.asSymbol;
		ndefManager = all[key];

		if (ndefManager.isNil)
		{
			ndefManager = super.new.init(key);
			all[key] = ndefManager;
		}
		^all[key]
	}

	init { |aKey|
		this.key = aKey;
		//Routing
		ins = NDict.new;
		outs = NDict.new;
		mixers = ();
	}

	// Routing
	addIns {| ... aKeyDefNamePairs|
		ins.add(*aKeyDefNamePairs);
	}

	removeIns {| ... aKeys|
		aKeys.do{|aKey|
			if (ins[aKey].notNil)
			{
				var in = ins[aKey];
				in.outs.remove(aKey)
			};
			this.def.unmap(aKey.postln);
		};
		ins.remove(*aKeys);
	}

	addOuts {| ... aKeyDefNamePairs|
		outs.add(*aKeyDefNamePairs);
	}

	removeOuts {| ... aKeys|
		aKeys.do{|aKey|
			if (outs[aKey].notNil)
			{
				var out = outs[aKey];
				out.ins.remove(aKey);
				out.unmap(aKey)
			};
		};
		outs.remove(*aKeys);
	}

	removeAllIns {
		ins.removeAll;
	}

	removeAllOuts {
		outs.removeAll;
	}

	def {
		^Ndef(this.key)
	}

	//Input Mixers
	makeMixerIfNotAlready {|aKey|
		if (this.mixers[aKey].isNil)
		{
			(this.cs ++ " new Input mixer").postln;
			this.mixers[aKey] = NdefChannelMixer(this.key);
		}
	}

	clearMixerInput {|aKey, aInKey|
		this.mixers.at(aKey).clear(aInKey);
	}

	freeMixer {|aKey|
		this.mixers.at(aKey).clear;
	}

	freeMixers {
		this.mixers.do{|mixer|
			mixer.free;
		}
	}


}
/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */
//Ndef Extensions for compatibility

+  Ndef {

	mixer {|aIndex|
		this.manager.makeMixerIfNotAlready(aIndex);
		^this.manager.mixers.at(aIndex);
	}

	// Routing Management
	manager {
		^NdefManager(this.key)
	}
	ins { ^this.manager.ins }
	outs { ^this.manager.outs }

	removeOuts {| ... aKeys|
		this.manager.removeOuts(*aKeys);
	}
	
	removeAllOuts {| ... aKeys|
		this.manager.removeAllOuts;
	}
	
	removeIns{| ... aKeys|
		this.manager.removeIns(*aKeys);
	}

	removeAllIns{| ... aKeys|
		this.manager.removeAllIns;
	}

	/* Is there a way to do this without the redefinition */
	<<> {|proxy, key = \in|
		var ctl, rate, numChannels, canBeMapped;
		if(proxy.isNil) { ^this.unmap(key) };
		ctl = this.controlNames.detect { |x| x.name == key };
		rate = ctl.rate ?? {
			if(proxy.isNeutral) {
				if(this.isNeutral) { \audio } { this.rate }
			} {
				proxy.rate
			}
		};
		numChannels = ctl !? { ctl.defaultValue.asArray.size };
		canBeMapped = proxy.initBus(rate, numChannels); // warning: proxy should still have a fixed bus
		if(canBeMapped) {
			if(this.isNeutral) { this.defineBus(rate, numChannels) };
			this.xmap(key, proxy);
		} {
			"Could not link node proxies, no matching input found.".warn
		};

		/* manager In and Out */
		if (proxy.class == Ndef) 
		{
			this.manager.addIns( key, proxy.key );
			proxy.manager.addOuts( key, this.key );
		}

		^proxy // returns first argument for further chaining
	}


	//Multi
	<<+ {|proxy, key = \in|
		this.mixer(proxy.key).put(proxy.key, proxy);
		this.perform ('<<>', this.mixer(key).proxy, key);
	}

	+>> {|proxy, key = \in|
		proxy.perform ('<<+', this, key);
	}

	// <<- {|proxy, key = \in|

	// }

	// ->> {|proxy, key = \in|

	// }

}

