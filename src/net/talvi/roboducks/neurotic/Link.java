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

package net.talvi.roboducks.neurotic;

import java.util.*;

/* A Link sits between two Axons. Storing stuff like the
   weight in one or the other of them leads to problems when
   the other one wants to get at it; this is neater.
*/
class Link {
  Axon input;		// input neuron
  Axon output;		// output neuron
  float weight;		// weighting of input
  boolean trainable;	// backlinks aren't trainable
  float wacc;		// accumulator for batch update
  float dw;		// change in weight (need to save for momentum)

  public Link(Axon i, boolean t, float w, float range)
  { this(i, t, w, range, new Random()); }

  //          input   trainable? mean wt  range of weights
  public Link(Axon i, boolean t, float w, float range, Random rnd) {
    input = i;
    output = null;	// this will be set during Neuron construction
    trainable = t;
    wacc = dw = 0f;
    if ( (!t) && (w!=1.0f || range!=0f) && Debug.on)
      System.err.println("Warning: you'd expect a non-trainable "+
			 "link to have a weight of 1.");
    randomizeWeight(w, range, rnd);
  }

  public void randomizeWeight(float mean, float range)
  { randomizeWeight(mean, range, new Random()); }

  public void randomizeWeight(float mean, float range, Random rnd)
  { weight = mean + rnd.nextFloat()*range - range/2; }
}

