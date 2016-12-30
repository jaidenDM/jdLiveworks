MIDIControl {

	var <>type, <>msgNum, <>chan, <>srcID, <>midifunc, <>ccFunc, arglist;
	
	*new {|type, msgNum, chan, srcID|
		^super.new.init(type,msgNum, chan, srcID).onReceive;
	}

	init {|type, msgNum, chan, srcID|
		this.type  = type; 
		this.msgNum  = msgNum; 
		this.chan  = chan; 
		this.srcID = srcID;
	}

	onReceive {
		midifunc !? { midifunc.free};
		midifunc = MIDIFunc({|val, num, chan, src|
			ccFunc.value(val, num, *this.args)
		}, msgNum, chan, type, srcID);
	}

	cc_ {|func|
		ccFunc = func;
		this.onReceive;
	}

	args_ {| ... arglist_ |
		arglist = arglist_ ;
	}

	args { ^arglist }

	clear {
		midifunc.free;
	}
}

MIDIControlTrig : MIDIControl {

	tr_ {|onFunc, offFunc|
		this.cc_{|val,num|
			if (val =< 0)
			{ onFunc.value(val,num)}
			{ offFunc.value(val,num)}
		}
	}
}

MIDIControlNote : MIDIControl {

	var <>msgNum, <>chan, <>srcID, <>controls;
	
	*new {|msgNum, chan, srcID|
		^super.new.init(msgNum, chan, srcID);
	}

	init {|msgNum, chan, srcID|
		this.msgNum  = msgNum; 
		this.chan  = chan; 
		this.srcID = srcID;

		this.controls = 
		( 
			\on : MIDIControl(\noteOn, msgNum, chan, srcID),
			\off : MIDIControl(\noteOff, msgNum, chan, srcID)
		);
	}

	on_ {|func|
		controls[\on]
		.args_(*this.args)
		.cc_(func);
	}

	off_ {|func|
		controls[\off]
		.args_(*this.args)
		.cc_(func);
	}

	clear {
		controls.keys{|control|
			control.clear
		}
	}
}

MIDIControlGroup {
	var <>type, <>msgNums, <>chan, <>srcID, <>group;

	*new {|type, msgNums, chan, srcID|
		^super.new.init(type, msgNums, chan, srcID);
	}

	init {
		group = msgNums.collect{|msgNum|
			MIDIControl(type, msgNum, chan, srcID)
		}
	}

	cc_{|func|
		group.do{|item, n|
			item
			.args_(n)
			.cc_(func)
		}
	}

	clear {
		group.do{|item|
			item.clear
		}
	}
}

MIDINoteGroup {

	var <>msgNums, <>chan, <>srcID, <>group;

	*new {|msgNums, chan, srcID|
		^super.new.init(msgNums, chan, srcID);
	}

	init {|msgNums, chan, srcID|
		this.chan = chan;
		this.srcID = srcID;

		this.group = msgNums.collect{|msgNum|
			MIDIControlNote(msgNum, chan, srcID)
		}
	}

	on_ {|func|
		group.do{|item, n|
			item
			.args_(n)
			.on_(func)
		}
	}

	off_ {|func|
		group.do{|item, n|
			item
			.args_(n)
			.off_(func)
		}
	}
}

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */
MIDIController {
	var <>name, <>port, <>controls, <>chan, <>srcID;

	*new {|name, port|
		^super.new.init(name, port)
	}

	init {|name, port|
		
		this.name = name.asString;
		this.port = port.asString;

		if (MIDIClient.initialized.not){
			MIDIClient.init;
		};

		MIDIClient.sources.do{|source,n|
			if(
				source.device == name and:
				source.name == port  )
			{
				name ++ " connected".postln;
				MIDIIn.connect(n, source);
				this.srcID = source.uid;
			};
		};	

		controls = ();
	}

	addControl{|key, type, msgNum|
		var control = MIDIControl(type, msgNum, this.chan, this.srcID);
		controls.put(key, control)
	}

	addControlNote{|key, msgNum|
		var control = MIDIControlNote (msgNum, this.chan, this.srcID);
		controls.put(key, control)
	}

	addGroup {|key, type, msgNums|
		var group = MIDIControlGroup(type, msgNums, this.chan, this.srcID);
		controls.put(key, group)
	}

	addNoteGroup {|key, msgNums|
		var group = MIDINoteGroup(msgNums, this.chan, this.srcID);
		controls.put(key, group)
	}

	at {|i|
		^controls[i]
	}

}






