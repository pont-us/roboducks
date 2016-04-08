/* = = = = >
 * This file is part of roboducks, an animal flocking and herding
 * simulator. Copyright 2001, 2016 Pontus Lurcock.
 *
 * roboducks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * roboducks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with roboducks. If not, see <http://www.gnu.org/licenses/>.
 * < = = = = */

/* ChengDucks: the duck model, developed by Richard Cheng
   last year from Richard Vaughan's original algorithm, now translated
   from C++ to Java and tidied up for this project. */

package net.talvi.roboducks.ducksim;

class ChengDucks extends AnimalModel {

  /* NB: The arena radius is hardwired to 1000. There's not really
     any reason to change this for the purposes of this project, so
     it makes sense to use final statics and gain some extra speed. */

  final static float ARENASIZE = 1000;
  final static float ARENAAREA = ARENASIZE * ARENASIZE;
  final static float DUCKRADIUS = ARENASIZE / 22;
  final static float K1 = ARENAAREA/10;
  final static float K2 = ARENAAREA/100;
  final static float K3 = ARENAAREA/100;
  final static float K4 = ARENAAREA/1;    // was ARENAAREA/10, rich
  final static float L = ARENASIZE/6;  // was ARENASIZE/4, rich
  final static float WALLDIST = DUCKRADIUS + 15; 
  // for duck-wall repulsion
  final static float MOMENTUM = 0.8f;
  // *new* proportion of duck's previous movement vector, rich
  final static float K5 = 1000*1000/10;
  // for duck-duck attraction in new1
  final static float ATTRACTDIST = 1000/4;
  // for duck-duck attraction in new2
  final static float FLIGHTDIST = 1000/4;
  // for duck-herder repulsion
  final static float SUCCDIST = 175;
  final static float DUCKTOPSPEED = 4; // a duck's top speed 
  int nFlockRadius;
  DuckControl state;

  ChengDucks(DuckControl state) {
    this.state = state;
  }

  void update()
  {
    // Vec vArenaCentre = new Vec(0,0);
    // We can ignore this since it's zero; speed is more important
    // for now.

    Vec totforce, wallpt, duckpos, otherduckpos, herderpos, duckV, endpt;

    for (int i = 0; i<state.ducks(); i++) {
      float dist, force;
      duckpos = state.duckPos(i);
      totforce = new Vec(0,0);

      // *new* duck attraction instinct 2 (limited range attraction)
      for (int j = 0; j < state.ducks(); j++) {
	if (j == i) continue;

	otherduckpos = state.duckPos(j);
	float attforce, repforce;

	dist = otherduckpos.mi(duckpos).abs();
	attforce =   K1 / ((dist + L) * (dist + L));
	repforce = - K2 / (dist * dist);

	attforce -=   K1 / ((ATTRACTDIST + L) * (ATTRACTDIST + L));
	repforce -= - K2 / (ATTRACTDIST * ATTRACTDIST); 

	if (attforce < 0.0) attforce = 0.0f;
	if (repforce > 0.0) repforce = 0.0f;
	force = attforce + repforce;
	totforce.pleq( otherduckpos.mi(duckpos).unit().ti(force) );
      }

      wallpt = duckpos.unit().ti(ARENASIZE);
      dist = wallpt.distTo(duckpos);
      force = - K3 / (dist * dist);
      force -= - K3 / (WALLDIST * WALLDIST);
      if (force > 0) force = 0.0f;
      totforce.pleq( duckpos.dirTo(wallpt).ti(force) );

      // *new* herder repulsion instinct
      for (int j = 0; j < state.dogs(); j++) {
	herderpos = state.dogPos(j);
	dist = herderpos.distTo(duckpos);
	force = - K4 / (dist * dist);
	force -= - K4 / (FLIGHTDIST * FLIGHTDIST);
	if (force > 0.0) force = 0.0f;
	totforce.pleq(duckpos.dirTo(herderpos).ti(force));
      }

      duckV = state.duck(i).d.ti(MOMENTUM).
	  pl(totforce.ti(1-MOMENTUM));

      // state.setDuck(i,state.duck(i).d.ti(MOMENTUM).
      //	    pl(totforce.ti(1-MOMENTUM)));

      // duck speed limiter
      // if (duckV.abs() > 3.5f) duckV.dieq(3.5f);
      if (duckV.abs() > 3.5f) duckV.tieq(3.0f / duckV.abs());

      //  richie's duck wall avoidance code.
      endpt = duckpos.pl(duckV);
      if (endpt.abs() > ARENASIZE/2 - 2) {
	wallpt = endpt.unit().ti(ARENASIZE/2-5);
	duckV.mieq( endpt.mi(wallpt).ti(0.01f) );
      }
      state.setDuck(i, duckV);
    }
  }

  void postUpdate() {} 
}
