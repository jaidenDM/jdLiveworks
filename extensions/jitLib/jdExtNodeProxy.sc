+ Ndef {
	/* Scheduling */
	doOn {| aClock, aQuant, func|
		var clock = aClock ? this.clock ? TempoClock.default;
		var quant = (aQuant ? this.quant).asQuant;
		
		clock.postln.schedAbs(quant.nextTimeOnGrid(clock).postln, {
			func.value(this);
			nil;
		})
	}

	quantizeSource {|aClock, aQuant|
		this.doOn(aClock, aQuant, {
			this.send;
			("resetting SOURCE of Ndef(\\" ++ (this.key.asString) ++ ")").postln;
		})
	}

	/*  */
	quantizeInputs {|aClock, aQuant|
		this.doOn(aClock, aQuant, {
			
			this.ins.ndefs.keysValuesDo{|key,input| 
				input.postln;
				input.send;
			};
			("resetting INPUTS of Ndef(\\" ++ (this.key.asString) ++ ")").postln;
		})
	}

	quantizeOutputs {|aClock, aQuant|
		this.doOn(aClock, aQuant, {
			this.ins.ndefs.keysValuesDo{|key, output| 
				output.send;
			};
			("resetting OUTPUTS of Ndef(\\" ++ (this.key.asString) ++ ")").postln;
		})
	}
}
/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */







