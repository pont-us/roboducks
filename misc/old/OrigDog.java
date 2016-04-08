public class OrigDog extends DogModel {

    float ARENASIZE, ARENAAREA, HERDERK1, HERDERK2;
    int nFlockRadius;
    DogControl state;
    Vec G;

    OrigDog(DogControl state, Vec goal) {
	this.state = state;
	this.ARENASIZE = state.arenaSize();
	ARENAAREA = ARENASIZE * ARENASIZE;
	HERDERK1 = 0.0005f;
	HERDERK2 = 0.9f;
	G = goal;
    }

    void update() {
	// Vec vArenaCentre = new Vec(ARENASIZE/2, ARENASIZE/2);
	Vec vArenaCentre = new Vec(0,0);

	// Calculate flock centre    
	Vec F = new Vec(0,0);
	for (int i = 0; i < state.ducks(); i++)
	    F = F.pl(state.duck(i).p);
	F = F.ti(1/state.ducks());

	for (int i = 0; i < state.dogs(); i++) {
	    // Apply herder control algorithm
	    Vec R = new Vec(state.dog(i).p.x, state.dog(i).p.y);
	    Vec r = new Vec(0,0);

	    r = F.mi(R).unit().ti(F.mi(G).abs()).ti(HERDERK1).
		mi(G.mi(R).unit().ti(HERDERK2));
		
	    // Matt's updated angle code in floats and radiansz2
	    // scope for optimisation here but not going into it yet
	  
	    float theta = (float) Math.atan(r.x /r.y); // note sign change!!
	    if (r.x > 0 && r.y >0) 
		{ state.dog(i).nAngle = theta;}
	    else if (r.x > 0 && r.y < 0) 
		{ state.dog(i).nAngle = (float) Math.PI + theta;}
	    else if (r.x < 0 && r.y < 0) 
		{ state.dog(i).nAngle = (float) Math.PI + theta;}
	    else if (r.x < 0 && r.y > 0)
		{ state.dog(i).nAngle = (float) Math.PI*2 + theta;}
	    else if (r.x == 0)
		if (r.y >= 0) { state.dog(i).nAngle = 0.0f;}
	    // the above referred to the ducks in the original code
	    // I assume that was a typo...
		else { state.dog(i).nAngle = (float) Math.PI;}
	  
	    state.setDog(i,r);
		
	    boolean close;
	    //Matt's wall check code - CRUDE!!
	    float a = state.dog(i).p.x - ARENASIZE/2;
	    float b = state.dog(i).p.y - ARENASIZE/2;
	    float walldist = ARENASIZE/2 - (float) Math.sqrt(a*a+b*b);
	    final float mul = walldist/ARENASIZE/16;
	    if (walldist < ARENASIZE/16) 
		{ close = true;} else {close = false;}
	  
	    if (close) {
		if (a >= 0.0f && b >= 0.0f) { 
		    if (state.dog(i).d.x > 0) {state.dog(i).d.x *= mul;}
		    if (state.dog(i).d.y > 0) {state.dog(i).d.y *= mul;} }
		if (a >= 0.0f && b <= 0.0f) { 
		    if (state.dog(i).d.x > 0) {state.dog(i).d.x *= mul;}
		    if (state.dog(i).d.y < 0) {state.dog(i).d.y *= mul;} }
		if (a <= 0.0f && b <= 0.0f) { 
		    if (state.dog(i).d.x < 0) {state.dog(i).d.x *= mul;}
		    if (state.dog(i).d.y < 0) {state.dog(i).d.y *= mul;} }
		if (a <= 0.0f && b >= 0.0f) { 
		    if (state.dog(i).d.x < 0) {state.dog(i).d.x *= mul;}
		    if (state.dog(i).d.y > 0) {state.dog(i).d.y *= mul;} }
	    }

	}

    }
    void postUpdate() {}

}

