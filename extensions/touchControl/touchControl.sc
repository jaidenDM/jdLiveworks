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
		this.controlProxy.clear;
	}
}

OSCTouchControl : AbstractOSCTouchControl { 

	*new {|viewName, name, sender, numChannels ... idArgs|
		^super.new.init(viewName, name, sender, numChannels, idArgs).onReceive;
	}

	init {|viewName, name, sender, numChannels ... idArgs|
		this.viewName = viewName; 
		this.name = name; 
		this.sender = sender;
		this.numChannels = numChannels;
		this.idArgs = idArgs.flatten;
		this.recvAddr = viewName.asSymbol ++ name.asSymbol;	

		super.init;
	}
	/* Control Function */
	onReceive {
		this.resetProxySource;
		oscfunc !? { oscfunc.free;};
		oscfunc = OSCFunc({|v,n,c,s|
			this.setControlProxy(v[1 .. this.numChannels]);
			respondFunc.value(*((v[1 .. this.numChannels]++[idArgs, n].flatten ).flatten) );
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

AbstractOSCMultiTouchOSC : AbstractControl {

	var <>controls, <>numControls;

	doAll { this.subClassResponsibility(thisMethod)  }

	persist_ {|func|
		this.doAll({|control ... arglist| control.persist_(func, *arglist)})
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

	on_ {|func|
		this.doAll({|control ... arglist| control.on_(func, *arglist)})
	}

	off_ {|func|
		this.doAll({|control ... arglist| control.off_(func, *arglist)})
	}

	zero {
		this.send(0)
	}

	free {|func|
		this.doAll({|control ... arglist| control.free_(func, *arglist)})
	}

	clearLabels_ {|func|
		this.doAll({|control ... arglist| control.clearLabel_(func, *arglist)})
	}

	clear {
		this.zero;
		this.free;
		this.clearLabels;
	}	
}

OSCMultiTouchControl : AbstractOSCMultiTouchOSC {
	/* unlike midi control group MultiTouchControls come as a set by default from the app */
	*new {|viewName, name, numControls, sender, numChannels|
		^super.new.init(viewName, name, numControls, sender, numChannels);
	}

	init {|viewName, name, numControls, sender, numChannels|
		this.numControls = numControls;
		this.controls = (1..numControls ).collect{|id|
			var numberedName = name ++ '/' ++ (id).asSymbol;
			OSCTouchControl(viewName, numberedName, sender, numChannels, id - 1);
		};

		super.init;
	}

	doAll {|func|
		this.controls.do{|control ... arglist|
			func.value(control, *arglist)
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
			this.controls[n]
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
		this.rows = rows;
		this.columns = columns;
		this.controls = ( 1 .. rows ).collect{|row|
			( 1 .. columns ).collect{|column|
				var numberedName = name ++ '/' ++ (row).asSymbol ++ '/' ++ column;
				OSCPushControl(viewName, numberedName, sender, 1, row-1, column-1, (row-1) * rows + (column-1));
			}
		};
		super.init;
	}

	/*onReceive {

		this.controls = ( 1 .. rows ).collect{|row|
			( 1 .. columns ).collect{|column|
				var numberedName = name ++ '/' ++ (row).asSymbol ++ '/' ++ column;
				OSCPushControl(viewName, numberedName, sender,row-1, column-1, (row-1) * rows + (column-1));
			}
		};
	}*/

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

	addMultiFaderControl {|name, numControls|
		var control = OSCMultiTouchControl(this.name, ('/' ++ name), numControls, sender);
		this.controls.put(name, control);
	}

	addXYPadControl {|name|
		var control = OSCTouchControl(this.name, ('/' ++ name), sender, 2);
		this.controls.put(name, control);
	}

	/* ACCESS */
	at {|idx,x,y|
		^controls.at(idx);//.at(x,y)
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
