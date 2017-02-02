+ Order {

	append {|index, object|
		indices = indices.add(index);
		array = array.add(object);
	}

	forceAdd {|object|
		var index = this.nextSlotFor(this.lastIndex);
		indices = indices.add(index);
		array = array.add(object);
	}

	putAtEnd {|index|
		var obj = this.at(index).copy;
		this.removeAt(index);
		this.append(index, obj);
	}

}

