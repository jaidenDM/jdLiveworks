/*
	TO DO

	Re-Structuring Too Soon? 

	MultiPush: better 2D access (MAKE OSC CONTROL ARRAY)
	(give matrix class 2d array object-> same for midi)

	- PersistOnServerTree X

	- Add Bus
		.kr ( )



*/
/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */
AbstractOSCTouchControl : AbstractControl {
	var  <>viewName, <>name, <>recvAddr, <>oscfunc, <>sender, <>label, <>respondFunc, <>idArgs;
	// <>server, <>controlProxy,
	// <>serverTreeFunc;

	/* TalkBack Messages */
	send {|val|
		sender !? { sender.sendMsg(recvAddr, (val ? 0)) } 
	}

	sendLabelMsg {|msg|
		sender !? { sender.sendMsg(recvAddr ++ '/label', msg ? '') } 
	}

	/* persist */

	// persist_ {|bool|
	// 	if (bool)
	// 	{ this.addToTree }
	// 	{ this.removeFromTree}
	// }

	// addToTree {
	// 	serverTreeFunc = ServerTreeFunc.put({ this.onReceive });
	// }

	// removeFromTree {
	// 	serverTreeFunc.remove
	// }

	/* CLEAN UP */
	zero { sender.sendMsg(recvAddr, 0 )}

	free { oscfunc.free }

	clearLabel { sender.sendMsg(recvAddr ++ '/label', '')	}

	clear {
		oscfunc.free;
		this.clearLabel;
		this.zero;
		this.controlProxy.clear;
	}
}

OSCTouchControl : AbstractOSCTouchControl { 

	*new {|viewName, name, sender ... idArgs|
		^super.new.init(viewName, name, sender, idArgs).onReceive;
	}

	init {|viewName, name, sender ... idArgs|

		this.viewName = viewName; 
		this.name = name; 
		this.sender = sender;
		this.idArgs = idArgs.flatten;
		this.recvAddr = viewName.asSymbol ++ name.asSymbol;	
		/* support more channels for xy */
		// this.controlProxy = this.controlProxy ? NodeProxy.control(this.server ? Server.default, 1);
		this.resetProxySource;
		this.persist_(true);
	}

	/* Control Function */
	onReceive {

		// this.controlProxy !? { 
		// 	this.controlProxy.clear;
		// };
		// if (this.controlProxy.isNeutral)
		// 	{ this.controlProxy.source_({|val = 0| val.lag(\lag.kr(0.05)) }) };
		this.resetProxySource;

		oscfunc !? { oscfunc.free;};
		oscfunc = OSCFunc({|v,n,c,s|
			
			this.setControlProxy(v[1]);

			respondFunc.value(*((v[1 .. v.size - 1]++[idArgs, n].flatten ).flatten) );
		}, recvAddr);
	}

	cc_ {|func, label|
		this.respondFunc = func;
		this.sendLabelMsg(label ? '');
		this.onReceive;
	}
}

OSCTrigControl : OSCTouchControl {
	tr_ {|onFunc, offFunc, onLabel, offLabel|
		this.cc_{| ... arglist|
			if (arglist[0] <= 0) 
			{ 
				offFunc !? { offFunc.value(*arglist) };
				this.sendLabelMsg(offLabel ? this.label ? '');
			} { 
				onFunc !? { onFunc.value(*arglist) };
				this.sendLabelMsg(onLabel ? this.label ? '');
			}
		}
	}
}

OSCPushControl : OSCTrigControl {

	var	<>onFunc, <>offFunc;

	on_ {|func, label|
		this.onFunc = {| ...  arglist|
			func.value(*arglist);
			this.label = label;
		};
		this.tr_(this.onFunc,this.offFunc);
	}

	off_ {|func, label|
		this.offFunc = {|...  arglist|
			func.value(*arglist);
			this.label = label;
		};
		this.tr_(this.onFunc,this.offFunc);
	}
}
/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

AbstractOSCMultiTouchOSC {

	var <>viewName, <>name, <>num, <>sender;
	var <>controls;

	doAll { this.subClassResponsibility(thisMethod)  }

	persist_ {|arglist|
		this.doAll({|control| control.persist_(*arglist)})
	}

	send_ {| ... arglist|
		this.doAll({|control| control.send_(*arglist)})
	}

	cc_ {| ... arglist|
		this.doAll({|control| control.cc_(*arglist)})
	}

	tr_ {| ... arglist|
		this.controls{|control| control.tr_(*arglist) }
	}

	on_ {| ... arglist|
		this.doAll({|control| control.on_(*arglist)})
	}

	off_ {| ... arglist|
		this.doAll({|control| control.off_(*arglist)})
	}

	zero {
		this.send(0)
	}

	free {| ... arglist|
		this.doAll({|control| control.free_(*arglist)})
	}

	clearLabels_ {| ... arglist|
		this.doAll({|control| control.clearLabel_(*arglist)})
	}

	clear {
		this.zero;
		this.free;
		this.clearLabels;
	}	
}

OSCMultiTouchControl : AbstractOSCMultiTouchOSC {

	*new {|viewName, name, num, sender|
		^super.new.init(viewName, name, num, sender);
	}

	init {|viewName, name, num, sender|
		this.viewName = viewName; 
		this.name = name;
		this.num = num;
		this.sender = sender;
		this.onReceive;

		this.persist_(true)
	}

	onReceive {
		controls = (1..num ).collect{|id|
			var numberedName = name ++ '/' ++ (id).asSymbol;
			OSCTouchControl(viewName, numberedName, sender, id - 1);
		}
	}

	doAll {|func|
		this.controls.do{| ... controlAndArgs|
			func.value(*controlAndArgs)
		}
	}

	/* TALKBACK */

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
} 

OSCMatrixTouchControl : AbstractOSCMultiTouchOSC {

	var  <>rows, <>columns;

	*new {|viewName, name, rows, columns, sender|
		^super.new.init(viewName, name, rows, columns, sender).onReceive;
	}

	init {|viewName, name, rows, columns, sender|
		this.viewName = viewName; 
		this.name = name;
		this.rows = rows;
		this.columns = columns;
		this.sender = sender;

		this.persist_(true);
	}

	onReceive {

		this.controls = ( 1 .. rows ).collect{|row|
			( 1 .. columns ).collect{|column|
				var numberedName = name ++ '/' ++ (row).asSymbol ++ '/' ++ column;
				OSCPushControl(viewName, numberedName, sender,row-1, column-1, (row-1) * rows + (column-1));
			}
		};
	}

	doAll {|func|
		rows.do{|row|
			columns.do{|column|
				func.value(controls[row][column])
			}
		}
	}

	/* TALKBACK */

	/* ACCESS */

	at {|row, col|
		if (col.notNil) 
		{
			^controls[row][col]
			} {
			^controls[row]
		}
	}

	/* CLEAN UP */
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
		var control = OSCPushControl(this.name, ('/' ++ name), sender);
		this.controls.put(name, control);
	}

	addToggleControl {| ... arglist|
		this.addPushControl(*arglist)
	}

	addMultiPushControl {|name, rows, columns|
		var control = OSCMatrixTouchControl(this.name, ('/' ++ name), rows, columns, sender);
		this.controls.put(name, control);
	}

	addMultiToggleControl {| ... arglist|
		this.addMultiPushControl(*arglist);
	}

	addFaderControl {|name|
		var control = OSCTouchControl(this.name, ('/' ++ name), sender);
		this.controls.put(name, control);
	}

	addRotaryControl {| ... arglist|
		this.addPushControl(*arglist)
	}

	addEncoderControl {| ... arglist|
		this.addPushControl(*arglist)
	}

	addMultiFaderControl {|name, num|
		var control = OSCMultiTouchControl(this.name, ('/' ++ name), num, sender);
		this.controls.put(name, control);
	}

	addXYPadControl {|name|
		var control = OSCTouchControl(this.name, ('/' ++ name), nil, sender);
		this.controls.put(name, control);
	}

	/* ACCESS */
	at {|idx|
		^controls.at(idx)
	} 
	/* CLEAN UP */
	clear {
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
