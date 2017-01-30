/* 
	Design Test - How To Structure a live performance

	Plan:
		Seperate out musical elements 
		Work with elements accoridng to their properties

	Review:
		Sub Nodes for NamedControls
		Ndef(\a,{ \env.kr })
		Ncon(\a,\env, {}) //something like
		Ndef(\a).env

	ObjectWishList;
		- NodeTrigger class TrDef([],0,[]).clock(?default);

		- Pattern/Demand with counter so that ever n times
		an function is diffferent value is pattern/list is used

		- Keyboard/Sampler Synth - instruments
		
		- Dbind: Demand "Patterns"

		- Instance Effects (Insert/sends)

		Ndefs keep a list of inputs and outputs so modifications 
		such as quantizing can be made downstream:
			e.g align start of triggers with synth

*/
//Bass

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */
//globals

t = TempoClock.new(60/60);

/* ------------------------------------------------------------------------
------------------------------------------------------------------------- */

Ndef(\kick, {
	var env, sig;
	env = Env.perc(\atk.kr(0.1),\rel.kr(0.1));
	env = EnvGen.kr(env,\tr.kr(1));
	sig = 
		Pan2.ar(
			SinOsc.ar(\freq.kr(80) * env,
			\pan.kr(0)
			)
		);
	sig * \amp.kr(0.1)  * env;
});


Ndef(\kick).resetSourceOnQuant(t, 8);
Ndef(\bass_kick).resetSourceOnQuant(t, 8);

t.play({1.postln;nil}, 4.asQuant)

t.nextTimeOnGrid(4)

(
Ndef(\kick)
	<<.tr Ndef(\bass_kick, {
		TDuty.kr(Dseq([1,Drand([1, Dseq([0.75,0.25]) ]), 1, 0.5, 0.5] * 0.5, inf));
	})
)

(	
	Ndef(\bass_amp, { 
 		Demand.kr(\tr.kr,0,Dwhite(0.4,0.5))
	}) <<.tr Ndef(\bass_kick)
	
) 

Ndef(\kick).set(\atk, 0.01, \rel,0.5)

//Percussion

a = (
	Scale.minorPentatonic.degrees.collect{|deg|
	[
		(( (3..10) * 12 ) + deg).midicps,
		exprand(400,10000)
	]
	}
).flat.do(_.postln);

.midicps

Ndef(\hh, {

	Pan2.ar(
		HPF.ar(
			WhiteNoise.ar,
			15000
		),
		\pan.kr(0)
		)
	*EnvGen.kr(Env.perc(0.01,0.1,-4),\tr.kr)
	*\amp.kr(0.1)
}).play

Ndef(\hh) <<.tr Ndef(\hh_tr, {
	TDuty.kr(Dseq([
		3,1,2,2,1,3,4
		+ 3,1,2,2,1,3,4
		] * 0.125,inf))
})

//Texture


Ndef(\kick).


//Lines

Ndef(\lead, {
	var oscs, env;

	oscs = 0!2;

	oscs = oscs.collect{
		var sig = [Saw].choose;


		Pan2.ar( 
			MoogFF.ar(
				sig.ar(\freq.kr(100) + 100),
				\freq.kr(100)
			),
			SinOsc.kr(Rand(0.2,4.5)).range(-1,1) 
			)
	};

	Splay.ar(oscs.flat)
	* EnvGen.kr(Env.asr) 
	* \amp.kr(0.01);
})

s.meter

Ndef(\lead).play


Ndef(\lead) <<.freq Ndef(\rnd, { 
	Duty.kr(Dseq({[1,2].choose * 0.125}!8, inf),0,Dseq(Scale.minorPentatonic.ratios.scramble * 1000, inf))!2
	// Duty.kr(Drand([0.125,0.25] * 0.5, inf),0,Drand(Scale.aeolian.ratios * 200, inf))!2
})



Ndef(\a, {SinOsc.ar(100)!2 * 0.2  }).play;

Ndef(\a)[2] = { LFTri.ar(80)!2 * 0.2  };
Ndef(\b, {LFTri.ar(80)!2 * 0.2  });


Ndef(\a).numChannels;
Ndef(\b).numChannels;
Ndef(\x).numChannels;

Ndef(\x, { \in.ar(0!2) })
Ndef(\x).play;
Ndef(\x) <<+ Ndef(\a) 
Ndef(\x).mixer(\in)[\a];


Ndef(\x).mixer [ \controlKey ] [ \proxyKey ]

Ndef(\x).mixer(\a).proxy.objects

Ndef(\x).manager.inputMixer.proxy.numChannels;

Ndef(\x).mixIn // returns mixer dictionary
			[\in] // returns mixer
				[\a] //returns channel
					
Ndef(\b).bus

s.plotTree
Ndef(\a).play;

Ndef(\a)[1] = { SinOsc.ar * 0.1} 
Ndef(\a).play

Ndef(\a).fadeTime_(4)
Ndef(\a)[1]
 = nil;

Ndef(\a).objects[1].parents
Ndef(\a).play

Ndef(\a).sources

s.plotTree


m = NdefMixGroup(\x);

m.dest


Ndef(\x) <<+ Ndef(\a)

Ndef(\x).inputMixer.proxy.play

a = NdefManager(\x)

a.makeInputMixer

a.inputMixer.mixer

NodeProxy.perform('audio')


NdefG(\g, \a, \b)
NdefG.all

Ndef(\x, { \in.ar(0!2) })

NdefMixerChannel.newFromNdef(\x);

Ndef.clear
Ndef(\a)[1] = 
a = `{ SinOsc.ar * 0.1  } ;

Ndef(\a).objects.at(1).set(\gate,1)

Ndef(\a).fadeTime_(1)
Ndef(\a).play
Ndef(\a)[1]




Ndef(\test, { SinOsc.ar * 0.1  }) ;

f = NdefChannelFunction(Ndef(\a), 1, { SinOsc.ar * 0.1  });
Ndef(\a).play

f.clear(4)
f.vol_(0.1,4)

Ndef(\m, {MouseX.kr})

f.map(\vol, Ndef(\m))
f.unmapAll


Ndef(\a).unmap(\a1_vol)
Ndef(\a).controlNames

f.map(\vol, Ndef(\m))
Ndef(\a).unmap(\vol)

(
Ndef(\a, { SinOsc.ar * 0.1 });
Ndef.ar(\m);
n = NdefChannel.newFromNdef(\m);
n.put( \a, Ndef(\a) );
n.play
)

n[\a].stop(4)
n[\a].play(8)
n[\a].set(\trigma, 1);
n[\a].set(n[\a].controlId('trig'), 1);
n[\a].controlId('trig') == \trigma

Ndef(\m).set(\t_gatema, 1)
Ndef(\m).stop
n.objects[\a] n[\a].output
Ndef(\m).objects
n.play
n.sources.cs
n.set()
n.controlNames

Ndef(\a)[0] =  {SinOsc.ar * 0.1 * EnvGen.kr( Env.perc(0.01,1.0),1)}; Ndef(\a).play
Ndef(\a).set(\t_gate,1)

(

Ndef(\a, { SinOsc.ar * 0.1!2 });
// Ndef(\z, {MouseX.kr});
NdefM(\m, { \in.ar(0!2) });

// m = NdefChannelMixer(\m);

// m.put(\a, Ndef(\a));

// {Ndef(\a).ar}.play
// m.play
NdefM(\m).play <<+ Ndef(\a)

)

NdefM(\m).hasMixerProxies

NdefM(\m)[\in][\a].play

Ndef(\m) <<+ Ndef(\b, { SinOsc.ar((Scale.aeolian.degrees + 48).midicps).mean!2 * 0.1 })

(
	Ndef(\m).mixer(\in, \b, \a ].stop;

)
Ndef(\m).mixer(\in).play
Ndef(\m).manager.mixers

Ndef(\a).play

m.isPlaying
m.stop
m.play
m.isPlaying

m[\a].stop







