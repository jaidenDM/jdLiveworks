/* Automatically resend Ndefs when redefining SynthDefs being used */

SynthDefManager {
	classvar <>all;
	var <> ndefsUsing, <>key;

	*initClass { all = () }

	*new {|name|
		if (all.at(name).isNil)
		{
			all.put(name, super.new.key_(name).init);
		}
		^all.at(name);
	}

	init {
		ndefsUsing = ();
	}
}
/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */


+  Ndef {

	onPut {|index, obj, channelOffset, extraArgs, now|

		var removeFromSynthDefManager = {
			var synthDefName = this.manager.synthDefsUsing.at(index ? 0);
			if (synthDefName.notNil)
			{
				SynthDefManager(synthDefName).ndefsUsing.removeAt(this.key);
			}
		};

		if (obj.class == Symbol)
		{
			removeFromSynthDefManager.value;
			this.manager.synthDefsUsing.put(index ? 0, obj);
			SynthDefManager(obj).ndefsUsing.put(this.key, (\ndef : this, \index : index ? 0))
		} {
			removeFromSynthDefManager.value;
		}
	}

	put { | index, obj, channelOffset = 0, extraArgs, now = true |
		var container, bundle, oldBus = bus;

		if (obj.isNil) { this.removeAt(index); ^this };
		this.onPut(index, obj, channelOffset, extraArgs, now);

		if(index.isSequenceableCollection) {
			^this.putAll(obj.asArray, index, channelOffset)
		};

		bundle = MixedBundle.new;
		container = obj.makeProxyControl(channelOffset, this);
		container.build(this, index ? 0); // bus allocation happens here


		if(this.shouldAddObject(container, index)) {
			// server sync happens here if necessary
			if(server.serverRunning) { container.loadToBundle(bundle, server) } { loaded = false; };
			this.prepareOtherObjects(bundle, index, oldBus.notNil and: { oldBus !== bus });
		} {
			format("failed to add % to node proxy: %", obj, this).postln;
			^this
		};

		this.putNewObject(bundle, index, container, extraArgs, now);
		this.changed(\source, [obj, index, channelOffset, extraArgs, now]);

	}
}
/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */
+ SynthDef {

	onAdd {
		this.manager.ndefsUsing.do{|ndef|
			ndef.at(\ndef).send(ndef.at(\index));
		}
	}

	add { arg libname, completionMsg, keepDef = true;
		var	servers, desc = this.asSynthDesc(libname ? \global, keepDef);
		if(libname.isNil) {
			servers = Server.allRunningServers
		} {
			servers = SynthDescLib.getLib(libname).servers
		};
		servers.do { |each|
			/* jd.beginOverwrite(): */
			this.onAdd();
			/* jd.endOverwrite(): */
			this.doSend(each.value, completionMsg.value(each))
		}
	}

	manager { ^SynthDefManager(this.name)}
}

