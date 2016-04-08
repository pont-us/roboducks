public class OrigDucks extends DuckModel {
    
    float K1, K2, K3, K4, L;
    float DUCKRADIUS, ARENASIZE, ARENAAREA;
    int nFlockRadius;
    DuckControl state;

    OrigDucks(DuckControl state) {
	this.state = state;
	this.ARENASIZE = state.arenaSize();
	ARENAAREA = ARENASIZE * ARENASIZE;
	K1 = ARENAAREA/10;
	K2 = ARENAAREA/100;
	K3 = ARENAAREA/100;
	K4 = ARENAAREA/10;
	L = ARENASIZE/4;
	DUCKRADIUS = ARENASIZE/22;
    }

    void update() {

	// Vec vArenaCentre = new Vec(ARENASIZE/2,ARENASIZE/2);
	Vec vArenaCentre = new Vec(0,0);

	for (int i = 0; i < state.ducks(); i++) {
	    Vec d = new Vec(0,0); // hope this is right
	    Vec W, D;
	    float nDist, nParam;
	    D = state.duck(i).p;
		
	    // Apply duck control algorithm

	    for (int j = 0; j < state.ducks(); j++) {
		if (j==i) continue;
		final Vec Dn = state.duck(j).p;
		nDist = Dn.mi(D).abs();
		nParam = K1 / ((nDist + L) * (nDist + L)) - 
		    K2 / (nDist * nDist);
		d = d.pl( Dn.mi(D).unit().ti(nParam) );
	    }
	    for (int j = 0; j < state.dogs(); j++) {
		final Vec R = state.dog(j).p;
		nDist = R.mi(D).abs();
		nParam = -K4 / (nDist * nDist);
		d = d.pl( R.mi(D).unit().ti(nParam) );
	    }

	    // Find nearest point on wall
	    W = D.mi(vArenaCentre).unit().ti(ARENASIZE/2).pl(vArenaCentre);
	    nDist = W.mi(D).abs();
	    nParam = -K3 / (nDist * nDist);
	    d = d.pl( W.mi(D).unit().ti(nParam) );
	    state.setDuck(i,d);

	    // Matt's updated angle code in floats and radians
	    // scope for optimisation here but not going into it yet

	    float theta = (float) Math.atan(d.x /d.y); // note sign change!!
	    if (d.x > 0 && d.y >0) 
		{ state.duck(i).nAngle = theta;}
	    else if (d.x > 0 && d.y < 0) 
		{ state.duck(i).nAngle = (float) Math.PI + theta;}
	    else if (d.x < 0 && d.y < 0) 
		{ state.duck(i).nAngle = (float) Math.PI + theta;}
	    else if (d.x < 0 && d.y > 0)
		{ state.duck(i).nAngle = (float) Math.PI*2 + theta;}
	    else if (d.x == 0)
		if (d.y >= 0) { state.duck(i).nAngle = 0.0f;}
		else { state.duck(i).nAngle = 2 * (float) Math.PI;}

	}
    }

    void postUpdate() {
	// calculate flock centre and radius

	Vec F = new Vec(0,0);
	for (int i = 0; i < state.ducks(); i++)
	    F = F.pl(state.duck(i).p);
	F = F.ti(1/state.ducks());
	nFlockRadius = 0;
	for (int i = 0; i < state.ducks(); i++)
	    {
		int nDist = (int) F.mi(state.duck(i).p).abs();
		nFlockRadius = nFlockRadius>=nDist ? nFlockRadius : nDist;
	    }
	nFlockRadius += DUCKRADIUS;

    }

}

