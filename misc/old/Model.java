import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;

class Vec implements Cloneable {
  float x,y;
  Vec(float x, float y) {
    this.x = x;
    this.y = y;
  }
  Vec() { this.x = this.y = 0; }

  float abs()
  { return (float) Math.sqrt(x*x + y*y); }
  float distTo(Vec v)
  { return (float) Math.sqrt((x-v.x)*(x-v.x)+(y-v.y)*(y-v.y)); }
  Vec dirTo(Vec v) {
    float dx, dy, a;
    dx = v.x-x; dy = v.y-y;
    a = (float) Math.sqrt(dx*dx+dy*dy);
    return new Vec(dx/a, dy/a);
  }

  // angle in radians: straight down = 0, straight up = +/- pi
  float angle() { return (float) Math.atan2(x,y); }
  float angleTo(Vec v) { return (float) Math.atan2(v.x-x,v.y-y); }
  float angleFrom(Vec v) { return (float) Math.atan2(x-v.x,y-v.y); }

  Vec unit() {
    float a = abs();
    if (a==0) throw new Error("Can't normalize a zero vector!");
    // if (a==0) return new Vec(0,0);
    return new Vec(x/a, y/a); }
  Vec norm() {
    float a = abs();
    if (a==0) throw new Error("Can't normalize a zero vector!");
    x/=a; y/=a; return this;
  }
  Vec ti(float a)    { return new Vec(x*a, y*a); }
  Vec tieq(float a)  { x*=a; y*=a; return this; }
  Vec di(float a)    { return new Vec(x/a, y/a); }
  Vec dieq(float a)  { x/=a; y/=a; return this; }
  Vec pl(float a)    { return new Vec(x+a, y+a); }
  Vec pl(Vec a)      { return new Vec(x+a.x, y+a.y); }
  Vec pleq(Vec a)    { x+=a.x; y+=a.y; return this; }
  Vec mi(float a)    { return new Vec(x-a, y-a); }
  Vec mieq(float a)  { x-=a; y-=a; return this; }
  Vec mi(Vec a)      { return new Vec(x-a.x, y-a.y); }
  Vec mieq(Vec a)    { x-=a.x; y-=a.y; return this; }

  boolean equals(float e, Vec v) {
    if (Math.abs(v.x-x)<e && Math.abs(v.y-y)<e) return true;
    return false;
  }

  static Vec add(Vec a, Vec b)
  { return new Vec(a.x+b.x, a.y+b.y); }
  static Vec add(Vec a, float b)
  { return new Vec(a.x+b, a.y+b); }
  static Vec add(float a, Vec b)
  { return new Vec(a+b.x, a+b.y); }
  static Vec sub(Vec a, Vec b)
  { return new Vec(a.x-b.x, a.y-b.y); }

  public Object clone() {
    try {return super.clone();}
    catch (CloneNotSupportedException e) {
      // this should never happen
      throw new InternalError("EEK! " + e.toString());
    }
  }
}

class Pos implements Cloneable {
  Vec p, d;
  float nAngle;
  Pos() {
    p = new Vec();
    d = new Vec();
  }
  void move() {
    p.x += d.x;
    p.y += d.y;
  }
  public Object clone() {
    Pos posCopy;
    try {posCopy = (Pos) super.clone();}
    catch (CloneNotSupportedException e) {
      // this should never happen
      throw new InternalError("ERK! " + e.toString());
    }
    posCopy.p = (Vec) p.clone();
    posCopy.d = (Vec) d.clone();
    return posCopy;
  }
}

class ModelState
implements DuckControl, DogControl, TrainerControl {
  private int numDucks;
  private Pos[] duck;
  private int numDogs;
  private Pos[] dog;
  private java.util.Random rand = new java.util.Random();
  private float arenaRadius = 1000;
  private int flockR; // why is this an integer?
  private Vec flockC;
  private boolean flUpToDate;

  ModelState(int numDucks) {
    this.numDucks = numDucks;
    duck = new Pos[numDucks];
    resetRandom(true);
    flUpToDate = false;
  }

  public void resetRandom() { resetRandom(false); }

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
      randPoint(dog[0].d, 0f, 0.5f);
    } else {
      dog[0].p.x = 20;
      dog[0].p.y = 20;
    }
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

  public float flockRadius() {
    if (!flUpToDate) calcFlockData();
    return flockR;
  }

  public Vec flockCentre() {
    if (!flUpToDate) calcFlockData();
    return (Vec) flockC.clone();
  }

  private void calcFlockData() {
    int nDist;
    Vec F = flockC;
    F.x = F.y = 0;
    for (int i=0; i<numDucks; i++) F.pleq(duck[i].p);
    F.dieq(numDucks);
    flockR = 0;
    for (int i=0; i<numDucks; i++) {
      nDist = (int) F.distTo(duck[i].p);
      flockR = flockR >= nDist ? flockR : nDist;
    }
    flockR += duckRadius();
    flUpToDate = true;
  }
}

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
  float calcAngle(Vec v); // Matt's infamous bloody updated angle code
  float flockRadius();
  Vec flockCentre();
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
}

interface DuckControl extends CommonControl {
  void setDuck(int duck, Vec velocity);
  void setDungle(int duck, float angle);
}

abstract class DuckModel {
  // abstract DuckModel(DuckControl d);
  abstract void update();
  abstract void postUpdate();
}

interface DogControl extends CommonControl {
  void setDog(int dog, Vec velocity);
  void setDongle(int dog, float angle);
}

abstract class DogModel {
  // abstract DogModel(DogControl d);
  abstract void update();
  abstract void postUpdate();
}
