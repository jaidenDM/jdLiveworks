/*
===========================================================================
===========================================================================
STRUCTURE:
<single controls>::<general> <note> <trig>
	<single-general>
	<single-note>[<single-noteOn>, <single-noteOff>]

	<group>[<single>]

	<matrix>[<group>]
===========================================================================
TO DO:]
	- Other types ? 

	matrix - choose rows, cols ? 
		store array of rows and array of columns?
			seems inefficient>
		single flat array and access
		rowsDo, colsDo -> (  rowSelection)

		use MIDIArray : contains methods for iterating and applying methods

	- normalize value options : default true

	

===========================================================================
*/
//
MIDIControl {

	var <>type, <>msgNum, <>chan, <>srcID, <>midifunc, <>ccFunc, arglist;
	var <>serverTreeFunc;
	
	*new {|type, msgNum, chan, srcID|
		^super.new.init(type,msgNum, chan, srcID).onReceive;
	}

	init {|type, msgNum, chan, srcID|
		this.type  = type; 
		this.msgNum  = msgNum; 
		this.chan  = chan; 
		this.srcID = srcID;

		this.persist_(true);
	}

	persist_ {|bool|
		if (bool)
		{ this.addToTree }
		{ this.removeFromTree}
	}

	addToTree {
		serverTreeFunc = ServerTreeFunc.put({ this.onReceive });
	}

	removeFromTree {
		serverTreeFunc.remove
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
		this.removeFromTree
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
/* GROUPS */
MIDIControlGroup {
	var <>type, <>msgNums, <>chan, <>srcID, <>group;
	var <>serverTreeFunc;

	*new {|type, msgNums, chan, srcID|
		^super.new.init(type, msgNums, chan, srcID);
	}

	init {
		group = msgNums.collect{|msgNum|
			MIDIControl(type, msgNum, chan, srcID)
		}
	}

	persist_{ |bool|
		group.do{|item, i|
			item.persist_(bool)
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

	var <>msgNums, <>chan, <>srcID, <>controls;

	*new {|msgNums, chan, srcID|
		^super.new.init(msgNums, chan, srcID);
	}

	init {|msgNums, chan, srcID|
		this.chan = chan;
		this.srcID = srcID;
		this.controls = msgNums.collect{|msgNum|
			MIDIControlNote(msgNum, chan, srcID)
		}
	}

	on_ {|func|
		controls.do{|item, n|
			item
			.args_(n)
			.on_(func)
		}
	}

	off_ {|func|
		controls.do{|item, n|
			item
			.args_(n)
			.off_(func)
		}
	}

	at {|index|
		controls[index]
	}
}
/* Groups of Groups */
MIDINoteMatrix {

	var <>msgNumRows, <>chan, <>srcID, <>controls, <>numRows, <>numCols;

	*new {|msgNumRows, chan, srcID|
		^super.new.init(msgNumRows, chan, srcID);
	}

	init {|msgNumRows, chan, srcID|
		this.chan = chan;
		this.srcID = srcID;
		this.msgNumRows = msgNumRows;
		
		this.numRows = msgNumRows.size;
		this.numCols = msgNumRows[0].size;

		this.controls = msgNumRows.collect{|msgNumRow|
			msgNumRow.collect{|msgNum| 
				MIDIControlNote(msgNum, chan, srcID)
			}
		}.flatten
	}

	persist_{ |bool|
		numRows.do{|x|
			controls[x * numRows].do{|control, y|
				var i = x * numRows + y;
				control.persist_(bool)
			}
		}
	}

	on_ {|func|
		numRows.do{|x|
			controls[x * numRows].do{|control, y|
				var i = x * numRows + y;
				control
					.args_(x, y, i)
					.on_(func)
			}
		}
	}

	off_ {|func|
		numRows.do{|x|
			controls[x * numRows].do{|control, y|
				var i = x * numRows + y;
				control
					.args_(x, y, i)
					.off_(func)
			}
		}
	}

	// rowsDo {}

	// colsDo {}

	at {|x = 0, y = 0|
		^controls[x][y]
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
	/* MAKE CONTROLS */
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

	addNoteMatrix {|key, msgNums2D|
		var group = MIDINoteMatrix(msgNums2D, this.chan, this.srcID);
		controls.put(key, group)
	}
	/* ACCESSING */
	at {| key |
		^controls[ key ]
	}

	/* CLEARING */

	clear {
		controls.clear;
	}
}
/*
-------------------------------------------------------------------------
-------------------------------------------------------------------------
*/
MIDIControlArray {

	var <>array;

	*newFrom {|newArray|
		var res = super.new;
		res.array = newArray;
		^res
	}

	cc_ {| ... arglist|
		array.do{|item|
			item.cc_(*arglist)
		}
	}

	tr_ {| ... arglist|
		array.do{|item|
			item.tr_(*arglist)
		}
	}

	on_ {| ... arglist|
		array.do{|item|
			item.on_(*arglist)
		}
	}

	off_ {| ... arglist|
		array.do{|item|
			item.off_(*arglist)
		}
	}	

	persist_{| ... arglist|
		array.do{|item|
			item.persist_(*arglist)
		}
	}
}




