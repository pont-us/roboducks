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

package net.talvi.roboducks.ducksim;

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
    if (a==0) throw new Error("Can't normalize a zero vector!");
    return new Vec(dx/a, dy/a);
  }

  // angle in radians: straight down = 0, straight up = +/- pi
  float angle() { return (float) Math.atan2(x,y); }
  float angleTo(Vec v) { return (float) Math.atan2(v.x-x,v.y-y); }
  float angleFrom(Vec v) { return (float) Math.atan2(x-v.x,y-v.y); }

  Vec unit() {
    float a = (float) Math.sqrt(x*x + y*y);
    if (a==0) throw new Error("Can't normalize a zero vector!");
    // if (a==0) return new Vec(0,0);
    return new Vec(x/a, y/a); }
  Vec norm() {
    float a = (float) Math.sqrt(x*x + y*y);
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
