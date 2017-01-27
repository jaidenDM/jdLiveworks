+ Function {

	onNext {|beat, offset, clock|
		clock = clock ? TempoClock.default;
		clock.schedAbs((clock.beats).roundUp(beat),
		{
			this.value;
			nil;
		});
	}
}