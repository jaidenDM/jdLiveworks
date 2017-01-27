/* Ndef Manager 
	runs parallel to node proxy
*/
NLog {
	var <>ins, outs;//Routing
	var <>all

	*classInit { all = () }

	*new {|aKey|
		var nLog, key;
		key = key.asSymbol;
		nLog = all[key];

		if (nLog.isNil)
		{
			nLog = super.new.init(aKey);
			all[key] = nLog;
		}

		^all[aKey]
	}

	init { |aKey|
		this.key = aKey;
		/* should they be ordered */
		ins = ();
		outs = ();
	}

}


+  Ndef {

	// Node Management
	enableLog {
		NLog(this.key)
	}

	log {
		^NLog(this.key)
	}

	<<> {|proxy, key = \in|
		this.log.addIn();
		super.perform('<<>', proxy, key);
	}
}

