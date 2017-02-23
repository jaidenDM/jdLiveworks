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

SegmentCapturer {

	classvar <>defaultQuant;
	var <>clock, <>lastEntryTime, <>runningData, <>returnIndexes, <>data, <>currentData, <>isRecording;
	var <>timeQuant;

	*initClass { defaultQuant = (2**(-4)) }

	*new {|aClock|
		^super.new.init(aClock);
	}

	init {|aClock|
		this.clock = aClock ? TempoClock.default;
		this.data = List.new;
		this.currentData = ();
		this.isRecording = false;
		timeQuant = defaultQuant;
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
		this.timeSinceLastEntry/*.postln*/;
		this.set(\dur, this.timeSinceLastEntry.round(timeQuant)/*.postln*/; );
		// activeList.add(this.timeSinceLastEntry.round(timeQuant));
		activeList.add(this.currentData.copy.postln);
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
			this.data = data.add(runningData.copy);
		}
	}

	endEntry { this.breakCapture{ this.addEntry } }

	endSubEntry { this.breakCapture{ this.addEntry } }

	endCapture { this.breakCapture { this.jumpToTop; this.addEntry } }

	set {| ... currentDataPairs|
		// runningData
		currentDataPairs.keysValuesDo{|key, value|
			this.currentData.put(key, value);
		}
	}

	get {|param|
		^this.data.deepCollect2(onElement:{|el| el.at(param)})
	}

	// data { ^this.data }

	// atKey {|aKey| 
	// 	^List.newFrom(this.data).deepCollect2(onElements:{|el|
	// 		var res;
	// 		el.keysValuesDo{|key, value|
	// 			if (key == aKey) { res = value }
	// 		};
	// 		res;
	// 	})
	// }

	/* return elements and summed arrays */
	atLevel {|level|
		
	}

	asPseq {|key| }

	asDseq {|key| }

	/* CleanUp */

	clear {

	}

}

/* ========================================================================
========================================================================= */


+ List {

	deepCollect2 {|onArray, onElement, depth = 0, limit|
		var res = List.newClear(0);
		onArray ?? {onArray = {|arr| arr} };
		onElement ?? {onElement = {|el| el} };
		// depth = depth ? 0;
		this.array.do{|el|
			if (el.isKindOf(List))
			{ 	
				depth = depth + 1;
				 res.add( 
					onArray.value(
						this.class
						.newFrom(el)
						.deepCollect2(onArray, onElement, depth, limit)
					) 
				);
			}
			{ res.add( onElement.value(el, depth) ) }
		};

		^res
	}

}
