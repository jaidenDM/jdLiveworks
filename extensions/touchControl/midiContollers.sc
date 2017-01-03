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

AbstractControl {

	classvar <>defaultPersist, <>defaultNormalize;

	var <>serverTreeFunc;
	var <>controlProxy, <>server, <>numChannels;

	*initClass { defaultNormalize = defaultPersist = true }

	*new {|type, msgNum, chan, srcID, numChannels|
		^super.new.init(type,msgNum, chan, srcID, numChannels).onReceive;
	}

	init {|type, msgNum, chan, srcID, numChannels|
		this.type  = type; 
		this.msgNum  = msgNum; 
		this.chan  = chan; 
		this.srcID = srcID;

		this.normalize_(true);
		this.persist_(true);

		this.numChannels = numChannels;
	}

	onReceive { ^this.subclassResponsibility(thisMethod) }

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

	resetProxySource {
		if (this.controlProxy.isNil) {
			// this.controlProxy = Bus.control(this.server ? Server.default, 1).set(0)
			this.controlProxy = 
				NodeProxy
				.control(this.server ? Server.default, (this.numChannels ? 1))
				.fadeTime_(0.05)
				.source_(0);
		}
	}

	setControlProxy {|val|
		this.controlProxy.source_(val)
	}

	kr {|spec|
		spec !? {
			spec = spec.asSpec.postln;
			^{ spec.map(this.controlProxy.kr) }
		};
		^this.controlProxy.kr;
	}
}

MIDIControl : AbstractControl {

	var <>type, <>msgNum, <>chan, <>srcID, <>midifunc, <>ccFunc, arglist;
	// var <>serverTreeFunc;
	var <>normalize;
	// var <>controlProxy, <>server;

	init {| ... arglist|
		super.init(*arglist);
		this.resetProxySource;/* unnecessary? */
	}

	onReceive {
		this.resetProxySource;
		midifunc !? { midifunc.free};
		midifunc = MIDIFunc({|val, num, chan, src|
			if (this.normalize) {
				val = val.linlin(0,127,0,1);
			};
			this.setControlProxy(val);

			ccFunc.value(val, num, *this.args)
		}, msgNum, chan, type, srcID);
	}

	cc_ {|func ... arglist|
		ccFunc = func;
		this.args_(*arglist);/*not tested*/
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
	var <>controls;

	init {
		super.init;
		this.controls = 
		( 
			\on : MIDIControl(\noteOn, msgNum, chan, srcID, numChannels),
			\off : MIDIControl(\noteOff, msgNum, chan, srcID, numChannels)
		);
	}

	on_ {|funcs ... arglist|
		controls[\on]
		.args_(*this.args)
		.cc_(func);
	}

	off_ {|func ... arglist |
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
/* Do abstract control for groups */
/* GROUPS */

AbstractMIDIControlGroup : AbstractControlGroup {

	var <>controls;

	send_ {| ... arglist|
		this.doAll({|control| control.send_(*arglist)})
	}

	cc_ {| ... arglist|
		this.doAll({|control| control.cc_(*arglist)})
	}

	tr_ {| ... arglist|
		this.controls{|control| control.tr_(*arglist) }
	}

	free {| ... arglist|
		this.doAll({|control| control.free_(*arglist)})
	}

	clear {
		this.free;
	}	
}

MIDIControlGroup : AbstractControl {
	/*var <>type, <>msgNums, <>chan, <>srcID,*/ 
/*
	*new {|type, msgNums, chan, srcID|
		^super.new.init(type, msgNums, chan, srcID);
	}

	init {|type, msgNums, chan, srcID|
		*/
	init {|type, msgNums, chan, srcID, numChannels|
		super.init(type, msgNums, chan, srcID, numChannels);
		controls = msgNums.collect{|msgNum|
			MIDIControl(type, msgNum, chan, srcID, numChannels)
		}
	}

	doAll {|func|
		this.controls.do{|control|
			func.value(control)
		} 
	}

	// persist_{ |bool|
	// 	controls.do{|item, i|
	// 		item.persist_(bool)
	// 	}
	// }

	// cc_{|func|
	// 	controls.do{|item, n|
	// 		item
	// 		.args_(n)
	// 		.cc_(func)
	// 	}
	// }

	// clear {
	// 	controls.do{|item|
	// 		item.clear
	// 	}
	// }

	msgNums { ^msgNum }
}



MIDINoteGroup {

	/*var <>msgNums, <>chan, <>srcID,<>controls;*/ 

	// *new {|type, msgNums, chan, srcID, numChannels|
		// ^super.new.init(type, msgNums, chan, srcID, numChannels);
	// }

	init {|type, msgNums, chan, srcID, numChannels|
		super.init(type, msgNums, chan, srcID, numChannels);
		this.controls = msgNums.collect{|msgNum|
			MIDIControlNote(type, msgNums, chan, srcID, numChannels)
		}
	}

	on_ {| ... arglist|
		this.doAll({|control| control.on_(*arglist)})
	}

	off_ {| ... arglist|
		this.doAll({|control| control.off_(*arglist)})
	}

	at {|index|
		controls[index]
	}
}
/* Groups of Groups */
MIDINoteMatrix {

	/*var <>msgNumRows, <>chan, <>srcID, <>controls,*/ <>numRows, <>numCols;

	/**new {|msgNumRows, chan, srcID|
		^super.new.init(msgNumRows, chan, srcID);
	}*/

	init {|type, msgNumRows, chan, srcID, numChannels|
		super.init(type, msgNumRows, chan, srcID, numChannels);
		this.numRows = msgNumRows.size;
		this.numCols = msgNumRows[0].size;

		this.controls = msgNumRows.collect{|msgNumRow|
			msgNumRow.collect{|msgNum| 
				MIDIControlNote(msgNum, chan, srcID)
			}
		}.flatten
	}

	doAll {|func|
		numRows.do{|x|
			controls[x * numRows].do{|control, y|
				var i = x * numRows + y;
				func.value(control.args_(x, y, i))
			}
		}

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

	row {|i|
		controls.reshape(numRows, numCols)[i]
	}

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






