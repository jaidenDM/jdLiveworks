OSCTouchControl1D {
	
	var <>viewName, <>name, <>id, <>sender, <>label, <>recvAddr, <>oscfunc, <>ccFunc;

	*new {|viewName, name, id, sender|
		^super.newCopyArgs(viewName, name, id, sender).onReceive;
	}

	type { ^1}

	onReceive {

		recvAddr = viewName.asSymbol ++ name.asSymbol;

		oscfunc !? { oscfunc.free };
		oscfunc = OSCFunc({|v,n,c,s|
			ccFunc.value(v[1], id);
			/* feedback */
			sender !? {
				sender.sendMsg(recvAddr ++ '/label', (label ? ''))
			} 
		}, recvAddr);
	}

	cc_ {|func, label|
		this.ccFunc = func;
		this.label = label ? '';
		this.onReceive;
	}

	send {|val|
		sender !? {
			sender.sendMsg(recvAddr, (val ? 0))
		} 
	}

	clear {
		oscfunc.free;
		sender.sendMsg(recvAddr ++ '/label', '')
	}
}
/* Array of slide controls */
OSCMultiTouchControl1D {

	var <>viewName, <>name, <>num, <>sender, <>controls;

	*new {|viewName, name, num, sender|
		^super.newCopyArgs(viewName, name, num, sender).onReceive;
	}

	onReceive {
		controls = (1..num ).collect{|id|
			var numberedName = name ++ '/' ++ (id).asSymbol;
			OSCTouchControl1D(viewName, numberedName, id - 1);
		}
	}

	cc_ {|func, label|
		this.controls.do{|control|
			control.ccFunc = func;
			control.label = label ? '';
			control.onReceive;
		}
	}

	clear {
		this.controls.do{|control|
			control.oscfunc.free;
			control.sender.sendMsg(control.recvAddr ++ '/label', '')
		}
	}

	send {|val|
		this.controls.do{|control|
			control.send(val)
		}
	}	

	at {|n|
		^controls[n]
	}

	copySeries {|first,second, last|
		var range = last - first;
		^(0..range-1).collect{|n|
			controls[n]
		}
	}
} 
/* xy-pad */
OSCTouchControl2D {
	
	var <>viewName, <>name, <>id, <>sender, <>label, <>recvAddr, <>oscfunc, <>ccFunc;

	*new {|viewName, name, id, sender, label|
		^super.newCopyArgs(viewName, name, id, sender, label).onReceive;
	}

	type { ^\cc }

	onReceive {

		recvAddr = viewName.asSymbol ++ name.asSymbol;
		recvAddr.postln;

		oscfunc !? { oscfunc.free };
		oscfunc = OSCFunc({|v,n,c,s|
			ccFunc.value(v[1], v[2]);
			/* feedback */
			sender !? {
				sender.sendMsg(recvAddr ++ '/label', (label ? ''))
			} 
		}, recvAddr);
	}

	cc_ {|func, label|
		this.ccFunc = func;
		this.label = label ? '';
		this.onReceive;
	}

	send {|val|
		sender !? {
			sender.sendMsg(recvAddr, (val ? 0))
		} 
	}

	clear {
		oscfunc.free;
		sender.sendMsg(recvAddr ++ '/label', '')
	}
}
/* push, toggle */
OSCTouchControl0D {

	var <>viewName, <>name, <>row, <>col,
		<>sender, <>label,
		<>recvAddr, <>oscfunc,
		<>onFunc,<>offFunc, <>onLabel, <>offLabel;

	*new {|viewName, name, row,col, sender, label|
		^super.newCopyArgs(viewName, name, row, col, sender, label).onReceive;
	}

	type { ^\push }

	onReceive {

		recvAddr = viewName ++ name;
		oscfunc !? { oscfunc.free };
		oscfunc = OSCFunc({|v,n,c,s|
			if(v[1] == 0)
			{
				this.offFunc.value(v[1], row, col);
				/* feedback */
				sender !? {
					sender.sendMsg(recvAddr ++ '/label', (offLabel ? label ? ''))
				} 
			} {
				this.onFunc.value(v[1], row, col);
				/* feedback */
				sender !? {
					sender.sendMsg(recvAddr ++ '/label', (label ? ''))
				} 
			};
		}, recvAddr);
	}

	onOff_ {|onFunc, offFunc, label, offLabel|
		this.onFunc = onFunc;
		this.offFunc = offFunc;
		this.label = label ? '';
		this.offLabel = offLabel ? label;
		this.onReceive;
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

	send {|val|
		sender !? {
			sender.sendMsg(recvAddr.postln, (val ? 0))
		} 
	}
}
/* Array of push controls */
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

	clear {
		this.send(0);
		rows.do{|row|
			columns.do{|column|
				controls[row][column].oscfunc.free;
				controls[row][column].sender.sendMsg(controls[row][column].recvAddr ++ '/label', '')
			}
		}
	}

	send {|val|
		rows.do{|row|
			columns.do{|column|
				controls[row][column].send(val);
			}
		}
	}	

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
} 

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

OSCControlView {
	
	var <>name, <>controls, <>sender;

	*new {|name, sender|
		^super.new.init(name, sender)
	}

	* clear {
		/* clear all controls*/
	}

	init {|name, sender|
		this.name = name;
		this.sender = sender;
		this.controls = ();
	}

	addPushControl {|name|
		var control = OSCTouchControl0D(this.name, ('/' ++ name), sender);
		this.controls.put(name, control);
	}

	addMultiPushControl {|name, rows, columns|
		var control = OSCMultiTouchControl0D(this.name, ('/' ++ name), rows, columns, sender);
		this.controls.put(name, control);
	}

	addSlideControl {|name|
		var control = OSCTouchControl1D(this.name, ('/' ++ name), sender);
		this.controls.put(name, control);
	}

	addMultiSlideControl {|name, num|
		var control = OSCMultiTouchControl1D(this.name, ('/' ++ name), num, sender);
		this.controls.put(name, control);
	}

	add2DSlideControl {|name|
		var control = OSCTouchControl2D(this.name, ('/' ++ name), sender);
		this.controls.put(name, control);
	}

	load {|config|

	}

	at {|idx|
		^controls.at(idx)
	} 
}
/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

