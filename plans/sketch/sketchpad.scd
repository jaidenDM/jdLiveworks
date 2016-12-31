1.postln;

MIDIClient.init;
// MIDIIn.connect(2,MIDIClient.sources[2]);
MIDIIn.connectAll;

g = MIDINoteGroup([48,52,54,58]);

a =  MIDIControlArray.newFrom(g.controls[0..2]);


g.clear


o = OSCTouchControl1D('/tcon','/xy')
o.cc_{|x,y,i,n|
	// x.postln;
	// y.postln;
	i.postln;
	// n.postln;
}

o.recvAddr