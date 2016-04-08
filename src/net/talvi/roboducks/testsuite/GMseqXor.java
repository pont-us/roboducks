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

// GMSeqXor: learn sequential XOR by genetic algorithm

package net.talvi.roboducks.testsuite;

import net.talvi.roboducks.neurotic.*;
import java.io.*;

public class GMseqXor {

  static java.util.Random rnd = new java.util.Random();

  public static void main(String[] argv) {
    Net net = new Net(1,2,1,true);
    boolean curr, prev = false;

    Population pop =
      new Population(net, new SeqXorFitness(), 128, 0, 4, 4, 4);

    pop.evolve(256);
    net.setAllWeights(pop.fittest());

    prev = false;
    for (int i=0; i<16; i++) {
	curr = rnd.nextBoolean();
	net.setIn(0, curr ? 1 : 0);
	net.think();
	System.out.println( (prev ? "X " : ". ") +
			    (curr ? "X  " : ".  ") +
			    ((prev^curr) ? "X" : ".") + 
			    ((net.getOut(0)>0.5) ? "X" : ".") +
			    "  " + (net.getOut(0) ));
	prev = curr;
    }
    net.dumpWeights(System.err);

  }

}

class SeqXorFitness implements Fitness {

  static java.util.Random rnd = new java.util.Random();
  float[][] inputs;
  float[][] outputs;
  static final int seqLen = 8;

  private float floatXor(float fa, float fb) {
    boolean ba = (fa>0.5) ? true : false;
    boolean bb = (fb>0.5) ? true : false;
    return (ba ^ bb) ? 1 : 0;
  }

  public void initEnviros(int n) {
    inputs = new float[n][];
    outputs = new float[n][];
    for (int i=0; i<n; i++) {
      inputs[i] = new float[seqLen];
      outputs[i] = new float[seqLen];
      outputs[i][0] = 0.5f;
      for (int j=0; j<seqLen; j++) {
	inputs[i][j] = rnd.nextBoolean() ? 1 : 0;
	if (j>0) outputs[i][j] =
		   floatXor(inputs[i][j],inputs[i][j-1]);
      }
    }
  }

  public float score(Net net, int env) {
    float total = 0;
    net.flushContext(0.5f);
    for (int i=0; i<seqLen; i++) {
      net.setIn(0,inputs[env][i]);
      net.think();
      total += 1-Math.abs(net.getOut(0) - outputs[env][i]);
    }
    return total;
  }

}
