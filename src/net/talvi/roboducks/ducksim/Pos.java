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

// Pos: a class to encapsulate the position and velocity of
//      an entity in the simulation

package net.talvi.roboducks.ducksim;

class Pos implements Cloneable {
  Vec p, d;
  float nAngle;
  /* the nAngle field is used by last year's algorithms. I'm
     not sure exactly what it represents, and I don't use it. */
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
