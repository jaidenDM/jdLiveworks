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

	var <>clock, <>lastEntryTime, <>storedTimes, <>runningTimes, <>runningData, <>storedData, <>currentData, <>isRecording;
	*new {|aClock|
		^super.new.init(aClock);
	}

	init {|aClock|
		this.clock = aClock ? TempoClock.default;
		this.storedTimes = [];
		this.storedData = [];

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
		this.runningTimes = [];
		this.runningData = [];
		isRecording = true;
	}

	storeEntry {
		var dur = this.timeSinceLastEntry;
		this.runningTimes = this.runningTimes.add(dur);
		this.set(\dur, dur);
		this.runningData = this.runningData.add(currentData.copy);
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
		this.endRecording;
		this.addEntry;
	}

	endRecording {
		if (this.isRecording) {
			this.addEntry;
			isRecording = false;
			storedTimes = storedTimes.add(runningTimes.copy);
			storedData = storedData.add(runningData.copy);
		}
	}

	set {| ... currentDataPairs|
		// runningData
		currentDataPairs.keysValuesDo{|key, value|
			this.currentData.put(key, value);
		}
	}

	times { ^this.storedTimes }

	data { ^this.storedData }

	asArray {|aKey| 
		RecursiveArray.collect(
			this.data, 
			elementFunc: {|el|
				var ret;
				el.keysValuesDo{|key, val|
					if (key == aKey) { ret = val }
				};
				ret;
			}
		)
	}

	asPseq {|key| }

	asDseq {|key| }



}

RecursiveArray {

	*new {|array|
		var res = [];
		array.do{|el|
			if (el.isArray) { 
				res = res.add(RecursiveArray(el)).postln
			} {
				res = res.add(el)
			}
		};
		"pass".postln;
		^res
	}

	*collect {|array, arrayFunc, elementFunc|
		var res = [];
		arrayFunc ?? {arrayFunc = {|item|item} };
		elementFunc ?? {elementFunc = {|item|item} };
		array.do{|el|
			if (el.isArray) { 
				res = res.add(arrayFunc.value(RecursiveArray.collect(el, arrayFunc, elementFunc))).postln
			} {
				res = res.add(elementFunc.value(el))
			}
		};
		"pass".postln;
		^res
	}

}


ControlDataArray {

	// *recursiveAt {|arr| 
	// var res = [];
	// arr.do{|el|
	// 	if (el.isArray) { 
	// 		res = res.add(recursiveAt.value(el)).postln
	// 	} {
	// 		res = res.add(el+1)
	// 	}
	// };
	// "pass".postln;
	// ^res
	// }

}