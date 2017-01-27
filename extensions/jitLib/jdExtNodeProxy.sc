+ NodeProxy {

	/* Routing */
	<< { | proxy, key = \in |
		proxy.perform('<>>', this, key);
		^this
	}

	>> { | proxy, key = \in |
		proxy.perform('<<>', this, key);
		^this
	}

	/* MultiIn Proxies */

	/* Scheduling */

	doOnQuant {| aClock, aQuant, func|
		var clock = aClock ? this.clock ? TempoClock.default;
		var quant = (aQuant ? this.quant).asQuant;
		
		clock.postln.schedAbs(quant.nextTimeOnGrid(clock).postln, {
			func.value(this);
			nil;
		})
	}

	resetSourceOnQuant {|aClock, aQuant|
		this.doOnQuant(aClock, aQuant, {
			this.source_(this.source);
			("resetting SOURCE of Ndef(\\" ++ (this.key.asString) ++ ")").postln;
		})
	}

	/*  */
	resetInputsOnQuant {|aClock, aQuant|
		this.doOnQuant(aClock, aQuant, {
			/*
				this.inputs.do{|input|
					input.resetSourceOnQuant(aClock, aQuant)
				}
			*/
			("resetting INPUTS of Ndef(\\" ++ (this.key.asString) ++ ")").postln;
		})
	}

}



/* Theoretical */
// Jdef /* name in progress : 3-4 letters */ : Ndef {
	
// 	var <>originalSource;

// 	/* esperimental use with TDuty */
// 	addSender {|addr|
// 		this.originalSource = this.source.copy;
// 		this.source_({
// 			var temp = this.source.value;
// 			SendReply.kr(temp, addr, temp)
// 			temp;
// 		})
// 	}

// }


/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */







