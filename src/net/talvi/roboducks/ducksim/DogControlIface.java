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

// DogControlIface

// a class to interface neural network inputs and outputs to
// the DogControl interface

package net.talvi.roboducks.ducksim;
import net.talvi.roboducks.neurotic.*;

class DogControlIface implements DogBreeder.ControlInterface {

  DogControl dogCont;
  TrainerControl trainCont;
  final static float PI = (float) Math.PI;
  final static int inputs = 5; // # input nodes

  DogControlIface(DogControl dogCont, TrainerControl trainCont) {
    this.dogCont = dogCont;
    this.trainCont = trainCont;
  }

  public int inputs() { return inputs; }

  public void update(Net net, DogControl control) {
    Pos dogPos = control.dog(0);
    Vec wall = wallPoint(dogPos.p); // nearest wall point

    net.setIn(0, normAngle(wall.angle() - dogPos.d.angle()));
    // Angle to nearest wall point
    net.setIn(1, dogPos.p.distTo(wall) / control.arenaSize());
    // distance to nearest wall point
    net.setIn(2, normAngle( dogPos.p.angleTo(control.flockCentre())
			    - dogPos.d.angle()));
    // angle to flock centre
    net.setIn(3, dogPos.p.distTo(control.flockCentre()) /
	      control.arenaSize());
    // distance to flock centre
    net.setIn(4, 2 * control.flockRadius() / control.arenaSize());
    // size of flock
    net.think();

    float theta = dogPos.d.angle();
    float r = dogPos.d.abs();
    r = net.getOut(0) * 16;
    theta += 0.9 * (net.getOut(1) - 0.5f); // * PI
    dogPos.d.x = (float) (r * Math.sin(theta));
    dogPos.d.y = (float) (r * Math.cos(theta));
    control.setDog(0, dogPos.d);
  }

  // map angle in radians onto [0,1]
  // -pi |-> 0, +pi |-> 1, values outside [-pi,pi] wrap.
  private float normAngle(float theta) {
    float out = ( (theta + PI) / (2*PI) ) % 1.0f;
    if (out<0) out += 1.0f;
    return out;
  }

  // find nearest point on arena wall
  Vec wallPoint(Vec v) {
    return v.unit().ti(trainCont.arenaSize()).mi(v);
  }

}
