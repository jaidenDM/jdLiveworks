MIDIControl {

	var <>msgNum, <>chan, <>srcID, <>msgType, <>midifunc;
	
	*new {
		^super.new.init();
	}

	init {

	}

	onReceive {
		midifunc !? { midifunc.free};
		midifunc = MIDIFunc({|val, num, chan, src|

		});
	}

}


/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */
MIDIController {
	var <>controls;

	*new {

	}

	init {
		
	}
}