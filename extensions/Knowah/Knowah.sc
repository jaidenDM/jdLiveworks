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

ControlDataRecorder {

	var <>clock, <>lastEntryTime, <>runningData, <>returnIndexes, <>currentLevel, <>storedData, <>currentData, <>isRecording;
	*new {|aClock|
		^super.new.init(aClock);
	}

	init {|aClock|
		this.clock = aClock ? TempoClock.default;
		this.storedData = List.new;
		this.currentData = ();
		this.isRecording = false;
	}

	timeSinceLastEntry {
		^ ( this.clock.beats - this.lastEntryTime )
	}

	resetLastEntryTime {
		this.lastEntryTime = this.clock.beats;
	}

	startRecording {
		this.resetLastEntryTime;
		this.runningData = List.new;
		this.returnIndexes  = List.new;
		isRecording = true;
	}

	storeEntry {
		// this.set(\dur, this.timeSinceLastEntry );

		// if (this.returnIndexes.size == 0) {
		// 	this.runningData = this.runningData.add(currentData.copy);
		// } {
		var arr = this.prDeepAt(this.runningData, this.returnIndexes);
		this.set(\dur, this.timeSinceLastEntry );
		arr.add(currentData.copy);
		// };
		this.resetLastEntryTime;
	}

	addEntry {

		if (this.isRecording.not)
		{ 
			this.startRecording;
		} {
			this.storeEntry;
		}
	}

	addEntryToNewStore {
		this.stopRecording;
		this.addEntry;
	}
	/* move this into List class and call as instance method in store */
	prDeepAt {|arr, inds, i|
		var ret;
		if (i.isNil) {
			i = 0;
		} {
			i = i + 1;
		};

		if ( i == inds.size ) {
			^ret = arr;
		} {
			ret = arr[inds[i]];
			ret = this.prDeepAt(ret,inds, i)
		};
		^ret;
	}

	storeSubEntry {   
		
		// if (this.returnIndexes.size == 0) {
		// 	var arr = this.prDeepAt(this.runningData, this.returnIndexes);
		// 	arr = arr.add(List.new);
		// 	this.returnIndexes  =  this.returnIndexes.add(this.runningData.size.postln - 1);
		// } {
		var arr = this.prDeepAt(this.runningData, this.returnIndexes);
		arr = arr.add(List.new);
		this.returnIndexes  =  this.returnIndexes.add(arr.size - 1);
		// };

		this.storeEntry;
	}

	addLowerEntry { 
		/* store index to return later */
	}

	addUpperEntry {
		this.removeLatestIndex
	}

	removeLatestIndex {  
		this.returnIndexes.removeAt(this.returnIndexes.size - 1);
	}

	stopRecording {
		if (this.isRecording) {
			this.addEntry;
			isRecording = false;
			this.storedData = storedData.add(runningData.copy);
		}
	}

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

	asPseq {|key| }

	asDseq {|key| }
}




/* ========================================================================
========================================================================= */



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
