
MIDIClient.init;
MIDIIn.connectAll;
// MIDIFunc.cc({|v,n| n.postln}

/* ------------------------------------------------------------------------
					CONTROLLER(s)
------------------------------------------------------------------------- */

m = MIDIController("padKontrol", "PORT A");
//Default Scene 1 - padKontrol
m.addNoteMatrix(\pad, [
	48,52,54,58,
	49,51,68,56,
	60,59,56,55,
	61,69,65,63 ]
	.reshape(4, 4)
	.reverse 
);
m.addControl(\prog, \program, 9);
m.addControlNote(\pdl, 47);

m.addControl(\k1, \control, 20);
m.addControl(\k2, \control, 21);
m.addControl(\y, \control, 1);

/* ------------------------------------------------------------------------
					 DATA RECORDER
------------------------------------------------------------------------- */

r = CompartmentalDataRecorder();

r.startCapture;

~prepNewStore = false;

m[\pdl].on{  }

// m[\pad][0,0].on_{ r.addEntry };
// m[\pad][0,1].on_{ r.addEntryToNewStore };
// m[\pad][0,2].on_{ r.addEntryToNewSubStore };


/* ------------------------------------------------------------------------
					Material
------------------------------------------------------------------------- */

/* Load Sound Files */
b = ();

/* Get Tempo from Folder Structure For Loops/Clips */




p = PathName("~/Music/sounds/chops/".standardizePath);



Fdef(\deepReadFromFoldersIntoBuffers).value(b,p);

~drums = Buffer.read(s,PathName("~/Music/sounds/loops/".standardizePath).entries[0].absolutePath);

TempoClock.default.tempo = (4/b.trumpet[0].duration) ;

SynthDef(\play2, {|out = 0|
	var sig;
	sig = PlayBuf.ar(2, \bufnum.kr, \rate.kr(1) * BufRateScale.kr(\bufnum.kr), loop:\loop.kr(1),doneAction:\done.kr(2)) * 
	EnvGen.kr(Env.asr(0.01,1,0.01), \gate.kr(1), doneAction: \done.kr)
	 * \amp.kr(0.5);
	Out.ar(out, sig);
}).add;

/* Piano Loop */

~sampler = ();

~sampler.synths = nil!16;
~sampler.bus =  ~sampler.bus ? Bus.audio(s,2);
~sampler.g = Group.new;

4.do{|n|
	m[\pad][0,n].on_{
		~sampler.synths[n] = Synth.head(~sampler.g, \play2, [\out, ~sampler.bus, \bufnum, b.piano[n],\loop,0]);
	};
	m[\pad][0,n].off_{
		~sampler.synths[n].release;
	};
};

Buffer.freeAll


TempoClock.default.schedAbs( (TempoClock.default.beats + 4.0).roundUp,{
Ndef(\trumpet, 
Pbindef(\trumpet,
	\instrument, \play2,
	\bufnum, Pseq([0,0,0,1], inf).collect{|i| b.trumpet[i]},
	\legato,1,
	\rate, 1,
	\dur, /* Pfunc{|e| e.bufnum.duration}*/4
)
).play;

Ndef(\drums,
Pbindef(\drums,
		\instrument, \play2,
	\bufnum, ~drums,
	\legato,1,
	\rate, (2.592970521542/3.4285625) ,
	\dur, 4
)
).play;
nil;
});


Ndef(\trumpet).quant_(4).play;


m[\k1].cc_{|v| Ndef(\drums).set(\amp,v)}
m[\k2].cc_{|v| Ndef(\trumpet).set(\amp,v)}

Ndef(\sweep, {
	RLPF.ar(\in.ar(0!2), SinOsc.kr(TempoClock.default.tempo*2).range(1000,10000), 0.05) * \amp.kr(0.3); 
}).play << Ndef(\trumpet).stop(16);

Ndef(\lpf,{
	LPF.ar( \in.ar(0!2), \cut.kr(10000) * (1 - LFSaw.kr(4).range(0.8,0.2)) )
}).play << Ndef(\drums).stop(4)

Ndef(\sing, {
	Pan2.ar(SinOsc.ar(MouseX.kr.range(100,10000)) * MouseY.kr, LFDNoise1.kr(2).range(-1,1))  * 0.8 
});

Ndef(\drums).stop(16);
Ndef(\lpf).stop(8);
Ndef(\reverb).stop(8);
Ndef(\reverb, {
    var src = \in.ar(0!2) * \amp.kr(1);
    src = JPverb.ar(
        src,
        \t60.kr(1,           0.05),
        \damp.kr(0,          0.05),
        \size.kr(1,          0.05),
        \earlydiff.kr(0.707, 0.05),
        \mdepth.kr(5,        0.05),
        \mfreq.kr(2,         0.05),
        \lowx.kr(1,          0.05),
        \midx.kr(1,          0.05),
        \highx.kr(1,         0.05),
        \lowband.kr(500,     0.05),
        \highband.kr(2000,   0.05)
    );
});
)
Ndef(\midx).stop(4)
Ndef(\drums).play(8)

Ndef(\mix, {
	[Ndef(\sweep), Ndef(\lpf)].mean
	}).fadeTime_(8)
Ndef(\reverb).play << Ndef(\mix)

Ndef(\reverb).set(\t60,0.5, \damp,0.95)


/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */


t = TempoClock(1).permanent_(true);

// {1.postln}.onNext(4)
r = CompartmentalDataRecorder();
Fdef(\data, {r.storedData[0].asArray});

m[\pad][0,0].on_{ "tick".postln;r.addEntry};

Fdef(\metro, {~metro =  {SinOsc.ar * 0.1 * EnvGen.kr(Env.perc(0.01,0.5),Impulse.kr(1))}.play })

SynthDef(\kik, {|out = 0|
	var env;
	env = EnvGen.kr(Env.perc(0.01,\rel.kr(0.1),curve:-4), \gate.kr(1), doneAction: \done.kr(2));
	Out.ar(out,
		SinOsc.ar(\freq.kr * [0.99,1] * env) * env * \amp.kr(0.1)
		)
}).add;


Tdef(\loop, {
	'start!'.postln;
		8.wait;
	loop {

		// If not the same change

		8.wait;
		'loopahhh!!!'.postln;

		r.endCapture;

		Pdef(\a, Pseq(Fdef(\data).value.postln));
	}
});

{ Tdef(\loop).play }.onNext(8);

{ Fdef(\metro).value }.onNext(8);

{ ~metro.free }.onNext{8}

Pdef(\a, Pseq())


// On Next Section begin capture
// Quantization -> round up data to 1/16

// On next section end capture/begin new capture

Pdef(\kik).play;

			Pbind(
 				\instrument, \kik, \rel, 1, \freq, 70, \dur, Pseq([Pdef(\a)],inf)
 			)
 		).play;







/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

Ndef(\a, {
	SinOsc.ar(\freq.kr(100))!2
	* \amp.kr(0.1)
	* EnvGen.kr(Env.asr(), \gate.kr(1), doneAction: 2)
})

Ndef(\a).set(\gate,0);

s.plotTree



MIDIClient.init;
~midiDest = MIDIClient.destinations[0]

m = MIDIOut.new(0);
m

m.noteOn(1,60,30);
m.noteOff(1);


Pbindef(
	\midi, 
	// \type,\midi,
	// \midiout, m,
	// \channel, 1,
	\type, \rest,
	\midinote,Prand(Scale.majorPentatonic.degrees, inf) + 67,
	\amp, Pwhite(0.01,0.5),
	\legato,1.1,
	\dur,Prand([0.5,1,2],inf),
	\ratio, Pwhite(-0.5,0.5),
	\pfunc, Pfunc{|e| o.sendMsg('/r', e[\ratio]) }
).play

o = NetAddr("192.168.0.21", 8000);

o.sendMsg('/ratio', 0.5)

TempoClock.default.tempo = 80/60;




/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */



{

arg car, mod, num, low=100, high=5000, q=0.02, hpf=5000, hpfscal=0.05, outscal=25;
	
			var width, cf, hf,  out, filtmod, filtcar, tracker, ratio;
	
	
		out = Mix.arFill(( num + 1 ), { arg i; 
					
					ratio = (( high / low)**num.reciprocal );
	
				 	cf =  ( ratio**i) * low;
				 	
					filtmod = BPF.ar( mod, cf, q);
				
					tracker = Amplitude.kr(filtmod);
				
					filtcar = BPF.ar( car, cf, q);	
				
	 
					( outscal * ( filtcar * tracker ));
					});

		hf = HPF.ar(HPF.ar( mod, hpf), hpf);
		
		^out + ( hpfscal * hf )

}



/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

b = ;

Ndef(\voc, {|num, low=100, high=5000, q=0.02, hpf=5000, hpfscal=0.05, outscal=25|
	
			var width, cf, hf,  out, filtmod, filtcar, tracker, ratio;
	
	
		out = Mix.arFill(( num + 1 ), { arg i; 
					
					ratio = (( high / low)**num.reciprocal );
	
				 	cf =  ( ratio**i) * low;
				 	
					filtmod = BPF.ar( \mod.ar(0!2), cf, q);
				
					tracker = Amplitude.kr(filtmod);
				
					filtcar = BPF.ar( \in.ar(0!2), cf, q);	
				
					( outscal * ( filtcar * tracker ));
					});

		hf = HPF.ar(HPF.ar( \mod.ar(0!2), hpf), hpf);
		^out + ( hpfscal * hf )
});








