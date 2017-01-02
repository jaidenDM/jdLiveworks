1.postln;
(
	s.boot;
MIDIClient.init;
// MIDIIn.connect(2,MIDIClient.sources[2]);
MIDIIn.connectAll;
m = MIDIControl(\control,16);
m.cc_{|v|v.postln};

)

m.serverTreeFunc.server

m.persist_(false)

m.serverTreeFunc.object

g = MIDINoteGroup([48,52,54,58]);

a =  MIDIControlArray.newFrom(g.controls[0..2]);


g.clear
(
s.boot;
o = NetAddr("192.168.0.18", 9000);
v = OSCControlView('/rout', o);
v.addPushControl('push');
v.addPushControl('tog');
v.addFaderControl('cc');

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

v[\cc].controlProxy.source
ß
BusBlug
v[\cc].controlBus
{ SinOsc.ar * v[\cc].controlBus.kr }.play
);

t[\faders].tr_{|v,n, id|
	n.postln;
};


ServerTree.dumpAllMethods

s.boot; ~func = {'5300'.postln}; ServerTreeDict.put(~func, key:\a);
s.boot; ~func = {'5300'.postln};

 a = ServerTreeFunc.put(~func, \all);

a.remove
a.put
ServerTree.objects[\all]

s.boot

ServerTree.objects[\all].firstIndexAt(~func)

ServerTree.objects[\all].do{|f|
	(f == ~func).postln	
}

ServerTree.objects[\all].includes(~func)
s.boot

s.quit;
JDServerTree.objects[\all]

l = List.new();

l.add(~func)
l.includes(~func)

ServerTree.add(~func);
ServerTree.objects[\all][0].firstIndexAt()
ServerTree.put
NamedServerTree.functionSelector

s.options.sampleRate_(44100);s.reboot


(\a : 1).do{|v| v.postln}

NodeWatcher







