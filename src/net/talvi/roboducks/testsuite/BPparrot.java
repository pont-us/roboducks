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

// Parrot: try to learn to repeat previous input by backpropagation

package net.talvi.roboducks.testsuite;

import net.talvi.roboducks.neurotic.*;
import java.io.*;

public class BPparrot {

  static java.util.Random rnd = new java.util.Random();

  public static void main(String[] argv) {
    Net net = new Net(1,2,1,true);

    /* Initial weights hand-calculated. Good performance is
       given by the set           {8,0,0,-4,0,8,0,-4,0,8,-4}
       so I start it fairly close to this and let backprop
       do the rest. */

    net.setAllWeights(new float[] {5,-1,1,-5,1,6,-1,-3,0,6,-2});
    boolean curr, prev = false;
    float[] target = new float[1];

    net.rate = 0.5f;
    net.momentum = 0.5f;

    System.out.println("Performance before training:\n");
    test(net);

    int k;
    for (k=0; k<800; k++) {
      net.resetDeltas();
      curr = rnd.nextBoolean();
      net.setIn(0, curr ? 1 : 0);
      target[0] = prev ? 1 : 0;
      net.train(target);
      prev = curr;
      net.dumpWeights(System.err);
      net.changeWeights();
    }

    System.out.println("\nAfter " + k + " backprop passes\n");
    test(net);

  }

  static void test(Net net) {
    StringBuffer ins, outs;
    boolean curr, prev = false;
    ins = new StringBuffer("Input  ");
    outs = new StringBuffer("Output ");
    for (int i=0; i<60; i++) {
      curr = rnd.nextBoolean();
      net.setIn(0, curr ? 1 : 0);
      net.think();
      ins.append(curr ? "*" : ".");
      if (net.getOut(0) < 0.2) outs.append(".");
      else if (net.getOut(0) < 0.8) outs.append("?");
      else outs.append("*");
      prev = curr;
    }
    System.out.println(ins);
    System.out.println(outs);
    // net.dumpWeights(System.err);
  }

}
