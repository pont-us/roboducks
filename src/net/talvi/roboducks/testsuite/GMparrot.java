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

// Parrot: try to learn to repeat previous input

package net.talvi.roboducks.testsuite;

import net.talvi.roboducks.neurotic.*;
import java.io.*;

public class GMparrot {

  static java.util.Random rnd = new java.util.Random();

  public static void main(String[] argv) {
    Net net = new Net(1,2,1,true);
    boolean curr, prev = false;

    Population pop =
      new Population(net, new ParrotFitness(), 32, 0, 2, 2, 4);

    pop.evolve(32);
    net.setAllWeights(pop.fittest());

    prev = false;
    for (int i=0; i<16; i++) {
	curr = rnd.nextBoolean();
	net.setIn(0, curr ? 1 : 0);
	net.think();
	System.out.println( (prev ? "X " : ". ") +
			    (curr ? "X  " : ".  ") +
			    (prev ? "X" : ".") + 
			    ((net.getOut(0)>0.5) ? "X" : ".") +
			    "  " + (net.getOut(0) ));
	prev = curr;
    }
    net.dumpWeights(System.err);

  }

}

class ParrotFitness implements Fitness {

  static java.util.Random rnd = new java.util.Random();
  float[][] inputs;
  float[][] outputs;
  static final int seqLen = 8;

  public void initEnviros(int n) {
    inputs = new float[n][];
    outputs = new float[n][];
    for (int i=0; i<n; i++) {
      inputs[i] = new float[seqLen];
      outputs[i] = new float[seqLen];
      outputs[i][0] = 0.5f;
      for (int j=0; j<seqLen; j++) {
	inputs[i][j] = rnd.nextBoolean() ? 1 : 0;
	if (j<seqLen-1) outputs[i][j+1] = inputs[i][j];
      }
    }
  }

  public float score(Net net, int env) {
    float total = 0;
    for (int i=0; i<seqLen; i++) {
      net.setIn(0,inputs[env][i]);
      net.think();
      total += 1-Math.abs(net.getOut(0) - outputs[env][i]);
    }
    return total;
  }

}
