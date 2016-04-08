public class ChengDog extends DogModel {
  // Dog? It's a real bitch and no mistake...

  float ARENASIZE, ARENAAREA;
  int nFlockRadius;
  DogControl state;
  Vec G;
  int phase = 0; // the part of the algorithm the herder is currently running
  // NOTE if you change fspeed from 3, spiral may also need to be altered
  float spiral = 0; // how far the dog circles from the wall
  float fspeed = 3; // speed multiplier when dog moves fast
  float sspeed = 0.2f; // speed multiplier when dog moves slow
  boolean justFinished = true;
  // has the function just finished? should the dog wait for a bit to show off?
  int t;
  float MAXRADIUS;  // for herder wall avoidance
  float FLIGHTDIST;  // for duck-herder repulsion

  ChengDog(DogControl state, Vec goal) {
    this.state = state;
    this.ARENASIZE = state.arenaSize();
    this.ARENAAREA = state.arenaArea();
    this.MAXRADIUS  = (ARENASIZE/2) - (state.dogRadius() + 5);
    this.FLIGHTDIST = 1000/4;  //ARENASIZE/4
    G = goal;
  }

  void update() {
    float xstep = 0;
    float ystep = 0;
    Vec dogpos, force, wallpt, endpt, duckpos;
    Vec F = state.flockCentre();
    Vec vArenaCentre = new Vec(0,0);

    for (int i = 0; i<state.dogs(); i++) { // for ahem... every herder
      Pos dogstate = state.dog(i);
      dogpos = dogstate.p;

      switch (phase) {

      case 0: // find nearest point on wall and go to it. ================
	wallpt = dogpos.mi(vArenaCentre).unit().
	  ti(ARENASIZE/2).pl(vArenaCentre);
	force = wallpt.mi(dogpos).unit().ti(fspeed);
	state.setDongle(i,state.calcAngle(force));
	state.setDog(i,force);
	dogstate.d = force;
	// we can't apply velocities ourselves, so we work out
	// where the dog *would* be
	dogstate.move();
	// if dog is at wall, turn 90 deg and enter phase 1
	if (wallpt.distTo(dogstate.p) < state.dogRadius()+10) {
	  state.setDongle(i, state.dog(i).nAngle + (float) Math.PI/2 );
	  phase = 1;
	}
	break;

      case 1: // go round perimeter ========================================

	float theta = state.dog(i).nAngle;
	xstep = (float) Math.sin(theta);
	ystep = (float) Math.cos(theta);
	force = new Vec(fspeed*xstep, fspeed*ystep);

	//  richie's herder circling code
	endpt = dogpos.pl(force);
	wallpt = endpt.mi(vArenaCentre).unit().
	  ti(MAXRADIUS).pl(vArenaCentre);

	if (endpt.distTo(vArenaCentre) > MAXRADIUS)
	  force.mieq(endpt.mi(wallpt));

	state.setDongle(i, state.calcAngle(force));
	state.setDog(i,force);
	dogstate.d = force; dogstate.move();

	int closest = closestDuckToWall();
	Vec Fsub = F.ti((float) state.ducks()); // centre of subflock
	Fsub.mieq(state.duckPos(closest));
	Fsub.dieq( (float) (state.ducks()-1) );

	boolean spiraltest = true; // start spiralling when true
	int radiussub = 0;
	// ie, when all but one duck (or maybe all) are far from wall
	for (int k=0; k < state.ducks(); k++) {
	  if (k != closest) {
	    int dist = (int) Fsub.distTo(state.duckPos(k));
	    radiussub = radiussub>dist ? radiussub : dist;
	    if (vArenaCentre.distTo(state.duckPos(k)) 
		> (ARENASIZE/2 - FLIGHTDIST - 20) )
	      spiraltest = false;
	  }
	}
	if (spiraltest) phase = 11;
	break;

      case 11: // spiral inwards =============================================

	theta = state.dog(i).nAngle;
	xstep = (float) Math.sin(theta);
	ystep = (float) Math.cos(theta);
	force = new Vec(fspeed*xstep, fspeed*ystep);

	//  richie's herder circling code
	endpt = dogpos.pl(force);
	wallpt = endpt.mi(vArenaCentre).unit().
	  ti(MAXRADIUS-spiral).pl(vArenaCentre); // ***

	if (endpt.distTo(vArenaCentre) > MAXRADIUS-spiral) // ***
	  force.mieq(endpt.mi(wallpt));

	state.setDongle(i,state.calcAngle(force));
	state.setDog(i,force);
	dogstate.d = force; dogstate.move();

	closest = closestDuckToWall();
	Fsub = F.ti((float) state.ducks()); // centre of subflock
	Fsub.mieq(state.duckPos(closest));
	Fsub.dieq( (float) (state.ducks()-1) );
	radiussub = 0;
	for (int k=0; k < state.ducks(); k++) {
	  if (k != closest) {
	    int dist = (int) Fsub.distTo(state.duckPos(k));
	    radiussub = radiussub>dist ? radiussub : dist;
	  }
	}

	if (true) // always spiral
	  spiral = spiral+0.05f; // maybe even smaller than 0.05?

	// if dog is too close to centre
	if (dogpos.distTo(vArenaCentre) < FLIGHTDIST) {
	  spiral = 0; phase = 0;
	  // there's been a problem. go back to start of algorithm
	}

	// if whole flock herded and lone duck is 
	// away from wall enter phase 5
	else if (vArenaCentre.distTo(state.duckPos(closest)) <
		 (ARENASIZE/2 - FLIGHTDIST)
		 && (state.flockRadius() < (90+7*state.ducks())))
	  phase = 5; // move flock to goal

	// if sub-flock *is* a flock, ie. if it is herded...
	else if ((radiussub < (60 + 7*state.ducks()))
		 && // and if it's away from the wall...
		 ((Fsub.distTo(vArenaCentre) + radiussub) <
		  (ARENASIZE/2 - FLIGHTDIST))
		 && // double check that lone duck isn't away from wall too.
		 (vArenaCentre.distTo(state.duckPos(closest)) > 
		  (ARENASIZE/2 - FLIGHTDIST)))
	  phase = 2; // then enter phase 2
	break;

      case 2: // go back to wall =============================================
	wallpt = dogpos.mi(vArenaCentre).unit().ti(ARENASIZE/2).
	  pl(vArenaCentre);
	force = wallpt.mi(dogpos).unit().ti(fspeed);

	theta = state.calcAngle(force);
	state.setDongle(i,theta);
	state.setDog(i,force);
	dogstate.nAngle = theta;
	dogstate.d = force; dogstate.move();

	float dist = wallpt.distTo(dogpos); // if herder is at wall,
	if (dist < state.dogRadius() + 10) { // turn 90 deg and enter phase 3
	  theta -= (float) Math.PI/2;
	  state.setDongle(i,theta);
	  phase = 3;
	}
	break;

      case 3: // go back around perimeter ====================================

	theta = state.dog(i).nAngle;
	xstep = (float) Math.sin(theta);
	ystep = (float) Math.cos(theta);
	force = new Vec(fspeed*xstep, fspeed*ystep);

	//  richie's herder circling code
	endpt = dogpos.pl(force);
	wallpt = vArenaCentre.dirTo(endpt).ti(MAXRADIUS+10).pl(vArenaCentre);

	if (endpt.distTo(vArenaCentre) > MAXRADIUS+10)
	  force.pleq(endpt.mi(wallpt));

	state.setDongle(i,state.calcAngle(force));
	// apply force
	state.setDog(i,force); dogstate.d = force;
	dogstate.move();

	closest = closestDuckToWall();
	Fsub = F.ti((float)state.ducks());
	Fsub.mieq(state.duck(closest).p);
	Fsub.dieq((float)(state.ducks() - 1)); // Fsub centre of sub-flock
	duckpos = state.duck(closest).p;
	dogpos = dogstate.p; // dogpos position of herder

	// if you're close to duck you're going the wrong way
	if (dogstate.p.distTo(duckpos) < FLIGHTDIST - 20)
	  state.setDongle(i,state.dog(i).nAngle + (float) Math.PI);
	else {
	  Vec a = dogpos.dirTo(Fsub);
	  Vec b = Fsub.dirTo(duckpos);
	  // by seeing if they are equal:
	  if (a.equals(0.01f,b)) phase = 4;
	  // when dog is behind sub-flock wrt. single duck, enter phase 4
	}
	break;

      case 4: // slowly push sub-flock towards single duck
	closdist = ARENASIZE;
	for (k=0; k < DuckList.GetSize(); k++) // find duck closest to wall
	  {
	    const FVector& duckpos = DuckList[k].r;
	    wallpt = (ARENASIZE/2) * UnitVector(duckpos - vArenaCentre) + vArenaCentre;
	    dist = abs(wallpt - duckpos);
	    if (dist < closdist)
	      {
		closdist = dist;
		closest = k;
	      }
	  }

	Fsub = F * (float)DuckList.GetSize();				
	Fsub -= DuckList[closest].r;
	Fsub /= (float)(DuckList.GetSize() - 1); // Fsub centre of sub-flock

	radiussub = 0;
	for (k=0; k < DuckList.GetSize(); k++)
	  {
	    if (k != closest)
	      {
		int dist = (int) abs(Fsub - DuckList[k].r);
		radiussub = __max(radiussub, dist);
	      }
	  }

	herderpos = HerderList[i].r;

	force = UnitVector(DuckList[closest].r - herderpos);

	if (abs(Fsub - herderpos) < radiussub+FLIGHTDIST+5)
	  force *= sspeed;
	else
	  force *= fspeed;

	// Matt's updated angle code in floats and radians
	// scope for optimisation here but not going into it yet
	theta = atanf(force.x /force.y); // note sign change!!
	if (force.x > 0 && force.y >0) 
	  { HerderList[i].nAngle = theta;}
	else if (force.x > 0 && force.y < 0) 
	  { HerderList[i].nAngle = M_PI + theta;}
	else if (force.x < 0 && force.y < 0) 
	  { HerderList[i].nAngle = M_PI + theta;}
	else if (force.x < 0 && force.y > 0)
	  { HerderList[i].nAngle = M_2PI + theta;}
	else if (force.x == 0)
	  if (force.y >= 0) { HerderList[i].nAngle = 0.0f;}
	  else { HerderList[i].nAngle = M_PI;}

				// apply force
	HerderList[i].v = force;

				// apply velocities
	HerderList[i].r += HerderList[i].v;

	dist = abs(DuckList[closest].r - Fsub);
	//if (dist - radiussub < ATTRACTDIST - 80) 
	// when ducks close enough to attract each other
	if (abs(DuckList[closest].v)>0.2)
	  {  // turn 180 degrees and enter phase 5
	    HerderList[i].nAngle += M_PI;
	    phase = 5;
	  }
	break;


      }
    }
  }

  void postUpdate() {}

  private int closestDuckToWall() {
    float closdist = ARENASIZE;
    Vec vArenaCentre = new Vec(0,0);
    int closest = 0;
    for (int k=0; k < state.ducks(); k++) { // find duck closest to wall
      Vec duckpos = state.duckPos(k);
      Vec wallpt = duckpos.mi(vArenaCentre).unit().
	ti(ARENASIZE/2).pl(vArenaCentre);
      float dist = wallpt.distTo(duckpos);
      if (dist < closdist)
	{ closdist = dist; closest = k; }
    }
    return closest;
  }

}
