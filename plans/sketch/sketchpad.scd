1.postln;
(
	s.boot;


// m.persist_(false);
)

s.waitForBoot

(
	MIDIClient.init;
// MIDIIn.connect(2,MIDIClient.sources[2]);
	MIDIIn.connectAll;
	m = MIDIControl(\control,16);
	m.cc_{};
)


{
	SinOsc.ar *  m.controlProxy.kr
}.play

m.serverTreeFunc.server

m.persist_(false)

m.serverTreeFunc.object

g = MIDIControl(\control, 16);

g.msgNum
g.controlProxy


a =  MIDIControlArray.newFrom(g.controls[0..2]);

g.clear
(
s.boot;
o = NetAddr("192.168.0.18", 9000);
v = OSCControlView('/rout', o);
v.addPushControl('push');
v.addPushControl('tog');
v.addFaderControl('cc');



);


v[\tog].controlProxy.fadeTime_(0.01);

a = NodeProxy.control(s,1).source_(0);
a.source_(1)
{ SinOsc.ar([20,200].asSpec.map(a.kr).poll) }.scope

{ SinOsc.ar() * 0.1) }.play;

v[\tog].kr([])






v[\tog].on_({1.postln},'on');
v[\tog].off_({0.postln},'off');
v[\tog].clear;

AbstractControl().onReceive

t[\toggle].controls[0][0].recvAddr

a = NodeProxy.control(s,1).source_{|v=1|v};
a.set(\v, 1);
a.get(\v);
{ a.kr }.play

a = OSCMatrixTouchControl('/tcon','/toggle',4,4,0)
a.onReceive
a[0,0]

(

a = NodeProxy.control(s,1).source_(0);
a.fadeTime_(0.5)
a.source_(100)
a.source
{a.kr}.scope;

NodeProxy
{ v[\cc].controlProxy.kr }.scope
v[\tog].controlProxy.get(\val)

v[\cc].cc_{}


ß
v[\cc].c
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

a = {| ... b| b.postln}

a.value(3,4,5)

p  =Parent(1,2)

p.b

c = Child(4,5,6)
4.collect{|i|i}
c.a
(
MIDIClient.init;
MIDIIn.connectAll;
m = MIDINoteMatrix( \note,[48,52,54,58,49,51,68,56,60,59,56,55,61,69,65,63].reshape(4,4).reverse );
m.on_{|v,n,x,y,i|
	v.postln;
	n.postln;
	x.postln;
	y.postln;
	i.postln;
}

)

m[0,0].chan

m = MIDINoteFroup(
	\note,[48,52,54,58,49,51,68,56,60,59,56,55,61,69,65,63].reshape(4,4).reverse
);
m.col(1)
m[0,0]

m.controls

m.controls.at(0)

s.boot;o = NetAddr("192.168.0.18", 9000);
c = OSCMultiTouchControl.new('/tcon','/faders',5,1,o);
t = OSCMatrixTouchControl.new('/tcon','/toggle',4,4,o);

t.on_{|v|v.postln}


c.cc_{|v,n|v.postln}

c.controls[0].recvAddr







