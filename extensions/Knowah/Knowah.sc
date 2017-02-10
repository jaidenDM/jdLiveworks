/*
	
	Record Control Data

	Store data

	manipulate data

	patterns/ demand rate ugens
	TO DO:

	Quantization - 

	Flexible Management for Deeper Nests

	Get rid of timeArrray -> just use dur in event return recursive collect [\dur]
	
}
*/

CompartmentalDataRecorder {

	var <>clock, <>lastEntryTime, <>runningData, <>returnIndexes, <>storedData, <>currentData, <>isRecording;
	var <>timeQuant;
	*new {|aClock|
		^super.new.init(aClock);
	}

	init {|aClock|
		this.clock = aClock ? TempoClock.default;
		this.storedData = List.new;
		this.currentData = ();
		this.isRecording = false;
		timeQuant = 2**(-4);
	}

	timeSinceLastEntry { ^( this.clock.beats - this.lastEntryTime ) }

	resetLastEntryTime { this.lastEntryTime = this.clock.beats }

	prepareNewStore { }

	startCapture {
		this.resetLastEntryTime;
		this.runningData = List.new;
		this.returnIndexes  = List.new;
		isRecording = true;
	}

	prStoreEntry {

		var activeList = this.prDeepAt(this.runningData, this.returnIndexes);
		this.timeSinceLastEntry.postln;
		this.set(\dur, this.timeSinceLastEntry.round(timeQuant).postln; );
		activeList.add(this.timeSinceLastEntry.round(timeQuant));

		this.resetLastEntryTime;
	}

	addEntry {

		if (this.isRecording.not)
		{ 
			this.startCapture;
		} {
			this.prStoreEntry;
		}
	}

	addEntryToNewStore {
		this.endEntry;
		this.addEntry;
	}

	addEntryToNewSubStore {
		this.endSubEntry;
		this.addEntry;
	}
	
	addSubEntry {   
		
		var activeList = this.prDeepAt(this.runningData, this.returnIndexes);
		activeList = activeList.add(List.new);
		this.returnIndexes = this.returnIndexes.add(activeList.size - 1);

		this.addEntry;
	}

	addUpperEntry {
		this.removeLatestIndex;
		this.addEntry;
	}

	addTopEntry {
		this.jumpToTop;
		this.addEntry;
	}

	jumpLevel { if (this.returnIndexes > 0) { this.returnIndexes.removeAt(this.returnIndexes.size - 1) } }

	jumpToTop { this.returnIndexes.clear }

	breakCapture {|func|
		if (this.isRecording) {
			func.value;
			isRecording = false;
			this.storedData = storedData.add(runningData.copy);
		}
	}

	endEntry { this.breakCapture{ this.addEntry } }

	endSubEntry { this.breakCapture{ this.addEntry } }

	endCapture { this.breakCapture{ this.jumpToTop; this.addEntry } }

	set {| ... currentDataPairs|
		// runningData
		currentDataPairs.keysValuesDo{|key, value|
			this.currentData.put(key, value);
		}
	}

	data { ^this.storedData }

	atKey {|aKey| 
		^RecursiveArray.newFromArray(this.data).collect(elementFunc:{|el|
			var res;
			el.keysValuesDo{|key, value|
				if (key == aKey) { res = value }
			};
			res;
		})
	}

	/* return elements and summed arrays */
	atLevel {|level|
		
	}

	asPseq {|key| }

	asDseq {|key| }

	/*PRIVATE*/
	/* move this into List class and call as instance method in store */
	// prDeepAt {|arr, inds, i|
	// 	var ret;
	// 	if (i.isNil) {
	// 		i = 0;
	// 	} {
	// 		i = i + 1;
	// 	};

	// 	if ( i == inds.size ) {
	// 		^ret = arr;
	// 	} {
	// 		ret = arr[inds[i]];
	// 		ret = this.prDeepAt(ret,inds, i)
	// 	};
	// 	^ret;
	// }	

	/* CleanUp */

	clear {

	}

}

/* ========================================================================
========================================================================= */

/*
	Recorder

	Approaches:
		Record one file and use time data
			- data more volatile
			- could write it to text file (py)

		Record into multiple smaller files

		Write to disk or just keep in buffers for session?

		could use the control recorder but with functions as arguments to execute
*/


// CompartmentAudioRecorder {
// 	/*
// 		similar to control data recorder but records into audio buffers 
// 	*/
// 	classvar <>recorder, buffer, defname;
	
// 	*initClass {
// 		recorder 
// 		defname = this.class.asString++"DiskOut";
		
// 		SynthDef(defname.asSymbol, {
// 			DiskOut.ar();
// 			})
// 	}

// 	*new {

// 	}

// 	init {

// 	}
// }


/* NEEDS REDOING */
// Unnecesary?
RecursiveArray : List {
	var <>depth;
	var <>parent;
	/* Instantiation  */
	*newFromArray {|aArray, depth| ^super.new.array_(aArray.asArray).depth_(depth ? 0) }
	
	/* Access */

	/* Enumeration  */
	asRecursiveArray {|arrayFunc, elementFunc|
		var res = this.class.newClear(0);

		this.array.do{|el|
			if (el.isArray)
			{ res.add( 
				this.class
					.newFromArray(el)
					.depth_(this.depth + 1)
					.asRecursiveArray
				) 
			}
			{ res.add((el) ) }
		};
		^res
	}

	collect {|arrayFunc, elementFunc|
		var res = [];
		arrayFunc ?? {arrayFunc = {|arr| arr} };
		elementFunc ?? {elementFunc = {|el| el} };

		this.array.do{|el|
			if (el.isArray)
			{ res = res.add( arrayFunc.value(
				this.class
					.newFromArray(el)
					.depth_(this.depth.postln + 1)
					.collect(arrayFunc, elementFunc) ) 
				) 
			}
			{ res = res.add( elementFunc.value(el) ) }
		};

		^res
	}

	do {|arrayFunc, elementFunc|
		arrayFunc ?? {arrayFunc = {|arr| arr} };
		elementFunc ?? {elementFunc = {|el| el} };

		this.array.do{|el|
			if (el.isArray)
			{ arrayFunc.value(
				this.class
					.newFromArray(el)
					.parent_(this)
					.depth_(this.depth + 1)
					.do(arrayFunc, elementFunc),
					this.depth)  
			}
			{ elementFunc.value(el, this.depth)  }
		};
	}

	/* state */
	flat {
		 ^this.class.newFromArray(this.array.flat);
	}

	// species { ^this.class}
}
