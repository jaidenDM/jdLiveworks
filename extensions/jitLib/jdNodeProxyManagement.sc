/* Ndef Manager 
	compliment to Ndef
*/
NLog {
	var <>key;
	// var <>originalSource;//Source Control
	var <>ins, <>outs;//Routing
	classvar <>all;

	*initClass { all = () }

	*new {|aKey|
		var nLog, key;
		key = aKey.asSymbol;
		nLog = all[key];

		if (nLog.isNil)
		{
			nLog = super.new.init(key);
			all[key] = nLog;
		}

		^all[key]
	}

	init { |aKey|
		this.key = aKey;
		/* should they be ordered?? */
		ins = NDict.new;
		outs = NDict.new;
	}

	addIns {| ... aKeyDefNamePairs|
		ins.add(*aKeyDefNamePairs);
	}

	removeIns {| ... aKeys|
		aKeys.do{|aKey|
			if (ins[aKey].notNil)
			{
				var in = ins[aKey];
				in.outs.remove(aKey)
			}
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
				out.ins.remove(aKey)
			}
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

}



+  Ndef {

	// Node Management

	log {
		^NLog(this.key)
	}
	ins { ^this.log.ins }
	outs { ^this.log.outs }

	removeOuts {| ... aKeys|
		this.log.removeOuts(*aKeys);
	}
	
	removeAllOuts {| ... aKeys|
		this.log.removeAllOuts;
	}
	
	removeIns{| ... aKeys|
		this.log.removeIns(*aKeys);
	}

	removeAllIns{| ... aKeys|
		this.log.removeAllIns;
	}

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

		/* log In and Out */
		this.log.addIns( key, proxy.key );
		proxy.log.addOuts( key, this.key );

		^proxy // returns first argument for further chaining

	}
}

