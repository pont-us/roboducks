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

// Arena: represents the current state of the duck arena.
//        Implements the *Control interfaces, through which the
//        DuckModel and DogModel classes interact with it.

package net.talvi.roboducks.ducksim;

class Arena implements DuckControl, DogControl, TrainerControl {
  private int numDucks;
  private Pos[] duck;
  private int numDogs;
  private Pos[] dog;
  private java.util.Random rand = new java.util.Random();
  private static final float arenaRadius = 1000;
  private float flockR, flockMR;
  private Vec flockC;
  private boolean flUpToDate;	// whether flock{C,R} are updated
  private int time;		// simulation time
  private AnimalModel duckModel;
  private AnimalModel dogModel;
  private ArenaView arenaView;

  Arena(int numDucks) {
    this.numDucks = numDucks;
    duck = new Pos[numDucks];
    flUpToDate = false;
    flockC = new Vec();
    time = 0;
    arenaView = new ArenaView(this);
    resetRandom(true);
  }

  public AnimalModel duckModel() { return duckModel; }
  public AnimalModel dogModel() { return dogModel; }
  public void startTraining() { arenaView.setActive(false); }
  public void stopTraining() { arenaView.setActive(true); }
  public void setDogModel(AnimalModel d) { dogModel = d; }
  public void setDuckModel(AnimalModel d) { duckModel = d; }
  public void repaint() { arenaView.repaint(); }
  public void resetRandom() { resetRandom(false); }
  public int time() { return time; }

  public void resetRandom(boolean setDog) {
    for (int i=0; i<numDucks; i++) {
      duck[i] = new Pos();
      randPoint(duck[i].p, 50f, arenaRadius-50);
      randPoint(duck[i].d, 0f, 0.5f);
    }
    this.numDogs = 1;
    dog = new Pos[numDogs];
    dog[0] = new Pos();
    if (setDog) {
      randPoint(dog[0].p, 50f, arenaRadius-50);
      randPoint(dog[0].d, 0f, 3.5f);
    } else {
      dog[0].p.x = 20;
      dog[0].p.y = 20;
    }
    time = 0;
    arenaView.reset();
  }	

  private void randPoint(Vec p, float min, float max) {
    float r = rand.nextFloat()*(max-min) + min;
    float a = rand.nextFloat() * 2 * (float) Math.PI;
    p.x = r * (float) Math.cos(a);
    p.y = r * (float) Math.sin(a);
  }

  public float arenaSize() { return arenaRadius; }
  public float arenaArea() { return arenaRadius*arenaRadius; }
  public float dogRadius() { return arenaRadius/25; }
  public float duckRadius() { return arenaRadius/22; }
  public float goalSize() { return arenaRadius/22; }
  public int ducks() { return numDucks; }
  public int dogs() { return numDogs; }
  public Pos duck(int i) { return (Pos) duck[i].clone(); }
  public Vec duckPos(int i) { return (Vec) duck[i].p.clone(); }
  public Vec duckV(int i) { return (Vec) duck[i].d.clone(); }
  public Pos dog(int i) { return (Pos) dog[i].clone(); }
  public Vec dogPos(int i) { return (Vec) dog[i].p.clone(); }
  public Vec dogV(int i) { return (Vec) dog[i].d.clone(); }
  public void setDuck(int i, Vec v)
  { duck[i].d.x = v.x; duck[i].d.y = v.y; }
  public void setDog(int i, Vec v)
  { dog[i].d.x = v.x; dog[i].d.y = v.y; }
  public void setDongle(int i, float a) { dog[i].nAngle = a; }
  public void setDungle(int i, float a) { duck[i].nAngle = a; }
  public Pos[] cloneAllDogs() { return cloneArray(dog); }
  public Pos[] cloneAllDucks() { return cloneArray(duck); }

  private Pos[] cloneArray(Pos[] in) {
    Pos[] out = new Pos[in.length];
    for (int j=0; j<in.length; j++) out[j] = (Pos) in[j].clone();
    return out;
  }

  // omnipotent trainer methods

  public Pos[] getAllDogs() { return dog; }
  public Pos[] getAllDucks() { return duck; }
  public void setAllDogs(Pos[] newDog) { dog = newDog; }
  public void setAllDucks(Pos[] newDuck) { duck = newDuck; }

  // this is actually a class method, but if I declared it static
  // I couldn't put it in the interface.
  // (inherited from last year's algorithms; I don't use it.)
  public float calcAngle(Vec f) {
    float r = 0.0f; // to prevent "variable not initialized"
    // Matt's updated angle code in floats and radians
    // scope for optimisation here but not going into it yet
    float theta = (float) Math.atan(f.x/f.y); // note sign change!!
    if (f.x > 0 && f.y >0)	 r = theta;
    else if (f.x > 0 && f.y < 0) r = (float) Math.PI + theta;
    else if (f.x < 0 && f.y < 0) r = (float) Math.PI + theta;
    else if (f.x < 0 && f.y > 0) r = (float) Math.PI*2 + theta;
    else if (f.x == 0) if (f.y >= 0) r = 0.0f; else r = (float) Math.PI;
    return r;
  }

  public void advance() {
    for (int i=0; i<ducks(); i++) duck[i].move();
    for (int i=0; i<dogs(); i++) dog[i].move();
    flUpToDate = false;
  }

  public void update() {
    time++;
    duckModel.update();
    dogModel.update();
    advance();
    duckModel.postUpdate();
    dogModel.postUpdate();
    arenaView.repaint();
  }

  public float flockRadius() {
    if (!flUpToDate) calcFlockData();
    return flockR;
  }

  public float flockMeanRadius() {
    if (!flUpToDate) calcFlockData();
    return flockMR;
  }

  public Vec flockCentre() {
    if (!flUpToDate) calcFlockData();
    return (Vec) flockC.clone();
  }

  private void calcFlockData() {
    float nDist;
    flockC.x = flockC.y = 0;
    for (int i=0; i<numDucks; i++) flockC.pleq(duck[i].p);
    flockC.dieq(numDucks);
    flockR = flockMR = 0;
    for (int i=0; i<numDucks; i++) {
      nDist = flockC.distTo(duck[i].p);
      flockR = flockR >= nDist ? flockR : nDist;
      flockMR += nDist;
    }
    flockR += duckRadius();
    flockMR /= numDucks;
    flUpToDate = true;
  }
}

// Abstract animal-model classes, and interfaces through which
// they should call Arena's methods.

interface CommonControl {
  float arenaSize();
  float arenaArea();
  float dogRadius();
  float duckRadius();
  float goalSize();
  int ducks();
  int dogs();
  Pos duck(int duck); // duck position & velocity
  Vec duckPos(int duck); // duck position
  Vec duckV(int duck); // duck velocity
  Pos dog(int dog); // dog position & velocity
  Vec dogPos(int dog); // dog position
  Vec dogV(int dog); // dog velocity
  float calcAngle(Vec v);
  float flockRadius();
  Vec flockCentre();
  float flockMeanRadius();
  Pos[] cloneAllDogs();
  Pos[] cloneAllDucks();
}

// Omnipotent control for training purposes
interface TrainerControl extends DogControl, DuckControl {
  void resetRandom();
  void resetRandom(boolean setDog);

  // Q: why are these "get" methods relegated to the Trainer i/face?
  // A: They return the actual array references (not clones), hence
  //    allow teleportation &c (makes setAllXs kinda pointless, maybe?)
  Pos[] getAllDogs();
  Pos[] getAllDucks();

  void setAllDogs(Pos [] p);
  void setAllDucks(Pos [] p);
  void setDuck(int duck, Vec velocity);
  void setDungle(int duck, float angle);
  void setDog(int dog, Vec velocity);
  void setDongle(int dog, float angle);
  void advance();
  void startTraining();
  void stopTraining();
  AnimalModel duckModel();
  AnimalModel dogModel();
}

interface DuckControl extends CommonControl {
  void setDuck(int duck, Vec velocity);
  void setDungle(int duck, float angle);
}

interface DogControl extends CommonControl {
  void setDog(int dog, Vec velocity);
  void setDongle(int dog, float angle);
}

// This class is used for both the dog and duck model.
// The difference is that each should only see Arena through
// the appropriate interface.

abstract class AnimalModel {
  abstract void update();
  abstract void postUpdate();
}


