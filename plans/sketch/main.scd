
~lib = "~/Music/sample".standardizePath;
~spl = ~lib +/+ "breaks/72.4/fleetwoodmac/proveyourlove/spls";
f = PathName(~spl).entries.do(_.postln)
b = [];

f.do{|file|
	b = b.add(Buffer.read(s,file.absolutePath))
}
b.do(_.postln)
b.choose.play
Buffer.freeAll;bnil;

~snare = List.new;
~kik = List.new;
~hh = List.new;

~kik.add(b[12])
~snare.add(b[14])
~hh.add(b[15])

~kik.choose.play

~pianoPath = PathName("~/Music/sample/chops/piano".standardizePath);

~piano = ~pianoPath.entries.collect{|file| Buffer.read(s, file.absolutePath)}

MIDIClient.init;
MIDIIn.connectAll;
(
MIDIFunc.noteOn({~kik.choose.play}, 48);
MIDIFunc.noteOn({~snare.choose.play}, 52);
MIDIFunc.noteOn({~hh.choose.play;'hh'.postln}, 54);
)
MIDIFunc.noteOn({|n,c,v|
	c.postln;
});
(~syn = ()).piano = nil!4;

(0..3).do{|n|
	MIDIFunc.noteOn({|v,num| 
		~syn.piano[n] = Synth(\sampler, [\buf, ~piano[n], \rate, [1.0].choose ])
	}, 24 + 4 + n);
	MIDIFunc.noteOff({|v,num| 
		~syn.piano[n].release
	}, 24 + 4 + n)	
}

SynthDef(\sampler, {|out=0|
	var sig, env;
	env = EnvGen.kr(Env.asr(0.01,1,0.01), \gate.kr(1) ,doneAction: \done.kr(2));
	sig = PlayBuf.ar(2,\bufnum.kr,\rate.kr(1),loop:\loop.kr(1));
	Out.ar(out, sig * env)
}).add;

TempoClock.default.tempo = 72/60;
Ndef.clear

Ndef(\t_play, { TDuty.kr(Drand([1],inf )) });

Ndef(\player,{

	PlayBuf.ar(
		2,
		\bufnum.kr,
		rate: \rate.kr(1),
		loop: \loop.kr(0),
		trigger: \tr.kr)
	* \env.kr(1)
}) <<>.tr Ndef(\t_play)

Ndef(\player) <<>.bufnum Ndef(\pno_sq, {
	Demand.kr(\tr.kr,0,Dseq(~piano,inf))
}) <<>.tr Ndef(\t_play)


Ndef(\sine,{
	SinOsc.ar(\freq.kr(10000)) * \amp.kr(0.1) *
	EnvGen.kr(Env.perc(0.01,0.01), Dust.kr(\dense.kr(1)))
	}).play

Ndef(\lpf,{
	LPF.ar(\in.ar(0!2),\cut.kr(100))
}).play <<> Ndef(\player).stop


Ndef(\lpf).play <<>.cut Ndef(\k_cut2)

Ndef(\k_cut2, {
	\in.kr.linlin(0,1,100,1000).clip(20,10000)
	}) <<> Ndef(\k_cut)

Ndef(\k_cut, {
	EnvGen.kr(Env([0,1,0],[0.01,0.99],[-1,1]),\tr.kr(1))
}) <<>.tr Ndef(\t_play)

Ndef(\player) <<>.env Ndef(\k_cut)


/* -------------------------OSC_TESTS---------------------------------------
------------------------------------------------------------------------- */


o = NetAddr("192.168.0.18", 9000);

o.sendMsg('/rout/fb_label', 'callback sad')

o.sendMsg('/rout/vumeter', )

Ndef(\vumeter, {SendReply.kr(Impulse.kr(60), '/vumeter', SinOsc.kr(0.5).range(0,1))});

{SendReply.kr(Impulse.kr(60), '/vumeter', SinOsc.kr(0.5).range(0,1))}

OSCFunc({|v|
o.sendMsg('/rout/vumeter', v[3])	
	},'/vumeter')


OSCdef(\sourceChoice, {|m,t,a,r|
	switch (m[1].asInteger)
		{0}	{'off'.postln;
		o.sendMsg('/rout/fb_label', 'none')}
		{1}	{'on'.postln;
		o.sendMsg('/rout/fb_label', 'player');
	}
},'/rout/source/tog')

/*
	
	Knoa:
	Data Recorder
	Record Pattern Data

*/

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

































