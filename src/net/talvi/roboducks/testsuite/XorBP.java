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

// XorBP: learn the XOR function by backpropagation

package net.talvi.roboducks.testsuite;

import net.talvi.roboducks.neurotic.*;
import java.io.*;

public class XorBP {

  static java.util.Random rnd = new java.util.Random();

  public static void main(String[] argv) {
    Net net = new Net(2,2,1,false);
    final float[][] trainingSet =
      {{0,0, 0},
       {0,1, 1},
       {1,0, 1},
       {1,1, 0}};

    net.rate = 1.0f;
    net.momentum = 0.8f;
    float[] target = new float[1];

    int k = 0;
    do {
      for (int i=0; i<trainingSet.length; i++) {
	net.resetDeltas();
	net.setIn(0,trainingSet[i][0]);
	net.setIn(1,trainingSet[i][1]);
	target[0] = trainingSet[i][2];
	net.train(target);
	net.changeWeights();
      }
      k++;
    } while (net.sumSqError() > 0.001f);


    System.out.println(k + " passes through training set.");
    for (int i=0; i<trainingSet.length; i++) {
      net.setIn(0,trainingSet[i][0]);
      net.setIn(1,trainingSet[i][1]);
      net.think();
      System.out.println(trainingSet[i][0] + " XOR " +
			 trainingSet[i][1] + " = " +
			 net.getOut(0));
    }

  }
}
