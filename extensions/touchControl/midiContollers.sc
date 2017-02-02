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

	- normalise value options : default true

	for groups : 
		fromArray ()
		new (newConsecutive)

===========================================================================
*/

AbstractControl {

	var <>serverTreeFunc;
	var <>proxy, <>server, <>numChannels;

	onReceive { }

	init {
		this.persist_(true);
		this.resetProxySource;
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

	resetProxySource {
		if (this.proxy.isNil) {
			// this.proxy = Bus.control(this.server ? Server.default, 1).set(0)
			this.proxy = 
				NodeProxy
				.control(this.server ? Server.default, (this.numChannels ? 1))
				.fadeTime_(0.05)
				.source_(0);
		};

		if (this.proxy.isNeutral) {
			this.proxy
				.fadeTime_(0.05)
				.source_(0);
		}
	}

	setControlProxy {|val|
		this.proxy.source_(val)
	}

	kr {|spec|
		spec !? {
			spec = spec.asSpec.postln;
			^spec.map(this.proxy.kr)
		};
		^this.proxy.kr;
	}

	asNodeProxy { ^this.proxy }
}

/* MIDI */
AbstractMIDIControl : AbstractControl{

	/*classvar <>defaultPersist, <>defaultNormalize;

	*initClass { defaultNormalize = defaultPersist = true }*/
	var <>type, <>msgNum, <>chan, <>srcID, <>numChannels;
	var <>normalise;

	*new {|type, msgNum, chan, srcID, numChannels|
		^super.new.init(type,msgNum, chan, srcID, numChannels).onReceive;
	}

	init {|type, msgNum, chan, srcID, numChannels|
		super.init;
		this.type  = type; 
		this.msgNum  = msgNum; 
		this.chan  = chan; 
		this.srcID = srcID;

		this.normalise_(true);

		this.numChannels = numChannels;/* Probably unnecessary for MIDI - can this be > 1 ? */
	}
}

MIDIControl : AbstractMIDIControl {

	var <>midifunc, <>respondFunc, arglist;

	init {| ... arglist|
		super.init(*arglist);
		this.resetProxySource;
	}

	onReceive {
		this.resetProxySource;
		midifunc !? { midifunc.free};
		midifunc = MIDIFunc({|val, num, chan, src|
			if (this.normalise) {
				val = val.linlin(0,127,0,1);
			};

			this.setControlProxy(val);

			respondFunc.value(val, num, *this.args)
		}, msgNum, chan, type, srcID);
	}

	cc_ {|func ... arglist|
		respondFunc = func;
		this.args_(*arglist);
		this.onReceive;
	}

	tr_ {|onFunc, offFunc|
		this.cc_{|val,num|
			val.postln;
			if (val > 0)
			{ onFunc.value(val,num)}
			{ (offFunc ? {} ).value(val,num)}
		}
	}

	args_ {| ... aArglist| arglist = aArglist }

	args { ^arglist }

	clear {
		midifunc.free;
		this.removeFromTree
	}
}

MIDIControlTrig : MIDIControl {  }

MIDIControlNote : MIDIControl {
	var <>controls;

	*new {|msgNum, chan, srcID, numChannels|
		^super.new.init(msgNum, chan, srcID, numChannels).onReceive;
	}

	init {|msgNum, chan, srcID, numChannels|
		super.init(msgNum, chan, srcID, numChannels);
		this.controls = 
		( 
			\on : MIDIControl(\noteOn, msgNum, chan, srcID, numChannels),
			\off : MIDIControl(\noteOff, msgNum, chan, srcID, numChannels)
		);
	}

	on_ {|func ... arglist| controls[\on].cc_(func,*arglist) }

	off_ {|func ... arglist | controls[\off].cc_(func, *arglist) }

	clear { controls.keys{|control| control.clear }
	}
}

/* GROUPS */
AbstractMIDIControlGroup : AbstractControl {

	var <>controls;

	doAll { ^this.subclassResponsibility(thisMethod) }

	persist_ {|func|
		this.doAll({|control ... arglist| control.persist_(func, *arglist)})
	}

	normalise_ {|func|
		this.doAll({|control ... arglist| control.normalise_(func, *arglist)})
	}

	send_ {|func|
		this.doAll({|control ... arglist| control.send_(func, *arglist)})
	}

	cc_ {|func|
		this.doAll({|control ... arglist| control.cc_(func, *arglist)})
	}

	tr_ {|func|
		this.controls{|control ... arglist| control.tr_(func, *arglist) }
	}

	free {|func|
		this.doAll({|control ... arglist| control.free(func, *arglist)})
	}

	clear {
		this.free;
	}	

	at {|index|
		^this.controls.at(index)
	}
}

MIDIControlGroup : AbstractMIDIControlGroup {

	var <>numControls;

	*new {|type, msgNums, chan, srcID, numChannels|
		^super.new.init(type, msgNums, chan, srcID, numChannels);
	}

	init {|type, msgNums, chan, srcID, numChannels|
		super.init;

		this.numControls = msgNums.size;
		this.controls = msgNums.collect{|msgNum|
			MIDIControl(type, msgNum, chan, srcID, numChannels)
		}
	}

	doAll {|func|
		this.controls.do{|control, i|
			func.value(control, i)
		} 
	}

}

MIDINoteGroup : MIDIControlGroup {

	init {|msgNums, chan, srcID, numChannels|
		super.init(msgNums, chan, srcID, numChannels);
		this.controls = msgNums.collect{|msgNum|
			MIDIControlNote(msgNum, chan, srcID, numChannels)
		}
	}

	on_ {|func| this.doAll({|control ... arglist| control.on_(func, *arglist)}) }

	off_ {|func| this.doAll({|control ... arglist| control.off_(func, *arglist)}) }

	at {|index|
		this.controls[index]
	}
}

MIDINoteMatrix : MIDINoteGroup {

	var <>numRows, <>numCols;

	init {|msgNumRows, chan, srcID, numChannels|
		super.init;
		this.numRows = msgNumRows.size;
		this.numCols = msgNumRows[0].size;

		this.controls = 
		msgNumRows.collect{|msgNumRow|
			msgNumRow.collect{|msgNum| 
				MIDIControlNote(msgNum, chan, srcID, numChannels)
			}
		}.flatten
	}

	doAll {|func|
		numRows.do{|x|
			this.numCols.do{|y|
				var i = x * numRows + y;
				func.value(this.controls[i], x, y, i)
			}
		}
	}
	
	/* ACCESS */
	row {|i|
		^MIDIControlArray.newFrom(this.controls.reshape(numRows, numCols)[i])
	}

	col {|i|
		^this.numRows.collect{|j|
			this.controls[j][i]
		}
	}

	at {|x = 0, y = 0|
		^this.controls.reshape(numRows, numCols)[x][y]
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
	addControl{|key, type, msgNum, numChannels = 1|
		var control = MIDIControl(type, msgNum, this.chan, this.srcID, numChannels);
		controls.put(key, control)
	}

	addControlNote{|key, msgNum, numChannels = 1|
		var control = MIDIControlNote (msgNum, this.chan, this.srcID, numChannels);
		controls.put(key, control)
	}

	addGroup {|key, type, msgNums, numChannels = 1|
		var group = MIDIControlGroup(type, msgNums, this.chan, this.srcID, numChannels);
		controls.put(key, group)
	}

	addNoteGroup {|key, msgNums, numChannels = 1|
		var group = MIDINoteGroup(msgNums, this.chan, this.srcID, numChannels);
		controls.put(key, group)
	}

	addNoteMatrix {|key, msgNums2D, numChannels = 1|
		var group = MIDINoteMatrix(msgNums2D, this.chan, this.srcID, numChannels);
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
		^super.new.array_(newArray)
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

	normalise_{| ... arglist|
		array.do{|item|
			item.normalise_(*arglist)
		}
	}

	at {|i|
		^array.at(i)
	}

}






