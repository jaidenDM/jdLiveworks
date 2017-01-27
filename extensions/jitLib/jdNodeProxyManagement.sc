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
		ins = NGroup.new;
		outs = NGroup.new;
	}

	addIns {| ... aKeys|
		ins.add(*aKeys);
	}
	removeIns {| ... aKeys|
		ins.remove(*aKeys);
	}

	addOuts {| ... aKeys|
		outs.add(*aKeys);
	}

	removeOuts {| ... aKeys|
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
	outs { ^this.log.ins }

	removeOuts {
		
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
		this.log.addIns( proxy.key );
		proxy.log.addOuts( this.key );

		^proxy // returns first argument for further chaining

	}
}

