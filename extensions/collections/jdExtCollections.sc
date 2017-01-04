+ Collection {

	firstIndexAt {|object|
		var index;
		this.do{|item, i|
			if (item == object)  {	^index = i }
		}
		^index;
	}

	performAt {|object, func|
		var index;
		this.do{|item, i|
			if (item == object)  {	func.value(this, item, i) }
		}
		^index;
	}

}

