/* Group manipulation of Ndefs */
AbstractNdefCollection {
	do { } //virtual method
	//Source Setting
	clear {| ... arglist|
		this.do{|def|
			def.clear(*arglist)
		}
	}

	fadeTime_ {| ... arglist|
		this.do{|def|
			def.fadeTime_(*arglist)
		}
	}

	//Initialisation
	ar {| ... arglist|
		this.do{|def|
			def.ar(*arglist)
		}
	}

	kr {| ... arglist|
		this.do{|def|
			def.kr(*arglist)
		}
	}

	// Play Controls
	play {| ... arglist|
		this.do{|def|
			def.play(*arglist)
		}
	}

	stop {| ... arglist|
		this.do{|def|
			def.stop(*arglist)
		}
	}

	//Timing
	clock {| ... arglist|
		this.do{|def|
			def.clock(*arglist)
		}
	}

	quant {| ... arglist|
		this.do{|def|
			def.quant(*arglist)
		}
	}

}


NGroup : AbstractNdefCollection {
	var <>defs;

	*new {| ... aKeys|
		^super.new.init(*aKeys)
	}
	
	init {| ... aKeys|
		this.defs = ();
		this.add(*aKeys);
	}

	defNames {
		^this.defs.asArray.collect{ |def| def.key }
	}

	size { ^defs.size }

	// Management
	add {| ... aDefNames|
		aDefNames.do{|key|
			this.defs[key] = Ndef(key);
		};
	}

	remove {| ... aDefNames|
		aDefNames.do{|key|
			this.defs.removeAt(key);
		};
	}

	removeAll {
		this.defNames.do{|key|
			this.remove(key)
		}
	}
	
	// Access
	at {| aDefName|
		^defs.at(aDefName.asSymbol);
	}

	// Iteration 
	do {|func|
		defs.do{|def|
			func.value(def);
		}
	}

	// Posting
	printOn {|stream|

		var string = "";

		this.defNames.do {|name|
			string = string + ("\n\t Ndef('" ++ name.asString ++ "')") 
		};
		stream << this.class.asString << ":" << string 

	}

	
}

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

NDict : NGroup {

	add {| ... aKeyDefnamePairs|
		if (aKeyDefnamePairs.size.even)
		{
			aKeyDefnamePairs.pairsDo {|key, def|
				this.defs[key.asSymbol] = Ndef(def.asSymbol);
			}
		} {
			^Error("Array Not Even").throw;
		}
	}

	printOn {|stream|
		var string = "";
		this.defs.pairsDo {|key, def|
			string = string + ("\n\t \\" ++ key.asString ++ " : Ndef('" ++ def.key.asString ++ "')") 
		};
		stream << this.class.asString << ":" << string 
	}

	//Access
	do {|func|
		defs.pairsDo{|key, def|
			func.value(key, def);
		}
	}

}

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

AbstractNamedNdefCollection : NGroup {

	classvar <>all;
	var <>key;
	
	*initClass { all = () }

	*new {| aKey ... arglist|
		var result;
		aKey = aKey.asSymbol;
		result = this.all.at(aKey);
		if (result.isNil)
		{
			result = super.new.init(aKey, *arglist);
			this.all[aKey] = result;
		} 
		^this.all[aKey]
	}

	init {| aKey ... arglist |
		super.init(*arglist);
		this.key = aKey;
	}

}

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

NdefG : AbstractNamedNdefCollection {

	// classvar <>all;
	// var <>key;
	
	// *initClass { all = () }

	// *new {|groupKey ... aKeys|
	// 	var result;
	// 	groupKey = groupKey.asSymbol;
	// 	result = this.all.at(groupKey);

	// 	if (result.isNil)
	// 	{
	// 		result = super.new.init(groupKey, *aKeys);
	// 		all[groupKey] = result;
	// 	} 

	// 	^this.all[groupKey]
	// }

	// init {|groupKey ... aKeys|
	// 	super.init(*aKeys);
	// 	this.key = groupKey;
	// }

	*new {|aKey ... aDefKeys|
		^super.new.init(aKey, *aDefKeys)
	}

}

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */
// : SynthDefControl
/* very basic: should work at least as well as the proxy functions
	goals: each slot can play fade/ 
	- some way to use a nodeproxy on same bus? 
		look into synth controls
 */
NodeProxyChannel {
	var <>mixer, <>index, <>input, <>mVol, <>mLag, <>volControl, <>output, <>fadeTime;
	var <>clock, <>quant;

	*new {|aMixer, aIndex, aInput, aVol, aLag|
		^super.new.init(aMixer, aIndex, aInput, aVol, aLag);
	}

	init {|aMixer, aIndex, aInput, aVol, aLag|
		this.mixer = aMixer;
		this.index = aIndex;
		this.input = aInput;
		this.lag = aLag ? 0.05;
		this.vol = aVol ? 1.0;
		this.fadeTime = 0.05;

		this.clock = aMixer.clock;
		this.quant = aMixer.quant;

		this.makeOutput(aInput);
	}

	idSymbol { ^(this.mixer.dest.key++index.asSymbol).asSymbol }
	controlId {|name | ^(name.asSymbol++this.idSymbol).asSymbol }
	idNamedControl{ | name, val, lag| ^NamedControl.kr( this.controlId(name), val, lag) }

	makeOutput {|input|
		this.output = { 
			input.value
			* 	this.idNamedControl(
					'vol',
					this.mVol,
					this.idNamedControl('lag', this.mLag)
				)
			* EnvGen.kr( 
				Env(
					[ this.idNamedControl('start', 1.0), this.idNamedControl('end', 0.0) ],
					[ this.idNamedControl('releaseTime', mixer.fadeTime)],
					[ this.idNamedControl('curve', 1)]
				),
				this.idNamedControl('t_trig', 0)
				// doneAction: 0
			)  
		};
	}

	// Setting/Getting
	set {| ... argPairs|
		argPairs.pairsDo {|key, val|
			this.mixer.postln;
			this.mixer.set(key, val);
		}
	}

	vol_{|aVol, aLag| 
		mVol = aVol;
		this.lag_(aLag);
		this.set(\vol, this.vol);
	}
	vol { ^mVol }

	lag_ {| aLag | 
		mLag = aLag;
		this.set(\lag, this.lag);
	}
	lag { ^mLag }

	//Mapping
	map {| ... argPairs|
		argPairs.pairsDo {|key, val|
			mixer.map(this.controlId(key), val);
		}
	}

	unmap {| ... aKeys |
		aKeys.do {|key|
			mixer.unmap(this.controlId(key));
		}
	}

	unmapAll {
		this.mixer.controlNames.do{|controlName|
			var name = controlName.name.asString;
			if ( name.contains(this.idSymbol.asString) )
			{ mixer.unmap(name.asSymbol) } //??? why no work
		};
	}
	// Source Setting

	remove { mixer.put(index, nil); }

	play {|aQuant, aFadeTime, curve = 1|
		this.clock.play({ 
			this.set(
				this.controlId('releaseTime'), aFadeTime ? this.fadeTime ? mixer.fadeTime,
				this.controlId('start'), 0.0, 
				this.controlId('end'), 1.0,
				this.controlId('curve'), curve,
				this.controlId('t_trig'), 1
			);
		}, aQuant ? this.quant)
	}

	stop {|aQuant, aFadeTime, curve = 1|
		this.clock.play({ 
			this.set(
				this.controlId('releaseTime'), aFadeTime ? this.fadeTime ? mixer.fadeTime,
				this.controlId('start'), 1.0, 
				this.controlId('end'), 0.0,
				this.controlId('curve'), curve,
				this.controlId('t_trig'), 1
			);
		}, aQuant ? this.quant)
	}

	//ClearUp
	clear {|aFadeTime, curve|
		var func = mixer.at(index);
		var clock = mixer.clock ? TempoClock.default;
		aFadeTime ?? {this.fadeTime = aFadeTime};
		this.stop(aFadeTime, curve);
		clock.sched(fadeTime, {
			this.remove;
			nil;
		})

	}
}

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

NdefChannel : NodeProxy {
	var <>chans, <>dest, <>rateSymbol;

	*newFromNdef {|aKey|
		var dest = Ndef(aKey);
		var rate = dest.rate.isNeutral.if({\audio});
		var numChannels = dest.numChannels;
		
		^this
		.perform(rate, Server.default, numChannels)
		.rateSymbol_( (rate == \audio).if({ \ar }, { \kr }))
		.dest_(dest);
	}

	init {| ... arglist|
		super.init(*arglist);
		this.chans = ();
	}

	put {| ... aKeyDefPairs|
		aKeyDefPairs.pairsDo{|key, def|
			var chan = this.chans.at(key);
			if (chan.isNil) 
			{ 
				this.chans.put(key, 
					NodeProxyChannel.new(
						this,
						key, 
						{ def.perform( this.rateSymbol ) }
					)
				) 
			};
			super.put( key, this.chans.at(key).output )
		}
	}

	at {|index|
		^chans.at(index)
	}
	
}

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

NdefGroupMixer : AbstractNamedNdefCollection {

	var <>proxy, <>dest, <>vols, <>chans;

	*new {|aKey|
		^super.new.init(aKey)
	}

	init {|aKey|
		super.init(aKey);
		this.dest = Ndef(aKey);
		this.proxy = NodeProxy.perform(
			this.dest.rate, 
			Server.default,
			numChannels: this.dest.numChannels);
				//.bus_(this.dest.bus); //TO DO: Find A way to use minimal buses


		this.vols = ();
	}

	put {| ... aKeyDefPairs|

		aKeyDefPairs.pairsDo{|key, def|
			var vol;
			vol = vols.at(key);
			if (vol.isNil)
			{
				vols[key] = NodeProxy.control.source_(1.0);
			};
			proxy.put(
				key, 
				{ def.ar * vols[key].kr }
			)

		}
	}

	remove {|aKeys, aFadeTime|
		aKeys.do{|aKey|
			proxy.at(aKey).clear(aFadeTime)
		}
	}

	// Setting
	// vol {|aKey, aVal|
	// 	vols[aKey].source_(aVal)
	// }

	// Access
	at {|aKey|
		// ^chan[aKey]
	}

	// Cleanup

	// removeAt {|aKey|
	// 	/* fadeTime ?  */
	// 	proxy.at(aKey).clear;
	// 	vols.removeAt(aKey).clear;	
	// }

	// free {
	// 	vols.clear;
	// 	proxy.clear;
	// }

}


















