1.postln;

MIDIClient.init;
// MIDIIn.connect(2,MIDIClient.sources[2]);
MIDIIn.connectAll;

g = MIDINoteGroup([48,52,54,58]);

a =  MIDIControlArray.newFrom(g.controls[0..2]);


g.clear
(
o = NetAddr("192.168.0.18", 9000);
v = OSCControlView('/rout', o);
v.addPushControl('push');
v.addPushControl('tog');


t = OSCControlView('/tcon', o);
t.addMultiFaderControl('faders',5);
t.addMultiPushControl(\toggle,4,4);

t[\toggle].on_{|v,x,y,i| 
	x.post;' '.post;
	y.postln;
	i.postln;
}
);

t[\toggle].controls[0][0].recvAddr

a = OSCMatrixTouchControl('/tcon','/toggle',4,4,0)
a.onReceive
a[0,0]

(
v[\tog].on_({1.postln},'on');
v[\tog].off_({0.postln},'off');
v[\tog].clear;

v[\tog].cc_{1.postln}
);

t[\faders].tr_{|v,n, id|
	n.postln;
};

