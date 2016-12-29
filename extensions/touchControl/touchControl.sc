/*
	TO DO

	Re-Structuring Too Soon? 

	MultiPush: better 2D access

*/
AbstractOSCTouchControl {
	var  <>viewName, <>name, <>recvAddr, <>oscfunc, <>sender, <>label;

	/* TalkBack Messages */
	send {|val|
		sender !? { sender.sendMsg(recvAddr, (val ? 0)) } 
	}

	sendLabelMsg {|msg|
		sender !? { sender.sendMsg(recvAddr ++ '/label', msg ? '') } 
	}

	/* CLEAN UP */
	zero { sender.sendMsg(recvAddr, 0 )}

	free { oscfunc.free }

	clearLabel { sender.sendMsg(recvAddr ++ '/label', '')	}

	clear {
		oscfunc.free;
		this.clearLabel;
		this.zero;
	}
}
/* Fader/Knob*/
OSCTouchControl1D : AbstractOSCTouchControl{
	
	var <>id, <>ccFunc;

	*new {|viewName, name, id, sender|
		^super.new.init(viewName, name, id, sender).onReceive;
	}

	init {|viewName, name, id, sender|

		this.viewName = viewName; 
		this.name = name; 
		this.id = id; 
		this.sender = sender;

		this.recvAddr = viewName.asSymbol ++ name.asSymbol;	
	}
	/* Control Function */
	onReceive {

		oscfunc !? { oscfunc.free };
		oscfunc = OSCFunc({|v,n,c,s|
			ccFunc.value(v[1], id);
			/* TalkBack */
			this.sendLabelMsg(label)
		}, recvAddr);
	}

	cc_ {|func, label|
		this.ccFunc = func;
		this.label = label ? '';
		this.onReceive;
	}
}

/* xy-pad */
OSCTouchControl2D : OSCTouchControl1D {

	onReceive {
		recvAddr = viewName.asSymbol ++ name.asSymbol;
		oscfunc !? { oscfunc.free };
		oscfunc = OSCFunc({|v,n,c,s|
			ccFunc.value(v[1], v[2]);
			this.sendLabelMsg(label)
		}, recvAddr);
	}
}
/* push, toggle */
OSCTouchControl0D : OSCTouchControl1D {

	var <>row, <>col,
		<>onFunc,<>offFunc, <>offLabel;

	*new {|viewName, name, row, col, sender|
		^super.new.init(viewName, name, row, col, sender).onReceive;
	}

	init {|viewName, name, row, col, sender|

		this.viewName = viewName; 
		this.name = name; 
		this.row = row; 
		this.col = col; 
		this.sender = sender;	
	}
	/* Control Function */
	onReceive {
		recvAddr = viewName ++ name;
		oscfunc !? { oscfunc.free };
		oscfunc = OSCFunc({|v,n,c,s|
			/* as Continuous Control */
			this.ccFunc.value(v[1], row, col);
			this.sendLabelMsg(label);
			/* As Trigger */
			if(v[1] == 0)
			{
				this.offFunc.value(v[1], row, col);
				this.sendLabelMsg(label);
			} {
				this.onFunc.value(v[1], row, col);
				this.sendLabelMsg(offLabel ? label);
			};
		}, recvAddr);
	}


	on_ {|func, label|
		this.onFunc = func;
		this.label = label;
		this.onReceive;
	}

	off_ {|func, label|
		this.offFunc = func;
		this.onReceive;
	}
}
/* 

	Worth Doing? Adaptable to MIDI?
	AbstractOSCTouchControl : AbstractTouchControl {

	var <>viewName, <>name, <>sender, <>controls;	

	doControls{|func|
		controls.do{|control|
			func.value(control)
		}
	}

	clear {|val|
		this.doControls{|control| control.clear }
	}	

	send {|val|
		this.doControls{|control| control.send(val) }
	}	
	
	zero {
		send {|val|
		this.doControls{|control| control.zero }
	}	

	at {|n|
		^controls[n]
	}
} 
*/
/* Multi Fader/Knob */
OSCMultiTouchControl1D {

	var <>viewName, <>name, <>num, <>sender, <>controls;

	*new {|viewName, name, num, sender|
		^super.newCopyArgs(viewName, name, num, sender).onReceive;
	}

	onReceive {
		controls = (1..num ).collect{|id|
			var numberedName = name ++ '/' ++ (id).asSymbol;
			OSCTouchControl1D(viewName, numberedName, id - 1, sender);
		}
	}

	cc_ {|func, label|
		this.controls.do{|control|
			control.ccFunc = func;
			control.label = label ? '';
			control.onReceive;
		}
	}

	
	/* TalkBack */
	send {|val|
		this.controls.do{|control|
			control.send(val)
		}
	}	
	/* ACCESS */
	at {|n|
		^controls[n]
	}

	copySeries {|first,second, last|
		var range = last - first;
		^(0..range-1).collect{|n|
			controls[n]
		}
	}
	/* CLEAN UP */
	clearLabels {
		this.controls.do{|control|
			control.sender.sendMsg(control.recvAddr ++ '/label', '')
		}
	}

	zero {
		this.controls.do{|control|
			control.zero;
		}
	}

	free {
		this.controls.do{|control|
			control.oscfunc.free;
		}
	}

	clear {
		this.free;
		this.zero;
		this.clearLabels;
	}
} 
/* Multi Push/Toggle*/
OSCMultiTouchControl0D {

	var <>viewName, <>name, <>rows, <>columns, <>sender, <>controls;

	*new {|viewName, name, rows, columns, sender|
		^super.newCopyArgs(viewName, name, rows, columns,sender).onReceive;
	}

	onReceive {
		controls = ( 1 .. rows ).collect{|row|
			( 1 .. columns ).collect{|column|
				var numberedName = name ++ '/' ++ (row).asSymbol ++ '/' ++ column;
				OSCTouchControl0D(viewName, numberedName, row-1, column-1).sender_(sender);
			}
		}
	}

	cc_ {| ... arglist|
		rows.do{|row|
			columns.do{|column|
				controls[row][column].cc_(*arglist);
			}
		}
	}

	on_ {| ... arglist|
		rows.do{|row|
			columns.do{|column|
				controls[row][column].on_(*arglist);
			}
		}
	}

	off_ {| ... arglist|
		rows.do{|row|
			columns.do{|column|
				controls[row][column].off_(*arglist);
			}
		}
	}

	onOff_ {| ... arglist|
		rows.do{|row|
			columns.do{|column|
				controls[row][column].onOff_(*arglist);
			}
		}
	}

	/* TALKBACK */

	send {|val|
		rows.do{|row|
			columns.do{|column|
				controls[row][column].send(val);
			}
		}
	}	

	/* ACCESS */

	at {|row, col|
		if (col.notNil) 
		{
			^controls[row][col]
			} {
			^controls[row]
		}
	}

	copySeries {|first,second, last|
		var range = last - first;
		^(0..range-1).collect{|n|
			controls[n]
		}
	}

	/* CLEAN UP */

	zero {
		this.send(0)
	}

	free {
		this.send(0);
		rows.do{|row|
			columns.do{|column|
				controls[row][column].oscfunc.free;
			}
		}
	}

	clearLabels {
		this.send(0);
		rows.do{|row|
			columns.do{|column|
				controls[row][column].sender.sendMsg(controls[row][column].recvAddr ++ '/label', '')
			}
		}
	}

	clear {
		this.zero;
		this.free;
		this.clearLabels;
	}	
} 

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

OSCControlView {
	
	var <>name, <>controls, <>sender;

	*new {|name, sender|
		^super.new.init(name, sender)
	}

	init {|name, sender|
		this.name = name;
		this.sender = sender;
		this.controls = ();
	}

	/* ADD CONTROLS  */
	addPushControl {|name|
		var control = OSCTouchControl0D(this.name, ('/' ++ name), sender);
		this.controls.put(name, control);
	}

	addToggleControl {| ... arglist|
		this.addPushControl(*arglist)
	}

	addMultiPushControl {|name, rows, columns|
		var control = OSCMultiTouchControl0D(this.name, ('/' ++ name), rows, columns, sender);
		this.controls.put(name, control);
	}

	addMultiToggleControl {| ... arglist|
		this.addMultiPushControl(*arglist)
	}

	addFaderControl {|name|
		var control = OSCTouchControl1D(this.name, ('/' ++ name), nil, sender);
		this.controls.put(name, control);
	}

	addRotaryControl {|name|
		var control = OSCTouchControl1D(this.name, ('/' ++ name), nil, sender);
		this.controls.put(name, control);
	}

	addEncoderControl {|name|
		var control = OSCTouchControl1D(this.name, ('/' ++ name), nil, sender);
		this.controls.put(name, control);
	}

	addMultiFaderControl {|name, num|
		var control = OSCMultiTouchControl1D(this.name, ('/' ++ name), num, sender);
		this.controls.put(name, control);
	}

	addXYPadControl {|name|
		var control = OSCTouchControl2D(this.name, ('/' ++ name), nil, sender);
		this.controls.put(name, control);
	}

	/* ACCESS */
	at {|idx|
		^controls.at(idx)
	} 
	/* CLEAN UP */
	clear {
		/* clear all controls*/
		controls.do{|control|
			control.clear
		}
	}

	zero {
		controls.do{|control|
			control.zero
		}
	} 
}
/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */
