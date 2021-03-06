class ChengDucks extends DuckModel {

  float ARENAAREA, ARENASIZE, DUCKRADIUS;
  float K1, K2, K3, K4, L;
  float MOMENTUM = 0.8f;
  // *new* proportion of duck's previous movement vector, rich
  float K5 = 1000*1000/10; // for duck-duck attraction in new1
  float ATTRACTDIST = 1000/4; // for duck-duck attraction in new2
  float FLIGHTDIST = 1000/4; // for duck-herder repulsion
  float WALLDIST = DUCKRADIUS + 15; // for duck-wall repulsion
  float SUCCDIST = 175;
  // *change* this to something proportional to no. in flock
  float DUCKTOPSPEED = 4; // a duck's top speed 
  int nFlockRadius;
  DuckControl state;

  ChengDucks(DuckControl state) {
    this.state = state;
    ARENASIZE = state.arenaSize();
    ARENAAREA = state.arenaArea();
    K1 = ARENAAREA/10;
    K2 = ARENAAREA/100;
    K3 = ARENAAREA/100;
    K4 = ARENAAREA/1;    // was ARENAAREA/10, rich
    // the above were all 1000*1000/whatever
    L = ARENASIZE/6;  // was ARENASIZE/4, rich
  }

  void update()
  {
    Vec vArenaCentre = new Vec(0,0);

    for (int i = 0; i<state.ducks(); i++) {
      Vec totforce, wallpt, duckpos;
      float dist, force;
      duckpos = state.duckPos(i);
      totforce = new Vec(0,0);

      // *new* duck attraction instinct 2 (limited range attraction)
      for (int j = 0; j < state.ducks(); j++) {
	if (j == i) continue;

	Vec otherduckpos = state.duckPos(j);
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

      wallpt = duckpos.mi(vArenaCentre).unit().
	ti(ARENASIZE/2).pl(vArenaCentre);
      dist = wallpt.mi(duckpos).abs();
      force = - K3 / (dist * dist);
      force -= - K3 / (WALLDIST * WALLDIST);
      if (force > 0) force = 0.0f;
      totforce.pleq( wallpt.mi(duckpos).unit().ti(force) );


      // *new* herder repulsion instinct
      for (int j = 0; j < state.dogs(); j++) {
	Vec herderpos = state.dogPos(j);
	dist = herderpos.mi(duckpos).abs();
	force = - K4 / (dist * dist);
	force -= - K4 / (FLIGHTDIST * FLIGHTDIST);
	if (force > 0.0) force = 0.0f;
	totforce.pleq(herderpos.mi(duckpos).unit().ti(force));
      }

      state.setDuck(i, state.duck(i).d.ti(MOMENTUM).pl(totforce.ti(1-MOMENTUM)));

      // duck speed limiter
      // WHAT THE YELLOW RUBBERY FUCK?! He's taking the
      // abs of a unit vector... the man's a maniac
      if (state.duck(i).d.abs() > 3.5)
	state.setDuck(i, state.duck(i).d.ti((float) (1/3.5)));

      //  richie's duck wall avoidance code.
      Vec endpt = state.duck(i).p.pl(state.duck(i).d);

      wallpt = 
	  endpt.mi(vArenaCentre).unit().ti(ARENASIZE/2-5).pl(vArenaCentre);
      if (endpt.mi(vArenaCentre).abs() > ARENASIZE/2 - 2)
	state.setDuck(i, state.duck(i).d.mi(endpt.mi(wallpt).ti(0.01f)));

      // Matt's updated angle code in floats and radians
      // scope for optimisation here but not going into it yet
      float theta = (float) Math.atan(totforce.x /totforce.y);
	// note sign change!!
      if (totforce.x > 0 && totforce.y >0) 
	{ state.duck(i).nAngle = theta;}
      else if (totforce.x > 0 && totforce.y < 0) 
	{ state.duck(i).nAngle = (float) Math.PI + theta;}
      else if (totforce.x < 0 && totforce.y < 0) 
	{ state.duck(i).nAngle = (float) Math.PI + theta;}
      else if (totforce.x < 0 && totforce.y > 0)
	{ state.duck(i).nAngle = (float) Math.PI*2 + theta;}
      else if (totforce.x == 0)
	if (totforce.y >= 0) { state.duck(i).nAngle = 0.0f;}
	else { state.duck(i).nAngle = (float) Math.PI;}

      // Apply velocities. Oh no we don't.
    }
  }

  void postUpdate() {} 
}
